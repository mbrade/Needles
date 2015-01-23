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

package net.sf.needles;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.needles.util.ExceptionUtils;

import org.apache.log4j.Logger;

/**
 * The Class NeedleContext.
 */
public class NeedleContext {

    private final static Logger LOG = Logger.getLogger(NeedleContext.class);
    private static final Charset charset = Charset.forName("UTF-8");
    private static HashBuilder hashBuilder = null;
    static {
	MessageDigest digest = null;
	try {
	    digest = MessageDigest.getInstance("MD5");
	} catch (final NoSuchAlgorithmException nsa) {
	    try {
		digest = MessageDigest.getInstance("SH1");
	    } catch (final NoSuchAlgorithmException e) {
		LOG.warn("Couldn't not find hash algorithms MD5 and SH1");
	    }
	}
	if (digest != null) {
	    hashBuilder = new MessageDigestHashBuilder(digest);
	} else {
	    hashBuilder = new StringHashBuilder();
	}
    }

    private static final ThreadLocal<NeedleContext> needleContextTL = new ThreadLocal<NeedleContext>() {
	@Override
	protected NeedleContext initialValue() {
	    return new NeedleContext();
	}
    };

    //shared over all LogContexts
    private static ConcurrentHashMap<NeedleId, StackTraceElement[]> stackTraceElements = new ConcurrentHashMap<NeedleId, StackTraceElement[]>();

    private static boolean doRegisterUncaughtExceptionHandler = false;
    private final ThreadLogs threadLogs = new ThreadLogs();

    private NeedleContext() {
    }

    public static NeedleInfo addToCurrentNeedleStack(final NeedleInfo needleInfo) {
	return addToCurrentNeedleStack(needleInfo, false);
    }

    public static NeedleInfo addToCurrentNeedleStack(final NeedleInfo needleInfo, final boolean aggregate) {
	return getNeedleContext().addNeedleToCurrentThreadStack(needleInfo, aggregate);
    }

    /**
     * Cleanup.
     */
    public static void cleanup() {
	final NeedleContext currentNeedleContext = getNeedleContext();
	currentNeedleContext.threadLogs.clear();
    }

    /**
     * Gets the current log.
     * 
     * @return the current log
     */
    public static Needle getCurrentNeedle() {
	return getNeedleContext().threadLogs.peek();
    }

    /**
     * Gets the log context.
     * 
     * @return the log context
     */
    public static NeedleContext getNeedleContext() {
	return needleContextTL.get();
    }

    /**
     * Gets the root needles.
     * 
     * @return the root needles
     */
    public static List<Needle> getRootNeedles() {
	return getNeedleContext().threadLogs.getNeedles();
    }

    /**
     * Registers uncaught exception handler.
     * 
     * @return true, if successful
     */
    public static boolean isRegisterUncaughtExceptionHandler() {
	return doRegisterUncaughtExceptionHandler;
    }

    /**
     * Will configure if the {@link NeedleUncaughtExceptionHandler} will be set for all Threads as {@link Thread#setUncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)} to set the
     * current log to abort if not currently stopped.
     * 
     * @param value
     *            the value
     */
    public static void registerUncaughtExceptionHandler(final boolean value) {
	doRegisterUncaughtExceptionHandler = value;
    }

    private static NeedleId calculateNeedleId(final Needle needle, final StackTraceElement element) {
	final StringBuilder sb = getPath(needle);
	if (element != null) {
	    sb.append(element.getClassName())
	      .append(".")
	      .append(element.getMethodName())
	      .append("#")
	      .append(element.getLineNumber());
	}
	needle.setNeedleId(new NeedleId(getHash(sb.toString())));
	return needle.getId();
    }

    private static byte[] getHash(final String value) {
	return hashBuilder.buildHash(value);
    }

    /**
     * Returns the ID of the LogEvent.
     * 
     * @return unique id
     */
    private static StringBuilder getPath(final NeedleInfo needle) {
	final StringBuilder builder = new StringBuilder();
	if (needle.getParentNeedle() != null) {
	    builder.append(needle.getParentNeedle().getId());
	}
	builder.append("/").append(needle.getName());
	return builder;
    }

    private NeedleInfo addChildNeedleToCurrentThreadStack(final NeedleInfo needle, final boolean aggregate) {
	StackTraceElement[] elements = stackTraceElements.get(needle.getId());
	if (elements == null) {
	    elements = new StackTraceElement[] { needle.getStartStackTraceElement(), needle.getStopStackTraceElement() };
	}
	stackTraceElements.putIfAbsent(needle.getId(), elements);
	final Needle resultNeedle = threadLogs.addNeedle(needle, false, false);//add the needles but don't touch the UncaughtExceptionHandler in this case.
	try {
	    for (final NeedleInfo child : needle.getChildren()) {
		addChildNeedleToCurrentThreadStack(child, aggregate);
	    }
	    return resultNeedle;
	} finally {
	    if (aggregate) {
		afterStopNeedle(resultNeedle);
	    } else {
		threadLogs.pop();
	    }
	}
    }

    private void afterStopNeedle(final NeedleInfo needle) {
	threadLogs.pop();
	AggregationContext.aggregateNeedle(needle);
    }

    /* (non-Javadoc)
     * @see de.arvatomobile.bdev.scr.utils.logevent.LogEvent#addToCurrentThreadStack()
     */
    /**
     * Adds the log to current thread stack.
     * 
     * @param needle
     *            the log
     * @return true, if successful
     */
    NeedleInfo addNeedleToCurrentThreadStack(final NeedleInfo needle, final boolean aggregate) {
	boolean containsEvent = (needle.getParentNeedle() != null);
	if (!containsEvent) {
	    for (final Needle aNeedle : threadLogs.getNeedles()) {
		if (containsEvent = (aNeedle == needle)) {
		    break;
		}
	    }
	}
	if (!containsEvent) {
	    StackTraceElement[] elements = stackTraceElements.get(needle.getId());
	    if (elements == null) {
		elements = new StackTraceElement[] { needle.getStartStackTraceElement(), needle.getStopStackTraceElement() };
	    }
	    stackTraceElements.putIfAbsent(needle.getId(), elements);
	    final Needle resultNeedle = threadLogs.addNeedle(needle, false);//add the needles but don't touch the UncaughtExceptionHandler in this case.
	    try {
		for (final NeedleInfo child : needle.getChildren()) {
		    addChildNeedleToCurrentThreadStack(child, aggregate);
		}
		return resultNeedle;
	    } finally {
		if (aggregate) {
		    afterStopNeedle(resultNeedle);
		} else {
		    threadLogs.pop();
		}
	    }
	}
	return needle;
    }

    /**
     * Calculate stack trace element.
     * 
     * @param index
     *            the index
     * @return the stack trace element
     */
    StackTraceElement calculateStackTraceElement(final int index) {
	return ExceptionUtils.getStackTraceElement(new Exception(), index);
    }

    /**
     * Gets the start element.
     * 
     * @param needle
     *            the log
     * @return the start element
     */
    StackTraceElement getStartElement(final Needle needle) {
	if (needle.getId() != null) {
	    final StackTraceElement[] elements = stackTraceElements.get(needle.getId());
	    return (elements != null) ? elements[0] : null;
	}
	return null;
    }

    /**
     * Gets the stop element.
     * 
     * @param needle
     *            the log
     * @return the stop element
     */
    StackTraceElement getStopElement(final Needle needle) {
	if (needle.getId() != null) {
	    final StackTraceElement[] elements = stackTraceElements.get(needle.getId());
	    return (elements != null) ? elements[1] : null;
	}
	return null;
    }

    /**
     * Start log.
     * 
     * @param needle
     *            the log
     */
    void startNeedle(final Needle needle) {
	//set the given log as the root one and or set it as child to the current root log.
	threadLogs.addNeedle(needle, doRegisterUncaughtExceptionHandler);
	final StackTraceElement startElement = calculateStackTraceElement(needle.getStartStackIndex());
	//get the log id
	final NeedleId id = calculateNeedleId(needle, startElement);
	if (!stackTraceElements.containsKey(id)) {
	    stackTraceElements.putIfAbsent(id, new StackTraceElement[] { startElement, null });
	}
    }

    /**
     * Stop log.
     * 
     * @param needle
     *            the log
     */
    void stopNeedle(final Needle needle) {
	//get the logId
	final NeedleId id = needle.getId();
	//calculate the stopping stacktracelement for the log and register (global part)
	final StackTraceElement[] elements = stackTraceElements.get(id);
	if (elements != null && elements[1] == null) {//dont care for double inserts
	    elements[1] = calculateStackTraceElement(needle.getStopStackIndex());
	}
	afterStopNeedle(needle);
    }

    /**
     * Stop log.
     * 
     * @param needle
     *            the log
     * @param element
     *            the element
     */
    void stopNeedle(final Needle needle, final StackTraceElement element) {
	final NeedleId id = needle.getId();
	//calculate the stopping stacktracelement for the log and register (global part)
	final StackTraceElement[] elements = stackTraceElements.get(id);
	if (elements != null && elements[1] == null) {//dont care for double inserts
	    elements[1] = element;
	}
	afterStopNeedle(needle);
    }

    private static class MessageDigestHashBuilder implements HashBuilder {

	private final MessageDigest digest;

	private MessageDigestHashBuilder(final MessageDigest digest) {
	    this.digest = digest;
	}

	@Override
	public byte[] buildHash(final String input) {
	    try {
		return ((MessageDigest) digest.clone()).digest(charset.encode(input).array());
	    } catch (final CloneNotSupportedException e) {
		return charset.encode(input).array();
	    }
	}

    }

    private static class StringHashBuilder implements HashBuilder {

	@Override
	public byte[] buildHash(final String input) {
	    return charset.encode(input).array();
	}

    }

    private static class ThreadLogs {

	/** The needles. */
	private final List<Needle> needles = new LinkedList<Needle>();

	/** The last needles */
	private final Stack<Needle> lastNeedles = new Stack<Needle>();

	/**
	 * Adds the log.
	 * 
	 * @param needle
	 *            the log
	 */
	public Needle addNeedle(final NeedleInfo needleInfo, final boolean registerUncaughExceptionHandler) {
	    return addNeedle(needleInfo, registerUncaughExceptionHandler, true);
	}

	public void clear() {
	    NeedleUncaughtExceptionHandler.unregisterLogUncaughtExceptionHandler();
	    needles.clear();
	    lastNeedles.clear();
	}

	/**
	 * Gets the needles.
	 * 
	 * @return the log
	 */
	public List<Needle> getNeedles() {
	    return needles;
	}

	public Needle peek() {
	    if (!lastNeedles.isEmpty()) {
		return lastNeedles.peek();
	    } else {
		return (!needles.isEmpty()) ? needles.get(0) : null;
	    }
	}

	/**
	 * Pop.
	 */
	public void pop() {
	    if (!lastNeedles.isEmpty()) {
		lastNeedles.pop();
	    }
	}

	private Needle addNeedle(final NeedleInfo needleInfo, final boolean registerUncaughExceptionHandler, final boolean checkNeedleState) {
	    final Needle lastNeedle = (lastNeedles.isEmpty()) ? null : lastNeedles.peek();
	    if (registerUncaughExceptionHandler && needles.isEmpty()) {
		NeedleUncaughtExceptionHandler.registerNeedleUncaughtExceptionHandler();
	    }
	    final Needle needle = (needleInfo.getClass() == Needle.class) ? (Needle) needleInfo : new Needle(needleInfo);
	    if (lastNeedle != null && (!checkNeedleState || (lastNeedle.isStarted() && !lastNeedle.isStopped()))) {
		lastNeedle.addChild(needle);
		lastNeedles.push(needle);
	    } else {
		lastNeedles.clear();
		lastNeedles.push(needle);
		needles.add(needle);
	    }
	    return needle;
	}
    }

    interface HashBuilder {
	byte[] buildHash(String input);
    }
}
