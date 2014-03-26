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

package net.sf.needles.aggregation.worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.needles.Needle;
import net.sf.needles.aggregation.Aggregation;
import net.sf.needles.aggregation.AggregationFactory;

/**
 * The Class PerAggregatorThreadAggregationWorker uses a Thread for each Aggregator for aggregation.
 */
public class AsyncAggregatorAggregationWorker extends AsyncAggregationWorker {

    private final ThreadGroup tg = new ThreadGroup(AsyncAggregatorAggregationWorker.class.getSimpleName());
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
	private final AtomicInteger ai = new AtomicInteger();

	@Override
	public Thread newThread(final Runnable r) {
	    final Thread result = new Thread(tg, r, tg.getName() + "-Thread-" + ai.incrementAndGet());
	    return result;
	}
    });

    @Override
    protected Aggregator createAggregator(final AggregationFactory<? extends Aggregation<?>> factory) {
	return new ThreadedAggregator(factory);
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.SingleThreadAggregationWorker#shutdown()
     */
    @Override
    void doShutdown() {
	super.shutdown();
	executor.shutdown();
    }

    private class ThreadedAggregator extends Aggregator implements Runnable {

	private static final long serialVersionUID = 1L;

	private Needle currentNeedle;

	private ThreadedAggregator(final AggregationFactory<? extends Aggregation<?>> factory) {
	    super(factory);
	}

	/* (non-Javadoc)
	 * @see net.sf.needles.aggregation.aggregator.Aggregator#aggregate(net.sf.needles.Needle)
	 */
	@Override
	public void aggregate(final Needle needle) {
	    if (currentNeedle != null) {
		throw new IllegalStateException("The PerAggregatorRunnable should get called for one log per time only.");
	    }
	    this.currentNeedle = needle;
	    executor.execute(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	    super.aggregate(currentNeedle);
	    currentNeedle = null;
	}

    }

}
