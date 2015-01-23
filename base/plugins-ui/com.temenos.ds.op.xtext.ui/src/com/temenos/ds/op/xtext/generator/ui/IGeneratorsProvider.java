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

import com.google.inject.ImplementedBy;

@ImplementedBy(MultiGeneratorsProvider.class) // TODO LATER Make this more configurable, instead of hard-coded here as-is currently 
public interface IGeneratorsProvider {
	
	Iterable<GeneratorIdPair> getGenerators(IProject builtProject);
	
}
