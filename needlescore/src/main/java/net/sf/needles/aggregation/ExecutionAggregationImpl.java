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

import net.sf.needles.NeedleInfo;

/**
 * The Class ExecutionAggregation.
 */
public class ExecutionAggregationImpl extends AbstractAggregation<ExecutionAggregation> implements ExecutionAggregation {

    private static final long serialVersionUID = 1L;
    private volatile long amount = 0;
    private volatile double totalmillis = 0.0;
    private volatile double maximum = 0.0;
    private volatile double minimum = Double.MAX_VALUE;
    private volatile double average = 0.0;

    /**
     * Instantiates a new execution aggregation.
     * 
     * @param name
     *            the name
     * @param needleId
     *            the log id
     */
    public ExecutionAggregationImpl(final ExecutionAggregationFactory factory, final NeedleInfo needleInfo) {
	super(factory, needleInfo);
    }

    @Override
    public void aggregate(final NeedleInfo needle) {
	final double temp = ++amount;
	synchronized (this) {
	    average = (needle.getDurationNanos() * (1.0 / temp)) + (average * ((temp - 1.0) / temp));
	}
	maximum = Math.max(needle.getDurationNanos(), maximum);
	minimum = Math.min(needle.getDurationNanos(), minimum);
	totalmillis += needle.getDurationNanos() / 1000000.0;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.ExecutionAggregation#getAggregationName()
     */
    @Override
    public String getAggregationName() {
	return AGGREGATION_NAME;
    }

    @Override
    public String getAggregationString() {
	final StringBuilder result = new StringBuilder();
	result.append("Min: ").append(getMinimum()).append(" ms");
	result.append(" ").append("Max: ").append(getMaximum()).append(" ms");
	result.append(" ").append("Avg: ").append(getAverage()).append(" ms");
	result.append(" ").append("Total: ").append(getTotal()).append(" ms");
	result.append(" ").append("Count: ").append(getMeasurements());
	return result.toString();
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.ExecutionAggregation#getAverage()
     */
    @Override
    public double getAverage() {
	return average / 1000000.0;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.ExecutionAggregation#getMaximum()
     */
    @Override
    public double getMaximum() {
	return maximum / 1000000.0;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.ExecutionAggregation#getMeasurements()
     */
    @Override
    public long getMeasurements() {
	return amount;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.ExecutionAggregation#getMinimum()
     */
    @Override
    public double getMinimum() {
	return minimum / 1000000.0;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.ExecutionAggregation#getTotal()
     */
    @Override
    public double getTotal() {
	return totalmillis;
    }

}
