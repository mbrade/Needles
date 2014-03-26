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

package net.sf.needles.aggregation;

import org.apache.commons.lang.StringUtils;

public final class AggregationRenderer {

    private AggregationRenderer() {
    }

    @SuppressWarnings("rawtypes")
    public static String buildString(final Aggregation aggregation) {
	return buildString(new StringBuilder(), aggregation, 0).toString();
    }

    @SuppressWarnings("rawtypes")
    private static StringBuilder buildString(final StringBuilder sb, final Aggregation aggregation, final int depth) {
	for (int i = 0; i < depth; i++) {
	    sb.append('\t');
	}
	if (depth == 0) {
	    sb.append(aggregation.getAggregationName()).append(" ");
	}
	sb.append(aggregation.getNeedleName());
	final String aggregationString = aggregation.getAggregationString();
	if (StringUtils.isNotBlank(aggregationString)) {
	    sb.append(" ").append(aggregation.getAggregationString());
	}
	for (final Object child : aggregation.getChildAggregations()) {
	    sb.append("\n");
	    buildString(sb, (Aggregation) child, depth + 1);
	}
	return sb;
    }
}
