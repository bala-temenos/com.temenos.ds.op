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
import static org.eclipse.xtext.junit4.ui.util.JavaProjectSetupUtil.addToClasspath;
import static org.eclipse.xtext.junit4.ui.util.JavaProjectSetupUtil.createJavaProject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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

	// This is a minimalistic IGenerator implementation
	private static final String MINIMAL_VALID_GENERATOR = "package test;\nimport org.eclipse.emf.ecore.resource.Resource;\nimport org.eclipse.xtext.generator.IFileSystemAccess;\nimport org.eclipse.xtext.generator.IGenerator;\npublic class Generator implements IGenerator {\n	@Override\n	public void doGenerate(Resource input, IFileSystemAccess fsa) {\n		fsa.generateFile(input.getURI().lastSegment() + \".inproject.txt\", \"hello\");\n	}\n}";
	
	private static final String PLUGIN_TEST_GENERATOR_ID = "com.temenos.ds.op.xtext.generator.tests.TestMultiGeneratorID"; // matching plugin.xml
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
		IProject project = createXtextJavaProject("testGeneratorPlugin").getProject();
		createFileAndAssertGenFile(project, "src/Minimal1.xtext", PLUGIN_TEST_GENERATOR_ID, "./test-gen", "test-gen/Minimal1.xtext.txt");		
		createFileAndAssertGenFile(project, "src/Minimal2.xtext", PLUGIN_TEST_GENERATOR_ID, "./other-gen", "other-gen/Minimal2.xtext.txt");		
	}

	@Test
	public void testMultiGeneratorXtextBuilderParticipantWithGeneratorInRuntimeWorkspace() throws Exception {
		IJavaProject javaProject = createXtextJavaProject("testGeneratorInProject");
		addPlatformJarToClasspath(javaProject, "org.eclipse.emf.common");
		addPlatformJarToClasspath(javaProject, "org.eclipse.emf.ecore");
		addPlatformJarToClasspath(javaProject, "org.eclipse.xtext");
		
		IProject project = javaProject.getProject();
		IFile generatorJavaFile = createFile(project, "src/test/Generator.java", MINIMAL_VALID_GENERATOR);
		IFile servicesFile = createFile(project, "src/META-INF/services/org.eclipse.xtext.generator.IGenerator", "test.Generator");
		createFileAndAssertGenFile(project, "src/Minimal3.xtext", "test.Generator", "./src-gen", "src-gen/Minimal3.xtext.inproject.txt");		
		createFileAndAssertGenFile(project, "src/Minimal3.xtext", "test.Generator", "./gen", "gen/Minimal3.xtext.inproject.txt");		

		// TODO CHANGE generator, in running IDE, and make sure new file gets gen and no longer old one

		generatorJavaFile.delete(true, monitor());
		servicesFile.delete(true, monitor());
	}

	// TODO propose this for core Xtext org.eclipse.xtext.junit4.ui.util.JavaProjectSetupUtil
	// It's existing addPlatformJarToClasspath(Plugin srcPlugin, String jarFileName, IJavaProject destProject) could be renamed (or deprecated and a new method introduced) 
	// to the perhaps much clearer addResourceFromInsidePlatformJarToClasspath() - because that's what that does.
	private IClasspathEntry addPlatformJarToClasspath(IJavaProject destJavaProject, String srcPluginID) throws JavaModelException {
		// Inspired by org.eclipse.xtend.ide.tests.macros.ActiveAnnotationsProcessingInIDETest
		// see also org.eclipse.xtend.ide.tests.WorkbenchTestHelper with more possibly useful helpers for tests like this
		// For now we can keep it simply and use only JavaProjectSetupUtil here.
		@SuppressWarnings("deprecation")
		Plugin srcPlugin = Platform.getPlugin(srcPluginID);
		if (srcPlugin == null)
			throw new IllegalArgumentException("Plugin not found: " + srcPluginID);
		String location = srcPlugin.getBundle().getLocation();
		final String PREFIX = "reference:file:";
		if (!location.startsWith(PREFIX)) {
			throw new IllegalStateException(location + " does not start with expected prefix " + PREFIX);
		}
		location = location.substring(PREFIX.length()); // chop off prefix
		IPath path = new Path(location);
		final IClasspathEntry newClassPathEntry = JavaCore.newLibraryEntry(path, null, null);
		addToClasspath(destJavaProject, newClassPathEntry);
		return newClassPathEntry;
	}
	
	private void createFileAndAssertGenFile(IProject project, String sourceFileName, String generatorID, String outputFolderName, String expectedGenFileName) throws Exception {
		setDefaultOutputFolderDirectory(project, generatorID, outputFolderName);
		IFile model1 = createFile(project, sourceFileName, MINIMAL_VALID_XTEXT_GRAMMAR);
		waitForAutoBuild();
		IFile generatedFile = project.getFile(expectedGenFileName);
		assertExists(generatedFile);
		deleteModelFileAndAssertGenFileAlsoGotDeleted(model1, generatedFile);
	}

	private void assertExists(IFile file) throws CoreException {
		String otherFiles = "";
		if (!file.exists()) {
			StringBuilder otherFilesBuilder = new StringBuilder();
			addMembersRecursively(otherFilesBuilder, file.getProject());
			otherFiles = otherFilesBuilder.toString();
		}
		assertTrue("Does not exist: " + file.toString() + otherFiles, file.exists());
	}

	private void addMembersRecursively(StringBuilder sb, IContainer container) throws CoreException {
		for (IResource member : container.members()) {
			sb.append('\n');
			sb.append(member.toString());
			if (member instanceof IContainer)
				addMembersRecursively(sb, (IContainer) member);
		} 
	}
	
	private IFile createFile(IProject project, String fileName, String fileContent) throws CoreException {
		IFile file = project.getFile(fileName);
		IFolder parentFolder = (IFolder) file.getParent();
		mkdirs(parentFolder);
		file.create(new StringInputStream(fileContent), true, monitor());
		project.build(IncrementalProjectBuilder.FULL_BUILD, monitor());
		waitForAutoBuild();
		return file;
	}

	private void mkdirs(IFolder folder) throws CoreException {
		if (folder.exists())
			return;
		IContainer container = folder.getParent();
		if (!container.exists())
			mkdirs((IFolder) container);
		folder.create(true, true, monitor());
	}
	
	private void deleteModelFileAndAssertGenFileAlsoGotDeleted(IFile file, IResource generatedFile) throws Exception {
		file.delete(true, monitor());
		waitForAutoBuild();
		assertTrue("Does still exist, was not deleted: " + generatedFile.toString(), !generatedFile.exists());
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
