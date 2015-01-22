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

import static org.eclipse.xtext.builder.EclipseOutputConfigurationProvider.OUTPUT_DIRECTORY;
import static org.eclipse.xtext.builder.EclipseOutputConfigurationProvider.OUTPUT_PREFERENCE_TAG;
import static org.eclipse.xtext.builder.EclipseOutputConfigurationProvider.USE_OUTPUT_PER_SOURCE_FOLDER;
import static org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil.addNature;
import static org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil.monitor;
import static org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil.waitForAutoBuild;
import static org.eclipse.xtext.junit4.ui.util.JavaProjectSetupUtil.addProjectReference;
import static org.eclipse.xtext.junit4.ui.util.JavaProjectSetupUtil.createJavaProject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.xtext.builder.DerivedResourceCleanerJob;
import org.eclipse.xtext.builder.preferences.BuilderPreferenceAccess;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.editor.preferences.PreferenceConstants;
import org.eclipse.xtext.ui.editor.preferences.PreferenceStoreAccessImpl;
import org.eclipse.xtext.util.StringInputStream;
import org.junit.Test;

import com.google.inject.Injector;
import com.temenos.ds.op.xtext.generator.tests.copypasted.AbstractBuilderTest;
import com.temenos.ds.op.xtext.generator.ui.MultiGeneratorsXtextBuilderParticipant;
import com.temenos.ds.op.xtext.ui.internal.NODslActivator;

/**
 * Test for MultiGeneratorsXtextBuilderParticipant.
 * 
 * @author Michael Vorburger
 * @author Umesh
 */
@SuppressWarnings("restriction")
public class MultiGeneratorXtextBuilderParticipantTest extends AbstractBuilderTest {

	// This is a minimalistic Xtext grammar. The point of this isn't this Grammar itself - we just need some, any, test grammar.  Using Xtext's Xtext itself is easier & causes one dependency less (e.g. we don't need a grammar for a "*.mydsl")
	private static final String MINIMAL_VALID_XTEXT_GRAMMAR = "grammar test.Minimal import \"http://www.eclipse.org/emf/2002/Ecore\" as ecore generate minimal \"minimal\" Greeting: 'Hello' name=S; terminal S: ('a'..'z')*;";
	
	private static final String TEST_GENERATOR_ID = "com.temenos.ds.op.xtext.generator.tests.TestMultiGeneratorID";
	private MultiGeneratorsXtextBuilderParticipant participant;
	private PreferenceStoreAccessImpl preferenceStoreAccess;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		final Injector injector = NODslActivator.getInstance().getInjector();
		participant = injector.getInstance(MultiGeneratorsXtextBuilderParticipant.class);
		preferenceStoreAccess = participant.getPreferenceStoreAccess();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		participant = null;
	}

	@Test
	public void testMultiGeneratorXtextBuilderParticipantWithGeneratorInPlugin() throws Exception {
		IProject project = createXtextJavaProject("testGenerateIntoProjectOutputDirectory").getProject();
		createFileAndAssertGenFile(project, "src/Minimal1.xtext", "./test-gen", "test-gen/Minimal1.xtext.txt");		
		createFileAndAssertGenFile(project, "src/Minimal2.xtext", "./other-gen", "other-gen/Minimal2.xtext.txt");		
	}

	private void createFileAndAssertGenFile(IProject project, String sourceFileName, String outputFolderName, String genFileName) throws Exception {
		setDefaultOutputFolderDirectory(project, TEST_GENERATOR_ID, outputFolderName);
		IFile model1 = createFile(project, sourceFileName, MINIMAL_VALID_XTEXT_GRAMMAR);
		IFile generatedFile = project.getFile(genFileName);
		assertTrue(generatedFile.exists());
		deleteModelFileAndAssertGenFileAlsoGotDeleted(model1, generatedFile);
	}

	private IFile createFile(IProject project, String fileName, String fileContent) throws CoreException {
		IFile file = project.getFile(fileName);
		file.create(new StringInputStream(fileContent), true, monitor());
		project.build(IncrementalProjectBuilder.FULL_BUILD, monitor());
		waitForAutoBuild();
		return file;
	}

	private void deleteModelFileAndAssertGenFileAlsoGotDeleted(IFile file, IResource generatedFile) throws Exception {
		file.delete(true, monitor());
		waitForAutoBuild();
		assertTrue(!generatedFile.exists());
	}

	protected IJavaProject createXtextJavaProject(String name) throws CoreException {
		IJavaProject project = createJavaProject(name);
		addNature(project.getProject(), XtextProjectHelper.NATURE_ID);
		return project;
	}

	protected void setDefaultOutputFolderDirectory(IProject project, String generatorID, String directoryName) {
		preferenceStoreAccess.setLanguageNameAsQualifier(generatorID);
		IPreferenceStore preferences = preferenceStoreAccess.getWritablePreferenceStore(project);
		preferences.setValue(getDefaultOutputDirectoryKey(), directoryName);
	}
	
	@SuppressWarnings("unused") // use later
	private void createTwoReferencedProjects() throws CoreException {
		IJavaProject firstProject = createXtextJavaProject("first");
		IJavaProject secondProject = createXtextJavaProject("second");
		addProjectReference(secondProject, firstProject);
	}

	public static void waitForResourceCleanerJob() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(DerivedResourceCleanerJob.DERIVED_RESOURCE_CLEANER_JOB_FAMILY, null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}

	protected String getDefaultOutputDirectoryKey() {
		return OUTPUT_PREFERENCE_TAG + PreferenceConstants.SEPARATOR + IFileSystemAccess.DEFAULT_OUTPUT
				+ PreferenceConstants.SEPARATOR + OUTPUT_DIRECTORY;
	}

	protected String getUseOutputPerSourceFolderKey() {
		return OUTPUT_PREFERENCE_TAG + PreferenceConstants.SEPARATOR + IFileSystemAccess.DEFAULT_OUTPUT
				+ PreferenceConstants.SEPARATOR + USE_OUTPUT_PER_SOURCE_FOLDER;
	}

	protected String getOutputForSourceFolderKey(String sourceFolder) {
		return BuilderPreferenceAccess.getOutputForSourceFolderKey(new OutputConfiguration(
				IFileSystemAccess.DEFAULT_OUTPUT), sourceFolder);
	}
	
}
