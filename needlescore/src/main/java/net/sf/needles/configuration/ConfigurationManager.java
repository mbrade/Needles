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

package net.sf.needles.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import net.sf.needles.GlobalContext;
import net.sf.needles.NeedleException;
import net.sf.needles.aggregation.AggregationFactory;
import net.sf.needles.aggregation.ExecutionAggregationFactory;
import net.sf.needles.aggregation.HotspotAggregationFactory;
import net.sf.needles.aggregation.Top10AggregationFactory;
import net.sf.needles.aggregation.keycreator.KeyCreator;
import net.sf.needles.aggregation.keycreator.NeedleIdKeyCreator;
import net.sf.needles.aggregation.keycreator.NeedleNameKeyCreator;
import net.sf.needles.aggregation.keycreator.SingletonKeyCreator;
import net.sf.needles.aggregation.worker.AggregationWorker;
import net.sf.needles.aggregation.worker.AsyncAggregationWorker;

import org.apache.commons.beanutils.BeanUtilsBean2;

/**
 * The Class ConfigurationManager.
 */
public class ConfigurationManager {

    private final static org.apache.log4j.Logger LOG = org.apache.log4j.LogManager.getLogger(ConfigurationManager.class);
    private static Configuration configuration = getDefaultConfiguration();

    /**
     * Uses the inner {@link Configuration} loaded via {@link #loadConfiguration(File)} or {@link #loadConfiguration(InputStream)}. This method calls {@link #configure()}.
     */
    public static void configure() {
	configure(configuration);
    }

    /**
     * Configures the {@link GlobalContext} with an {@link AggregationWorker} and a list of {@link AggregationFactory}s according to the given {@link Configuration}.
     * 
     * @param configuration
     *            the configuration
     */
    public static void configure(final Configuration configuration) {
	if (configuration == null) {
	    throw new IllegalArgumentException("The configuration shouldn't be null.");
	}
	AggregationWorker worker;
	if (configuration.getWorkerClass() != null) {
	    try {
		final Class<?> workerClass = configuration.getWorkerClass();
		worker = (AggregationWorker) workerClass.newInstance();
		GlobalContext.setAggregationWorker(worker);
	    } catch (final InstantiationException e) {
		throw new NeedleException(String.format("Failed to instantiate AggregationWorker class: %1$s", configuration.getWorkerClass()), e);
	    } catch (final IllegalAccessException e) {
		throw new NeedleException(String.format("Failed to instantiate worker class: %1$s", configuration.getWorkerClass()), e);
	    }
	}

	worker = GlobalContext.getAggregationWorker();
	if (worker != null) {
	    if (configuration.getPersistenceConfiguration() == null) {
		LOG.info("No needles persistenceConfiguration set.");
	    } else {
		worker.setPersistenceConfiguration(new PersistenceConfiguration(configuration.getPersistenceConfiguration().getPath(), configuration.getPersistenceConfiguration().getName()));
		LOG.info(String.format("Needles persistence configuration set to path: %1$s and filename: %2$s",
		                       configuration.getPersistenceConfiguration().getPath(),
		                       configuration.getPersistenceConfiguration().getName()));
	    }
	    for (final AggregationFactoryConfiguration config : configuration.getAggregationFactories()) {

		if (config instanceof HotspotAggregationFactoryConfiguration) {
		    final HotspotAggregationFactoryConfiguration hotspotConfig = (HotspotAggregationFactoryConfiguration) config;
		    if (hotspotConfig.getCount() != null) {
			if (hotspotConfig.getAggregationName() != null) {
			    worker.addAggregationFactory(new HotspotAggregationFactory(hotspotConfig.getCount(), hotspotConfig.getAggregationName()));
			} else {
			    worker.addAggregationFactory(new HotspotAggregationFactory(hotspotConfig.getCount()));
			}
		    } else {
			if (hotspotConfig.getAggregationName() != null) {
			    worker.addAggregationFactory(new HotspotAggregationFactory(hotspotConfig.getAggregationName()));
			} else {
			    worker.addAggregationFactory(new HotspotAggregationFactory());
			}
		    }
		} else if (config instanceof Top10AggregationFactoryConfiguration) {
		    final Top10AggregationFactoryConfiguration top10Config = (Top10AggregationFactoryConfiguration) config;
		    if (top10Config.getKeyCreator() != null) {
			if (top10Config.getAggregationName() != null) {
			    worker.addAggregationFactory(new Top10AggregationFactory(getKeyCreator(top10Config.getKeyCreator()), top10Config.getAggregationName()));
			} else {
			    worker.addAggregationFactory(new Top10AggregationFactory(getKeyCreator(top10Config.getKeyCreator())));
			}
		    } else {
			if (top10Config.getAggregationName() != null) {
			    worker.addAggregationFactory(new Top10AggregationFactory(top10Config.getAggregationName()));
			} else {
			    worker.addAggregationFactory(new Top10AggregationFactory());
			}
		    }
		} else if (config instanceof ExecutionAggregationFactoryConfiguration) {
		    final ExecutionAggregationFactoryConfiguration executionConfig = (ExecutionAggregationFactoryConfiguration) config;
		    if (executionConfig.getKeyCreator() != null) {
			if (executionConfig.getAggregationName() != null) {
			    worker.addAggregationFactory(new ExecutionAggregationFactory(getKeyCreator(executionConfig.getKeyCreator()), executionConfig.getAggregationName()));
			} else {
			    worker.addAggregationFactory(new ExecutionAggregationFactory(getKeyCreator(executionConfig.getKeyCreator())));
			}
		    } else {
			if (executionConfig.getAggregationName() != null) {
			    worker.addAggregationFactory(new ExecutionAggregationFactory(executionConfig.getAggregationName()));
			} else {
			    worker.addAggregationFactory(new ExecutionAggregationFactory());
			}
		    }
		} else if (config instanceof CustomAggregationFactoryConfiguration) {
		    final CustomAggregationFactoryConfiguration customConfig = (CustomAggregationFactoryConfiguration) config;
		    try {
			final String aggregationFactoryClass = customConfig.getAggregationFactoryClass();
			final Class<?> forName = Class.forName(aggregationFactoryClass);
			final Object newInstance = forName.newInstance();
			final BeanUtilsBean2 beanUtilsBean2 = new BeanUtilsBean2();
			final List<PropertyType> properties = customConfig.getProperties();
			for (final PropertyType propertyType : properties) {
			    try {
				final String name = propertyType.getName();
				beanUtilsBean2.setProperty(newInstance, name, propertyType.getValue());
			    } catch (final InvocationTargetException ite) {
				throw new NeedleException("Failure during invoking of property " + propertyType.getName() + " for: " + customConfig.getAggregationFactoryClass(), ite);
			    }
			}
		    } catch (final ClassNotFoundException cnfe) {
			throw new NeedleException("Could not load the class specified: " + customConfig.getAggregationFactoryClass(), cnfe);
		    } catch (final IllegalAccessException iae) {
			throw new NeedleException("No default constructor found or is accessible for class: " + customConfig.getAggregationFactoryClass(), iae);
		    } catch (final InstantiationException ie) {
			throw new NeedleException("Failure during invoking of constructor for: " + customConfig.getAggregationFactoryClass(), ie);
		    }
		}
	    }
	}
    }

    /**
     * Gets the configuration.
     * 
     * @return the configuration
     */
    public static Configuration getConfiguration() {
	return configuration;
    }

    /**
     * Loads the configuration from the given {@link File}. You can get the configuration with {@link #getConfiguration()} and modify it programmatically. To initialize the {@link GlobalContext} you
     * have to call {@link #configure()}
     * 
     * @param file
     *            the file
     */
    public static void loadConfiguration(final File file) {
	try {
	    final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 2048);
	    try {
		loadConfiguration(bis);
	    } finally {
		if (bis != null) {
		    bis.close();
		}
	    }
	} catch (final FileNotFoundException e) {
	    throw new NeedleException("Configuration could not be found", e);
	} catch (final IOException e) {
	    throw new NeedleException("Failed to load configuration", e);
	}
    }

    /**
     * Loads the configuration from the given {@link InputStream}. The stream will stay open. It's necessary to close it afterwards.
     * 
     * @param is
     *            the inputstream
     * @throws NeedleException
     *             the needle exception
     */
    public static void loadConfiguration(final InputStream is) throws NeedleException {
	try {
	    final JAXBContext context = JAXBContext.newInstance(Configuration.class.getPackage().getName());
	    // parse the XML and return an instance of the AppConfig class
	    configuration = (Configuration) context.createUnmarshaller().unmarshal(is);
	} catch (final JAXBException e) {
	    throw new NeedleException("Failure during loading configuration", e);
	}
    }

    public static void loadConfiguration(final String fileName) {
	loadConfiguration(new File(fileName));
    }

    private static Configuration getDefaultConfiguration() {
	final Configuration configuration = new Configuration();
	configuration.setWorkerClass(AsyncAggregationWorker.class);
	configuration.setPersistenceConfiguration(null);
	return configuration;
    }

    private static KeyCreator getKeyCreator(final net.sf.needles.configuration.KeyCreator keyCreator) {
	switch (keyCreator) {
	    case NEEDLE_ID_KEY_CREATOR: {
		return NeedleIdKeyCreator.INSTANCE;
	    }
	    case NEEDLE_NAME_KEY_CREATOR: {
		return NeedleNameKeyCreator.INSTANCE;
	    }
	    case SINGLETON_KEY_CREATOR: {
		return SingletonKeyCreator.INSTANCE;
	    }
	}
	throw new IllegalArgumentException("No known keyCreator found for: " + ((keyCreator != null) ? keyCreator.name() : "null"));
    }
}
