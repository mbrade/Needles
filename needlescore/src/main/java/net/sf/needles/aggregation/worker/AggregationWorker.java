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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.needles.NeedleInfo;
import net.sf.needles.aggregation.Aggregation;
import net.sf.needles.aggregation.AggregationFactory;
import net.sf.needles.configuration.PersistenceConfiguration;
import net.sf.needles.persistence.PersistenceUtils;

import org.apache.log4j.Logger;

/**
 * The Class AggregationWorker.
 */
public abstract class AggregationWorker {

    /** The queue. */
    final Queue<NeedleInfo> queue = new LinkedBlockingQueue<NeedleInfo>();
    private final ConcurrentHashMap<String, Aggregator> aggregatorMap = new ConcurrentHashMap<String, Aggregator>();
    private PersistenceConfiguration persistenceConfiguration = null;

    private final static Logger LOG = Logger.getLogger(AggregationWorker.class);

    /**
     * Adds the aggregation factory.
     * 
     * @param factory
     *            the factory
     */
    //<AGGREGATION extends Aggregation<AGGREGATION>, FACTORY extends AggregationFactory<AGGREGATION>>
    public void addAggregationFactory(final AggregationFactory<? extends Aggregation<?>> factory) {
	aggregatorMap.putIfAbsent(factory.getName(), createAggregator(factory));
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.AggregationHandler#notifyAddedLog(net.sf.needles.Needle)
     */
    /**
     * Aggregate.
     * 
     * @param needle
     *            the needle
     */
    public void aggregate(final NeedleInfo needle) {
	queue.offer(needle);
	if (shouldAggregateNow()) {
	    aggregateNow();
	}
    }

    /**
     * Continue aggregation.
     * 
     * @return true, if successful
     */
    public abstract boolean continueAggregation();

    /**
     * Gets the aggregation factories.
     * 
     * @return the aggregation factories
     */

    public List<AggregationFactory<? extends Aggregation<?>>> getAggregationFactories() {
	final List<AggregationFactory<? extends Aggregation<?>>> result = new LinkedList<AggregationFactory<? extends Aggregation<?>>>();
	for (final Aggregator aggregator : aggregatorMap.values()) {
	    result.add(aggregator.getAggregationFactory());
	}
	return result;
    }

    /**
     * Gets the aggregation factory.
     * 
     * @param key
     *            the key
     * @return the aggregation factory
     */
    public AggregationFactory<? extends Aggregation<?>> getAggregationFactory(final String key) {
	if (key == null) {
	    return null;
	}
	final Aggregator aggregator = aggregatorMap.get(key);
	return (aggregator != null) ? aggregator.getAggregationFactory() : null;
    }

    public PersistenceConfiguration getPersistenceConfiguration() {
	return persistenceConfiguration;
    }

    /**
     * Gets the queue size.
     * 
     * @return the queue size
     */
    public final int getQueueSize() {
	return queue.size();
    }

    public void setPersistenceConfiguration(final PersistenceConfiguration persistenceConfiguration) {
	this.persistenceConfiguration = persistenceConfiguration;
    }

    /**
     * Shutdown.
     */
    public final void shutdown() {
	PersistenceUtils.persistAggregationData(persistenceConfiguration, getAggregationFactories());
	doShutdown();
	for (final Aggregator aggregator : aggregatorMap.values()) {
	    aggregator.getAggregationFactory().shutdown();
	}
    }

    public void start() {
	PersistenceUtils.loadAggregationData(persistenceConfiguration, getAggregationFactories());
	for (final AggregationFactory<? extends Aggregation<?>> factory : getAggregationFactories()) {
	    factory.start();
	}
    }

    /**
     * Creates the aggregator.
     * 
     * @param factory
     *            the factory
     * @return the aggregator
     */
    protected Aggregator createAggregator(final AggregationFactory<? extends Aggregation<?>> factory) {
	return new Aggregator(factory);
    }

    /**
     * Aggregate needle.
     * 
     * @param needle
     *            the needle
     */
    void aggregateNeedle(final NeedleInfo needle) {
	for (final Aggregator aggregator : aggregatorMap.values()) {
	    final AggregationFactory<? extends Aggregation<?>> factory = aggregator.getAggregationFactory();
	    try {
		aggregateNeedleWithAggregator(needle, factory);
	    } catch (final Exception e) {
		LOG.warn("AggregationFactory " + ((factory != null) ? factory.getName() : " - null - ") + " throws exception" + ((needle != null) ? " on log " + needle.toString() : ""), e);
	    }
	}
    }

    /**
     * Aggregate needle with aggregator.
     * 
     * @param needle
     *            the needle
     * @param factory
     *            the factory
     */
    void aggregateNeedleWithAggregator(final NeedleInfo needle, final AggregationFactory<? extends Aggregation<?>> factory) {
	final Aggregation<?> aggregation = factory.getOrCreateAggregation(needle);
	aggregation.aggregate(needle);
    }

    /**
     * Aggregate now.
     */
    void aggregateNow() {
	NeedleInfo needle = null;
	while ((needle = queue.poll()) != null && continueAggregation()) {
	    aggregateNeedle(needle);
	}
    }

    /**
     * Do shutdown.
     */
    abstract void doShutdown();

    /**
     * Should aggregate now.
     * 
     * @return true, if successful
     */
    abstract boolean shouldAggregateNow();

}
