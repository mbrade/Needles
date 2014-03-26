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

package net.sf.needles.aggregation.worker;

import net.sf.needles.Needle;
import net.sf.needles.aggregation.Aggregation;
import net.sf.needles.aggregation.AggregationFactory;

/**
 * The Aggregator.
 */
public class Aggregator {

    private final AggregationFactory<? extends Aggregation<?>> aggregationFactory;

    /**
     * Instantiates a new aggregator.
     * 
     * @param aggregationFactory
     *            the aggregation factory
     */
    public Aggregator(final AggregationFactory<? extends Aggregation<?>> aggregationFactory) {
	this.aggregationFactory = aggregationFactory;
    }

    /**
     * Aggregate.
     * 
     * @param needle
     *            the log
     */
    public void aggregate(final Needle needle) {
	final Aggregation<?> aggregation = getAggregationFactory().getOrCreateAggregation(needle);
	aggregation.aggregate(needle);
    }

    /**
     * Gets the aggregation factory.
     * 
     * @return the aggregation factory
     */
    public AggregationFactory<? extends Aggregation<?>> getAggregationFactory() {
	return aggregationFactory;
    }

    /**
     * Gets the aggregation key.
     * 
     * @return the aggregation key
     */
    public String getAggregationKey() {
	return getAggregationFactory().getName();
    }

}
