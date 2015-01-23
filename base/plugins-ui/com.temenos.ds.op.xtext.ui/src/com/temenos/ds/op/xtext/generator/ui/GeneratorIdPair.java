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

import org.eclipse.xtext.generator.IGenerator;

/**
 * Pair of IGenerator and its ID.
 * 
 * Inspired by org.eclipse.xtext.util.Pair (which cannot be extended, as
 * it's constructor is package private; usable only from
 * org.eclipse.xtext.util.Tuples factory).
 */
public class GeneratorIdPair {

	public static GeneratorIdPair of(IGenerator generator, String id) {
		return new GeneratorIdPair(generator, id);
	}

	protected final IGenerator generator;
	protected final String id;
	
	protected GeneratorIdPair(IGenerator generator, String id) {
		this.generator = generator;
		this.id = id;
	}
	
	public IGenerator getGenerator() {
		return generator;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((generator == null) ? 0 : generator.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeneratorIdPair other = (GeneratorIdPair) obj;
		if (generator == null) {
			if (other.generator != null)
				return false;
		} else if (!generator.equals(other.generator))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GeneratorIdPair [id=" + id + ", generator=" + generator.toString() + "]";
	}
	
}