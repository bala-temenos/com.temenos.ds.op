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

import org.eclipse.core.resources.IProject;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * An IGeneratorsProvider which delegates to other IGeneratorsProvider implementations.
 *
 * @author Michael Vorburger
 */
public class MultiGeneratorsProvider implements IGeneratorsProvider {

	// TODO LATER Make this more configurable, instead of hard-coded here as-is currently
	
//	private List<IGeneratorsProvider> delegates = new ArrayList<IGeneratorsProvider>(
//			Arrays.asList(new PluginsGeneratorsProvider(), new RuntimeJDTGeneratorsProvider()));
	
	protected @Inject PluginsGeneratorsProvider pluginsGeneratorsProvider;
	protected @Inject RuntimeJDTGeneratorsProvider runtimeJDTGeneratorsProvider;
	
	@Override
	public Iterable<GeneratorIdPair> getGenerators(IProject project) {
		Iterable<GeneratorIdPair> iterable = Iterables.concat(pluginsGeneratorsProvider.getGenerators(project), runtimeJDTGeneratorsProvider.getGenerators(project));
//		Iterable<GeneratorIdPair> iterable = Collections.emptyList();
//		for (IGeneratorsProvider generatorsProvider : delegates) {
//			iterable = Iterables.concat(iterable, generatorsProvider.getGenerators(resource));
//		}
		return iterable;
	}

}
