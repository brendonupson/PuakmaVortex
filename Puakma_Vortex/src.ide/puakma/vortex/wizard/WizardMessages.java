/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 10, 2005
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

import org.eclipse.osgi.util.NLS;

public class WizardMessages extends NLS
{
	static {
		NLS.initializeMessages("puakma.vortex.wizard.messages", WizardMessages.class);
	}

	public static String NewPage_Title;
	public static String NewPage_Description;
	public static String NewPage_Field_Name;
	public static String NewPage_Field_Options;
	public static String NewPage_Field_Comment;
	public static String NewPage_Error_Need_Name;
	public static String NewPageWizard_Title;
	public static String NewPageWizard_Error_Open_Title;
	public static String NewPageWizard_Error_Open_Message;
	public static String NewPageWizard_Error_Open_Message1;
	public static String NewPageWizard_Error_Create_Title;
	public static String NewPageWizard_Error_Create_Message;
	public static String NewResourcePage_Title;
	public static String NewResourcePage_Description;
	public static String NewResourcePage_Field_Name;
	public static String NewResourcePage_Field_File;
	public static String NewResourcePage_Field_Comment;
	public static String NewResourcePage_Error_Name_Missing;
	public static String NewResourcePage_Error_Object_Alread_Exists;
	public static String NewResourcePage_Error_File_Exists;
	public static String NewResourcePage_Error_File_Exists1;
	public static String NewResourcePage_Error_File_Directory;
	public static String NewResourcePage_Error_File_Directory1;
	public static String NewResourcePage_Error_File_Read;
	public static String NewResourcePage_Error_File_Read1;
	public static String NewResourceWizard_Run_Monitor_Add_Object;
	public static String NewResourceWizard_ERROR_OPEN_TITLE;
	public static String NewResourceWizard_ERROR_OPEN_MESSAGE;
	public static String NewResourceWizard_Invalid_Resource_Wizard_Type;
}
