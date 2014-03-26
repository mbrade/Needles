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

import java.util.HashMap;
import java.util.Map;

import net.sf.needles.NeedleInfo;

import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * The Class ExtendedRemoteInvocationResult. Extends Springs RemoteInvocationResult and allows to transmit back attributes to the original results.
 * 
 * @author Marco Brade
 */
public class ExtendedRemoteInvocationResult extends RemoteInvocationResult {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant LOGEVENT_KEY. */
    public final static String NEEDLE_ATTRIBUTE_KEY_NAME = "needleinfo";

    /** The attributes. */
    private Map<String, Object> attributes = new HashMap<String, Object>(1, 1.0f);

    /**
     * Instantiates a new extended remote invocation result.
     * 
     * @param value
     *            the value
     */
    public ExtendedRemoteInvocationResult(final Object value) {
	super(value);
    }

    /**
     * Instantiates a new extended remote invocation result.
     * 
     * @param exception
     *            the exception
     */
    public ExtendedRemoteInvocationResult(final Throwable exception) {
	super(exception);
    }

    /**
     * Adds the attribute.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public void addAttribute(final String key, final Object value) {
	this.attributes.put(key, value);
    }

    /**
     * Gets the attributes.
     * 
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
	return attributes;
    }

    /**
     * Gets the needleInfo.
     * 
     * @return the needleInfo
     */
    public NeedleInfo getNeedleInfo() {
	return (NeedleInfo) attributes.get(NEEDLE_ATTRIBUTE_KEY_NAME);
    }

    /**
     * Sets the attributes.
     * 
     * @param attributes
     *            the attributes
     */
    public void setAttributes(final Map<String, Object> attributes) {
	this.attributes = attributes;
    }

    /**
     * Sets the needleInfo.
     * 
     * @param needleInfo
     *            the new log events
     */
    public void setNeedleInfo(final NeedleInfo needleInfo) {
	attributes.put(NEEDLE_ATTRIBUTE_KEY_NAME, needleInfo);
    }

}
