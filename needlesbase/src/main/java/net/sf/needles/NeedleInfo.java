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
import java.util.List;
import java.util.Map;

/**
 * The Interface NeedleInformation.
 */
public interface NeedleInfo extends Serializable {

    Throwable getAbortReason();

    /**
     * Gets the child count.
     * 
     * @return the child count
     */
    int getChildCount();

    /**
     * Gets the children.
     * 
     * @return the children
     */
    List<NeedleInfo> getChildren();

    /**
     * Gets the context informations of this Needle
     * 
     * @return context informations
     */
    Map<String, Object> getContext();

    /**
     * Gets the debug lines.
     * 
     * @return the debug lines
     */
    List<String> getDebugLines();

    /**
     * Gets the depth.
     * 
     * @return the depth
     */
    int getDepth();

    /**
     * Gets the duration millis.
     * 
     * @return the duration millis
     */
    long getDurationMillis();

    /**
     * Gets the duration nanos.
     * 
     * @return the duration nanos
     */
    long getDurationNanos();

    /**
     * Gets the id.
     * 
     * @return the id
     */
    NeedleId getId();

    /**
     * Gets the name.
     * 
     * @return the name
     */
    String getName();

    /**
     * Gets the log state.
     * 
     * @return the log state
     */
    NeedleState getNeedleState();

    /**
     * Gets the own duration nanos.
     * 
     * @return the own duration nanos
     */
    long getOwnDurationNanos();

    /**
     * Gets the parent needle.
     * 
     * @return the parent needle
     */
    NeedleInfo getParentNeedle();

    /**
     * Gets the root needle.
     * 
     * @return the root needle
     */
    NeedleInfo getRootNeedle();

    /**
     * Gets the start element.
     * 
     * @return the start element
     */
    StackTraceElement getStartStackTraceElement();

    /**
     * Gets the start time millis.
     * 
     * @return the start time millis
     */
    long getStartTimeMillis();

    /**
     * Gets the stop element.
     * 
     * @return the stop element
     */
    StackTraceElement getStopStackTraceElement();

    /**
     * Gets the stop time millis.
     * 
     * @return the stop time millis
     */
    long getStopTimeMillis();

    /**
     * Checks if is aborted.
     * 
     * @return true, if is aborted
     */
    boolean isAborted();

    /**
     * Indicator that should return true if the Needle has been created by another concurrent thread.
     * 
     * @return
     */
    boolean isFromParallelProcess();

    /**
     * Returns true if this is the root of a method call.
     * 
     * @return true if this is a root.
     */
    boolean isRoot();

    /**
     * Checks if is started.
     * 
     * @return true, if is started
     */
    boolean isStarted();

    /**
     * Checks if is stopped.
     * 
     * @return true, if is stopped
     */
    boolean isStopped();

    /**
     * Creates a stub object that is unbound to any Needles contexts. This can be used to serialize.
     * 
     * @return {@link NeedleStub}
     */
    NeedleStub toNeedleStub();
}
