/*
 * Copyright (c) 2013,
 * Marco Brade
 * 							[https://github.com/mbrade]
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

import net.sf.needles.NeedleConfig;
import net.sf.needles.NeedleConfigFactory;
import net.sf.needles.NeedleContext;
import net.sf.needles.NeedleInfo;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.StringUtils;

/**
 * The Class EnhancedCommonsHttpInvokerRequestExecutor.
 * 
 * @author Marco Brade
 */
public class EnhancedCommonsHttpInvokerRequestExecutor extends CommonsHttpInvokerRequestExecutor implements NeedleHeaders {

    /** The use log event header. */
    private boolean useLogEventHeader = true;

    /** The use log event config from log event factory. */
    private boolean useLogEventConfigFromLogEventFactory = false;

    /**
     * Instantiates a new EnhancedCommonsHttpInvokerRequestExecutorr.
     */
    public EnhancedCommonsHttpInvokerRequestExecutor() {
	super();
    }

    /**
     * Instantiates a new EnhancedCommonsHttpInvokerRequestExecutor with the given {@link HttpClient}.
     * 
     * @param httpClient
     *            the http client
     */
    public EnhancedCommonsHttpInvokerRequestExecutor(final HttpClient httpClient) {
	super(httpClient);
    }

    /**
     * Checks if to use log event header as real header parameter.
     * 
     * @return true, if is use log event header
     */
    public boolean isUseNeedleConfigHeader() {
	return this.useLogEventHeader;
    }

    /**
     * Setting this to true will use {@link LogEventServiceFactory#getConfig()} instead of using the {@link LogEventConfigFactory}.
     * 
     * @param useLogEventConfigFromLogEventFactory
     *            the new use log event config from log event factory
     */
    public void setUseLogEventConfigFromLogEventFactory(final boolean useLogEventConfigFromLogEventFactory) {
	this.useLogEventConfigFromLogEventFactory = useLogEventConfigFromLogEventFactory;
    }

    /**
     * Set this to false to use RemoteInvocation attributes instead of header values.
     * 
     * @param useLogEventHeader
     *            the new use log event header
     */
    public void setUseLogEventHeader(final boolean useLogEventHeader) {
	this.useLogEventHeader = useLogEventHeader;
    }

    /**
     * Checks if is use log event config from log event factory.
     * 
     * @return true, if is use log event config from log event factory
     */
    public boolean useNeedleConfigFactory() {
	return useLogEventConfigFromLogEventFactory;
    }

    /**
     * Adds the request header.
     * 
     * @param postMethod
     *            the post method
     * @param config
     *            the config
     */
    protected void addRequestHeader(final PostMethod postMethod, final HttpInvokerClientConfiguration config) {
    }

    /* (non-Javadoc)
     * @see org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor#createPostMethod(org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration)
     */
    @Override
    protected PostMethod createPostMethod(final HttpInvokerClientConfiguration config) throws IOException {
	final PostMethod postMethod = new PostMethod(config.getServiceUrl());
	final LocaleContext locale = LocaleContextHolder.getLocaleContext();
	if (locale != null) {
	    postMethod.addRequestHeader(HTTP_HEADER_ACCEPT_LANGUAGE, StringUtils.toLanguageTag(locale.getLocale()));
	}
	if (isAcceptGzipEncoding()) {
	    postMethod.addRequestHeader(HTTP_HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
	}

	if (isUseNeedleConfigHeader()) {
	    postMethod.addRequestHeader(NeedleHeaders.NEEDLE_CONFIG_HEADER, Integer.toString(getNeedleConfig().getConfigId()));
	    final String needleSession = getNeedleSession();
	    if (needleSession != null) {
		postMethod.addRequestHeader(NEEDLE_SESSION_HEADER, getNeedleSession());
	    }
	}
	addRequestHeader(postMethod, config);
	return postMethod;
    }

    /**
     * Creates the stream and read remote invocation result.
     * 
     * @param is
     *            the is
     * @param codebaseUrl
     *            the codebase url
     * @return the remote invocation result
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    protected RemoteInvocationResult createStreamAndReadRemoteInvocationResult(final InputStream is, final String codebaseUrl) throws IOException, ClassNotFoundException {
	final ObjectInputStream ois = new ObjectInputStream(is);
	try {
	    return (RemoteInvocationResult) ois.readObject();
	} finally {
	    ois.close();
	}
    }

    /**
     * Creates the stream and write remote invocation.
     * 
     * @param invocation
     *            the invocation
     * @param os
     *            the os
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void createStreamAndWriteRemoteInvocation(final RemoteInvocation invocation, final OutputStream os) throws IOException {
	final ObjectOutputStream oos = new ObjectReplacingObjectOutputStream(os);
	try {
	    oos.writeObject(invocation);
	    oos.flush();
	} finally {
	    oos.close();
	}
    }

    /**
     * Gets the log event config.
     * 
     * @return the log event config
     */
    protected NeedleConfig getNeedleConfig() {
	if (!useNeedleConfigFactory()) {
	    return NeedleConfigFactory.getNeedleConfig();
	} else {
	    return NeedleConfig.QUIET;
	}
    }

    protected String getNeedleSession() {
	if (!useNeedleConfigFactory()) {
	    return NeedleConfigFactory.getSessionId();
	} else {
	    return null;
	}
    }

    /* (non-Javadoc)
     * @see org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor#readRemoteInvocationResult(java.io.InputStream, java.lang.String)
     */
    @Override
    protected RemoteInvocationResult readRemoteInvocationResult(final InputStream is, final String codebaseUrl) throws IOException, ClassNotFoundException {
	final RemoteInvocationResult result = createStreamAndReadRemoteInvocationResult(is, codebaseUrl);
	if (result instanceof ExtendedRemoteInvocationResult) {
	    final NeedleInfo needleInfo = ((ExtendedRemoteInvocationResult) result).getNeedleInfo();
	    if (needleInfo != null) {
		NeedleContext.addToCurrentNeedleStack(needleInfo, true);
	    }
	}
	return result;
    }

    /* (non-Javadoc)
     * @see org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor#writeRemoteInvocation(org.springframework.remoting.support.RemoteInvocation, java.io.OutputStream)
     */
    @Override
    protected final void writeRemoteInvocation(final RemoteInvocation invocation, final OutputStream os) throws IOException {
	if (!isUseNeedleConfigHeader()) {
	    invocation.addAttribute(NEEDLE_CONFIG_HEADER, getNeedleConfig());
	    final String needleSession = getNeedleSession();
	    if (needleSession != null) {
		invocation.addAttribute(NEEDLE_SESSION_HEADER, getNeedleSession());
	    }
	}
	createStreamAndWriteRemoteInvocation(invocation, os);
    }

}
