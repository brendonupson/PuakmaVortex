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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationEvent;
import puakma.coreide.objects2.ApplicationListener;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.ObjectChangeEvent;
import puakma.coreide.objects2.Parameters;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.controls.PuakmaParametersEditor;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.MultiPageEditorPart2;
import puakma.vortex.swt.NewLFEditorPage;
import puakma.vortex.swt.SWTUtil;


/**
 * @author Martin Novak
 */
public class AppPropertiesPage extends NewLFEditorPage
{
	private MultiPageEditorPart2 editor;
	private Application application;
	private Parameters parameters;
	private PuakmaParametersEditor propsEditor;
	boolean propsDirty;

	private Text commentText;
	private Text inheritFrom;
	private Text template;
	private Text defaultOpen;
	private Text loginPage;

	private Combo openActionCombo;
	private Combo openAction1Combo;
	private Combo saveActionCombo;
	private Combo saveAction1Combo;
	private Combo defaultCharset;

	private InternalApplicationListener appListener = new InternalApplicationListener();
	private InternalPropsChangeListener propsListener = new InternalPropsChangeListener();
	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			handleApplicationChangeUIEvent();
		}
	};
	//  private List actions = new ArrayList();
	private boolean disableNotification = false;

	class InternalApplicationListener implements ApplicationListener {
		public void update(final ApplicationEvent event)
		{
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					updatePage();
				}
			});
		}
		public void objectChange(final ObjectChangeEvent event)
		{
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					switch(event.getEventType()) {
					case ObjectChangeEvent.EV_ADD_APP_OBJECT:
						addAppObject(event.getObject());
						break;
					case ObjectChangeEvent.EV_CHANGE:
						if(event.isRenamed())
							renameAppObject(event.getObject(), event.getOldName());
						break;
					case ObjectChangeEvent.EV_REMOVE:
						delAppObject(event.getObject());
						break;
					}
				}
			});
		}
		public void disconnect(Application application) {  }
	}

	/**
	 * This class is used for listening changes in the internal copy of properties.
	 *
	 * @author Martin Novak
	 */
	 class InternalPropsChangeListener implements ModifyListener, SelectionListener
	 {
		 public void modifyText(ModifyEvent e) {
			 handlePropsChange();
		 }
		 public void widgetSelected(SelectionEvent e) {
			 handlePropsChange();
		 }
		 public void widgetDefaultSelected(SelectionEvent e) {
			 handlePropsChange();
		 }
	 }


	 public AppPropertiesPage(Composite parent, MultiPageEditorPart2 editor, final Application application)
	 {
		 super(parent, editor);

		 // setup some class fields
		 this.editor = editor;
		 this.application = application;

		 // create listeners
		 propsListener = new InternalPropsChangeListener();

		 // setup properties
		 parameters = application.makeWorkingCopy();

		 DialogBuilder2 builder = new DialogBuilder2(this);
		 builder.createFormsLFComposite("Application Properties",  false, 1);

		 // create all the controls
		 builder.createSection("Application Group and Name", null, 4);
		 builder.setEffectiveColumns(2);
		 builder.createTwoLabelRow("Group", application.getGroup());
		 builder.createTwoLabelRow("Name", application.getName());
		 builder.closeSection();

		 builder.createSection("General Properties", null, 4);
		 commentText = builder.createMemoRow("Comment", 4);
		 commentText.addModifyListener(this.modifyListener);

		 builder.setEffectiveColumns(2);
		 inheritFrom = builder.createEditRow("Inherit from");
		 inheritFrom.addModifyListener(this.modifyListener);
		 template = builder.createEditRow("Template");
		 template.addModifyListener(this.modifyListener);

		 defaultOpen = builder.createEditRow("Default URL");
		 defaultOpen.addModifyListener(this.propsListener);
		 loginPage = builder.createEditRow("Login Page");
		 loginPage.addModifyListener(this.propsListener);
		 builder.closeSection();

		 builder.createSection("Actions and Charsets", null, 4);

		 openActionCombo = createHyperlinkCombo("Open Action 1", builder);
		 openAction1Combo = createHyperlinkCombo("Open Action 2", builder);
		 saveActionCombo = createHyperlinkCombo("Save Action 1", builder);
		 saveAction1Combo = createHyperlinkCombo("Save Action 2", builder);

		 defaultCharset = builder.createComboRow("Character Set", false);
		 defaultCharset.addModifyListener(this.propsListener);

		 builder.createLabelRow("");

		 propsEditor = new PuakmaParametersEditor(builder.getCurrentComposite(), builder
				 .getFormToolkit(), parameters, application, 4);
		 propsEditor.setDirtyListener(new PuakmaParametersEditor.DirtyListener() {
			 public void dirtyChanged() {
				 updateDirty();
			 }
		 });

		 propsDirty = false; setDirty(false);
		 application.addListener(appListener);
		 setDirty(false);
		 updatePage();
		 propsDirty = false; setDirty(false);
	 }

	 private Combo createHyperlinkCombo(String hlLabel, DialogBuilder2 builder)
	 {
		 Hyperlink hl = builder.appendHyperlink(hlLabel, false);
		 final Combo combo = builder.appendCombo(true);
		 combo.add("");
		 combo.select(0);
		 combo.addModifyListener(propsListener);
		 hl.addHyperlinkListener(new HyperlinkAdapter() {
			 public void linkActivated(HyperlinkEvent e) {
				 DesignObject dob = application.getDesignObject(combo.getText());
				 if(dob != null)
					 WorkbenchUtils.openDesignObject(dob);
			 }
		 });
		 return combo;
	 }

	 /**
	  * This function updates the dialog
	  */
	 void updatePage()
	 {
		 // UPDATE ONLY IF NOTHING HAS CHANGED
		 if(super.isDirty() == false) {
			 commentText.setText(StringUtil.safeString(application.getDescription()));
			 inheritFrom.setText(StringUtil.safeString(application.getInheritFrom()));
			 template.setText(StringUtil.safeString(application.getTemplateName()));
		 }

		 if(propsDirty == false) {
			 String value = parameters.getParameterValue(Parameters.PARAM_DEFAULT_OPEN);
			 defaultOpen.setText(StringUtil.safeString(value));
			 value = parameters.getParameterValue(Parameters.PARAM_LOGIN_PAGE);
			 loginPage.setText(StringUtil.safeString(value));
			 value = parameters.getParameterValue(Parameters.PARAM_DEFAULT_CHARSET);
			 defaultCharset.setText(StringUtil.safeString(value));

			 value = parameters.getParameterValue(Parameters.PARAM_OPEN_ACTION);
			 int index = -1;
			 index = getIndex(value, openActionCombo.getItems());
			 openActionCombo.select(index);
			 value = parameters.getParameterValue(Parameters.PARAM_OPEN_ACTION_1);
			 index = getIndex(value, openAction1Combo.getItems());
			 openAction1Combo.select(index);

			 value = parameters.getParameterValue(Parameters.PARAM_SAVE_ACTION);
			 index = getIndex(value, saveActionCombo.getItems());
			 saveActionCombo.select(index);
			 value = parameters.getParameterValue(Parameters.PARAM_SAVE_ACTION_1);
			 index = getIndex(value, saveAction1Combo.getItems());
			 saveAction1Combo.select(index);
		 }
	 }

	 /**
	  * Finds case sensitively string in the array, and returns index of that string
	  * in the array.
	  *
	  * @param str
	  * @param array
	  * @return index of the item in array
	  */
	 private int getIndex(String str, String[] array)
	 {
		 for(int i = 0; i < array.length; ++i) {
			 if(array[i].equals(str))
				 return i;
		 }

		 return -1;
	 }

	 void addAppObject(ApplicationObject object)
	 {
		 if(object instanceof JavaObject
				 && ((JavaObject)object).getDesignType() == DesignObject.TYPE_ACTION) {
			 JavaObject jo = (JavaObject) object;

			 addCombo(openActionCombo, jo, Parameters.PARAM_OPEN_ACTION);
			 addCombo(openAction1Combo, jo, Parameters.PARAM_OPEN_ACTION_1);
			 addCombo(saveActionCombo, jo, Parameters.PARAM_SAVE_ACTION);
			 addCombo(saveAction1Combo, jo, Parameters.PARAM_SAVE_ACTION_1);
		 }
	 }

	 void delAppObject(ApplicationObject object)
	 {
		 if(object instanceof JavaObject) {
			 JavaObject ao = (JavaObject) object;

			 openActionCombo.remove(ao.getName());
			 openAction1Combo.remove(ao.getName());
			 saveActionCombo.remove(ao.getName());
			 saveAction1Combo.remove(ao.getName());
		 }
	 }

	 void renameAppObject(ApplicationObject object, String oldName)
	 {
		 if(object instanceof JavaObject
				 && ((JavaObject)object).getDesignType() == DesignObject.TYPE_ACTION) {
			 JavaObject ao = (JavaObject) object;

			 int index = openActionCombo.indexOf(oldName);
			 if(index != -1) {
				 openActionCombo.setItem(index, ao.getName());
				 SWTUtil.sortCombo(openActionCombo);
			 }
			 index = -1;
			 index = openAction1Combo.indexOf(oldName);
			 if(index != -1)
				 openAction1Combo.setItem(index,ao.getName());
			 index = -1;

			 index = saveActionCombo.indexOf(oldName);
			 if(index != -1)
				 saveActionCombo.setItem(index,ao.getName());
			 index = -1;
			 index = saveAction1Combo.indexOf(oldName);
			 if(index != -1)
				 saveAction1Combo.setItem(index,ao.getName());
			 index = -1;

			 //      propsDirty = true;
			 //      editor.updateDirty();
		 }
	 }

	 /**
	  * Checks if something has been changed in the editor.
	  *
	  * @return true if something has been changed, and should be saved, otherwise
	  * false
	  */
	 public boolean isDirty()
	 {
		 return super.isDirty() || isPropsDirty();
	 }

	 private boolean isPropsDirty()
	 {
		 return propsDirty || propsEditor.isDirty();
	 }

	 private void setPropsDirty(boolean dirty)
	 {
		 propsDirty = dirty;
		 if(dirty == false)
			 propsEditor.setDirty(dirty);
	 }

	 /**
	  * Updates dirty status of the whole editor.
	  */
	 private void updateDirty()
	 {
		 editor.updateDirty();
	 }

	 /**
	  * Saves the changes in the editor.
	  *
	  * @param monitor is the monitor monitoring progress of saving
	  */
	 public void doSave(IProgressMonitor monitor)
	 {
		 //  UPDATE APPLICATION
		 try {
			 if(super.isDirty()) {
				 Application wCopy = application.makeWorkingCopy();
				 wCopy.setDescription(commentText.getText());
				 wCopy.setInheritFrom(inheritFrom.getText());
				 wCopy.setTemplateName(template.getText());
				 wCopy.commit();

				 setDirty(false);
				 editor.updateDirty();
			 }
		 }
		 catch(Exception e) {
			 Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			 MessageDialog.openError(shell, "Error saving application properties",
					 "Error occured during saving application properties.\nReason:\n" + e.getLocalizedMessage());
			 VortexPlugin.log(e);
		 }

		 // AND ALSO UPDATE PARAMETERS
		 try {
			 if(isPropsDirty()) {
				 parameters.setParameter(Parameters.PARAM_OPEN_ACTION, openActionCombo.getText());
				 parameters.setParameter(Parameters.PARAM_OPEN_ACTION_1, openAction1Combo.getText());
				 parameters.setParameter(Parameters.PARAM_SAVE_ACTION, saveActionCombo.getText());
				 parameters.setParameter(Parameters.PARAM_SAVE_ACTION_1, saveAction1Combo.getText());

				 parameters.setParameter(Parameters.PARAM_DEFAULT_CHARSET, defaultCharset.getText());
				 parameters.setParameter(Parameters.PARAM_DEFAULT_OPEN, defaultOpen.getText());
				 parameters.setParameter(Parameters.PARAM_LOGIN_PAGE, loginPage.getText());
				 parameters.commitParams();

				 setPropsDirty(false);
				 editor.updateDirty();
			 }
		 }
		 catch(PuakmaCoreException e) {
			 Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			 MessageDialog.openError(shell, "Error saving application properties",
					 "Error occured during saving application properties.\nReason:\n" + e.getLocalizedMessage());
			 VortexPlugin.log(e);
		 }
	 }

	 /**
	  * Handles all changes of application parameters.
	  */
	 private void handlePropsChange()
	 {
		 if(disableNotification == false) {
			 propsDirty = true;
			 editor.updateDirty();
		 }
	 }

	 private void handleApplicationChangeUIEvent()
	 {
		 if(disableNotification == false) {
			 setDirty(true);
			 editor.updateDirty();
		 }
	 }

	 public void addCombo(Combo combo, DesignObject action, String paramName)
	 {
		 if(action.getDesignType() == DesignObject.TYPE_ACTION) {
			 String currentValue = application.getParameterValue(paramName);

			 String[] vals = combo.getItems();
			 int newPos = Arrays.binarySearch(vals, action.getName(), new Comparator<Object>() {
				 public int compare(Object o1, Object o2)
				 {
					 String s1 = (String) o1, s2 = (String) o2;
					 return s1.compareToIgnoreCase(s2);
				 }
			 });
			 if(newPos < 0)
				 newPos = - (newPos+1);

			 disableNotification  = true;


			 combo.add(action.getName(), newPos);
			 if(currentValue != null && currentValue.length() > 0) {
				 vals = combo.getItems();
				 for(int i = 0; i < vals.length; ++i) {
					 if(vals[i].equals(currentValue)) {
						 combo.select(i);
						 break;
					 }
				 }
			 }

			 disableNotification = false;
		 }
	 }

	 public void disposePage()
	 {
		 application.removeListener(appListener);
		 propsEditor.dispose();    
	 }
}
