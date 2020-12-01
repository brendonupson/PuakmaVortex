/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 18, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.application;

import org.eclipse.swt.widgets.Composite;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.vortex.IdeMessages;
import puakma.vortex.swt.MultiPageEditorPart2;


/**
 * @author Martin Novak
 */
public class ApplicationEditor extends MultiPageEditorPart2
{
	public static final int PAGE_MAIN = 0;
	public static final int PAGE_ROLES = 1;
	public static final int PAGE_KEYWORDS = 2;
	public static final int PAGE_DATABASES = 3;

	private Application connection;
	private AppPropertiesPage mainPropertiesArea;
	private RolesPage rolesPage;
	private KeywordsPage keywordsPage;
	private DatabaseConnectionPage databasePage;
	private SummaryPage summaryPage;
	public static final String EDITOR_ID = "puakma.vortex.appEditor"; //$NON-NLS-1$
	private int mainPropsIndex;
	private int secutiryIndex;
	private int keywordsIndex;
	private int databaseIndex;
	private int summaryIndex;
	private JavaPropertiesPage javaPropsPage;
	private int javaPropsIndex;

	protected void createPages()
	{
		ApplicationEditorInput input = (ApplicationEditorInput) getEditorInput();
		connection = input.getApplication();

		//    addPage(new TestPage(this.getContainer()));

		mainPropertiesArea = new AppPropertiesPage(getContainer(), this, connection);
		mainPropsIndex = addPage(mainPropertiesArea);
		setPageText(mainPropsIndex,IdeMessages.ApplicationEditor_MainPage_Title);

		javaPropsPage = new JavaPropertiesPage(getContainer(), this, connection);
		javaPropsIndex = addPage(javaPropsPage);
		setPageText(javaPropsIndex, "Java Properties");

		rolesPage = new RolesPage(this.getContainer(), this, input.getApplication());
		secutiryIndex = addPage(rolesPage);
		setPageText(secutiryIndex, IdeMessages.ApplicationEditor_SecPage_Title);

		keywordsPage = new KeywordsPage(this.getContainer(), this, input.getApplication());
		keywordsIndex = addPage(keywordsPage);
		setPageText(keywordsIndex, IdeMessages.ApplicationEditor_KwPage_Title); 

		databasePage = new DatabaseConnectionPage(this.getContainer(), this, input.getApplication());
		databaseIndex = addPage(databasePage);
		setPageText(databaseIndex, IdeMessages.ApplicationEditor_DbPage_Title); 

		summaryPage = new SummaryPage(this.getContainer(), this, input.getApplication());
		summaryIndex = addPage(summaryPage);
		setPageText(summaryIndex, "Design Element Summary");

		String grp = connection.getGroup() != null ? connection.getGroup() + '/' : "";
		setPartName(IdeMessages.ApplicationEditor_Title + grp + connection.getName());

		// OPEN PAGE...
		int openPage = input.getOpenPageIndex();
		if(openPage == PAGE_DATABASES) {
			selectDatabaseObject(input.getDatabaseObject());
		}
		else
			setActivePage(openPage);
	}

	/**
	 * Selects database object in the database page.
	 *
	 * @param object is the database object to select
	 */
	public void selectDatabaseObject(DatabaseConnection object)
	{
		setActivePage(PAGE_DATABASES);
		if(object != null)
			databasePage.selectDbObject(object);
	}

	public Composite getContainer()
	{
		return super.getContainer();
	}
}
