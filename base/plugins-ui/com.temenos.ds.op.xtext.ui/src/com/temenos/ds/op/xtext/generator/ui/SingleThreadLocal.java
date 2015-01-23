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

public class SingleThreadLocal<T> extends ThreadLocal<T> {
	
	@Override
	public void set(T value) {
		if (get() != null)
			throw new IllegalStateException("Multithreading issues - previous (parallel?) thread has not (yet?) remove()'d this ThreadLocal");
		super.set(value);
	}
}
