/*******************************************************************************
 * Copyright (c) 2015 Michael Vorburger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Vorburger - initial API and implementation
 ******************************************************************************/
package com.temenos.ds.op.xtext.generator.ui;

import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.temenos.ds.op.xtext.ui.internal.se.JdtBasedClassLoaderProvider;

/**
 * An IGeneratorsProvider which finds fixed IGenerators from the JDT runtime classpath of the Resource being generated. 
 *
 * @author Michael Vorburger
 */
public class RuntimeJDTGeneratorsProvider implements IGeneratorsProvider {
	private final static Logger logger = LoggerFactory.getLogger(RuntimeJDTGeneratorsProvider.class);
	
	protected @Inject JdtBasedClassLoaderProvider classloaderProvider;

	@Override
	public Iterable<GeneratorIdPair> getGenerators(IProject project) {
		StopWatch stopWatch = new StopWatch();

		// TODO HIGH look up a list, don't hard-code just one, as for first test..
		String generatorClassName = "test.Generator";
		classloaderProvider.setParentClassLoaderClass(IGenerator.class);
		Optional<IGenerator> generator = classloaderProvider.getInstance(project, generatorClassName);
		if (!generator.isPresent()) {
			final String msg = "Generator class could not be found on this project: " + generatorClassName;
			logger.error(msg);
			return Collections.emptyList();

		} else {
			final Object generator2 = generator.get();
			IGenerator generator3 = (IGenerator) generator2;
			String generatorId = generatorClassName; // TODO LATER generatorId could be read from an fixed annotation on classname (which would remain stable on refactorings)
		
			stopWatch.resetAndLog(getClass().getName() + " getGenerators()");
			return Collections.singleton(GeneratorIdPair.of(generator3, generatorId));
		}
	}

}
