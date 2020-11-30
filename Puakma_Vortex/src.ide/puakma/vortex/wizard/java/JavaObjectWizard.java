/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 14, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.wizard.java;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.PuakmaProject2;
import puakma.vortex.wizard.AbstractWizard;


/**
 * TODO: NEEDS REDONE
 * @author Martin Novak
 */
public abstract class JavaObjectWizard extends AbstractWizard
{

	public static final int TYPE_CLASS = NewTypeWizardPage.CLASS_TYPE;
	public static final int TYPE_INTERFACE = NewTypeWizardPage.INTERFACE_TYPE;
	public static final int TYPE_ENUM = NewTypeWizardPage.ENUM_TYPE;
	// TODO: add support for annotations
	public static final int TYPE_ANOTATION = NewTypeWizardPage.ANNOTATION_TYPE;

	private int javaType;
	private int designType;

	private ClazzPage clazzPage;
	private JavaObjectPropsPage propsPage;

	private Application application;
	private IPackageFragment packageFragment;

	/**
	 * This class performs new class creation.
	 *
	 * @author Martin Novak
	 */
	private class InternalRunnable implements IRunnableWithProgress
	{
		String name;
		String comment = "";
		String className;
		String packageName;
		String superClass;

		public InternalRunnable()
		{
			if(designType != DesignObject.TYPE_LIBRARY) {
				name = propsPage.getName();
				comment = propsPage.getComment();
			}
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
		{
			monitor.beginTask("Creating java object", 3);

			try {
				PuakmaProject2 project = ProjectManager.getProject(application);
				if(project.javaStarted() == false) {
					project.startJava(monitor);
				}

				IPackageFragment fragment = clazzPage.getPackageFragment();
				if(fragment == null)
					packageName = "";
				else
					packageName = fragment.getElementName();
				className = clazzPage.getTypeName();
				superClass = clazzPage.getSuperClass();

				monitor.setTaskName("Create internal object");

				// STEP 1 - CREATE OBJECT ON THE SERVER
				if(designType == DesignObject.TYPE_LIBRARY)
					name = className;
				final JavaObject jobj = ObjectsFactory.createJavaObject(packageName,className, designType);

				monitor.worked(2);
				monitor.setTaskName("Creating object on the server");

				jobj.setName(name);
				jobj.setDescription(comment);
				jobj.setClassName(className);
				jobj.setPackage(packageName);

				application.addObject(jobj);
				String typeName = WorkbenchUtils.getObjectTypeName(jobj);
				VortexPlugin.warning("CREATE OBJECT " + typeName
						+ " (" + jobj.getId() + ":" + jobj.getFullyQualifiedName() + ")");

				// STEP 2 - CREATE .JAVA FILE ON DISC
				clazzPage.prefillDialog();
				clazzPage.createType(monitor);
				IType type = clazzPage.getCreatedType();

				// STEP 3 - UPLOAD THE .JAVA FILE
				//        boolean isSource = true;
				//        ProjectUtils.uploadFile(jobj, isSource);

				final Display display = Display.getDefault();
				display.asyncExec(new Runnable() {
					public void run() {
						WorkbenchUtils.openDesignObject(jobj);
					}
				});

				monitor.worked(1);
			}
			catch(Exception e) {
				VortexPlugin.log(e);
				throw new InvocationTargetException(e);
			}
			finally {
				monitor.done();
			}
		}
	}

	public JavaObjectWizard(Application application, int designType, int javaType)
	{
		assert ObjectsFactory.isValidDesignObjectType(designType) : "Invalid design object type: " + designType;
		assert javaType == TYPE_CLASS || javaType == TYPE_INTERFACE || javaType == TYPE_ENUM : "Invalid java type. It has to be either class, interface or enumeration";

		if((javaType == TYPE_INTERFACE || javaType == TYPE_ENUM) && designType != DesignObject.TYPE_LIBRARY)
			throw new IllegalArgumentException("Interfaces and enumeration are allowed only with simple classes");

		this.application = application;
		this.designType = designType;
		this.javaType = javaType;

		setWindowTitle("New " + generateObjectName(designType, javaType));
		setNeedsProgressMonitor(true);
	}

	/**
	 * This function generates the name of the object we are trying to produce. If we want to
	 * create action, it returns "Action", etc...
	 *
	 * @param designType is the design object type
	 * @param javaType is the java type - enumeration, class, etc...
	 * @return type of thing we are trying to do
	 */
	public static String generateObjectName(int designType, int javaType)
	{
		switch(designType) {
		case DesignObject.TYPE_ACTION:
			return "Action";
		case DesignObject.TYPE_LIBRARY:
			if(javaType == TYPE_INTERFACE)
				return "Interface";
			else if(javaType == TYPE_ENUM)
				return "Enum";
			else
				return "Class";
		case DesignObject.TYPE_SCHEDULEDACTION:
			return "Scheduled Action";
		case DesignObject.TYPE_WIDGET:
			return "SOAP Widget";
		default:
			throw new IllegalArgumentException("Invalid type and java object");
		}
	}

	public void addPages()
	{
		if(designType != DesignObject.TYPE_LIBRARY) {
			propsPage = new JavaObjectPropsPage(application, designType, javaType, "props");
			addPage(propsPage);
		}
		else {
			clazzPage = new ClazzPage(application, designType, javaType, "clazz");
			addPage(clazzPage);
		}
	}

	public boolean performFinish()
	{
		if(application == null) {
			if(propsPage != null)
				application = propsPage.getApplication();
			else
				application = clazzPage.getApplication();
		}

		InternalRunnable runnable = new InternalRunnable();

		try {
			getContainer().run(true, false, runnable);
			return true;
		}
		catch(InvocationTargetException e) {
			e.printStackTrace();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public JavaObjectPropsPage getMainPage()
	{
		return propsPage;
	}

	public ClazzPage getClassPage()
	{
		return clazzPage;
	}

	public void setupClazzPage(ClazzPage clazzPage)
	{
		this.clazzPage = clazzPage;
	}

	public void setPackageFragment(IPackageFragment fragment)
	{
		this.packageFragment = fragment;
	}

	public IPackageFragment getInitialPackageFragment()
	{
		return packageFragment;
	}
}
