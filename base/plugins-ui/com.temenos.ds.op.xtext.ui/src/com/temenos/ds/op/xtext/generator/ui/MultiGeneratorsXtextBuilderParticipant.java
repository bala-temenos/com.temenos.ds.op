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
package com.temenos.ds.op.xtext.generator.ui;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.builder.BuilderParticipant;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;
import org.eclipse.xtext.ui.editor.preferences.PreferenceStoreAccessImpl;
import org.eclipse.xtext.util.StopWatch;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;


/**
 * An IXtextBuilderParticipant which runs a list of IGenerator, obtained from an IGeneratorsProvider.
 * 
 * @author Michael Vorburger
 */
public class MultiGeneratorsXtextBuilderParticipant extends BuilderParticipant /* implements IXtextBuilderParticipant */ {
	// TODO Xtext MAYBE later send a proposal to Xtext core to split up BuilderParticipant so it's more suitable to be extended here
	
	// private final static Logger logger = LoggerFactory.getLogger(MultiGeneratorsXtextBuilderParticipant.class);

	private @Inject IGeneratorsProvider generatorsProvider; 
	private SingleThreadLocal<IBuildContext> buildContextLocal = new SingleThreadLocal<IBuildContext>();
	private @Inject PreferenceStoreAccessImpl preferenceStoreAccess;
	private SubMonitor subMonitor;
	
	public PreferenceStoreAccessImpl getPreferenceStoreAccess() {
		return preferenceStoreAccess;
	}

	@Override
	public void build(IBuildContext context, IProgressMonitor monitor) throws CoreException {
		try {
			buildContextLocal.set(context);
			subMonitor = SubMonitor.convert(monitor, context.getBuildType() == BuildType.RECOVERY ? 5 : 3);
			super.build(context, monitor);
		} finally {
			buildContextLocal.remove();
			subMonitor = null;
		}
	}

	@Override
	protected void handleChangedContents(Delta delta, IBuildContext context,
			EclipseResourceFileSystemAccess2 fileSystemAccess) throws CoreException {

		if (delta.getUri().scheme().equals("java"))
			return; // Skip any Xbase java:/Objects/test.Generator like resources
		
		// copy/paste from super() -- TODO Xtext refactor BuilderParticipant with more protected methods so that this can be done cleanly
		Resource resource = context.getResourceSet().getResource(delta.getUri(), true);
		if (shouldGenerate(resource, context)) {
			try {
				Iterable<GeneratorIdPair> generators = generatorsProvider.getGenerators(context.getBuiltProject());
				registerCurrentSourceFolder(context, delta, fileSystemAccess);
				// TODO LATER inject generator with lang specific configuration.. is to return only class, not instance, and re-lookup from lang specific injector obtained from extension going to have any perf. drawback? measure it.
				for (GeneratorIdPair entry : generators) {
					IGenerator generator = entry.getGenerator();
					String generatorId = entry.getId();
					generate(context, fileSystemAccess, resource, generator, generatorId);
				}
			} catch (RuntimeException e) {
				if (e.getCause() instanceof CoreException) {
					throw (CoreException) e.getCause();
				}
				throw e;
			}
		}
	}

	protected void generate(IBuildContext context, EclipseResourceFileSystemAccess2 fileSystemAccess, Resource resource, IGenerator generator, String generatorId) throws CoreException {
		preferenceStoreAccess.setLanguageNameAsQualifier(generatorId);
		// This is copy/pasted from BuilderParticipant.build() - TODO Xtext refactor (PR) to be able to share code
		// TODO HIGH do we need to copy/paste more here.. what's all the Cleaning & Markers stuff??  
		final Map<String, OutputConfiguration> outputConfigurations = getOutputConfigurations(context, generatorId);
		refreshOutputFolders(context, outputConfigurations, subMonitor.newChild(1));
		fileSystemAccess.setOutputConfigurations(outputConfigurations);
		GenerationTimeLogger logger = GenerationTimeLogger.getInstance();
		StopWatch stopWatch = new StopWatch();
		generator.doGenerate(resource, fileSystemAccess);
		logger.updateTime(generatorId, (int)stopWatch.reset());
		logger.updateCount(generatorId, 1);
	}

	@Override
	protected Map<OutputConfiguration, Iterable<IMarker>> getGeneratorMarkers(
			IProject builtProject, Collection<OutputConfiguration> outputConfigurations)
			throws CoreException {

		Map<OutputConfiguration, Iterable<IMarker>> generatorMarkers = newHashMap();
		for (OutputConfiguration outputConfiguration : outputConfigurations) {
			// We need to do this because there may not be a Generator for each OutputConfiguration (yet/anymore?) but we still need an entry - else we get a NPE later
			generatorMarkers.put(outputConfiguration, Collections.<IMarker>emptyList());
		}
		
		final Iterable<GeneratorIdPair> generators = generatorsProvider.getGenerators(builtProject);
		for (GeneratorIdPair entry : generators) {
			String generatorId = entry.getId();
			final Map<String, OutputConfiguration> modifiedConfigs = getOutputConfigurations(buildContextLocal.get(), generatorId);
			Map<OutputConfiguration, Iterable<IMarker>> markers = super.getGeneratorMarkers(builtProject, modifiedConfigs.values());
			for (OutputConfiguration key : markers.keySet()) {
				Iterable<IMarker> mainMarkers = generatorMarkers.get(key);
				Iterable<IMarker> newMarkers = markers.get(key);
				generatorMarkers.put((OutputConfiguration) key,	Iterables.concat(mainMarkers, newMarkers));
			}
		}
		return generatorMarkers;
	}
	
	protected Map<String, OutputConfiguration> getOutputConfigurations(IBuildContext context, String generatorId) {
		IPreferenceStoreAccess preferenceStoreAccess = getOutputConfigurationProvider().getPreferenceStoreAccess();
		PreferenceStoreAccessImpl preferenceStoreAccessImpl = (PreferenceStoreAccessImpl) preferenceStoreAccess;
		preferenceStoreAccessImpl.setLanguageNameAsQualifier(generatorId);
		return super.getOutputConfigurations(context);
	}
	
	@Override
	protected boolean isEnabled(IBuildContext context) {
		// TODO HIGH TDD better later read this from.. an IGenerator specific Preference
		return true;
	}
	
	@Override
	protected List<Delta> getRelevantDeltas(IBuildContext context) {
		// TODO better for future compat. to just make sure we @Inject an resourceServiceProvider where canHandle => true always instead of this short term solution:
		return context.getDeltas();
	}
	
	@Override
	public void setOutputConfigurationProvider(
			EclipseOutputConfigurationProvider outputConfigurationProvider) {
		super.setOutputConfigurationProvider(outputConfigurationProvider);
	}
}
