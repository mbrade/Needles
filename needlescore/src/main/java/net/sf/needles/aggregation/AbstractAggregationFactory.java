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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.needles.NeedleInfo;
import net.sf.needles.aggregation.keycreator.AggregationKey;
import net.sf.needles.aggregation.keycreator.KeyCreator;
import net.sf.needles.aggregation.keycreator.NeedleIdKeyCreator;
import net.sf.needles.persistence.PersistenceData;

public abstract class AbstractAggregationFactory<AGGREGATION extends Aggregation<AGGREGATION>> implements AggregationFactory<AGGREGATION> {

    private KeyCreator keyCreator;
    private final Map<AggregationKey, AGGREGATION> rootAggregations = new HashMap<AggregationKey, AGGREGATION>();
    private final Map<AggregationKey, AGGREGATION> allAggregations = new ConcurrentHashMap<AggregationKey, AGGREGATION>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private String name;

    public AbstractAggregationFactory() {
	this(NeedleIdKeyCreator.INSTANCE, null);
	this.name = getClass().getSimpleName();
    }

    public AbstractAggregationFactory(final KeyCreator keyCreator, final String aggregationName) {
	this.keyCreator = keyCreator;
	this.name = aggregationName;
    }

    public final AGGREGATION createAggregation(final NeedleInfo needle) {
	final AGGREGATION result = doCreateAggregation(needle);
	allAggregations.put(result.getAggregationKey(), result);
	return result;
    }

    @SuppressWarnings("rawtypes")
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
	final AbstractAggregationFactory other = (AbstractAggregationFactory) obj;
	if (getName() == null) {
	    if (other.getName() != null) {
		return false;
	    }
	} else if (!getName().equals(other.getName())) {
	    return false;
	}
	if (keyCreator == null) {
	    if (other.keyCreator != null) {
		return false;
	    }
	} else if (!keyCreator.equals(other.keyCreator)) {
	    return false;
	}
	return true;
    }

    @Override
    public void fillPersistenceData(final PersistenceData persistenceData) {
	lock.readLock().lock();
	try {
	    persistenceData.setAggregationData(new HashMap<Serializable, AGGREGATION>(rootAggregations));
	} finally {
	    lock.readLock().unlock();
	}
    }

    @Override
    public AGGREGATION getAggregation(final AggregationKey aggregationKey) {
	return allAggregations.get(aggregationKey);
    }

    @Override
    public AGGREGATION getAggregation(final NeedleInfo needleInfo) {
	return getAggregation(keyCreator.getKey(needleInfo));
    }

    @Override
    public KeyCreator getKeyCreator() {
	return keyCreator;
    }

    public final String getName() {
	return name;
    }

    @Override
    public AGGREGATION getOrCreateAggregation(final NeedleInfo needle) {
	final NeedleInfo parent = needle.getParentNeedle();
	AGGREGATION result;
	if (parent != null) {
	    final AGGREGATION parentAggregation = getOrCreateAggregation(parent);
	    if (getKeyCreator().getKey(needle).equals(getKeyCreator().getKey(parent))) {
		result = parentAggregation;
	    } else {
		result = parentAggregation.getChildAggregation(needle);
	    }
	} else {
	    result = createRootAggregation(needle);
	}
	return result;
    }

    @Override
    public List<AGGREGATION> getRootAggregations() {
	lock.readLock().lock();
	try {
	    return new ArrayList<AGGREGATION>(rootAggregations.values());
	} finally {
	    lock.readLock().unlock();
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
	result = prime * result + ((keyCreator == null) ? 0 : keyCreator.hashCode());
	return result;
    }

    @Override
    public abstract void start();

    @SuppressWarnings("unchecked")
    @Override
    public void loadPersistenceData(final PersistenceData value) {
	lock.writeLock().lock();
	try {
	    this.rootAggregations.putAll((HashMap<AggregationKey, AGGREGATION>) value.getAggregationData());
	} finally {
	    lock.writeLock().unlock();
	}
    }

    public void setAggregationName(final String name) {
	this.name = name;
    }

    public void setKeyCreator(final KeyCreator keyCreator) {
	this.keyCreator = keyCreator;
    }

    @Override
    public abstract void shutdown();

    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append(getClass().getName()).append(" [aggregationMethod=").append(keyCreator).append(", aggregationName()=").append(getName()).append("]");
	return builder.toString();
    }

    private AGGREGATION createRootAggregation(final NeedleInfo needle) {
	AGGREGATION result = null;
	lock.readLock().lock();
	try {
	    final AggregationKey needleKey = getKeyCreator().getKey(needle);
	    result = rootAggregations.get(needleKey);
	    if (result == null) {
		lock.readLock().unlock();
		lock.writeLock().lock();
		try {
		    result = rootAggregations.get(needleKey);
		    if (result == null) {
			result = createAggregation(needle);
			rootAggregations.put(needleKey, result);
		    }
		} finally {
		    lock.writeLock().unlock();
		    lock.readLock().lock();
		}
	    }
	} finally {
	    lock.readLock().unlock();
	}
	return result;
    }

    protected abstract AGGREGATION doCreateAggregation(NeedleInfo needle);
}
