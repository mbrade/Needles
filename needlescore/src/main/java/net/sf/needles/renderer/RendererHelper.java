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

import java.util.Date;

/**
 * Class providing helper methods to format Needle log data.
 * 
 * @author Marco Brade
 */
public class RendererHelper extends AbstractRendererHelper {

    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see net.sf.needles.renderer.AbstractRendererHelper#getFormatedDate(long)
     */
    @Override
    public CharSequence getFormatedDate(final long time) {
	return String.format("%1$tY.%1$tm.%1$td", new Date(time));
    }

    /* (non-Javadoc)
     * @see net.sf.needles.renderer.AbstractRendererHelper#getFormatedDouble(double)
     */
    @Override
    public CharSequence getFormatedDouble(final double number) {
	return String.format("%,.3f", number);
    }

    /* (non-Javadoc)
     * @see net.sf.needles.renderer.AbstractRendererHelper#getFormatedTime(long)
     */
    @Override
    public CharSequence getFormatedTime(final long time) {
	return String.format("%1$tH:%1$tM:%1$tS,%1$tL", new Date(time));
    }
}
