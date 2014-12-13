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
package com.temenos.ds.op.xtext.ui.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.shared.SharedStateModule;
import org.eclipse.xtext.util.Modules2;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Activator.
 * 
 * @author Michael Vorburger
 */
@SuppressWarnings("restriction")
public class NODslActivator extends AbstractUIPlugin {
	private static final Logger logger = LoggerFactory.getLogger(NODslActivator.class);
	
	private static NODslActivator INSTANCE;
	
	private Injector injector;	
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		injector = createInjector(context);
		INSTANCE = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		injector = null;
		INSTANCE = null;
		super.stop(context);
	}
	
	public static NODslActivator getInstance() {
		return INSTANCE;
	}
	
	public Injector getInjector() {
		if (injector == null) {
			throw new IllegalStateException("Needs start() first");
		}
		return injector;
	}
	
	protected Injector createInjector(BundleContext context) {
		try {
//			Module runtimeModule = getRuntimeModule();
			Module sharedStateModule = getSharedStateModule();
			Module uiModule = getUiModule();
			Module noopModule = getNoopModule();
			Module eclipseModule = getEclipseModule();

			// TODO Is this right? Do I really have to repeat all of these standard Xtext modules here, above and below?? How to get them all automatically, future proof?

			// Module sharedContributionModule = new DefaultSharedContribution();
			// Module sharedContributionWithJDTModule = new SharedContributionWithJDT();
			// Module sharedModule = new SharedModule(context);
			
			Module mergedModule = Modules2.mixin(/*runtimeModule, */ sharedStateModule,  uiModule, eclipseModule, noopModule
					// , sharedContributionModule, sharedContributionWithJDTModule, sharedModule
					);
			return Guice.createInjector(mergedModule);
		} catch (Exception e) {
			logger.error("Failed to create injector: " + e.getMessage(), e);
			throw new RuntimeException("Failed to create injector", e);
		}
	}

//	protected Module getRuntimeModule() {
//		return new NODslRuntimeModule();
//	}
//	
	protected Module getUiModule() {
		return new NODslUiModule(this);
	}
	
	protected Module getSharedStateModule() {
		return new SharedStateModule();
	}

	protected Module getEclipseModule() {
		return new NODslEclipseModule();
	}

	protected Module getNoopModule() {
		return new NODslNoopModule();
	}

}
