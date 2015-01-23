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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.needles.aggregation.AggregationFactory;
import net.sf.needles.configuration.PersistenceConfiguration;
import net.sf.needles.persistence.PersistenceData;

/**
 * The Class PersistenceManager.
 */
public final class PersistenceUtils {

    private final static org.apache.log4j.Logger LOG = org.apache.log4j.LogManager.getLogger(PersistenceUtils.class);

    /**
     * Instantiates a new persistence manager.
     */
    private PersistenceUtils() {
    }

    private static Map<String, ? extends AggregationFactory<?>> createAggregationMap(final Collection<? extends AggregationFactory<?>> aggregations) {
	final Map<String, AggregationFactory<?>> result = new HashMap<String, AggregationFactory<?>>();
	for (final AggregationFactory<?> factory : aggregations) {
	    result.put(factory.getName(), factory);
	}
	return result;
    }

    /**
     * Load aggregation data.
     */
    static void loadAggregationData(final PersistenceConfiguration persistenceConfiguration, final Collection<? extends AggregationFactory<?>> aggregationFactories) {
	if (persistenceConfiguration != null && persistenceConfiguration.getPersistencePath() != null && persistenceConfiguration.getPersistenceName() != null) {
	    final Map<String, ? extends AggregationFactory<?>> aggregationMap = createAggregationMap(aggregationFactories);
	    ObjectInputStream ois = null;
	    final File f = new File(persistenceConfiguration.getPersistencePath(), persistenceConfiguration.getPersistenceName());
	    if (f.exists() && f.canRead()) {
		try {
		    try {
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f), 2048));
			@SuppressWarnings("unchecked")
			final List<PersistenceData> dataList = (List<PersistenceData>) ois.readObject();
			for (final PersistenceData entry : dataList) {
			    final AggregationFactory<?> factory = aggregationMap.get(entry.getAggregationKey());
			    if (factory != null) {
				factory.loadPersistenceData(entry);
			    }
			}
		    } finally {
			if (ois != null) {
			    ois.close();//this will close all underlaying streams
			}
		    }
		} catch (final FileNotFoundException fnfe) {
		    LOG.info(String.format("Didn't found file: '%1%s'.", f.getPath()), fnfe);
		} catch (final IOException e) {
		    LOG.warn(String.format("Couldn't load file: '%1$s'.", f.getPath()), e);
		} catch (final ClassNotFoundException e) {
		    LOG.warn(String.format("Failed to load classes from file: '%1$s'.", f.getPath()), e);
		}
	    } else {
		LOG.info(String.format("Needles %2$s file does not exist at %1$s . Skipping it.", persistenceConfiguration.getPersistencePath(), persistenceConfiguration.getPersistenceName()));
	    }
	}
    }

    /**
     * Persist aggregation data.
     */
    static void persistAggregationData(final PersistenceConfiguration persistenceConfiguration, final Collection<? extends AggregationFactory<?>> aggregationFactories) {
	if (persistenceConfiguration != null && persistenceConfiguration.getPersistencePath() != null && persistenceConfiguration.getPersistenceName() != null) {
	    final Map<String, ? extends AggregationFactory<?>> aggregationMap = createAggregationMap(aggregationFactories);
	    ObjectOutputStream oos = null;
	    final List<PersistenceData> dataList = new ArrayList<PersistenceData>(aggregationMap.size());
	    PersistenceData persistenceData = null;
	    for (final Map.Entry<String, ? extends AggregationFactory<?>> entry : aggregationMap.entrySet()) {
		persistenceData = new PersistenceData(entry.getKey());
		entry.getValue().fillPersistenceData(persistenceData);
		dataList.add(persistenceData);
	    }
	    final File f = new File(persistenceConfiguration.getPersistencePath(), persistenceConfiguration.getPersistenceName());
	    final File parentFile = f.getParentFile();
	    if (parentFile != null) {
		parentFile.mkdirs();
	    }
	    try {
		try {
		    oos = new ObjectOutputStream(new BufferedOutputStream(Channels.newOutputStream(new FileOutputStream(f).getChannel()), 2048));
		    oos.writeObject(dataList);
		} finally {
		    if (oos != null) {
			oos.close();
		    }
		}
	    } catch (final IOException e) {
		LOG.warn(String.format("Couldn't write file: '%1$s'.", f.getPath()), e);

	    }
	}
    }
}
