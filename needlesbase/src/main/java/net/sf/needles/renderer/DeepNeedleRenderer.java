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

import net.sf.needles.NeedleInfo;

/**
 * Is able to render the first needle different than the sublogs.
 * 
 * @author Marco Brade
 */
public class DeepNeedleRenderer implements NeedleRenderer {

    /** The first needle renderer. */
    private NeedleRenderer firstneedleRenderer;

    /** The deep needle renderer. */
    private NeedleRenderer deepneedleRenderer;

    /** The needle break. */
    private String needleBreak;

    private AbstractRendererHelper helper;

    /**
     * Instantiates a new deep needle renderer.
     */
    public DeepNeedleRenderer() {
    }

    /**
     * Instantiates a new deep needle renderer.
     * 
     * @param firstNeedleRenderer
     *            the first needle renderer
     * @param deepNeedleRenderer
     *            the deep needle renderer
     * @param needleBreak
     *            the needle break
     * @param helper
     *            the helper
     */
    public DeepNeedleRenderer(final NeedleRenderer firstNeedleRenderer, final NeedleRenderer deepNeedleRenderer, final String needleBreak, final AbstractRendererHelper helper) {
	this.firstneedleRenderer = firstNeedleRenderer;
	this.deepneedleRenderer = deepNeedleRenderer;
	this.needleBreak = (needleBreak != null) ? needleBreak : "";
	this.helper = helper;
    }

    /**
     * Instantiates a new deep needle renderer.
     * 
     * @param needleRenderer
     *            the needle renderer
     * @param needleBreak
     *            the needle break
     * @param helper
     *            the helper
     */
    public DeepNeedleRenderer(final NeedleRenderer needleRenderer, final String needleBreak, final AbstractRendererHelper helper) {
	this(needleRenderer, needleRenderer, needleBreak, helper);
    }

    /**
     * Gets the deep needle renderer.
     * 
     * @return the deep needle renderer
     */
    public NeedleRenderer getDeepneedleRenderer() {
	return deepneedleRenderer;
    }

    /**
     * Gets the first needle renderer.
     * 
     * @return the first needle renderer
     */
    public NeedleRenderer getFirstneedleRenderer() {
	return firstneedleRenderer;
    }

    /**
     * Gets the needle break.
     * 
     * @return the needle break
     */
    public String getNeedleBreak() {
	return needleBreak;
    }

    /**
     * Gets the renderer helper.
     * 
     * @return the renderer helper
     */
    public AbstractRendererHelper getRendererHelper() {
	return helper;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.renderer.NeedleRenderer#render(net.sf.needles.NeedleInfo)
     */
    @Override
    public StringBuilder render(final NeedleInfo needle) {
	final StringBuilder strb = new StringBuilder();
	strb.append(firstneedleRenderer.render(needle));
	final DeepNeedleRenderer subRenderer = new DeepNeedleRenderer(deepneedleRenderer, needleBreak, helper);
	for (final NeedleInfo childneedle : needle.getChildren()) {
	    strb.append(needleBreak);
	    strb.append(subRenderer.render(childneedle));
	}
	return strb;
    }

    /**
     * Sets the deep needle renderer.
     * 
     * @param deepneedleRenderer
     *            the new deep needle renderer
     */
    public void setDeepneedleRenderer(final NeedleRenderer deepneedleRenderer) {
	this.deepneedleRenderer = deepneedleRenderer;
    }

    /**
     * Sets the first needle renderer.
     * 
     * @param firstneedleRenderer
     *            the new first needle renderer
     */
    public void setFirstneedleRenderer(final NeedleRenderer firstneedleRenderer) {
	this.firstneedleRenderer = firstneedleRenderer;
    }

    /**
     * Sets the needle break.
     * 
     * @param needleBreak
     *            the new needle break
     */
    public void setNeedleBreak(final String needleBreak) {
	this.needleBreak = needleBreak;
    }

    /**
     * Sets the renderer helper.
     * 
     * @param helper
     *            the new renderer helper
     */
    public void setRendererHelper(final AbstractRendererHelper helper) {
	this.helper = helper;
    }

}
