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

package net.sf.needles.jms;

import net.sf.needles.GlobalContext;
import net.sf.needles.Needle;
import net.sf.needles.NeedleConfig;
import net.sf.needles.NeedleConfigFactory;
import net.sf.needles.aggregation.JmsAggregationServiceFactory;
import net.sf.needles.aggregation.worker.SimpleAggregationWorker;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class JMSAggregationTest {

    @BeforeClass
    public final static void setup() {
	NeedleConfigFactory.setNeedleConfig(NeedleConfig.DEBUG);
	final SimpleAggregationWorker simpleAggregationWorker = new SimpleAggregationWorker();
	final JmsAggregationServiceFactory factory = new JmsAggregationServiceFactory();
	factory.setJmsConnectionFactoryName("ConnectionFactory");
	factory.setJmsQueueName("dynamicQueues/jms/needles");
	factory.setJndiInitialContextFactoryClass("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
	factory.setJndiProviderUrl("tcp://localhost:61616");
	simpleAggregationWorker.addAggregationFactory(factory);
	GlobalContext.setAggregationWorker(simpleAggregationWorker);
	GlobalContext.start();
    }

    @AfterClass
    public final static void shutdown() {
	GlobalContext.shutdown();
    }

    @Test
    public void testJMSAggregation() throws InterruptedException {
	synchronized (this) {
	    final Needle start = Needle.start("first");
	    wait(500);
	    Needle.start("second");
	    wait(250);
	    Needle.stopCurrentNeedle();
	    start.stop();
	}
    }
}
