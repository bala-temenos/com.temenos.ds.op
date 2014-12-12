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

import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;

/**
 * Ugly abstract IGenerator base class with throws Exception in doGenerate();
 * 
 * This is better than if each IGenerator implementation does something like this itself. 
 * 
 * @author Michael Vorburger
 */
public abstract class IGeneratorThrowsWithThrowsException implements IGenerator {

	// TODO: Move out from tests/ into prod, and make MultiGeneratorsXtextBuilderParticipant take this into account, and "unwraps" the WrappedException?
	// TODO MultiGeneratorXtextBuilderParticipantTest should test this.
	// This should be in upstream core Xtext?

	// TODO Throws unrelated - add support for requesting org.eclipse.xtext.builder.IXtextBuilderParticipant.IBuildContext.needRebuild()
	
	/* (non-Javadoc)
	 * @see org.eclipse.xtext.generator.IGenerator#doGenerate(org.eclipse.emf.ecore.resource.Resource, org.eclipse.xtext.generator.IFileSystemAccess)
	 */
	@Override
	public void doGenerate(Resource input, IFileSystemAccess fsa) {
		try {
			doGenerateWithThrowsException(input, fsa);
		} catch (Exception e) {
			throw new WrappedException(e);
		}
	}

	public abstract void doGenerateWithThrowsException(Resource input, IFileSystemAccess fsa) throws Exception;

}
