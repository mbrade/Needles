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

package net.sf.needles.aggregation;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.sf.needles.GlobalContext;
import net.sf.needles.Needle;
import net.sf.needles.NeedleConfig;
import net.sf.needles.NeedleConfigFactory;
import net.sf.needles.NeedleInfo;
import net.sf.needles.aggregation.keycreator.AggregationKey;
import net.sf.needles.aggregation.keycreator.NeedleNameKeyCreator;
import net.sf.needles.aggregation.worker.AggregationWorker;
import net.sf.needles.aggregation.worker.SimpleAggregationWorker;

import org.junit.Before;
import org.junit.Test;

public class AggregationTreeTest {

    @Before
    public void setup() {
	final AggregationWorker worker = new SimpleAggregationWorker();
	final AggregationFactory<? extends Aggregation<?>> factory = new ExecutionAggregationFactory();
	worker.addAggregationFactory(factory);
	GlobalContext.setAggregationWorker(worker);
	NeedleConfigFactory.setDefaultNeedleConfig(NeedleConfig.DEBUG);
    }

    @Test
    public void testAggregationTreeCreation() {
	Needle.start("id1");
	Needle.start("id2");
	Needle.stopCurrentNeedle();
	Needle.stopCurrentNeedle();
	final List<AggregationFactory<? extends Aggregation<?>>> aggregators = GlobalContext.getAggregationWorker().getAggregationFactories();
	Assert.assertEquals(1, aggregators.size());
	Assert.assertEquals(1, aggregators.get(0).getRootAggregations().size());
	Assert.assertEquals("id1", aggregators.get(0).getRootAggregations().get(0).getNeedleName());
	Assert.assertEquals(1, aggregators.get(0).getRootAggregations().get(0).getChildAggregations().size());
	//	Assert.assertEquals("id2", aggregators.get(0).getRootAggregations().get(0).getChildAggregations().get(0).getNeedleName());
    }

    @Test
    public void testHotspotAggregation() throws InterruptedException {
	final int hotspotCount = 10;
	final int measures = 20;
	Assert.assertTrue("Measures should be greater than count.", measures > hotspotCount);
	final AggregationFactory<HotspotAggregation> factory = new HotspotAggregationFactory(hotspotCount);
	GlobalContext.getAggregationWorker().addAggregationFactory(factory);
	for (int i = 0; i < 20; i++) {
	    Needle.start(i + "");
	    Thread.sleep(((i >= hotspotCount) ? 100 : 0) * i);
	    Needle.start("bla");
	    Needle.stopCurrentNeedle();
	    Needle.stopCurrentNeedle();
	}
	final Map<AggregationKey, Aggregation<?>> aggregations = GlobalContext.getAggregations(factory.getName());
	Assert.assertEquals(1, aggregations.size());
	final Aggregation<?> aggregation = aggregations.values().iterator().next();
	final HotspotAggregationImpl hag = (HotspotAggregationImpl) aggregation;
	Assert.assertEquals(hotspotCount, hag.getHotspots().size());
	NeedleInfo last = null;
	int i = measures - 1;
	for (final NeedleInfo needle : hag.getHotspots()) {
	    if (last == null) {
		last = needle;
	    } else {
		Assert.assertTrue("The log from HotspotAggregation is not in the correct order.", needle.getDurationMillis() < last.getDurationMillis());
	    }
	    Assert.assertEquals(i-- + "", needle.getName());
	}
    }

    @Test
    public void testRecursion() {
	final AggregationFactory<ExecutionAggregation> factory = new ExecutionAggregationFactory(NeedleNameKeyCreator.INSTANCE, "Second ExecutionAggregation");
	final String aggregationKey = factory.getName();
	GlobalContext.getAggregationWorker().addAggregationFactory(factory);
	recursion(5);
	final Map<String, List<? extends Aggregation<?>>> aggregations = GlobalContext.getAggregations();
	Assert.assertEquals(2, aggregations.size());
	for (final Map.Entry<String, List<? extends Aggregation<?>>> aggregation : aggregations.entrySet()) {
	    if (aggregation.getKey().equals(aggregationKey)) {
		final List<? extends Aggregation<?>> rootAggregations = aggregation.getValue();
		Assert.assertEquals(1, rootAggregations.size());
		final ExecutionAggregationImpl exAggregation = (ExecutionAggregationImpl) rootAggregations.get(0);
		Assert.assertEquals(6, exAggregation.getMeasurements());
		Assert.assertEquals(0, rootAggregations.get(0).getChildAggregations().size());
	    }
	    if (!aggregation.getKey().equals(aggregationKey)) {
		final List<? extends Aggregation<?>> rootAggregations = aggregation.getValue();
		Assert.assertEquals(1, rootAggregations.size());
		Assert.assertEquals(1, rootAggregations.get(0).getChildAggregations().size());
		ExecutionAggregationImpl exAggregation = (ExecutionAggregationImpl) rootAggregations.get(0);
		Assert.assertEquals(1, exAggregation.getMeasurements());
		int c = 1;
		while (!exAggregation.getChildAggregations().isEmpty()) {
		    exAggregation = (ExecutionAggregationImpl) exAggregation.getChildAggregations().get(0);
		    Assert.assertEquals(1, exAggregation.getMeasurements());
		    c++;
		}
		Assert.assertEquals(6, c);
	    }
	}
    }

    @Test
    public void testTop10Aggregator() throws InterruptedException {
	final AggregationWorker worker = new SimpleAggregationWorker();
	final AggregationFactory<Top10Aggregation> factory = new Top10AggregationFactory();
	worker.addAggregationFactory(factory);
	GlobalContext.setAggregationWorker(worker);
	Needle.start("1", 1);
	Thread.sleep(500);
	Needle.stopCurrentNeedle();
	Needle.start("1", 2);
	Thread.sleep(450);
	Needle.stopCurrentNeedle();
	Needle.start("1", 3);
	Thread.sleep(400);
	Needle.stopCurrentNeedle();
	Needle.start("1", 4);
	Thread.sleep(350);
	Needle.stopCurrentNeedle();
	Needle.start("1", 5);
	Thread.sleep(300);
	Needle.stopCurrentNeedle();
	Needle.start("1", 6);
	Thread.sleep(250);
	Needle.stopCurrentNeedle();
	Needle.start("1", 7);
	Thread.sleep(200);
	Needle.stopCurrentNeedle();
	Needle.start("1", 8);
	Thread.sleep(150);
	Needle.stopCurrentNeedle();
	Needle.start("1", 9);
	Thread.sleep(100);
	Needle.stopCurrentNeedle();
	Needle.start("1", 10);
	Thread.sleep(50);
	Needle.stopCurrentNeedle();
	Needle.start("1", 11);
	Thread.sleep(30);
	Needle.stopCurrentNeedle();
	Needle.start("1", 12);
	Thread.sleep(10);
	Needle.stopCurrentNeedle();
	final Map<AggregationKey, Aggregation<?>> rootAggregations = GlobalContext.getAggregations(factory.getName());
	final Top10AggregationImpl agg = (Top10AggregationImpl) rootAggregations.values().iterator().next();
	for (int i = 1; i <= 10; i++) {
	    Assert.assertEquals(i, (agg.getTop10Needles().get(i - 1)).getContext()[0]);
	}
    }

    private void recursion(int i) {
	Needle.start("recursion", i);
	if (i > 0) {
	    recursion(--i);
	}
	Needle.stopCurrentNeedle();
    }

}
