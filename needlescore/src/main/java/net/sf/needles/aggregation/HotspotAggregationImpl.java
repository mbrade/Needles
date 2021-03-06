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

public class HotspotAggregationImpl extends AbstractAggregation<HotspotAggregation> implements HotspotAggregation {

    private static final long serialVersionUID = 1L;
    public final static String NAME = "Hotspot";
    private final ConcurrentSkipListSet<NeedleInfo> hotspots = new ConcurrentSkipListSet<NeedleInfo>(NeedleDurationComparator.INSTANCE);
    private final int maxHotspots;

    public HotspotAggregationImpl(final AggregationFactory<HotspotAggregation> factory, final NeedleInfo needle, final int maxHotspots) {
	super(factory, needle);
	this.maxHotspots = maxHotspots;
    }

    @Override
    public void aggregate(final NeedleInfo needle) {
	hotspots.add(needle);
	if (hotspots.size() > maxHotspots) {
	    hotspots.pollLast();
	}
    }

    @Override
    public String getAggregationString() {
	return null;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.HotspotAggregation#getHotspots()
     */
    @Override
    public List<NeedleInfo> getHotspots() {
	return new ArrayList<NeedleInfo>(hotspots);
    }

}
