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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Assert;
import net.StubClass;
import net.sf.StubClass2;

import org.junit.Before;
import org.junit.Test;

public class TestRendererHelper {

    private static RendererHelper helper = new RendererHelper();

    @Test
    public void testFormattedBoolean() {
	Assert.assertEquals("true", helper.getFormatedBoolean(true).toString());
    }

    @Test
    public void testFormattedDate() throws ParseException {
	final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	Assert.assertEquals("2000.01.31", helper.getFormatedDate(sdf.parse("31.01.2000").getTime()));
    }

    @Test
    public void testFormattedDouble() {
	Assert.assertEquals("123.001", helper.getFormatedDouble(123.0012223));
	Assert.assertEquals("4,123.001", helper.getFormatedDouble(4123.0012223));
    }

    @Test
    public void testFormattedTime() throws ParseException {
	final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	Assert.assertEquals("2000.01.31", helper.getFormatedDate(sdf.parse("31.01.2000").getTime()));
	Assert.assertEquals("00:01:06,123", helper.getFormatedTime(1000 * 60 + 6000 + 123));
    }

    @Before
    public void testLocaleSet() {
	Locale.setDefault(Locale.ENGLISH);
	TimeZone.setDefault(TimeZone.getTimeZone("GMT+0"));
    }

    @Test
    public void testShortenClassName() {
	Assert.assertEquals("...String", helper.shortenClassName(String.class.getName(), 2).toString());
	Assert.assertEquals("...String", helper.shortenClassName(String.class, 2).toString());
	//test anonymous class
	final Runnable run = new Runnable() {

	    @Override
	    public void run() {
	    }

	};
	Assert.assertEquals("...needles.renderer.TestRendererHelper$1", helper.shortenClassName(run.getClass(), 2).toString());
	Assert.assertEquals("net.sf.needles.renderer.TestRendererHelper$InnerClass", helper.shortenClassName(new InnerClass().getClass(), -1).toString());
	Assert.assertEquals("net.sf.needles.renderer.TestRendererHelper$InnerClass", helper.shortenClassName(new InnerClass().getClass(), 0).toString());
	Assert.assertEquals("...sf.needles.renderer.TestRendererHelper$InnerClass", helper.shortenClassName(new InnerClass().getClass(), 1).toString());
	Assert.assertEquals("...needles.renderer.TestRendererHelper$InnerClass", helper.shortenClassName(new InnerClass().getClass(), 2).toString());
	Assert.assertEquals("...renderer.TestRendererHelper$InnerClass", helper.shortenClassName(new InnerClass().getClass(), 3).toString());
	Assert.assertEquals("...TestRendererHelper$InnerClass", helper.shortenClassName(new InnerClass().getClass(), 4).toString());
	Assert.assertEquals("...TestRendererHelper$InnerClass", helper.shortenClassName(new InnerClass().getClass(), 5).toString());
	Assert.assertEquals("...StubClass", helper.shortenClassName(new StubClass().getClass(), 2).toString());
	Assert.assertEquals("...StubClass2", helper.shortenClassName(new StubClass2().getClass(), 2).toString());
    }

    public class InnerClass {

    }

}
