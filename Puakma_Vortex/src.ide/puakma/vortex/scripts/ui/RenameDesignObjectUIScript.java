/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    20/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.scripts.ui;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.PuakmaProject2;
import puakma.vortex.scripts.RenameDesignObjectScript;
import puakma.vortex.swt.InputDialog2;
import puakma.vortex.swt.InputDialogWithToogle;
import puakma.vortex.swt.KeyValueValidator;

/**
 * This script asks user if he wants to rename java object, if yes, it does it.
 * 
 * @author Martin Novak
 */
public class RenameDesignObjectUIScript extends RenameDesignObjectScript
{
	public RenameDesignObjectUIScript(DesignObject dobject)
	{
		super(dobject);
	}

	public void run()
	{
		Shell shell = Display.getDefault().getActiveShell();
		String type = "";
		DesignObject dob = getDesignObject();
		switch(dob.getDesignType()) {
		case DesignObject.TYPE_PAGE:
			type = "page";
			break;
		case DesignObject.TYPE_JAR_LIBRARY:
			type = "library";
			break;
		case DesignObject.TYPE_RESOURCE:
			type = "resource";
			break;
		case DesignObject.TYPE_DOCUMENTATION:
			type = "documentation";
			break;
		case DesignObject.TYPE_LIBRARY:
			type = "java object";
			break;
		case DesignObject.TYPE_ACTION:
			type = "action";
			break;
		case DesignObject.TYPE_SCHEDULEDACTION:
			type = "scheduled action";
			break;
		case DesignObject.TYPE_WIDGET:
			type = "SOAP widget";
			break;
		default:
			throw new IllegalStateException("Invalid java object design type: " + dob.getDesignType());
		}

		String title = "Rename " + type;
		String msg = "Type new name for " + type + ".";
		String textLabel = "Name";
		String initialValue = dob.getName();
		KeyValueValidator validator = new KeyValueValidator() {
			public String isValid(Map values) {
				return checkIsValid(values);
			}
		};
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		boolean enableToogle = getEnableToogleForRenameClass();
		InputDialog2 dlg;

		if(enableToogle) {
			boolean toogle = true;//store.getBoolean(PreferenceConstants.PREF_TOOGLE_RENAME_CLASS_WITH_ACTION);
			String toogleText = "Also rename class";
			dlg = new InputDialogWithToogle(shell, title, msg, textLabel, initialValue, validator, toogle, toogleText);
		}
		else {
			dlg = new InputDialog2(shell, title, msg, textLabel, initialValue, validator);
		}

		if(dlg.open() == Window.OK) {
			String newName = dlg.getValue();
			setRenameTo(newName);

			if(enableToogle) {
				boolean toogle = ((InputDialogWithToogle) dlg).getToogle();

				// SAVE TOOGLE STATE
				store.setValue(PreferenceConstants.PREF_TOOGLE_RENAME_CLASS_WITH_ACTION, toogle);

				setRefactorClass(toogle);
			}

			super.run();
		}
	}

	/**
	 * Checks if the input is valid.
	 */
	protected String checkIsValid(Map values)
	{
		setRenameTo((String) values.get(InputDialog2.KEY_INPUT));
		String error = getNewNameError();
		if(error != null)
			return error;

		Boolean toogle = (Boolean) values.get(InputDialogWithToogle.KEY_TOOGLE);
		if(toogle != null && toogle.booleanValue() == true) {
			setRefactorClass(true);
			error = getRefactorClassError();
			if(error != null)
				return error;
		}

		return null;
	}

	/**
	 * Returns true if we can include toogle button for renaming class. This can
	 * be visible only if the design object is java object, and also only if java
	 * has been started for the design object's project.
	 */
	private boolean getEnableToogleForRenameClass()
	{
		DesignObject dob = getDesignObject();
		PuakmaProject2 project = ProjectManager.getProject(dob.getApplication());
		if(project.javaStarted() == false)
			return false;

		if(dob instanceof JavaObject == false)
			return false;

		// FOR JAVA OBJECT ALWAYS ALLOW TO RENAME CLASS
		return true;
	}
}
