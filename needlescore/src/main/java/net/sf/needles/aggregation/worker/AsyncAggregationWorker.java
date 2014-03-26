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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * The Class SingleThreadAggregationWorker will use it's own thread while aggregating the logs. {@link #aggregateNow()} will use it's own Thread to process the {@link Aggregator}s
 */
public class AsyncAggregationWorker extends AggregationWorker {

    private final static Logger LOG = Logger.getLogger(AsyncAggregationWorker.class);
    private final AggregationWorker aggregationWorker = new AggregationWorker();
    private ThreadGroup tg;
    private final Semaphore sema = new Semaphore(2);
    private ExecutorService executor;

    public AsyncAggregationWorker() {
	AccessController.doPrivileged(new PrivilegedAction<Object>() {

	    @Override
	    public Object run() {
		try {
		    tg = new ThreadGroup(AsyncAggregationWorker.class.getName());
		} catch (final Exception se) {
		    tg = null;
		}
		executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(2), new ThreadFactory() {
		    private final AtomicInteger ai = new AtomicInteger();

		    @Override
		    public Thread newThread(final Runnable r) {
			final Thread result = new Thread(tg, r, AsyncAggregationWorker.class.getName() + "-Thread-" + ai.incrementAndGet());
			return result;
		    }
		});
		return null;
	    }

	});
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.AbstractAggregationWorker#continueAggregation()
     */
    @Override
    public boolean continueAggregation() {
	return true;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.AbstractAggregationWorker#aggregateNow()
     */
    @Override
    protected void aggregateNow() {
	try {
	    executor.execute(aggregationWorker);//this should be called immediately
	} catch (final RejectedExecutionException ree) {
	    LOG.warn("aggregateNow shouldn't be called if the Runnable queue has no remaining capacity.");
	}
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.AbstractAggregationWorker#shouldAggregateNow()
     */
    @Override
    protected boolean shouldAggregateNow() {
	return !executor.isTerminated() && sema.tryAcquire();
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.AggregationWorker#shutdown()
     */
    @Override
    void doShutdown() {
	executor.shutdown();
    }

    private class AggregationWorker implements Runnable {

	@Override
	public void run() {
	    try {
		AsyncAggregationWorker.super.aggregateNow();
	    } finally {
		sema.release();
	    }
	}

    }

}
