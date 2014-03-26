/*
 * Copyright (c) 2013,
 * Marco Brade
 * 							[https://sourceforge.net/users/mbrade],
 * Stephan Huth
 * 							[https://sourceforge.net/users/shuth]
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.sf.needles.spring.httpinvoker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;

import net.sf.needles.Needle;
import net.sf.needles.NeedleConfig;
import net.sf.needles.NeedleConfigFactory;
import net.sf.needles.NeedleContext;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Logger;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.Assert;
import org.springframework.web.util.NestedServletException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * EnhancedHttpInvokerServiceExporter extends springs {@link HttpInvokerServiceExporter} to support the transmission of {@link Needle}s.<br/>
 * Services are able to extend this class to add further processing steps during the process of incoming requests by using there own Objects as state object.
 * 
 * @param <STATEOBJECT>
 *            the generic type
 * @author Marco Brade
 */
@SuppressWarnings("restriction")
public class EnhancedHttpInvokerServiceExporter<STATEOBJECT> extends HttpInvokerServiceExporter implements
    NeedleHeaders, HttpHandler {

    /** The Constant VERSION_ATTRIBUTE_NAME. */
    public final static String VERSION_ATTRIBUTE_NAME = "version";

    /** The Constant log. */
    protected final static Logger log = Logger.getLogger(EnhancedHttpInvokerServiceExporter.class);

    /** The service log. */
    protected org.apache.log4j.Logger serviceLog = org.apache.log4j.Logger.getLogger(getClass().getName() + ".Logger");

    /** The proxy. */
    private Object proxy = null;

    /**
     * Called by {@link #prepare()} on startup. This method can be used to initialize inner structures and correct initialization.
     */
    public void doPrepare() {
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
	try {
	    final STATEOBJECT stateObject = createRequestStateObject(exchange);
	    try {
		final RemoteInvocation remoteInvocation = readRemoteInvocation(exchange.getRequestBody(), stateObject);
		final NeedleConfig clientConfig = readNeedleConfigFromRequest(exchange, remoteInvocation);
		final ExtendedRemoteInvocationResult remoteInvocationResult = invoke(stateObject, remoteInvocation, clientConfig);
		long size = 0;
		final Needle transmission = Needle.start("send response");
		try {
		    size = writeRemoteInvocationResult(exchange, remoteInvocationResult, stateObject);
		} finally {
		    transmission.debug(new StringBuilder("Send ").append(size).append(" bytes"));
		    transmission.stop();
		}
		doAfterSending(remoteInvocation, remoteInvocationResult, size, stateObject);
	    } catch (final ClassNotFoundException ex) {
		log.error(ex.getMessage(), ex);
		exchange.sendResponseHeaders(500, -1);
		throw new IOException("Class not found during deserialization", ex);
	    } catch (final RuntimeException e) {
		log.error(e.getMessage(), e);
		exchange.sendResponseHeaders(500, -1);
		throw e;
	    }
	} finally {
	    NeedleContext.cleanup();
	    exchange.close();
	}
    }

/**
		     * Handles incoming requests and sends a response back.<br/>
		     * This method accepts an ObjectInputStream with an {@link RemoteInvocation} as transferred class.<br/>
		     * The service will try to handle the request and create a {@link ExtendedRemoteInvocationResult} as result. It has the following call order:
		     * <ul>
		     * <li>{@link #incomingRequest(HttpServletRequest)}</li>
		     * <li>{@link #createRequestStateObject(HttpServletRequest)}</li>
		     * <li>{@link #readRemoteInvocation(InputStream, Object)</li> <li><li><li><li><li><li>configure the needleServiceFactory by the given needleConfig within the<li><li> configure the needleServiceFactory
		     * by the given needleConfig within the
		     * 
		     * @link RemoteInvocation#getAttribute(String)} or use the default from {@link needleConfigFactory}</li> <li>Resets the needles from previous requests</li> <li>Creates an {@link needle} for
		     *       complete time measurement (including transfer time)</li> <li>Creates another {@link needle} for the processing time and to transmit back to the client.</li> <li>
		     *       {@link #doAfterReadingRemoteInvocation(RemoteInvocation, Object)}</li> <li>{@link #executeRemoteInvocation(RemoteInvocation, Object, needle, Object)}</li> <li>
		     *       {@link #doBeforeSending(RemoteInvocation, RemoteInvocationResult, Object)}</li> <li>Stopps the {@link needle} for transmitting back to client.</li> <li>
		     *       {@link #countingWriteRemoteInvocationResult(HttpServletRequest, HttpServletResponse, RemoteInvocationResult, Object)}</li> <li>
		     *       {@link #doAfterSending(RemoteInvocation, RemoteInvocationResult, Long, Object)}</li> <li>Stopps the overall {@link needle}</li> <li>Calls {@link #doFinally(HttpServletRequest, needle)}
		     *       </li>
		     *       </ul>
		     * 
		     * @param request
		     *            the incoming request
		     * @param response
		     *            the outgoing response
		     * @throws IOException
		     *             during problems during reading or writing the stream
		     * @throws NestedServletException
		     *             during exceptions that occure within handling request and response (not during processing the {@link RemoteInvocation})
		     */
    @Override
    public final void handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
	NestedServletException {
	try {
	    incomingRequest(request);
	    final STATEOBJECT stateObject = createRequestStateObject(request, response);
	    try {
		final RemoteInvocation remoteInvocation = readRemoteInvocation(request.getInputStream(), stateObject);

		final NeedleConfig clientConfig = readNeedleConfigFromRequest(request, remoteInvocation);

		final ExtendedRemoteInvocationResult remoteInvocationResult = invoke(stateObject, remoteInvocation, clientConfig);
		long size = 0;
		final Needle transmission = Needle.start("send response");
		try {
		    size = countingWriteRemoteInvocationResult(request, response, remoteInvocationResult, stateObject);
		} finally {
		    transmission.debug(new StringBuilder("Send ").append(size).append(" bytes"));
		    transmission.stop();
		}

		doAfterSending(remoteInvocation, remoteInvocationResult, size, stateObject);

	    } catch (final ClassNotFoundException ex) {
		log.error(ex.getMessage(), ex);
		throw new NestedServletException("Class not found during deserialization", ex);
	    } catch (final RuntimeException e) {
		log.error(e.getMessage(), e);
		throw e;
	    }
	} finally {
	    try {
		doFinally(request);
	    } finally {
		NeedleContext.cleanup();
	    }
	}
    }

    /* (non-Javadoc)
     * @see org.springframework.remoting.rmi.RemoteInvocationSerializingExporter#prepare()
     */
    @Override
    public final void prepare() {
	this.proxy = getProxyForService();
	Assert.notNull(getServiceInterface(), "The ServiceInterface has to be set.");
	doPrepare();
    }

    private ExtendedRemoteInvocationResult invoke(final STATEOBJECT stateObject, final RemoteInvocation remoteInvocation, final NeedleConfig clientConfig) {
	NeedleConfigFactory.resetNeedleConfig();//this will set the current config to the default needleConfig of the needleServiceFactory.
	final NeedleConfig serviceConfig = NeedleConfigFactory.getNeedleConfig();

	if (clientConfig != null) {
	    //Get the highest mode to match the need of the service.
	    NeedleConfigFactory.setNeedleConfig((clientConfig.getConfigId() > serviceConfig.getConfigId()) ? clientConfig
		: serviceConfig);
	} else {
	    NeedleConfigFactory.setNeedleConfig(serviceConfig);
	}

	ExtendedRemoteInvocationResult remoteInvocationResult = null;
	final Needle needleTransfer = new Needle("handleRequest");
	Needle needleTransferForClient = null;
	try {
	    needleTransfer.start();
	    doAfterReadingRemoteInvocation(remoteInvocation, stateObject);

	    //don't transfer needles back. They could force OOM on the client if the client doesn't take care to clean them on each request.
	    needleTransferForClient = (clientConfig == null || clientConfig == NeedleConfig.QUIET) ? null : needleTransfer;

	    remoteInvocationResult = executeRemoteInvocation(remoteInvocation, this.proxy, stateObject);

	    doBeforeSending(remoteInvocation, remoteInvocationResult, stateObject);
	} finally {
	    needleTransfer.stop();
	}
	if (needleTransferForClient != null) {
	    remoteInvocationResult.setNeedleInfo(needleTransferForClient.toNeedleStub());
	}
	return remoteInvocationResult;
    }

    /**
     * Gets the {@link OutputStream} from the {@link Response} and calls
     * {@link #countingWriteRemoteInvocationResult(HttpServletRequest, HttpServletResponse, RemoteInvocationResult, OutputStream, Object)} .
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param result
     *            the RemoteInvocationResult object
     * @param stateObject
     *            the state object
     * @return the long
     * @throws IOException
     *             in case of I/O failure
     */
    protected long countingWriteRemoteInvocationResult(final HttpServletRequest request, final HttpServletResponse response,
	final ExtendedRemoteInvocationResult result, final STATEOBJECT stateObject) throws IOException {
	response.setContentType(getContentType());

	return countingWriteRemoteInvocationResult(request, response, result, response.getOutputStream(), stateObject);
    }

/**
             * Decorates the given OutputStream with an {@link CountingOutputStream}
             * and calls {@link #doWriteRemoteInvocationResult(RemoteInvocationResult, OutputStream, STATEOBJECT).
             *
             * @param request the request
             * @param response the response
             * @param result the {@link RemoteInvocationResult}
             * @param os the {@link OutputStream}
             * @param stateObject the state object
             * @return the long
             * @throws IOException Signals that an I/O exception has occurred.
             */
    protected long countingWriteRemoteInvocationResult(final HttpServletRequest request, final HttpServletResponse response,
	final RemoteInvocationResult result, final OutputStream os, final STATEOBJECT stateObject) throws IOException {
	long count = -1;
	final CountingOutputStream dos = decorateOutputStream(os);
	doWriteRemoteInvocationResult(result, dos, stateObject);
	count = dos.getByteCount();
	return count;
    }

    /**
     * Creates an object which can be used to transfer a state through the request execution.<br/>
     * The default implementation will return null. Subclasses can override this.
     * 
     * @param exchange
     *            the request
     * @return the STATEOBJECT
     */
    protected STATEOBJECT createRequestStateObject(final HttpExchange exchange) {
	return null;
    }

    /**
     * Creates an object which can be used to transfer a state through the request execution.<br/>
     * The default implementation will return null. Subclasses can override this.
     * 
     * @param request
     *            the request
     * @return the STATEOBJECT
     */
    protected STATEOBJECT createRequestStateObject(final HttpServletRequest request, final HttpServletResponse response) {
	return null;
    }

    /**
     * Creates an proper ObjectInputStream and reads the RemoteInvocation.
     * 
     * @param is
     *            the is
     * @param stateObject
     *            the state object
     * @return the remote invocation
     * @throws IOException
     *             Signals that an I/O exception has occurred or the read object is no {@link RemoteInvocation}
     * @throws ClassNotFoundException
     *             if the class was not found (normally it should be a {@link RemoteInvocation})
     */
    protected RemoteInvocation createStreamAndReadInvocation(final InputStream is, final STATEOBJECT stateObject)
	throws IOException, ClassNotFoundException {
	ObjectInputStream ois = null;
	try {
	    ois = new ObjectInputStream(is);
	    final Object obj = ois.readObject();
	    if (!(obj instanceof RemoteInvocation)) {
		throw new RemoteException("Deserialized object needs to be assignable to type ["
		    + RemoteInvocation.class.getName() + "]: " + obj);
	    }
	    return (RemoteInvocation) obj;
	} finally {
	    if (ois != null) {
		ois.close();
	    }
	}
    }

    protected InputStream decorateInputStream(final HttpExchange exchange, final InputStream is) {
	return is;
    }

    /**
     * Return the OutputStream to use for writing remote invocation results. It will be decorated by an {@link CountingOutputStream}
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param os
     *            the original OutputStream
     * @return the potentially decorated OutputStream
     * @throws IOException
     *             in case of I/O failure
     */
    protected final CountingOutputStream decorateOutputStream(final OutputStream os) throws IOException {
	return new CountingOutputStream(os);
    }

    /**
     * Callback-method to notify subclasses about the incoming RemoteInvocation.
     * 
     * @param result
     *            the result
     * @param stateObject
     *            the state object
     */
    protected void doAfterReadingRemoteInvocation(final RemoteInvocation result, final STATEOBJECT stateObject) {
    }

    /**
     * Will be called after the result has been transmitted back to the client.
     * 
     * @param invocation
     *            the invocation
     * @param result
     *            the result
     * @param transferSize
     *            the transfer size
     * @param stateObject
     *            the state object
     */
    protected void doAfterSending(final RemoteInvocation invocation, final RemoteInvocationResult result, final Long transferSize,
	final STATEOBJECT stateObject) {
    }

    /**
     * Will be called before the result will be transmitted back to the client.
     * 
     * @param invocation
     *            the invocation
     * @param result
     *            the result
     * @param stateObject
     *            the state object
     */
    protected void doBeforeSending(final RemoteInvocation invocation, final RemoteInvocationResult result, final STATEOBJECT stateObject) {
    }

    /**
     * Do finally will be called after the response has been send. The {@link needle} will be stopped and contains the total duration of the request.
     * 
     * @param request
     *            the request
     * @param event
     *            the event
     */
    protected void doFinally(final HttpServletRequest request) {

    }

    /**
     * Creates an ObjectOutputStream writes the RemoteInvocationResult and closes the stream.
     * 
     * @param result
     *            the result
     * @param os
     *            the os
     * @param stateObject
     *            the state object
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void doWriteRemoteInvocationResult(final RemoteInvocationResult result, final OutputStream os, final STATEOBJECT stateObject)
	throws IOException {
	ObjectOutputStream oos = null;
	try {
	    oos = new ObjectReplacingObjectOutputStream(os);
	    oos.writeObject(result);
	    oos.flush();
	} finally {
	    oos.close();
	}
    }

    /**
     * Executes the given {@link RemoteInvocation}. It calls {@link #invoke(RemoteInvocation, Object)}.<br/>
     * As a result this method generates a {@link ExtendedRemoteInvocationResult}.<br/>
     * Sets the given {@link needle} to the {@link RemoteInvocationResult} as attribute with the key {@link NeedleHeaders#needleCONFIGHEADER}<br/>
     * 
     * @param invocation
     *            the invocation
     * @param target
     *            the target
     * @param event
     *            the event. The given event might be null if the client doesn't expected to get needles.
     * @param stateObject
     *            the state object
     * @return the remote invocation result
     */
    protected ExtendedRemoteInvocationResult executeRemoteInvocation(final RemoteInvocation invocation, final Object target, final STATEOBJECT stateObject) {
	ExtendedRemoteInvocationResult result = null;
	try {
	    final Object value = invoke(invocation, target);
	    result = new ExtendedRemoteInvocationResult(value);
	} catch (final Throwable ex) {
	    result = new ExtendedRemoteInvocationResult(ex);
	}
	return result;
    }

    /**
     * Callback-method to inform subclasses about an incoming request.
     * 
     * @param request
     *            the request
     */
    protected void incomingRequest(final HttpServletRequest request) {
    }

    protected NeedleConfig readNeedleConfigFromRequest(final HttpExchange exchange, final RemoteInvocation remoteInvocation) {
	NeedleConfig clientConfig = (NeedleConfig) remoteInvocation.getAttribute(NEEDLE_CONFIG_HEADER);
	if (clientConfig == null) { //look in the header if we find a proper value
	    final String header = exchange.getRequestHeaders().getFirst(NEEDLE_CONFIG_HEADER);
	    if (header != null) {
		try {
		    final int needleConfigValue = Integer.parseInt(header);
		    clientConfig = NeedleConfig.getNeedleConfigById(needleConfigValue);
		} catch (final Exception e) {
		}
	    }
	}
	return clientConfig;
    }

    protected NeedleConfig readNeedleConfigFromRequest(final HttpServletRequest request, final RemoteInvocation remoteInvocation) {
	NeedleConfig clientConfig = (NeedleConfig) remoteInvocation.getAttribute(NEEDLE_CONFIG_HEADER);
	if (clientConfig == null) { //look in the header if we find a proper value
	    final String header = request.getHeader(NEEDLE_CONFIG_HEADER);
	    if (header != null) {
		try {
		    final int needleConfigValue = Integer.parseInt(header);
		    clientConfig = NeedleConfig.getNeedleConfigById(needleConfigValue);
		} catch (final Exception e) {
		}
	    }
	}
	return clientConfig;
    }

    /**
     * Just calls {@link #createStreamAndReadInvocation(InputStream, Object)}.
     * 
     * @param is
     *            the inputstream
     * @param stateObject
     *            the state object
     * @return the remote invocation
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    protected final RemoteInvocation readRemoteInvocation(final InputStream is, final STATEOBJECT stateObject) throws IOException,
	ClassNotFoundException {
	final RemoteInvocation result = createStreamAndReadInvocation(is, stateObject);
	return result;
    }

    /**
     * Serialize the given RemoteInvocation to the given OutputStream.
     * <p>
     * The default implementation gives {@link #decorateOutputStream(OutputStream)} a chance to decorate the stream first (for example, for custom encryption or compression). Creates an
     * {@link java.io.ObjectOutputStream} for the final stream and calls {@link #doWriteRemoteInvocationResult} to actually write the object.
     * <p>
     * Can be overridden for custom serialization of the invocation.
     * 
     * @param exchange
     *            current HTTP request/response
     * @param result
     *            the RemoteInvocationResult object
     * @param os
     *            the OutputStream to write to
     * @return the written bytes
     * @throws java.io.IOException
     *             in case of I/O failure
     * @see #decorateOutputStream
     * @see #doWriteRemoteInvocationResult
     */
    protected long writeRemoteInvocationResult(final HttpExchange exchange, final ExtendedRemoteInvocationResult result, final STATEOBJECT stateObject) throws IOException {
	exchange.getResponseHeaders().set("Content-Type", getContentType());
	exchange.sendResponseHeaders(200, 0);
	long count = -1;
	final CountingOutputStream dos = decorateOutputStream(exchange.getResponseBody());
	doWriteRemoteInvocationResult(result, dos, stateObject);
	count = dos.getByteCount();
	return count;
    }

}
