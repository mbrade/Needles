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

/**
 * The Class NeedleException. Encapsulates an Exception. Caused exceptions will be converted to NeedleExceptions in order to keep Exceptions be transferable over wire without the need to have all
 * exceptions in classpath. The stacktraces will be copied.
 */
public class NeedleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new needle exception.
     */
    public NeedleException() {
	super();
    }

    /**
     * Instantiates a new needle exception.
     * 
     * @param message
     *            the message
     */
    public NeedleException(final String message) {
	this(message, null);
    }

    /**
     * Instantiates a new needle exception.
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public NeedleException(final String message, final Throwable cause) {
	super(getMessage(message, cause), getCause(cause));
	if (cause != null) {
	    setStackTrace(cause.getStackTrace());
	}
    }

    /**
     * Instantiates a new needle exception.
     * 
     * @param cause
     *            the cause
     */
    public NeedleException(final Throwable cause) {
	this(null, cause);
    }

    private static NeedleException getCause(final Throwable t) {
	if (t != null && t.getCause() != null) {
	    if (t.getCause() instanceof NeedleException) {
		return (NeedleException) t.getCause();
	    } else {
		return new NeedleException(t.getCause());
	    }
	}
	return null;
    }

    private static String getMessage(final String message, final Throwable t) {
	final StringBuilder sb = new StringBuilder();
	boolean needsDelimiter = false;
	if (message != null) {
	    sb.append(message);
	    needsDelimiter = true;
	}
	if (t != null) {
	    if (needsDelimiter) {
		sb.append(" - ");
	    }
	    sb.append(String.format("Exception: (%1$s / %2$s)", t.getClass().getName(), t.getMessage()));
	}
	return sb.toString();
    }

}
