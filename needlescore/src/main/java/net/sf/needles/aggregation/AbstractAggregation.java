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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.needles.NeedleId;
import net.sf.needles.NeedleInfo;
import net.sf.needles.aggregation.keycreator.AggregationKey;

/**
 * The Class AbstractAggregation.
 */
public abstract class AbstractAggregation<AGGREGATION extends Aggregation<AGGREGATION>> implements Aggregation<AGGREGATION> {

    private static final long serialVersionUID = 1L;
    private final String needleName;
    private final NeedleId needleId;
    private final Map<AggregationKey, AGGREGATION> children = new LinkedHashMap<AggregationKey, AGGREGATION>();
    private AGGREGATION parent = null;
    private transient ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final AggregationFactory<AGGREGATION> aggregationFactory;
    private final AggregationKey aggregationkey;

    /**
     * Instantiates a new abstract aggregation.
     * 
     * @param factory
     *            the factory
     * @param logName
     *            the log name
     * @param needleId
     *            the log id
     */
    public AbstractAggregation(final AggregationFactory<AGGREGATION> factory, final NeedleInfo needleInfo) {
	this.needleName = needleInfo.getName();
	this.needleId = needleInfo.getId();
	this.aggregationFactory = factory;
	this.aggregationkey = generateAggregationKey(needleInfo);
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.Aggregation#aggregate(net.sf.needles.Needle)
     */
    @Override
    public abstract void aggregate(final NeedleInfo needle);

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
	@SuppressWarnings("rawtypes")
	final AbstractAggregation other = (AbstractAggregation) obj;
	if (aggregationFactory == null) {
	    if (other.aggregationFactory != null) {
		return false;
	    }
	} else if (!aggregationFactory.equals(other.aggregationFactory)) {
	    return false;
	}
	if (needleId == null) {
	    if (other.needleId != null) {
		return false;
	    }
	} else if (!needleId.equals(other.needleId)) {
	    return false;
	}
	return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AGGREGATION getAggregationForNeedleInfo(final NeedleInfo needleInfo) {
	//TODO go to the root of the needleInfo and use the root of this aggregation to go through the childs until you get this needleInfo aggregationKey
	final AggregationKey key = generateAggregationKey(needleInfo);
	if (aggregationkey.equals(key)) {
	    return (AGGREGATION) this;
	} else {
	    AGGREGATION aggregation = children.get(key);
	    AGGREGATION childAggregation = null;
	    for (final Iterator<AGGREGATION> aggregationIterator = children.values().iterator(); aggregation == null && aggregationIterator.hasNext(); childAggregation = aggregationIterator.next()) {
		aggregation = childAggregation.getAggregationForNeedleInfo(needleInfo);
	    }
	    return aggregation;
	}
    }

    @Override
    public AggregationKey getAggregationKey() {
	return aggregationkey;
    }

    @Override
    public String getAggregationName() {
	return this.aggregationFactory.getName();
    }

    @Override
    public abstract String getAggregationString();

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.Aggregation#getChildAggregation(net.sf.needles.Needle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final AGGREGATION getChildAggregation(final NeedleInfo needle) {
	final AggregationKey aggregationKey = generateAggregationKey(needle);
	lock.readLock().lock();
	try {
	    AGGREGATION result = children.get(aggregationKey);
	    if (result == null) {
		lock.readLock().unlock();
		lock.writeLock().lock();
		try {
		    result = children.get(aggregationKey);
		    if (result == null) {
			result = getAggregationFactory().createAggregation(needle);
			result.setParentAggregation((AGGREGATION) this);
			children.put(aggregationKey, result);
		    }
		} finally {
		    lock.writeLock().unlock();
		    lock.readLock().lock();
		}
	    }
	    return result;
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.Aggregation#getChildAggregations()
     */
    @Override
    public final List<AGGREGATION> getChildAggregations() {
	lock.readLock().lock();
	try {
	    return new ArrayList<AGGREGATION>(children.values());
	} finally {
	    lock.readLock().unlock();
	}
    }

    public int getDepth() {
	if (parent == null) {
	    return 1;
	} else {
	    return parent.getDepth() + 1;
	}
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.Aggregation#getNeedleId()
     */
    @Override
    public final NeedleId getNeedleId() {
	return needleId;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.Aggregation#getName()
     */
    @Override
    public final String getNeedleName() {
	return needleName;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.Aggregation#getParentAggregation()
     */
    @Override
    public AGGREGATION getParentAggregation() {
	return this.parent;
    }

    /* (non-Javadoc)
     * @see net.sf.needles.aggregation.Aggregation#hasChildren()
     */
    public boolean hasChildren() {
	return !children.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((aggregationFactory == null) ? 0 : aggregationFactory.hashCode());
	result = prime * result + ((needleId == null) ? 0 : needleId.hashCode());
	return result;
    }

    /**
     * Sets the parent.
     * 
     * @param parent
     *            the new parent
     */
    @Override
    public final void setParentAggregation(final AGGREGATION parent) {
	this.parent = parent;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
	return AggregationRenderer.buildString(this);
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
	stream.defaultReadObject();
	this.lock = new ReentrantReadWriteLock();
    }

    protected AggregationKey generateAggregationKey(final NeedleInfo needle) {
	return aggregationFactory.getKeyCreator().getKey(needle);
    }

    /**
     * Gets the aggregation factory.
     * 
     * @return the aggregation factory
     */
    protected AggregationFactory<AGGREGATION> getAggregationFactory() {
	return aggregationFactory;
    }

}
