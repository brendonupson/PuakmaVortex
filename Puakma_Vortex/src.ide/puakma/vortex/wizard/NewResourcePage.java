/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 10, 2005
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
public class NewResourcePage extends AbstractWizardPage2
{
	private Text nameText;
	private Text fileText;
	private Text commentMemo;

	private ModifyListener listener = new ModifyListener() {
		public void modifyText(ModifyEvent e)
		{
			updateErrorMsg();
		}
	};

	private int type;

	protected NewResourcePage(Application application, int type)
	{
		super("newPagePage", application);

		this.type = type;

		setTitle(WizardMessages.NewResourcePage_Title);
		setDescription(WizardMessages.NewResourcePage_Description);
		setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
	}

	protected void createContent(DialogBuilder2 builder)
	{
		nameText = builder.createEditRow(WizardMessages.NewResourcePage_Field_Name);
		nameText.addModifyListener(listener);
		if(type == NewResourceWizard.TYPE_UPLOAD) {
			IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
			String path = store.getString(PreferenceConstants.PREF_NEW_WIZARD_PATH);
			String[][] filterExtsNames = {
					{ "*", "All files" }
			};
			fileText = builder.createFileSelectionRow(WizardMessages.NewResourcePage_Field_File,
					path, true, "Import File", filterExtsNames);
			fileText.addModifyListener(listener);
		}
		commentMemo = builder.createMemoRow(WizardMessages.NewResourcePage_Field_Comment, 6);
		commentMemo.addModifyListener(listener);

		setPageComplete(false);
		nameText.setFocus();
	}

	/**
	 * Updates error message after modifying some control.
	 */
	private void updateErrorMsg()
	{
		String errorMsg = checkError();
		setErrorMessage(errorMsg);
		setPageComplete(errorMsg == null);
		if(errorMsg == null) {
			IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
			store.setValue(PreferenceConstants.PREF_NEW_WIZARD_PATH, fileText.getText());
		}
	}

	/**
	 * Returns the most important error message. If there is no error in the dialog,
	 * returns null.
	 *
	 * @return String with error message or null when no error is found
	 */
	protected String checkError()
	{
		String str = super.checkError();
		if(str != null)
			return str;

		// CHECK THE DESIGN OBJECT
		String name = nameText.getText();
		if(name.length() == 0)
			return WizardMessages.NewResourcePage_Error_Name_Missing;
		Application app = getApplication();
		if(app.getDesignObject(name) != null)
			return WizardMessages.NewResourcePage_Error_Object_Alread_Exists;
		TornadoDatabaseConstraints consts = app.getServer().getTornadoDatabaseConstraints();
		int maxLen = consts.getMaxDObj_NameLen();
		if(name.length() > maxLen)
			return "Design object name cannot be longer than " + maxLen + " characters";
		// CHECK THE FILE
		if(type == NewResourceWizard.TYPE_UPLOAD) {
			File file = new File(fileText.getText());
			if(file.exists() == false)
				return WizardMessages.NewResourcePage_Error_File_Exists + file.toString() + WizardMessages.NewResourcePage_Error_File_Exists1;
			else if(file.isDirectory())
				return WizardMessages.NewResourcePage_Error_File_Directory + file.toString() + WizardMessages.NewResourcePage_Error_File_Directory1;
			else if(file.canRead() == false)
				return WizardMessages.NewResourcePage_Error_File_Read + file.toString() + WizardMessages.NewResourcePage_Error_File_Read1;
		}
		return null;
	}

	public String getName()
	{
		return nameText.getText();
	}

	public String getFile()
	{
		return fileText.getText();
	}

	public String getComment()
	{
		return commentMemo.getText();
	}
}
