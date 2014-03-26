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

import java.util.concurrent.atomic.AtomicReference;

import net.sf.needles.NeedleId;
import net.sf.needles.NeedleInfo;
import net.sf.needles.NeedleStub;
import net.sf.needles.aggregation.keycreator.SingletonKeyCreator;

public class HotspotAggregationFactory extends AbstractAggregationFactory<HotspotAggregation> {

    private final int maxHotSpots;
    private final static AtomicReference<HotspotAggregationImpl> aggregation = new AtomicReference<HotspotAggregationImpl>();

    public HotspotAggregationFactory() {
	this(20, HotspotAggregationImpl.NAME);
    }

    public HotspotAggregationFactory(final int maxHotspots) {
	this(maxHotspots, HotspotAggregationImpl.NAME);
    }

    public HotspotAggregationFactory(final int maxHotspots, final String aggregationName) {
	super(SingletonKeyCreator.INSTANCE, aggregationName);
	this.maxHotSpots = maxHotspots;
	//Create an dummy Needle
	final NeedleStub stub = new NeedleStub(NeedleId.EMPTY_LOG_ID, getName());
	aggregation.compareAndSet(null, new HotspotAggregationImpl(this, stub, getMaxHotspots()));
    }

    public HotspotAggregationFactory(final String aggregationName) {
	this(20, aggregationName);
    }

    @Override
    public HotspotAggregation doCreateAggregation(final NeedleInfo needle) {
	return aggregation.get();
    }

    public int getMaxHotspots() {
	return maxHotSpots;
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

}
