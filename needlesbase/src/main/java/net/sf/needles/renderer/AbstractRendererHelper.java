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

package net.sf.needles.renderer;

import java.io.Serializable;

import net.sf.needles.NeedleInfo;

/**
 * Class providing helper methods to format Needle log data.
 * 
 * @author Marco Brade
 */
public abstract class AbstractRendererHelper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Gets the formated boolean.
     * 
     * @param value
     *            the value
     * @return the formated boolean
     */
    public CharSequence getFormatedBoolean(final boolean value) {
	return Boolean.toString(value);
    }

    /**
     * Returns the formated context info.
     * 
     * @param needle
     *            Needle instance
     * @return formated stack element
     */
    public CharSequence getFormatedContext(final NeedleInfo needle) {
	final StringBuilder ret = new StringBuilder();
	final Serializable[] infos = needle.getContext();
	if (infos != null && infos.length >= 1) {
	    ret.append(" O:");
	    for (int i = 0; i < infos.length; i++) {
		final Object myObject = infos[i];
		ret.append("{").append((myObject != null) ? myObject.toString() : "null").append("}");
		if (i < infos.length - 1) {
		    ret.append(",");
		}
	    }
	}

	return ret;
    }

    /**
     * Gets the formated date.
     * 
     * @param time
     *            the time
     * @return the formated date
     */
    public abstract CharSequence getFormatedDate(final long time);

    /**
     * Returns the formated time range.
     * 
     * @param needle
     *            Needle instance
     * @return formated date range
     */
    public CharSequence getFormatedDateRange(final NeedleInfo needle) {
	final CharSequence from = getFormatedTime(needle.getStartTimeMillis());
	final CharSequence to = getFormatedTime(needle.getStopTimeMillis());
	final CharSequence date = getFormatedDate(needle.getStartTimeMillis());
	return new StringBuilder(date).append(": (").append(from).append(" - ").append(to).append(")");
    }

    /**
     * Gets the formated double.
     * 
     * @param number
     *            to format
     * @return the formated double
     */
    public abstract CharSequence getFormatedDouble(final double number);

    /**
     * Returns the formated stack element.
     * 
     * @param needle
     *            Needle instance
     * @return formated stack element
     */
    public CharSequence getFormatedStack(final NeedleInfo needle) {
	final StringBuilder ret = new StringBuilder();
	final StackTraceElement startElement = needle.getStartStackTraceElement();
	if (startElement != null) {
	    final CharSequence clazz = shortenClassName(startElement.getClassName(), 2);
	    ret.append(clazz)
	       .append(".")
	       .append(startElement.getMethodName())
	       .append("(")
	       .append(startElement.getLineNumber());
	    final StackTraceElement stopElement = needle.getStopStackTraceElement();
	    if (stopElement != null) {
		if (!stopElement.getClassName().equals(startElement.getClassName()) ||
		    !stopElement.getMethodName().equals(startElement.getMethodName())) {
		    ret.append(") - ")
		       .append(shortenClassName(stopElement.getClassName(), 2))
		       .append(".")
		       .append(startElement.getMethodName())
		       .append("(")
		       .append(startElement.getLineNumber());
		} else {
		    ret.append(" - ").append(stopElement.getLineNumber());
		}
	    }
	    ret.append(")");
	}
	return ret;
    }

    /**
     * Returns the formated time in hh:mm:ss,SSSS format.
     * 
     * @param time
     *            to format.
     * @return formated String
     */
    public abstract CharSequence getFormatedTime(final long time);

    /**
     * Shorten class name.
     * 
     * @param clazz
     *            the clazz
     * @return the char sequence
     */
    public CharSequence shortenClassName(@SuppressWarnings("rawtypes") final Class clazz, final int packageCount) {
	return shortenClassName(clazz.getName(), packageCount);
    }

    /**
     * Shorten class name.
     * 
     * @param clazzName
     *            the clazz name
     * @return the char sequence
     */
    public CharSequence shortenClassName(final String clazzName, final int packageCount) {
	int idx = 0, tmp = 0;
	for (int i = 0; i < packageCount; i++) {
	    tmp = clazzName.indexOf('.', idx) + 1;
	    if (tmp <= 0) {
		break;
	    }
	    idx = tmp;
	}
	final CharSequence result = new StringBuilder((idx != 0) ? "..." : "").append(clazzName.substring(idx));
	return result;
    }
}
