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

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.common.base.Optional;

public class JdtBasedClassLoaderProvider extends JdtBasedProcessorProvider {

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
	public <T> Optional<T> getInstance(Resource ctx, String identifier) {
		Optional<ClassLoader> classLoader = getClassLoader(ctx, identifier);
		if (!classLoader.isPresent())
			return Optional.absent();
		try {
			Class<T> clazz = (Class<T>) classLoader.get().loadClass(identifier);
			return Optional.of(clazz.newInstance());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Problem during instantiation of " + identifier + " : " + e.getMessage(), e);
		}
	}
	
	protected Optional<ClassLoader> getClassLoader(Resource ctx, String identifier) {
		final Optional<IJavaProject> project = getJavaProject(ctx);
		if (project.isPresent())
			return Optional.fromNullable(createClassLoader(identifier, project.get()));
		else
			return Optional.absent();
	}
	
	protected Optional<IJavaProject> getJavaProject(Resource ctx) {
		ResourceSet _resourceSet = ctx.getResourceSet();
		Object _classpathURIContext = ((XtextResourceSet) _resourceSet).getClasspathURIContext();
// Do NOT check here - it may just not be a Java Project! Handle it in caller..
//		if (_classpathURIContext == null)
//			throw new IllegalStateException("Ctx Resource is not in RS that has an IJavaProject ClasspathURIContext - IResourceSetInitializer binding to JavaProjectResourceSetInitializer missing, see SharedContributionWithJDT?");
		final IJavaProject project = ((IJavaProject) _classpathURIContext);
		return Optional.fromNullable(project);
	}
	
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
