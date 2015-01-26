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
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.emf.ecore.plugin.RegistryReader;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import com.temenos.ds.op.xtext.ui.internal.NODslActivator;

/**
 * An IGeneratorsProvider which finds fixed IGenerators from other plugins in the target platform via the Eclipse extension point mechanism. 
 *
 * @author Michael Vorburger
 */
public class PluginsGeneratorsProvider implements IGeneratorsProvider {
	// private final static Logger logger = LoggerFactory.getLogger(PluginsGeneratorsProvider.class);

	// These incl. volatile, inspired by (copy/pasted from) org.eclipse.xtext.builder.impl.RegistryBuilderParticipant
	protected @Inject IExtensionRegistry extensionRegistry;
	private volatile Iterable<GeneratorIdPair> generatorToId;

	@Override
	public Iterable<GeneratorIdPair> getGenerators(IProject project) {
		// Resource is, obviously, completely ignored by this IGeneratorsProvider implementation
		return getGenerators();
	}

	// inspired by (copy/pasted from) org.eclipse.xtext.builder.impl.RegistryBuilderParticipant.getParticipants()
	protected Iterable<GeneratorIdPair> getGenerators() {
		Iterable<GeneratorIdPair> result = generatorToId;
		if (generatorToId == null) {
			StopWatch stopWatch = new StopWatch();
			result = initGenerators();
			stopWatch.resetAndLog(getClass().getName() + " initGenerators() - should only be called once!");
		}
		if (result == null)
			return Collections.emptySet();
		return result;
	}
	
	// inspired by (copy/pasted from) org.eclipse.xtext.builder.impl.RegistryBuilderParticipant.initParticipants()
	// If later sending a proposal to Xtext core to split up BuilderParticipant so it's more suitable to be extended here, refactor to make this re-usable across instead copy/paste 
	protected synchronized Iterable<GeneratorIdPair> initGenerators() {
		Iterable<GeneratorIdPair> result = generatorToId;
		if (result == null) {
			if (generatorToId == null) {
				HashBiMap<String, IGenerator> idToGenerator = HashBiMap.create();
				NODslActivator activator = NODslActivator.getInstance();
				if (activator != null) {
					String pluginID = activator.getBundle().getSymbolicName();
					GeneratorReader<IGenerator> reader = new GeneratorReader<IGenerator>(extensionRegistry, pluginID, "multigenerator", idToGenerator);
					reader.readRegistry();
				}
				Builder<GeneratorIdPair> resultBuilder = ImmutableList.builder();
				for (Entry<String, IGenerator> entry : idToGenerator.entrySet()) {
					resultBuilder.add(GeneratorIdPair.of(entry.getValue(), entry.getKey()));
				}
				generatorToId = resultBuilder.build();
			}
		}
		return result;
	}

	// This inner class is inspired by org.eclipse.xtext.builder.impl.RegistryBuilderParticipant.BuilderParticipantReader - 
	// TODO Xtext If later sending a proposal to Xtext core to split up BuilderParticipant so it's more suitable to be extended here, refactor to make this re-usable across instead copy/paste 
	// It's intentionally static to make sure it doesn't access members of the outer class, and will thus be easier to factor out later 
	public static class GeneratorReader<T> extends RegistryReader {
		private final static Logger logger = LoggerFactory.getLogger(GeneratorReader.class);

		private static final String ATT_CLASS = "class";
		private static final String ATT_ID = "id";

		protected final String extensionPointID;
		protected final Map<String, T> generatorIdToInstance;

		public GeneratorReader(IExtensionRegistry pluginRegistry, String pluginID, String extensionPointID, Map<String, T> generatorIdToInstance) {
			super(pluginRegistry, pluginID, extensionPointID);
			this.extensionPointID = extensionPointID;
			this.generatorIdToInstance = generatorIdToInstance;
		}
		
		@Override
		protected boolean readElement(IConfigurationElement element, boolean add) {
			boolean result = false;
			if (element.getName().equals(extensionPointID)) {
				final String id = element.getAttribute(ATT_ID);
				final String className = element.getAttribute(ATT_CLASS);
				if (className == null) {
					logMissingAttribute(element, ATT_CLASS);
				} else if (id == null) {
					logMissingAttribute(element, ATT_ID);
				} else if (add) {
					if (generatorIdToInstance.containsKey(id)) {
						logger.warn("The builder participant '" + id + "' was registered twice."); //$NON-NLS-1$ //$NON-NLS-2$
					}
					Optional<T> instance = get(element);
					if (!instance.isPresent()) {
						result = false;
					}
					generatorIdToInstance.put(id, instance.get());
					// ? participants = null;
					result = true;
				} else {
					generatorIdToInstance.remove(id);
					// ? participants = null;
					result = true;
				}
			}
			return result;
		}


		@SuppressWarnings("unchecked")
		protected Optional<T> get(IConfigurationElement element) {
			try {
				return Optional.of((T) element.createExecutableExtension(ATT_CLASS));
			} catch (CoreException e) {
				logError(element, e);
			} catch (NoClassDefFoundError e) {
				logError(element, e);
			}
			return Optional.absent();
		}

		protected void logError(IConfigurationElement element, Throwable e) {
			logger.error(getErrorTag(element), e);
		}

		@Override
		protected void logError(IConfigurationElement element, String text) {
			logger.error(getErrorTag(element));
			logger.error(text);
		}
		
		protected String getErrorTag(IConfigurationElement element) {
			IExtension extension = element.getDeclaringExtension();
			return "Plugin " + extension.getContributor().getName() + ", extension " + extension.getExtensionPointUniqueIdentifier(); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
}
