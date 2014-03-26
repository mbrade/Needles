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

package net.sf.needles.spring.service.test;

import java.util.Arrays;
import java.util.Map;

import junit.framework.Assert;
import net.sf.needles.GlobalContext;
import net.sf.needles.Needle;
import net.sf.needles.NeedleConfig;
import net.sf.needles.NeedleConfigFactory;
import net.sf.needles.NeedleContext;
import net.sf.needles.aggregation.Aggregation;
import net.sf.needles.aggregation.ExecutionAggregationFactory;
import net.sf.needles.aggregation.keycreator.AggregationKey;
import net.sf.needles.aggregation.keycreator.NeedleIdKeyCreator;
import net.sf.needles.aggregation.worker.SimpleAggregationWorker;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class NeedleServiceTest {

    @Autowired(required = true)
    @Qualifier("local")
    private TestService testService;

    @BeforeClass
    public static void setup() {
	GlobalContext.setAggregationWorker(new SimpleAggregationWorker());
	GlobalContext.getAggregationWorker().addAggregationFactory(new ExecutionAggregationFactory(NeedleIdKeyCreator.INSTANCE, "execution"));
	GlobalContext.start();
    }

    @AfterClass
    public static void shutdown() {
	GlobalContext.shutdown();
    }

    @Test
    public void testRemoteServiceInvocation() throws InterruptedException {
	NeedleConfigFactory.setNeedleConfig(NeedleConfig.DEBUG);
	final Needle startInvokerService = Needle.start("startInvokeService");
	System.out.println(testService.executeServiceMethod("Hallo"));
	final Needle afterService1 = Needle.start("afterService1");
	synchronized (this) {
	    this.wait(210);
	}
	Needle.stopCurrentNeedle();
	Needle.stopCurrentNeedle();
	final Needle afterService2 = Needle.start("afterService2");
	synchronized (this) {
	    this.wait(200);
	}
	Needle.stopCurrentNeedle();
	Assert.assertEquals(2, NeedleContext.getRootNeedles().size());
	Assert.assertEquals(2, NeedleContext.getRootNeedles().get(0).getChildCount());
	Assert.assertEquals(1, NeedleContext.getRootNeedles().get(0).getChildren().get(0).getChildCount());

	final Map<AggregationKey, Map<String, Aggregation<?>>> aggregations = GlobalContext.getAggregations(Arrays.asList(new AggregationKey[] { startInvokerService.getId(), afterService1.getId(),
	                                                                                                                                        afterService2.getId() }));
	for (final AggregationKey key : aggregations.keySet()) {
	    final Map<String, Aggregation<?>> map = aggregations.get(key);
	    for (final Aggregation aggregation : map.values()) {
		System.out.println(aggregation.toString());
	    }
	}

    }
}
