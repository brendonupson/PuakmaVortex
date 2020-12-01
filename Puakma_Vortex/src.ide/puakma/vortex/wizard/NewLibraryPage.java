/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 15, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.wizard;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.TornadoDatabaseConstraints;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.swt.DialogBuilder2;

/**
 * @author Martin Novak
 */
public class NewLibraryPage extends AbstractWizardPage2
{
	private Text libName;
	private Text fileText;


	private InternalUIListener uiListener = new InternalUIListener();

	class InternalUIListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			handleChange();
		}
	}

	public NewLibraryPage(Application connection)
	{
		super("addClassPage", connection);

		setTitle("Add new library");
		setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
		setDescription("Create a new library on the server.");
	}

	protected void createContent(DialogBuilder2 builder)
	{
		libName = builder.createEditRow("Library name");
		libName.addModifyListener(uiListener);

		String[][] filterExtsNames = {
				{ "Java archive", "jar" },
				{ "All files", "*" }
		};
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		String path = store.getString(PreferenceConstants.PREF_NEW_WIZARD_PATH);
		fileText = builder.createFileSelectionRow("Jar file to add", path, true,
				"Select Jar Archive", filterExtsNames);
		fileText.addModifyListener(uiListener);

		setPageComplete(false);
	}

	/**
	 * Handles UI change.
	 */
	private void handleChange()
	{
		String str = checkUiForError();
		setErrorMessage(str);
		if(str == null)
			setPageComplete(true);
		else
			setPageComplete(false);
	}

	/**
	 * Checks user interface for errors.
	 * 
	 * @return if there is some error, returns string with the first error,
	 * otherwise returns null
	 */
	private String checkUiForError()
	{
		String str = super.checkError();
		if(str != null)
			return str;

		// check the library name
		String name = libName.getText();
		if(name.length() == 0)
			return "Library name is required parameter";
		TornadoDatabaseConstraints consts = getApplication().getServer().getTornadoDatabaseConstraints();
		if(name.length() > consts.getMaxApplication_AppNameLen())
			return "Name of the application cannot be longer than "
			+ consts.getMaxApplication_AppNameLen()
			+ " which is the maximum on the server";

		if(getApplication().getDesignObject(name) != null)
			return "Design object with name \"" + name + "\" already exists on the server.";

		// check the jar file
		String fileName = fileText.getText();
		if(fileName.length() == 0)
			return "You have to choose file to upload";
		File file = new File(fileName);
		if(file.exists() == false)
			return "File doesn't exists";
		if(file.isFile() == false)
			return "Not a valid file";
		IPath path = new Path(file.toString());
		if(path.getFileExtension().equals("jar") == false)
			return "Not a java library. Java libraries have to have extension jar.";
		return null;
	}

	/**
	 * Returns the library file name.
	 *
	 * @return IPath object containing the file name.
	 */
	public IPath getFile()
	{
		String fileName = fileText.getText();
		IPath path = new Path(fileName);
		return path;
	}

	/**
	 * Return proposed name of the library.
	 *
	 * @return String containing name of the new library
	 */
	public String getName()
	{
		return libName.getText();
	}
}
