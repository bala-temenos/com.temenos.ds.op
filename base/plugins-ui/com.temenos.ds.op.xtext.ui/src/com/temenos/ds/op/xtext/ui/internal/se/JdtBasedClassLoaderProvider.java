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
package com.temenos.ds.op.xtext.ui.internal.se;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class JdtBasedClassLoaderProvider extends JdtBasedProcessorProvider {
	private static Logger logger = LoggerFactory.getLogger(JdtBasedClassLoaderProvider.class);

	// TODO Propose to Xtext to refactor JdtBasedProcessorProvider, and its parent ProcessorInstanceForJvmTypeProvider, into a more generic ClassLoaderProvider
	
	protected Class<?> parentClassLoaderClass;
	
	public void setParentClassLoaderClass(Class<?> parentClassLoaderClass) {
		this.parentClassLoaderClass = parentClassLoaderClass;
	}

	protected ClassLoader getParentClassLoader() {
		// NOTE super() is hard-coded to TransformationContext - but we use IGenerator
		return parentClassLoaderClass.getClassLoader();
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getInstance(IProject project, String identifier) {
		Optional<ClassLoader> classLoader = getClassLoader(project, identifier);
		if (!classLoader.isPresent())
			return Optional.absent();
		try {
			Class<T> clazz = (Class<T>) classLoader.get().loadClass(identifier);
			return Optional.of(clazz.newInstance());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// NOT throw new IllegalStateException(...);
			logger.error("Problem during instantiation of " + identifier + " : " + e.getMessage(), e);
			return Optional.absent();
		}
	}
	
	protected Optional<ClassLoader> getClassLoader(IProject project, String identifier) {
		final Optional<IJavaProject> javaProject = getJavaProject(project);
		if (javaProject.isPresent())
			return Optional.fromNullable(createClassLoader(identifier, javaProject.get()));
		else
			return Optional.absent();
	}
	
	// TODO This probably already exists somewhere?
	protected Optional<IJavaProject> getJavaProject(IProject project) {
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
			    IJavaProject javaProject = JavaCore.create(project);
			    return Optional.of(javaProject);
			} else {
				return Optional.absent();
			}
		} catch (CoreException e) {
			logger.error("CoreException from IProject.hasNature(JavaCore.NATURE_ID)" , e);
			return Optional.absent();
		}
	}
	
//	protected Optional<IJavaProject> getJavaProject(Resource ctx) {
//		ResourceSet _resourceSet = ctx.getResourceSet();
//		Object _classpathURIContext = ((XtextResourceSet) _resourceSet).getClasspathURIContext();
//// Do NOT check here - it may just not be a Java Project! Handle it in caller..
////		if (_classpathURIContext == null)
////			throw new IllegalStateException("Ctx Resource is not in RS that has an IJavaProject ClasspathURIContext - IResourceSetInitializer binding to JavaProjectResourceSetInitializer missing, see SharedContributionWithJDT?");
//		final IJavaProject project = ((IJavaProject) _classpathURIContext);
//		return Optional.fromNullable(project);
//	}
//	
//	@Override
//	public ClassLoader getClassLoader(EObject ctx) {
//		Resource _eResource = ctx.eResource();
//		return getClassLoader(_eResource);
//	}
//
//	public ClassLoader getClassLoader(Resource _eResource) {
//		ResourceSet _resourceSet = _eResource.getResourceSet();
//		return getClassLoader(_resourceSet);
//	}
//	
//	public ClassLoader getClassLoader(ResourceSet _resourceSet) {
//		Object _classpathURIContext = ((XtextResourceSet) _resourceSet).getClasspathURIContext();
//		final IJavaProject project = ((IJavaProject) _classpathURIContext);
//		return createClassLoaderForJavaProject(project);
//	}

}
