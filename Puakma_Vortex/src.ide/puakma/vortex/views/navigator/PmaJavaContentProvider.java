/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 19, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.preference.IPreferenceStore;

import puakma.utils.lang.CollectionsUtil;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;

/**
 * This content provider should make the same output as package explorer in the
 * term of creating packages as lists or folders in hierarchy.
 * @author Martin Novak
 */
public class PmaJavaContentProvider extends StandardJavaElementContentProvider
{
	private ApplicationTreeViewer viewer;

	public PmaJavaContentProvider(ApplicationTreeViewer viewer)
	{
		super(true);

		this.viewer = viewer;
	}

	public Object[] getChildren(Object element)
	{
		try {
			if(exists(element) == false)
				return NO_CHILDREN;

			if(element instanceof IFolder) {
				if(isSourceFolder((IFolder) element))
					return getPackageFragmentRootChildren((IFolder) element);
				else
					return getResources((IFolder) element);
			}
			else if(element instanceof IPackageFragment) {
				return getPackageFragmentChildren((IPackageFragment) element);
			}
			else if(getProvideMembers()) {
				if(element instanceof ISourceReference && element instanceof IParent) {
					IJavaElement[] ret = ((IParent)element).getChildren();
					List<IJavaElement> l = new ArrayList<IJavaElement>();
					for(int i = 0; i < ret.length; ++i) {
						if(ret[i] instanceof IPackageDeclaration == false &&
								ret[i] instanceof IImportContainer == false) {
							l.add(ret[i]);
						}
					}

					return l.toArray();
				}
			}
		}
		catch(Exception ex) {
			// IGNORE...
		}

		return super.getChildren(element);
	}

	private Object[] getPackageFragmentChildren(IPackageFragment fragment) throws JavaModelException
	{
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		boolean useFlatPackages = store.getBoolean(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES);
		if(useFlatPackages) {
			return super.getChildren(fragment);
		}
		else {
			// ADD ALL COMPILATION UNITS
			List<Object> l = new ArrayList<Object>();
			if(fragment.getKind() == IPackageFragmentRoot.K_SOURCE)
				CollectionsUtil.addArrayToList(l, fragment.getCompilationUnits());
			else
				CollectionsUtil.addArrayToList(l, fragment.getOrdinaryClassFiles());
			// AND ALSO ADD ALL NON JAVA RESOURCES
			CollectionsUtil.addArrayToList(l, fragment.getNonJavaResources());

			// NOW ADD ALL THE CHILD FRAGMENTS
			IPackageFragmentRoot root = (IPackageFragmentRoot) fragment.getParent();
			IJavaElement[] elems = root.getChildren();
			String fragmentName = fragment.getElementName();
			for(int i = 0; i < elems.length; ++i) {
				String elemName = elems[i].getElementName();
				int index = elemName.lastIndexOf('.');
				int len = fragmentName.length();
				if(elems[i] instanceof IPackageFragment
						&& elemName.startsWith(fragment.getElementName())
						&& elems[i].equals(fragment) == false
						&& (index == len || index == -1))
					l.add(elems[i]);
			}

			return l.toArray();

		}
	}

	private Object[] getPackageFragmentRootChildren(IFolder folder)
			throws JavaModelException
			{
		IJavaProject project = JavaCore.create(folder.getProject());
		IPath prjPath = project.getPath();
		IPackageFragmentRoot root = project.findPackageFragmentRoot(prjPath
				.append(folder.getProjectRelativePath().segment(0)));
		if(root != null) {
			IJavaElement[] elems = root.getChildren();
			List<Object> l = new ArrayList<Object>();

			IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
			boolean useFlatPackages = store.getBoolean(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES);

			if(useFlatPackages) {
				// WELL, WE BASICALLY CAN RETURN THAT, BUT WE SHOULD FILTER AT FIRST THE EMPTY
				// PACKAGES WHICH ARE NOT LEAFS
				OUTER: for(int i = 0; i < elems.length; ++i) {
					if(elems[i] instanceof IPackageFragment) {
						IPackageFragment currentFragment = (IPackageFragment) elems[i];
						String currentPackageName = currentFragment.getElementName();
						boolean currentEmpty = (currentFragment.getNonJavaResources().length + currentFragment.getChildren().length) == 0;
						Iterator<Object> it = l.iterator();
						while(it.hasNext()) {
							IPackageFragment fragment = (IPackageFragment) it.next();
							if(currentEmpty && fragment.getElementName().startsWith(currentPackageName))
								continue OUTER;
							boolean fragmentEmpty = (fragment.getNonJavaResources().length + fragment.getChildren().length) == 0;
							if(currentPackageName.startsWith(fragment.getElementName()) && fragmentEmpty) {
								it.remove();
							}
						}
						l.add(currentFragment);
					}
				}

			return l.toArray();
			}
			else {
				// VIEWER IS NOT FLAT
				for(int i = 0; i < elems.length; ++i) {
					if(elems[i] instanceof IPackageFragment) {
						IPackageFragment fragment = (IPackageFragment) elems[i];
						if(fragment.isDefaultPackage() == false) {
							if(fragment.getElementName().indexOf('.') == -1)
								l.add(fragment);
						}
						else {
							// FRAGMENT IS ROOT FRAGMENT -> ADD ALL THE COMPILATION UNITS
							// THERE
							ICompilationUnit[] cus = fragment.getCompilationUnits();
							for(int j = 0; j < cus.length; ++j) {
								l.add(cus[j]);
							}
							Object[] objs = fragment.getNonJavaResources();
							for(int j = 0; j < objs.length; ++j) {
								l.add(objs[j]);
							}
						}
					}
				}
				return l.toArray();
			}
		}

		return NO_CHILDREN;
			}

	private boolean isSourceFolder(IFolder folder)
	{
		IJavaElement element = JavaCore.create(folder);
		if(element instanceof IPackageFragment || element instanceof IPackageFragmentRoot)
			return true;
		else
			return false;
	}

	protected Object[] getResources(IFolder folder)
	{
		try {
			IResource[] members = folder.members();
			IJavaProject javaProject = JavaCore.create(folder.getProject());
			if(javaProject == null || !javaProject.exists())
				return members;
			boolean isFolderOnClasspath = javaProject.isOnClasspath(folder);
			ArrayList<IResource> nonJavaResources = new ArrayList<IResource>();
			// Can be on classpath but as a member of non-java resource folder
			for(int i = 0; i < members.length; i++) {
				IResource member = members[i];
				// A resource can also be a java element
				// in the case of exclusion and inclusion filters.
				// We therefore exclude Java elements from the list
				// of non-Java resources.
				if(isFolderOnClasspath) {
					if(javaProject.findPackageFragmentRoot(member.getFullPath()) == null) {
						nonJavaResources.add(member);
					}
				}
				else if(!javaProject.isOnClasspath(member)) {
					nonJavaResources.add(member);
				}
			}
			return nonJavaResources.toArray();
		}
		catch(CoreException e) {
			return NO_CHILDREN;
		}
	}
}
