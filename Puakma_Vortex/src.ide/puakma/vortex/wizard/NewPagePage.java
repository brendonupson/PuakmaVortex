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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.TornadoDatabaseConstraints;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;


/**
 * @author Martin Novak
 */
public class NewPagePage extends AbstractWizardPage2
{
	private Text nameText;
	private Text optionsText;
	private Text commentMemo;

	private ModifyListener listener = new ModifyListener() {
		public void modifyText(ModifyEvent e)
		{
			updateErrorMsg();
		}
	};

	protected NewPagePage()
	{
		super("newPagePage", null);

		init();
	}

	public NewPagePage(Application application)
	{
		super("newPagePage", application);

		init();
	}

	public void init()
	{
		setTitle(WizardMessages.NewPage_Title);
		setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
		setDescription(WizardMessages.NewPage_Description);
	}

	protected void createContent(DialogBuilder2 content)
	{
		nameText = content.createEditRow(WizardMessages.NewPage_Field_Name);
		nameText.addModifyListener(listener);
		optionsText = content.createEditRow(WizardMessages.NewPage_Field_Options);
		optionsText.addModifyListener(listener);
		commentMemo = content.createMemoRow(WizardMessages.NewPage_Field_Comment, 6);
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

		String name = nameText.getText();
		if(name.length() == 0)
			return WizardMessages.NewPage_Error_Need_Name;
		Application app = getApplication();
		if(app != null) {
			if(app.getDesignObject(name) != null)
				return "Design object with name " + name + " already exists in application";
		}
		
		TornadoDatabaseConstraints consts = app.getServer().getTornadoDatabaseConstraints();
	
		int maxLen = consts.getMaxDObj_NameLen();
		if(name.length() > maxLen)
			return "Design object name cannot be longer than " + maxLen + " characters";

		return null;
	}

	public String getName()
	{
		return nameText.getText();
	}

	public String getOptions()
	{
		return optionsText.getText();
	}

	public String getComment()
	{
		return commentMemo.getText();
	}
}
