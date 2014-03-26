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

package net.sf.needles;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.needles.renderer.DeepNeedleRenderer;
import net.sf.needles.renderer.NeedleRenderer;
import net.sf.needles.renderer.RendererHelper;
import net.sf.needles.renderer.SimpleNeedleRenderer;
import net.sf.needles.util.ExceptionUtils;

/**
 * The Class Needle.
 */
public final class Needle implements NeedleInfo {

    private static final long serialVersionUID = 1L;

    private static final RendererHelper RENDERER_HELPER = new RendererHelper();

    /** The Constant DEFAULT_RENDERER. */
    public static final NeedleRenderer DEFAULT_RENDERER = new DeepNeedleRenderer(
	                                                                         new SimpleNeedleRenderer(true, true, false, null, false, true, true, true, true, RENDERER_HELPER),
	                                                                         new SimpleNeedleRenderer(true, false, true, "\t", true, true, true, true, true, RENDERER_HELPER),
	                                                                         "\n", RENDERER_HELPER);
    private transient long startNanos = 0;
    private transient int startStackIndex = 3;
    private transient int stopStackIndex = 3;
    private long startTime = 0;
    private long duration = -1;
    private String name;
    private NeedleId needleId;
    private Needle parent;
    private Throwable abortReason;
    private Serializable[] context;
    private final List<String> debugLines = new LinkedList<String>();
    private final LinkedBlockingQueue<Needle> children = new LinkedBlockingQueue<Needle>();
    private boolean fromParallelProcess;//will be set by NeedleContext

    /**
     * Instantiates a new log.
     * 
     * @param name
     *            the name
     * @param context
     *            the context
     */
    public Needle(final String name, final List<Serializable> context) {
	this(name, context.toArray(new Serializable[context.size()]));
    }

    public Needle(final String name, final Serializable... context) {
	if (name == null || name.length() == 0 || name.trim().length() == 0) {
	    throw new IllegalArgumentException("The name has to be valid and not empty or null.");
	}
	this.context = (NeedleConfigFactory.isContextEnabled()) ? context : null;
	this.name = name;
    }

    protected Needle(final NeedleInfo needle) {
	copyData(needle);
    }

    /**
     * Start.
     * 
     * @param name
     *            the name
     * @return the log
     */
    public static Needle start(final String name) {
	final Needle needle = new Needle(name);
	needle.startStackIndex = 4;
	needle.start();
	return needle;
    }

    /**
     * Start.
     * 
     * @param name
     *            the name
     * @param context
     *            the context
     * @return the log
     */
    public static Needle start(final String name, final Serializable... context) {
	final Needle needle = new Needle(name, context);
	needle.startStackIndex = 4;
	needle.start();
	return needle;
    }

    /**
     * Stop current log.
     */
    public static void stopCurrentNeedle() {
	final Needle needle = NeedleContext.getCurrentNeedle();
	if (needle != null) {
	    needle.stopStackIndex = 4;
	    needle.stop();
	} else {
	    throw new IllegalStateException("There is no current log to stop within this Threastack.");
	}
    }

    /**
     * Gets the log context.
     * 
     * @return the log context
     */
    static NeedleContext getNeedleContext() {
	return NeedleContext.getNeedleContext();
    }

    public void abort() {
	abort(null);
    }

    /**
     * Abort.
     * 
     * @param reason
     *            the reason
     */
    public synchronized void abort(final Throwable reason) {
	if (!isStarted()) {
	    throw new IllegalStateException("Abort can not be called on a not started needle.");
	}
	if (!isStopped()) {
	    if (reason == null) {
		abortReason = new NeedleException("Needle got aborted manually without exception.");
	    } else {
		abortReason = reason;
	    }
	    duration = System.nanoTime() - startNanos;
	    stopStackIndex = (reason != null) ? 0 : 1;
	    getNeedleContext().stopNeedle(this, ExceptionUtils.getStackTraceElement(abortReason, stopStackIndex));
	}
    }

    /**
     * Adds the child.
     * 
     * @param needle
     *            the log
     */
    public void addChild(final Needle needle) {
	children.add(needle);
	needle.setParent(this);
    }

    /*
    public final void addNeedleToCurrentThreadStack() {
    getNeedleContext().addNeedleToCurrentThreadStack(this);
    }*/

    public Needle debug(final CharSequence... debugLines) {
	if (NeedleConfigFactory.isDebugEnabled()) {
	    for (final CharSequence debugLine : debugLines) {
		this.debugLines.add(debugLine.toString());
	    }
	}
	return this;
    }

    /**
     * Debug.
     * 
     * @param message
     *            the message to append debug informations
     */
    public Needle debug(final CharSequence message) {
	if (NeedleConfigFactory.isDebugEnabled()) {
	    debugLines.add(message.toString());
	}
	return this;
    }

    public Needle debug(final Collection<? extends CharSequence> debugLines) {
	if (NeedleConfigFactory.isDebugEnabled()) {
	    for (final CharSequence debugLine : debugLines) {
		this.debugLines.add(debugLine.toString());
	    }
	}
	return this;
    }

    /**
     * Gets the abort reason.
     * 
     * @return the abort reason
     */
    @Override
    public Throwable getAbortReason() {
	return abortReason;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getChildCount()
     */
    @Override
    public int getChildCount() {
	return children.size();
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getChildren()
     */
    @Override
    public List<NeedleInfo> getChildren() {
	final List<NeedleInfo> result = new LinkedList<NeedleInfo>();
	for (final NeedleInfo needle : children) {
	    result.add(needle);
	}
	return result;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getContext()
     */
    @Override
    public Serializable[] getContext() {
	return context;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getDebugLines()
     */
    @Override
    public List<String> getDebugLines() {
	return Collections.unmodifiableList(debugLines);
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getDepth()
     */
    @Override
    public int getDepth() {
	return (parent != null) ? parent.getDepth() + 1 : 1;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getDurationMillis()
     */
    @Override
    public long getDurationMillis() {
	return getDurationNanos() / 1000000;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getDurationNanos()
     */
    @Override
    public long getDurationNanos() {
	if (isStopped()) {
	    return duration;
	} else if (isStarted()) {
	    return System.nanoTime() - startNanos;
	} else {
	    return 0;
	}
    }

    @Override
    public NeedleId getId() {
	return needleId;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getName()
     */
    @Override
    public String getName() {
	return name;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getNeedleState()
     */
    @Override
    public NeedleState getNeedleState() {
	return (isAborted()) ? NeedleState.ABORTED : (isStopped()) ? NeedleState.STOPPED : (isStarted()) ? NeedleState.STARTED : NeedleState.NEW;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getOwnDurationNanos()
     */
    @Override
    public long getOwnDurationNanos() {
	Long childDurations = 0L;
	for (final Needle child : children) {
	    childDurations += child.getDurationNanos();
	}
	return getDurationNanos() - childDurations;
    }

    /**
     * Gets the parent log.
     * 
     * @return the parent log
     */
    @Override
    public NeedleInfo getParentNeedle() {
	return parent;
    }

    public Needle getRootNeedle() {
	if (parent != null) {
	    return parent.getRootNeedle();
	} else {
	    return this;
	}
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getStartElement()
     */
    @Override
    public StackTraceElement getStartStackTraceElement() {
	return getNeedleContext().getStartElement(this);
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getStartTimeMillis()
     */
    @Override
    public long getStartTimeMillis() {
	return startTime;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getStopElement()
     */
    @Override
    public StackTraceElement getStopStackTraceElement() {
	return getNeedleContext().getStopElement(this);
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getStopTimeMillis()
     */
    @Override
    public long getStopTimeMillis() {
	return getStartTimeMillis() + getDurationMillis();
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#isAborted()
     */
    @Override
    public boolean isAborted() {
	return abortReason != null;
    }

    /*
     * (non-Javadoc)
     * @see de.arvatomobile.logevent.LogEvent#isFromParallelProcess()
     */
    @Override
    public boolean isFromParallelProcess() {
	return fromParallelProcess;
    }

    @Override
    public boolean isRoot() {
	return parent == null;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#isStarted()
     */
    @Override
    public boolean isStarted() {
	return startTime != 0;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#isStopped()
     */
    @Override
    public boolean isStopped() {
	return duration != -1;
    }

    /*
     * (non-Javadoc)
     * @see de.arvatomobile.logevent.LogEvent#setFromParallelProcess(boolean)
     */
    public void setFromParallelProcess(final boolean fromParallelProcess) {
	this.fromParallelProcess = fromParallelProcess;
    }

    /**
     * Start.
     */
    public synchronized void start() {
	if (isStopped()) {
	    throw new IllegalStateException("The log has already been stopped.");
	}
	if (isStarted()) {
	    throw new IllegalStateException("The log has already been started.");
	}
	getNeedleContext().startNeedle(this);
	startTime = System.currentTimeMillis();
	startNanos = System.nanoTime();
    }

    /**
     * Stop.
     */
    public synchronized void stop() {
	if (!isStarted()) {
	    throw new IllegalStateException("The log has not been started yet.");
	}
	if (!isAborted()) {//if already aborted we do nothing because its also stopped but within an unusual way. 
	    if (isStopped()) {
		throw new IllegalStateException("The log has already been stopped.");
	    }
	    duration = System.nanoTime() - startNanos;
	    //pop from stack
	    getNeedleContext().stopNeedle(this);
	}
    }

    public NeedleStub toNeedleStub() {
	return NeedleStub.copy(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return DEFAULT_RENDERER.render(this).toString();
    }

    private void copyData(final NeedleInfo needle) {
	startTime = needle.getStartTimeMillis();
	duration = needle.getDurationNanos();
	name = needle.getName();
	needleId = needle.getId();
	if (NeedleConfigFactory.isContextEnabled()) {
	    context = needle.getContext();
	}
	abortReason = needle.getAbortReason();
	debug(needle.getDebugLines());
    }

    /**
     * Gets the start stack index.
     * 
     * @return the start stack index
     */
    protected int getStartStackIndex() {
	return startStackIndex;
    }

    /**
     * Gets the stop stack index.
     * 
     * @return the stop stack index
     */
    protected int getStopStackIndex() {
	return stopStackIndex;
    }

    /**
     * Sets the log id.
     * 
     * @param needleId
     *            the new log id
     */
    protected void setNeedleId(final NeedleId needleId) {
	this.needleId = needleId;
    }

    /**
     * Sets the parent.
     * 
     * @param parent
     *            the new parent
     */
    protected void setParent(final Needle parent) {
	this.parent = parent;
    }
}
