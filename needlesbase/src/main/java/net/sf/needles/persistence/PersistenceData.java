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

package net.sf.needles.persistence;

import java.io.Serializable;

/**
 * The Class PersistenceData.
 */
public class PersistenceData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String aggregationKey;
    private Serializable aggregationData;

    public PersistenceData() {
    }

    public PersistenceData(final String key) {
	this.aggregationKey = key;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final PersistenceData other = (PersistenceData) obj;
	if (aggregationKey == null) {
	    if (other.aggregationKey != null) {
		return false;
	    }
	} else if (!aggregationKey.equals(other.aggregationKey)) {
	    return false;
	}
	return true;
    }

    /**
     * Gets the aggregation data.
     * 
     * @return the aggregation data
     */
    public Serializable getAggregationData() {
	return aggregationData;
    }

    /**
     * Gets the aggregation key.
     * 
     * @return the aggregation key
     */
    public String getAggregationKey() {
	return aggregationKey;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((aggregationKey == null) ? 0 : aggregationKey.hashCode());
	return result;
    }

    /**
     * Sets the aggregation data.
     * 
     * @param aggregationData
     *            the new aggregation data
     */
    public void setAggregationData(final Serializable aggregationData) {
	this.aggregationData = aggregationData;
    }

    /**
     * Sets the aggregation key.
     * 
     * @param aggregationKey
     *            the new aggregation key
     */
    public void setAggregationKey(final String aggregationKey) {
	this.aggregationKey = aggregationKey;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append("PersistenceData [aggregationKey=").append(aggregationKey).append("]");
	return builder.toString();
    }

}
