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

/**
 * 
 */
package net.sf.needles.web.renderer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import net.sf.needles.NeedleInfo;
import net.sf.needles.renderer.NeedleRenderer;
import net.sf.needles.renderer.RendererHelper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author sthuth
 * 
 */
public class JsonRenderer implements NeedleRenderer {

    private static final RendererHelper RENDERER_HELPER = new RendererHelper();

    /**
     * 
     */
    public JsonRenderer() {
	// TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see net.sf.needles.renderer.NeedleRenderer#render(net.sf.needles.NeedleInfo)
     */
    @Override
    public StringBuilder render(final NeedleInfo needle) {
	try {
	    final JsonFactory factory = new JsonFactory();
	    final StringWriter stringWriter = new StringWriter(500);
	    final JsonGenerator generator = factory.createGenerator(stringWriter);
	    renderNeedleWithGivenGenerator(needle, generator);
	    generator.close();
	    stringWriter.close();
	    return new StringBuilder(stringWriter.toString());
	} catch (final JsonProcessingException e) {
	    e.printStackTrace();
	} catch (final IOException e) {
	    e.printStackTrace();
	} catch (final IllegalArgumentException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public StringBuilder renderNeedles(final Collection<? extends NeedleInfo> needles) {

	final StringBuilder sb = new StringBuilder();

	final JsonFactory factory = new JsonFactory();
	final StringWriter writer = new StringWriter((500 * needles.size()));

	try {
	    final JsonGenerator generator = factory.createGenerator(writer);
	    generator.writeStartArray();
	    for (final NeedleInfo ni : needles) {
		renderJsonNeedle(ni, generator);
	    }
	    generator.writeEndArray();
	    generator.flush();
	    writer.close();
	    generator.close();
	} catch (final IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	sb.append(writer.toString());
	return sb;
    }

    /**
     * @param needle
     * @param generator
     * @throws IOException
     * @throws JsonGenerationException
     */
    private void renderJsonNeedle(final NeedleInfo needle, final JsonGenerator generator) throws IOException, JsonGenerationException {
	generator.writeStartObject();

	generator.writeStringField("id", needle.getId().toString());
	generator.writeStringField("name", needle.getName());
	generator.writeStringField("date", RENDERER_HELPER.getFormatedDateRange(needle).toString());
	generator.writeStringField("duration", RENDERER_HELPER.getFormatedDouble(needle.getDurationNanos()).toString());
	generator.writeNumberField("childrenCount", needle.getChildCount());
	generator.writeStringField("context", RENDERER_HELPER.getFormatedContext(needle).toString());

	generator.writeEndObject();
    }

    protected void renderNeedleWithGivenGenerator(final NeedleInfo needle, final JsonGenerator generator) {
	try {
	    renderJsonNeedle(needle, generator);
	} catch (final JsonProcessingException e) {
	    e.printStackTrace();
	} catch (final IOException e) {
	    e.printStackTrace();
	} catch (final IllegalArgumentException e) {
	    e.printStackTrace();
	}
    }
}
