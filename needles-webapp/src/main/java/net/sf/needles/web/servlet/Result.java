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
package net.sf.needles.web.servlet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import net.sf.needles.NeedleInfo;
import net.sf.needles.web.renderer.JsonRenderer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author sthuth
 *
 */
public class Result {

    private String resultKey;
    private Collection<? extends NeedleInfo> needlesToRender;
    private JsonRenderer jsonRenderer = new JsonRenderer();
    
    /**
     * 
     */
    public Result(String resultKey, Collection<? extends NeedleInfo> needlesToRender) {
	this.resultKey = resultKey;
	this.needlesToRender = needlesToRender;
    }

    public String toJson() throws IOException {
	
	JsonFactory jsonFactory = new JsonFactory();
	StringWriter writer = new StringWriter(4000);
	JsonGenerator generator = jsonFactory.createGenerator(writer);
	generator.writeStartObject();
	generator.writeStringField("resultKey", resultKey);
	generator.writeStringField("needles", this.jsonRenderer.renderNeedles(needlesToRender).toString());
	generator.writeEndObject();
	generator.flush();
	generator.close();
	writer.close();
	return writer.toString();
    }
    
}
