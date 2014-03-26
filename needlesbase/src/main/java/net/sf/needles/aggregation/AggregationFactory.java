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

import java.util.List;

import net.sf.needles.NeedleInfo;
import net.sf.needles.aggregation.keycreator.AggregationKey;
import net.sf.needles.aggregation.keycreator.KeyCreator;
import net.sf.needles.persistence.PersistenceData;

/**
 * A factory for creating Aggregation objects.
 */
public interface AggregationFactory<AGGREGATION extends Aggregation<AGGREGATION>> {

    /**
     * Creates a new Aggregation object.
     * 
     * @param needle
     *            the log
     * @return the aggregation
     */
    AGGREGATION createAggregation(NeedleInfo needle);

    /**
     * Fill persistence data.
     * 
     * @param persistenceData
     *            the persistence data
     */
    void fillPersistenceData(PersistenceData persistenceData);

    /**
     * Gets the aggregation.
     * 
     * @param aggregationKey
     *            the aggregation key
     * @return the aggregation
     */
    AGGREGATION getAggregation(AggregationKey aggregationKey);

    /**
     * Gets the aggregation.
     * 
     * @param needleInfo
     *            the needle info
     * @return the aggregation
     */
    AGGREGATION getAggregation(NeedleInfo needleInfo);

    /**
     * Gets the log aggregation method.
     * 
     * @return the log aggregation method
     */
    KeyCreator getKeyCreator();

    /**
     * Gets the name.
     * 
     * @return the name
     */
    String getName();

    /**
     * Gets the aggregation.
     * 
     * @param needle
     *            the log
     * @return the aggregation
     */
    AGGREGATION getOrCreateAggregation(NeedleInfo needle);

    /**
     * Gets the root aggregations.
     * 
     * @return the root aggregations
     */
    List<AGGREGATION> getRootAggregations();

    void start();

    /**
     * Initialize.
     * 
     * @param data
     *            the data
     */
    void loadPersistenceData(PersistenceData data);

    /**
     * Shutdown.
     */
    void shutdown();
}
