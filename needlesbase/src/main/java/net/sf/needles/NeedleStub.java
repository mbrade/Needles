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

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.needles.renderer.AbstractRendererHelper;
import net.sf.needles.renderer.BasicRendererHelper;
import net.sf.needles.renderer.DeepNeedleRenderer;
import net.sf.needles.renderer.NeedleRenderer;
import net.sf.needles.renderer.SimpleNeedleRenderer;

/**
 * The Class NeedleStub.
 */
public class NeedleStub implements NeedleInfo {

    private static final AbstractRendererHelper RENDERER_HELPER = new BasicRendererHelper();
    private static final NeedleRenderer DEFAULT_RENDERER = new DeepNeedleRenderer(
	                                                                          new SimpleNeedleRenderer(true, true, false, null, false, true, true, true, true, RENDERER_HELPER),
	                                                                          new SimpleNeedleRenderer(true, false, true, "\t", true, true, true, true, true, RENDERER_HELPER),
	                                                                          "\n", RENDERER_HELPER);

    private static final long serialVersionUID = 1L;

    private List<String> debugLines;
    private int depth;
    private long durationNanos;
    private long durationMillis;
    private String name;
    private long startTimeMillis;
    private long stopTimeMillis;
    private NeedleId needleId;
    private String shortInfo;
    private NeedleState needleState;
    private long ownDurationNanos;
    private net.sf.needles.NeedleInfo parentNeedle;
    private StackTraceElement startStackTraceElement;
    private StackTraceElement stopStackTraceElement;
    private List<NeedleInfo> children = new LinkedList<NeedleInfo>();
    private Serializable[] context;
    private Throwable abortReason;
    private boolean fromparallelProcess;

    /**
     * Instantiates a new needle stub.
     */
    public NeedleStub() {
    }

    public NeedleStub(final NeedleId needleId, final String name) {
	this.needleId = needleId;
	this.name = name;
    }

    /**
     * Copies an given NeedleInfo into an {@link NeedleStub} structure. This method gets the root Needle of the given Parameter sourceNeedle.
     * 
     * @param sourceNeedle
     *            the source needle
     * @return the needle stub
     */
    public static NeedleStub copy(final NeedleInfo sourceNeedle) {
	final NeedleInfo rootNeedle = getRootNeedle(sourceNeedle);
	final Map<NeedleInfo, NeedleStub> copyMap = new HashMap<NeedleInfo, NeedleStub>();
	final NeedleStub stub = copyNeedleData(rootNeedle, new NeedleStub());
	copyMap.put(rootNeedle, stub);
	copyChildren(rootNeedle, stub, copyMap);
	return copyMap.get(sourceNeedle);
    }

    private static NeedleStub copyChildren(final NeedleInfo sourceNeedle, final NeedleStub targetNeedle, final Map<NeedleInfo, NeedleStub> copyMap) {
	for (final NeedleInfo sourceChild : sourceNeedle.getChildren()) {
	    final NeedleStub child = new NeedleStub();
	    copyNeedleData(sourceChild, child);
	    copyMap.put(sourceChild, child);
	    copyChildren(sourceChild, child, copyMap);
	    child.parentNeedle = targetNeedle;
	    targetNeedle.children.add(child);
	}
	return targetNeedle;
    }

    /**
     * Copy needle info to needle stub.
     * 
     * @param sourceNeedle
     *            the source needle
     * @param targetNeedle
     *            the target needle
     * @return the needle stub
     */
    private static NeedleStub copyNeedleData(final NeedleInfo sourceNeedle, final NeedleStub targetNeedle) {
	targetNeedle.debugLines = sourceNeedle.getDebugLines();
	targetNeedle.depth = sourceNeedle.getDepth();
	targetNeedle.durationMillis = sourceNeedle.getDurationMillis();
	targetNeedle.durationNanos = sourceNeedle.getDurationNanos();
	targetNeedle.name = sourceNeedle.getName();
	targetNeedle.needleId = sourceNeedle.getId();
	targetNeedle.needleState = sourceNeedle.getNeedleState();
	targetNeedle.ownDurationNanos = sourceNeedle.getOwnDurationNanos();
	targetNeedle.shortInfo = sourceNeedle.toString();
	targetNeedle.startStackTraceElement = sourceNeedle.getStartStackTraceElement();
	targetNeedle.startTimeMillis = sourceNeedle.getStartTimeMillis();
	targetNeedle.stopStackTraceElement = sourceNeedle.getStopStackTraceElement();
	targetNeedle.stopTimeMillis = sourceNeedle.getStopTimeMillis();
	targetNeedle.context = sourceNeedle.getContext();
	targetNeedle.abortReason = sourceNeedle.getAbortReason();
	targetNeedle.fromparallelProcess = sourceNeedle.isFromParallelProcess();
	return targetNeedle;
    }

    private static NeedleInfo getRootNeedle(final NeedleInfo sourceNeedle) {
	NeedleInfo rootNeedle = sourceNeedle;
	while (rootNeedle.getParentNeedle() != null) {
	    rootNeedle = rootNeedle.getParentNeedle();
	}
	return rootNeedle;
    }

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
    public List<net.sf.needles.NeedleInfo> getChildren() {
	return children;
    }

    @Override
    public Serializable[] getContext() {
	return context;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getDebugLines()
     */
    public List<String> getDebugLines() {
	return debugLines;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getDepth()
     */
    public int getDepth() {
	return depth;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getDurationMillis()
     */
    public long getDurationMillis() {
	return durationMillis;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getDurationNanos()
     */
    public long getDurationNanos() {
	return durationNanos;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getId()
     */
    @Override
    public NeedleId getId() {
	return needleId;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getName()
     */
    public String getName() {
	return name;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getNeedleState()
     */
    @Override
    public NeedleState getNeedleState() {
	return needleState;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getOwnDurationNanos()
     */
    @Override
    public long getOwnDurationNanos() {
	return ownDurationNanos;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getParentNeedle()
     */
    @Override
    public net.sf.needles.NeedleInfo getParentNeedle() {
	return parentNeedle;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getRootNeedle()
     */
    @Override
    public NeedleInfo getRootNeedle() {
	return (parentNeedle != null) ? parentNeedle.getRootNeedle() : this;
    }

    /**
     * Gets the short info.
     * 
     * @return the short info
     */
    public String getShortInfo() {
	return shortInfo;
    }

    @Override
    public StackTraceElement getStartStackTraceElement() {
	return startStackTraceElement;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getStartTimeMillis()
     */
    public long getStartTimeMillis() {
	return startTimeMillis;
    }

    @Override
    public StackTraceElement getStopStackTraceElement() {
	return stopStackTraceElement;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#getStopTimeMillis()
     */
    public long getStopTimeMillis() {
	return stopTimeMillis;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#isAborted()
     */
    public boolean isAborted() {
	return needleState == NeedleState.ABORTED;
    }

    @Override
    public boolean isFromParallelProcess() {
	return fromparallelProcess;
    }

    @Override
    public boolean isRoot() {
	return parentNeedle == null;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#isStarted()
     */
    public boolean isStarted() {
	return needleState == NeedleState.STARTED;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.NeedleInfo#isStopped()
     */
    public boolean isStopped() {
	return needleState == NeedleState.STOPPED;
    }

    @Override
    public NeedleStub toNeedleStub() {
	return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return DEFAULT_RENDERER.render(this).toString();
    }

}
