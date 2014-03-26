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

import java.util.Properties;

import net.sf.needles.NeedleException;
import net.sf.needles.NeedleInfo;
import net.sf.needles.aggregation.keycreator.NeedleIdKeyCreator;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.core.JmsTemplate;

public class JmsAggregationServiceFactory extends AbstractAggregationFactory<AbstractAggregationService> {

    private String jmsQueueName;
    private String jmsConnectionFactoryName;
    private String jndiInitialContextFactoryClass;
    private String jndiProviderUrl;
    private GenericApplicationContext context;
    private JmsTemplate jmsTemplate;

    public JmsAggregationServiceFactory() {
	super(NeedleIdKeyCreator.INSTANCE, JmsAggregationServiceFactory.class.getSimpleName());
    }

    public String getJmsConnectionFactoryName() {
	return jmsConnectionFactoryName;
    }

    public String getJmsQueueName() {
	return jmsQueueName;
    }

    public String getJndiInitialContextFactoryClass() {
	return jndiInitialContextFactoryClass;
    }

    public String getJndiProviderUrl() {
	return jndiProviderUrl;
    }

    public void setJmsConnectionFactoryName(final String jmsConnectionFactoryName) {
	this.jmsConnectionFactoryName = jmsConnectionFactoryName;
    }

    public void setJmsQueueName(final String jmsQueueName) {
	this.jmsQueueName = jmsQueueName;
    }

    public void setJndiInitialContextFactoryClass(final String jndiInitialContextFactoryClass) {
	this.jndiInitialContextFactoryClass = jndiInitialContextFactoryClass;
    }

    public void setJndiProviderUrl(final String jndiProviderUrl) {
	this.jndiProviderUrl = jndiProviderUrl;
    }

    @Override
    public void shutdown() {
	if (context != null) {
	    context.close();
	}
    }

    @Override
    public void start() {
	if (StringUtils.isBlank(getJndiInitialContextFactoryClass())) {
	    throw new NeedleException("JNDI context factory class has not been defined.");
	}
	if (StringUtils.isBlank(getJndiProviderUrl())) {
	    throw new NeedleException("JNDI provider url has not been defined.");
	}
	if (StringUtils.isBlank(getJmsConnectionFactoryName())) {
	    throw new NeedleException("JNDI connection factory has not been defined.");
	}
	if (StringUtils.isBlank(getJmsQueueName())) {
	    throw new NeedleException("JNDI queue name has not been defined.");
	}
	final GenericApplicationContext ctx = new GenericApplicationContext();
	final XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
	xmlReader.loadBeanDefinitions(new ClassPathResource("net/sf/needles/jms/JMSApplicationContext.xml"));
	final PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
	final Properties properties = new Properties();
	properties.setProperty("jndiInitialFactory", getJndiInitialContextFactoryClass());
	properties.setProperty("jndiProviderURL", getJndiProviderUrl());
	properties.setProperty("jmsConnectionFactory", getJmsConnectionFactoryName());
	properties.setProperty("jmsQueueName", getJmsQueueName());
	configurer.setProperties(properties);
	ctx.addBeanFactoryPostProcessor(configurer);
	ctx.registerShutdownHook();
	ctx.refresh();
	this.context = ctx;
	jmsTemplate = context.getBean(JmsTemplate.class);
    }

    @Override
    protected JmsAggregationService doCreateAggregation(final NeedleInfo needle) {
	return new JmsAggregationService(this, needle, jmsTemplate);
    }

}
