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

package net.sf.needles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.needles.aggregation.Aggregation;
import net.sf.needles.aggregation.AggregationFactory;
import net.sf.needles.aggregation.keycreator.AggregationKey;
import net.sf.needles.aggregation.worker.AggregationWorker;
import net.sf.needles.aggregation.worker.AsyncAggregationWorker;

/**
 * The Class GlobalContext.
 */
public class GlobalContext {

    private static AtomicReference<AggregationWorker> worker = new AtomicReference<AggregationWorker>(new AsyncAggregationWorker());

    static {
	Runtime.getRuntime().traceInstructions(true);
	Runtime.getRuntime().traceMethodCalls(true);
	Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    private GlobalContext() {
    }

    public static Aggregation<?> getAggregation(final String aggregationName, final AggregationKey aggregationKey) {
	final AggregationFactory<?> factory = worker.get().getAggregationFactory(aggregationName);
	if (factory != null) {
	    return factory.getAggregation(aggregationKey);
	}
	return null;
    }

    //
    //    public static Class<? extends Aggregation<?>> getAggregationClassForName(final String aggregationName) {
    //	final AggregationFactory<? extends Aggregation<?>> factory = worker.get().getAggregationFactory(aggregationName);
    //	return factory.getAggregationClass();
    //    }

    /**
     * Gets the aggregation factories with there names as key.
     * 
     * @return the aggregation factories
     */
    public static Map<String, AggregationFactory<? extends Aggregation<?>>> getAggregationFactories() {
	final Map<String, AggregationFactory<? extends Aggregation<?>>> result = new HashMap<String, AggregationFactory<? extends Aggregation<?>>>();
	final List<AggregationFactory<? extends Aggregation<?>>> factories = worker.get().getAggregationFactories();
	for (final AggregationFactory<? extends Aggregation<?>> factory : factories) {
	    result.put(factory.getName(), factory);
	}
	return result;
    }

    public static AggregationFactory<? extends Aggregation<?>> getAggregationFactory(final String aggregationName) {
	final AggregationFactory<? extends Aggregation<?>> factory = worker.get().getAggregationFactory(aggregationName);
	return factory;
    }

    /**
     * Gets the aggregations.
     * 
     * @return the aggregations
     */
    public static Map<String, List<? extends Aggregation<?>>> getAggregations() {
	final Map<String, List<? extends Aggregation<?>>> result = new HashMap<String, List<? extends Aggregation<?>>>();
	final List<AggregationFactory<? extends Aggregation<?>>> factories = worker.get().getAggregationFactories();
	for (final AggregationFactory<? extends Aggregation<?>> factory : factories) {
	    result.put(factory.getName(), factory.getRootAggregations());
	}
	return result;
    }

    public static Map<AggregationKey, Map<String, Aggregation<?>>> getAggregations(final List<AggregationKey> aggregationKeys) {
	final Map<AggregationKey, Map<String, Aggregation<?>>> result = new TreeMap<AggregationKey, Map<String, Aggregation<?>>>();
	for (final AggregationFactory aggregationFactory : worker.get().getAggregationFactories()) {
	    for (final AggregationKey aggregationKey : aggregationKeys) {
		final Aggregation aggregation = aggregationFactory.getAggregation(aggregationKey);
		if (aggregation != null) {
		    Map<String, Aggregation<?>> map = result.get(aggregationKey);
		    if (map == null) {
			map = new TreeMap<String, Aggregation<?>>();
			result.put(aggregationKey, map);
		    }
		    map.put(aggregationFactory.getName(), aggregation);
		}
	    }
	}
	return result;
    }

    /**
     * The aggregations of all registered Aggregation-Factories for the given NeedleInfo object.
     * 
     * @param needleInfo
     * @return Ths list of aggregations
     */
    public static List<Aggregation<?>> getAggregations(final NeedleInfo needleInfo) {
	final List<Aggregation<?>> aggregations = new ArrayList<Aggregation<?>>();
	final List<AggregationFactory<? extends Aggregation<?>>> factories = worker.get().getAggregationFactories();
	for (final AggregationFactory<? extends Aggregation<?>> factory : factories) {
	    final Aggregation<?> aggregation = factory.getAggregation(needleInfo);
	    if (aggregation != null) {
		aggregations.add(aggregation);
	    }
	}
	return aggregations;
    }

    /**
     * The root aggregations of the Aggregation-Factory registered with the given aggregationName.
     * 
     * @param aggregationName
     * @return A map of aggregations
     */
    public static Map<AggregationKey, Aggregation<?>> getAggregations(final String aggregationName) {
	final AggregationFactory<?> factory = worker.get().getAggregationFactory(aggregationName);
	if (factory != null) {
	    final Map<AggregationKey, Aggregation<?>> result = new TreeMap<AggregationKey, Aggregation<?>>();
	    for (final Object rootAggregation : factory.getRootAggregations()) {
		final Aggregation<?> aggregation = (Aggregation<?>) rootAggregation;
		result.put(aggregation.getAggregationKey(), aggregation);
	    }
	    return result;
	}
	return Collections.emptyMap();
    }

    /**
     * Gets the aggregation worker.
     * 
     * @return the aggregation worker
     */
    public static AggregationWorker getAggregationWorker() {
	return worker.get();
    }

    /**
     * Sets the aggregation worker.
     * 
     * @param worker
     *            the new aggregation worker
     */
    public static void setAggregationWorker(final AggregationWorker worker) {
	if (worker == null) {
	    throw new IllegalArgumentException("An AggregationWorker has to be set.");
	}
	final AggregationWorker oldWorker = GlobalContext.worker.getAndSet(worker);
	if (oldWorker != null && oldWorker != worker) {
	    oldWorker.shutdown();
	}
    }

    /**
     * Shutdown.
     */
    public static void shutdown() {
	final AggregationWorker worker = getAggregationWorker();
	if (worker != null) {
	    worker.shutdown();
	}
    }

    public static void start() {
	worker.get().start();
    }

    /**
     * Aggregate log.
     * 
     * @param needle
     *            the log
     */
    static void aggregateNeedle(final NeedleInfo needle) {
	worker.get().aggregate(needle);
    }

    private static class ShutdownHook extends Thread {

	private ShutdownHook() {
	    super("Needles-ShutdownHook");
	}

	@Override
	public void run() {
	    try {
		shutdown();
	    } catch (final Exception e) {
		e.printStackTrace();//using log4j on shutdown is mostly a bad idea.
	    }
	}

    }
}
