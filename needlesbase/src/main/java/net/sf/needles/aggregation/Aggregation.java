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

import java.io.Serializable;
import java.util.List;

import net.sf.needles.NeedleId;
import net.sf.needles.NeedleInfo;
import net.sf.needles.aggregation.keycreator.AggregationKey;

/**
 * The Interface Aggregation.
 */
public interface Aggregation<AGGREGATION extends Aggregation<AGGREGATION>> extends Serializable {

    /**
     * Aggregate.
     * 
     * @param needle
     *            the needle
     */
    void aggregate(final NeedleInfo needle);

    /**
     * Gets the aggregation for needle info.
     * 
     * @param needleInfo
     *            the needle info
     * @return the aggregation for needle info
     */
    AGGREGATION getAggregationForNeedleInfo(NeedleInfo needleInfo);

    /**
     * Gets the aggregation key.
     * 
     * @return the aggregation key
     */
    AggregationKey getAggregationKey();

    /**
     * Gets the aggregation name.
     * 
     * @return the aggregation name
     */
    String getAggregationName();

    String getAggregationString();

    /**
     * Gets the child.
     * 
     * @param needle
     *            the log
     * @return the child
     */
    AGGREGATION getChildAggregation(NeedleInfo needle);

    /**
     * Gets the child aggregations.
     * 
     * @return the child aggregations
     */
    List<AGGREGATION> getChildAggregations();

    int getDepth();

    /**
     * Gets the log id.
     * 
     * @return the log id
     */
    NeedleId getNeedleId();

    /**
     * Gets the name of this aggregation.
     * 
     * @return the name
     */
    String getNeedleName();

    /**
     * Gets the parent aggregation.
     * 
     * @return the parent aggregation
     */
    AGGREGATION getParentAggregation();

    /**
     * Checks for children.
     * 
     * @return true, if successful
     */
    boolean hasChildren();

    /**
     * Sets the parent aggregation.
     * 
     * @param parent
     *            the new parent aggregation
     */
    void setParentAggregation(AGGREGATION parent);
}
