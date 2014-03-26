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

package net.sf.needles.renderer;

import net.sf.needles.NeedleInfo;

/**
 * Renders a single Needle.
 * 
 * @author Marco Brade
 */
public class SimpleNeedleRenderer implements NeedleRenderer {

    /** The print calendar line. */
    private boolean printCalendarLine;

    /** The print prefix. */
    private boolean printPrefix;

    /** The prefix. */
    private String prefix;

    /** The print stack elements. */
    private boolean printStackElements;

    /** The print context. */
    private boolean printContext;

    /** The multiply prefix by depth. */
    private boolean multiplyPrefixByDepth;

    /** The print debug. */
    private boolean printDebug;

    private boolean printIsFromParallelProcess;

    private boolean printStatus;

    private AbstractRendererHelper rendererHelper;

    /**
     * Instantiates a new simple needle renderer.
     */
    public SimpleNeedleRenderer() {
	this(true, true, true, " ", true, true, true, false, true, null);
    }

    /**
     * Instantiates a new simple needle renderer.
     * 
     * @param printStatus
     *            the print status
     * @param printCalendarLine
     *            the print calendar line
     * @param printPrefix
     *            the print prefix
     * @param prefix
     *            the prefix
     * @param multiplyPrefixByDepth
     *            the multiply prefix by depth
     * @param printStackElements
     *            the print stack elements
     * @param printContext
     *            the print context
     * @param printDebug
     *            the print debug
     * @param printIsFromParallelProcess
     *            the print is from parallel process
     */
    public SimpleNeedleRenderer(final boolean printStatus, final boolean printCalendarLine, final boolean printPrefix, final String prefix, final boolean multiplyPrefixByDepth,
	final boolean printStackElements, final boolean printContext, final boolean printDebug, final boolean printIsFromParallelProcess, final AbstractRendererHelper rendererHelper) {
	this.printStatus = printStatus;
	this.printCalendarLine = printCalendarLine;
	this.printPrefix = printPrefix;
	this.prefix = (prefix != null) ? prefix : "";
	this.multiplyPrefixByDepth = multiplyPrefixByDepth;
	this.printStackElements = printStackElements;
	this.printContext = printContext;
	this.printDebug = printDebug;
	this.printIsFromParallelProcess = printIsFromParallelProcess;
	this.rendererHelper = rendererHelper;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.renderer.NeedleRenderer#render(net.sf.needles.NeedleInfo)
     */
    @Override
    public StringBuilder render(final NeedleInfo needle) {
	final StringBuilder strb = new StringBuilder();
	if (printPrefix) {
	    if (multiplyPrefixByDepth) {
		for (int i = 1; i < needle.getDepth(); i++) {
		    strb.append(prefix);
		}
	    } else {
		strb.append(prefix);
	    }
	}
	strb.append(needle.getName()).append(" - ");
	if (printStatus) {
	    strb.append(needle.isAborted() ? "(A) - " : "(N) - ");
	}
	if (printIsFromParallelProcess) {
	    //    strb.append(log.isFromParallelProcess() ? "(P) - " : "(S) - ");
	}
	if (rendererHelper != null) {
	    strb.append(rendererHelper.getFormatedDouble(needle.getDurationNanos() / 1000000.0));

	    if (printCalendarLine) {
		strb.append(" - ").append(rendererHelper.getFormatedDateRange(needle));
	    }

	    if (printStackElements && needle.getStartStackTraceElement() != null) {
		strb.append(" - ").append(rendererHelper.getFormatedStack(needle));
	    }

	    if (printContext) {
		strb.append(" - ").append(rendererHelper.getFormatedContext(needle));
	    }
	}

	if (printDebug) {
	    for (final String line : needle.getDebugLines()) {
		strb.append("\n");//System.getProperty("line.separator"));
		if (multiplyPrefixByDepth) {
		    for (int i = 1; i < needle.getDepth(); i++) {
			strb.append(prefix);
		    }
		}
		strb.append(" - ");
		strb.append(line);
	    }
	}

	return strb;
    }

}
