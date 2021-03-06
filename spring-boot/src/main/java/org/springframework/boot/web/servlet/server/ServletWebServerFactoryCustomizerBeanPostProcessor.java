/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.web.servlet.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;

/**
 * {@link BeanPostProcessor} that applies all {@link ServletWebServerFactoryCustomizer}s
 * from the bean factory to {@link ConfigurableServletWebServerFactory} beans.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public class ServletWebServerFactoryCustomizerBeanPostProcessor
		implements BeanPostProcessor, BeanFactoryAware {

	private ListableBeanFactory beanFactory;

	private List<ServletWebServerFactoryCustomizer> customizers;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isInstanceOf(ListableBeanFactory.class, beanFactory,
				"ServletWebServerFactoryCustomizerBeanPostProcessor can only be used "
						+ "with a ListableBeanFactory");
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof ConfigurableServletWebServerFactory) {
			postProcessBeforeInitialization((ConfigurableServletWebServerFactory) bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	private void postProcessBeforeInitialization(
			ConfigurableServletWebServerFactory bean) {
		for (ServletWebServerFactoryCustomizer customizer : getCustomizers()) {
			customizer.customize(bean);
		}
	}

	private Collection<ServletWebServerFactoryCustomizer> getCustomizers() {
		if (this.customizers == null) {
			// Look up does not include the parent context
			this.customizers = new ArrayList<>(this.beanFactory
					.getBeansOfType(ServletWebServerFactoryCustomizer.class, false, false)
					.values());
			Collections.sort(this.customizers, AnnotationAwareOrderComparator.INSTANCE);
			this.customizers = Collections.unmodifiableList(this.customizers);
		}
		return this.customizers;
	}

}
