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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import net.sf.needles.NeedleInfo;
import net.sf.needles.util.NeedleDurationComparator;

public class Top10AggregationImpl extends AbstractAggregation<Top10Aggregation> implements Top10Aggregation {

    private static final long serialVersionUID = 1L;
    public final static String NAME = "TOP10";
    private final ConcurrentSkipListSet<NeedleInfo> top10 = new ConcurrentSkipListSet<NeedleInfo>(NeedleDurationComparator.INSTANCE);

    public Top10AggregationImpl(final AggregationFactory<Top10Aggregation> factory, final NeedleInfo needle) {
	super(factory, needle);
    }

    @Override
    public void aggregate(final NeedleInfo needle) {
	top10.add(needle);
	if (top10.size() > 10) {
	    top10.pollLast();
	}
    }

    @Override
    public String getAggregationName() {
	return NAME;
    }

    @Override
    public String getAggregationString() {
	return null;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.Top10AggregationI#getTop10ChildAggregations()
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Top10Aggregation> getTop10ChildAggregations() {
	@SuppressWarnings("rawtypes")
	final List result = getChildAggregations();
	return result;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.Top10AggregationI#getTop10Needles()
     */
    @Override
    public List<NeedleInfo> getTop10Needles() {
	final List<NeedleInfo> result = new ArrayList<NeedleInfo>(10);
	for (final NeedleInfo needle : top10) {
	    if (result.size() < 10) {
		result.add(needle);
	    }
	}
	return result;
    }

}
