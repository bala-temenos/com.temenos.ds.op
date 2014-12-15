/*******************************************************************************
 * Copyright (c) 2014 Michael Vorburger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Vorburger - initial API and implementation
 ******************************************************************************/
package com.temenos.ds.op.xtext.generator.tests;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess;

/**
 * Example IMultiGenerator, for Tests.
 * 
 * @author Michael Vorburger
 */
public class TestMultiGenerator extends IGeneratorThrowsWithThrowsException /* implements IGenerator | IMultiGenerator */ {

	// TODO Test @Inject - make sure it's language specific
	
	@Override
	public void doGenerateWithThrowsException(Resource input, IFileSystemAccess fsa) throws Exception {
		fsa.generateFile(input.getURI().lastSegment() + ".txt", "hello, world: " + input.getURI().toString());
	}

}
