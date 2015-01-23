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

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ServiceLoader;

import org.eclipse.core.resources.IProject;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.util.StopWatch;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.temenos.ds.op.xtext.ui.internal.se.JdtBasedClassLoaderProvider;

/**
 * An IGeneratorsProvider which finds fixed IGenerators from the JDT runtime classpath of the Resource being generated. 
 *
 * @author Michael Vorburger
 */
public class RuntimeJDTServiceLoaderGeneratorsProvider implements IGeneratorsProvider {
	// private final static Logger logger = LoggerFactory.getLogger(RuntimeJDTGeneratorsProvider.class);
	
	protected @Inject JdtBasedClassLoaderProvider classloaderProvider;

	@Override
	public Iterable<GeneratorIdPair> getGenerators(IProject project) {
		StopWatch stopWatch = new StopWatch();

		classloaderProvider.setParentClassLoaderClass(IGenerator.class);
		// TODO Perhaps this should be cached?
		Optional<URLClassLoader> optClassLoader = classloaderProvider.getClassLoader(project);
		if (!optClassLoader.isPresent())
			return Collections.emptyList();
		
		ArrayList<GeneratorIdPair> generators = new ArrayList<GeneratorIdPair>();
		ClassLoader classLoader = optClassLoader.get();
		// TODO Perhaps this should be cached?
		ServiceLoader<IGenerator> serviceLoader = ServiceLoader.load(IGenerator.class, classLoader);
		for (IGenerator generator : serviceLoader) {
			String generatorId = generator.getClass().getName(); // TODO LATER generatorId could be read from an fixed annotation on classname (which would remain stable on refactorings)
			generators.add(GeneratorIdPair.of(generator, generatorId));
		}
		
		stopWatch.resetAndLog(getClass().getName() + " getGenerators()");
		return ImmutableList.copyOf(generators);
	}

}
