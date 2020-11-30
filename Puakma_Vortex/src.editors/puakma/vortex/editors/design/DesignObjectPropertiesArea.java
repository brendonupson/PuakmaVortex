/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 13, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.design;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationEvent;
import puakma.coreide.objects2.ApplicationListener;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ObjectChangeEvent;
import puakma.coreide.objects2.Parameters;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.controls.PuakmaParametersEditor;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.MultiPageEditorPart2;
import puakma.vortex.swt.NewLFEditorPage;

/**
 * @author Martin Novak
 */
public class DesignObjectPropertiesArea extends NewLFEditorPage
{
	Text name;
	Text comment;
	Text inheritFrom;
	Text options;
	Combo contentType;
	Combo openActionCombo;
	Combo saveActionCombo;
	private Combo parentPageCombo;
	PuakmaParametersEditor propsEditor;

	private boolean disableNotification = false;

	private Parameters parameters;
	private boolean isDirty;
	private MultiPageEditorPart2 editor;
	private IProgressMonitor monitor;

	private InternalApplicationListener appListener;
	private InternalUIListener uiModListener = new InternalUIListener();

	private InternalPropsChangeListener propsListener = new InternalPropsChangeListener();
	private boolean propsDirty = false;
	private DesignObject object;

	/**
	 * This class handles all user interface changes in the whole page.
	 * 
	 * @author Martin Novak
	 */
	class InternalUIListener implements ModifyListener, SelectionListener {
		public void modifyText(ModifyEvent e) {
			handleControlChange();
		}
		public void widgetSelected(SelectionEvent e) {
			handleControlChange();
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			handleControlChange();
		}
	}

	class InternalApplicationListener implements ApplicationListener {
		public void update(ApplicationEvent event)
		{
		}
		public void objectChange(final ObjectChangeEvent event)
		{
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if(event.getObject() instanceof DesignObject == false)
						return;
					DesignObject object = (DesignObject) event.getObject();
					switch(event.getEventType()) {
					case ObjectChangeEvent.EV_ADD_APP_OBJECT:
						handleAddDesignObject(object);
						break;
					case ObjectChangeEvent.EV_CHANGE:
						if(event.isRenamed())
							handlerRenameDesignObject(object, event.getOldName());
						break;
					case ObjectChangeEvent.EV_REMOVE:
						handlerRemoveDesignObject(object);
						break;
					}
				}
			});
		}
		public void disconnect(Application application)
		{

		}
	}

	/**
	 * This class is used for listening changes in the internal copy of properties.
	 *
	 * @author Martin Novak
	 */
	 class InternalPropsChangeListener implements ModifyListener, SelectionListener
	 {
		 //    public void propertiesAdd(ObjectPropertyEvent event) {
		 //      handlePropsChange();
		 //    }
		 //    public void propertiesRemove(ObjectPropertyEvent event) {
		 //      handlePropsChange();
		 //    }
		 //    public void propertiesChange(ObjectPropertyEvent event) {
		 //      handlePropsChange();
		 //    }
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

	 public DesignObjectPropertiesArea(Composite parent, MultiPageEditorPart2 editor, final DesignObject object)
	 {
		 super(parent, editor);

		 this.editor = editor;
		 this.object = object;

		 // create listeners
		 appListener = new InternalApplicationListener();

		 parameters = object.makeWorkingCopy();

		 DialogBuilder2 builder = new DialogBuilder2(this);

		 builder.createFormsLFComposite("Design Object Properties", false, 1);

		 createGeneralSection(builder);

		 createOptionsSection(builder);

		 createParamsSection(builder);

		 builder.closeComposite();
		 builder.finishBuilder();

		 isDirty = propsDirty = false;
		 editor.updateDirty();
		 doUpdate(object);
	 }

	 private void createGeneralSection(DialogBuilder2 builder)
	 {
		 builder.createSection("General", null, 4);
		 name = builder.createEditRow("Name");
		 name.addModifyListener(uiModListener);
		 builder.createLabelRow("");
		 builder.closeSection();
	 }

	 private void createOptionsSection(DialogBuilder2 builder)
	 {
		 builder.createSection("Options", null, 4);

		 options = builder.createEditRow("Options");
		 options.addModifyListener(uiModListener);
		 contentType = builder.createComboRow("Content Type", false);
		 contentType.addModifyListener(uiModListener);

		 inheritFrom = builder.createEditRow("Inherit From");
		 inheritFrom.addModifyListener(uiModListener);
		 builder.createLabelRow("");

		 comment = builder.createMemoRow("Comment", 4);
		 comment.addModifyListener(uiModListener);
		 GridData gd = (GridData) comment.getLayoutData();
		 gd.horizontalSpan = 3;

		 builder.closeSection();
	 }

	 private void createParamsSection(DialogBuilder2 builder)
	 {
		 builder.createSection("Params",
				 "Set design object parameters here. Some of them are predefined, but you can also use your own.",
				 4);
		 if(object.getDesignType() == DesignObject.TYPE_PAGE) {
			 openActionCombo = createHyperlinkCombo("Open Action", builder);
			 saveActionCombo = createHyperlinkCombo("Save Action", builder);
			 parentPageCombo = createHyperlinkCombo("Parent Page", builder);

			 builder.createLabelRow("");
		 }

		 object.getApplication().addListener(appListener);

		 propsEditor = new PuakmaParametersEditor(builder.getCurrentComposite(), builder.getFormToolkit(), parameters, object, 4);
		 propsEditor.setDirtyListener(new PuakmaParametersEditor.DirtyListener() {
			 public void dirtyChanged()
			 {
				 updateDirty();
			 }
		 });
		 builder.closeSection();
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
				 DesignObject dob = object.getApplication().getDesignObject(combo.getText());
				 if(dob != null)
					 WorkbenchUtils.openDesignObject(dob);
			 }
		 });
		 return combo;
	 }

	 public void doSave(IProgressMonitor monitor)
	 {
		 doSave1(monitor);
	 }

	 public boolean doSave1(IProgressMonitor monitor)
	 {
		 // compute amount of work
		 int totalWork = 0;
		 if(isDirty) totalWork += 1;
		 if(propsDirty) totalWork += 1;
		 monitor.beginTask("Updating design object properties", totalWork);

		 if(isDirty) {
			 try {
				 DesignObject obj = object.makeWorkingCopy();
				 obj.setName(name.getText());
				 obj.setDescription(comment.getText());
				 obj.setInheritFrom(inheritFrom.getText());
				 obj.setOptions(options.getText());
				 obj.setContentType(contentType.getText());
				 obj.commit();
				 isDirty = false;
			 }
			 catch(Exception e) {
				 VortexPlugin.log(e);
			 }
			 finally {
				 monitor.worked(1);
			 }
		 }

		 if(isPropsDirty()) {
			 try {
				 if(openActionCombo != null)
					 parameters.setParameter(Parameters.PARAM_OPEN_ACTION, openActionCombo.getText());
				 if(saveActionCombo != null)
					 parameters.setParameter(Parameters.PARAM_SAVE_ACTION, saveActionCombo.getText());
				 if(parentPageCombo != null)
					 parameters.setParameter(Parameters.PARAM_PARENT_PAGE, parentPageCombo.getText());

				 parameters.commitParams();
				 setPropsDirty(false);
			 }
			 catch(PuakmaCoreException e) {
				 VortexPlugin.log(e);
			 }
			 finally {
				 monitor.worked(1);
			 }
		 }
		 updateDirty();
		 monitor.done();
		 return true;
	 }

	 /**
	  * Returns dirty status - true if something changed in the dialog, false if
	  * the page is without any changes.
	  *
	  * @return dirty status
	  */
	 public boolean isDirty()
	 {
		 return isDirty || isPropsDirty();
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

	 // TODO: this is never called, so handle update somehow!!!
	 void doUpdate(DesignObject obj)
	 {
		 // ENABLE UPDATING ONLY IF NOTHING HAS CHANGED THERE
		 if(isDirty == false) {
			 disableNotification = true;
			 name.setText(StringUtil.safeString(obj.getName()));
			 comment.setText(StringUtil.safeString(obj.getDescription()));
			 inheritFrom.setText(StringUtil.safeString(obj.getInheritFrom()));
			 options.setText(StringUtil.safeString(obj.getOptions()));
			 contentType.setText(StringUtil.safeString(obj.getContentType()));

			 disableNotification = false;

			 isDirty = false;
			 editor.updateDirty();
		 }

		 if(isPropsDirty()) {
			 setComboText(openActionCombo, Parameters.PARAM_OPEN_ACTION);
			 setComboText(saveActionCombo, Parameters.PARAM_SAVE_ACTION);
			 setComboText(parentPageCombo, Parameters.PARAM_PARENT_PAGE);

			 propsDirty = false;
			 editor.updateDirty();
		 }
	 }

	 /**
	  * Sets combo box text from the current properties.
	  *
	  * @param combo is the Combo box on which will be changed text
	  * @param paramName is the parameter name to set
	  */
	 private void setComboText(Combo combo, String paramName)
	 {
		 if(combo != null) {
			 String value = parameters.getParameterValue(paramName);
			 int index = -1;
			 index = getIndex(value, combo.getItems());
			 combo.select(index);
		 }
	 }

	 /**
	  * Handles all changes in internal components - updates dirty status in UI.
	  */
	 private void handleControlChange()
	 {
		 if(disableNotification == false) {
			 isDirty = true;
			 editor.updateDirty();
		 }
	 }

	 /**
	  * Handles changes of properties on the page.
	  */
	 private void handlePropsChange()
	 {
		 if(disableNotification == false) {
			 propsDirty = true;
			 editor.updateDirty();
		 }
	 }

	 public void handleAddDesignObject(DesignObject object)
	 {
		 if(openActionCombo != null && object.getDesignType() == DesignObject.TYPE_ACTION)
			 addCombo(openActionCombo, object, Parameters.PARAM_OPEN_ACTION);
		 if(saveActionCombo != null && object.getDesignType() == DesignObject.TYPE_ACTION)
			 addCombo(saveActionCombo, object, Parameters.PARAM_SAVE_ACTION);
		 if(parentPageCombo != null && object.getDesignType() == DesignObject.TYPE_PAGE)
			 addCombo(parentPageCombo, object, Parameters.PARAM_PARENT_PAGE);
		 //    boolean tmpDirty = isDirty;
		 //    if(object.getDesignType() == DesignObject.TYPE_ACTION) {
		 //      if(openActionCombo != null) {
		 //        openActionCombo.add(object.getName());
		 //        Object val = this.object.getParameterValue(Parameters.PARAM_OPEN_ACTION);
		 //        if(val != null && val.equals(object.getName()))
		 //          openActionCombo.select(openActionCombo.getItemCount());
		 //        SWTUtil.sortCombo(openActionCombo);
		 //        isDirty = tmpDirty;
		 //      }
		 //      if(saveActionCombo != null) {
		 //        saveActionCombo.add(object.getName());
		 //        Object val = this.object.getParameterValue(Parameters.PARAM_SAVE_ACTION);
		 //        if(val != null && val.equals(object.getName()))
		 //          saveActionCombo.select(saveActionCombo.getItemCount());
		 //        SWTUtil.sortCombo(saveActionCombo);
		 //        isDirty = tmpDirty;
		 //      }
		 //    }
	 }

	 public void addCombo(Combo combo, DesignObject action, String paramName)
	 {
		 String currentValue = object.getParameterValue(paramName);

		 String[] vals = combo.getItems();
		 int newPos = Arrays.binarySearch(vals, action.getName(), new Comparator<Object>() {
			 public int compare(Object o1, Object o2)
			 {
				 String s1 = (String) o1, s2 = (String) o2;
				 return s1.compareToIgnoreCase(s2);
			 }
		 });
		 if(newPos < 0)
			 newPos = -(newPos + 1);

		 disableNotification = true;

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

	 /**
	  * This is a handler for removing design object event.
	  * 
	  * @param object is the design object being removed
	  */
	 public void handlerRemoveDesignObject(DesignObject object)
	 {
		 if(object.getDesignType() == DesignObject.TYPE_ACTION) {
			 if(openActionCombo != null) {
				 removeComboItem(openActionCombo, object);
			 }
			 if(saveActionCombo != null) {
				 removeComboItem(saveActionCombo, object);
			 }
		 }
		 else if(object.getDesignType() == DesignObject.TYPE_PAGE) {
			 if(parentPageCombo != null) {
				 removeComboItem(parentPageCombo, object);
			 }
		 }
	 }

	 /**
	  * This removed item from the combo, and remains selected item in combo.
	  *
	  * @param combo is the combo in which is item being removed
	  * @param object is the design object to remove
	  */
	 private void removeComboItem(Combo combo, DesignObject object)
	 {
		 int selected = combo.getSelectionIndex();
		 if(selected == 0)
			 return;

		 // WE SHOULD DISABLE NOTIFICATION THERE BECAUSE IF THIS IS GONNA CHANGE, IT'S BEING CHANGED
		 // ALSO ON THE SERVER
		 disableNotification = true;
		 String[] vals = combo.getItems();
		 int index = StringUtil.findInSortedArrayIgnoreCase(vals, object.getName());
		 // RESET THE INDEX ONLY WHEN WE ARE REMOVING AN ITEM WITH THE BIGGER INDEX THAN SELECTED
		 combo.remove(object.getName());
		 if(index < selected) {
			 combo.select(index - 1);
		 }
		 else if(index == selected) {
			 combo.select(0);
		 }
		 disableNotification = false;
	 }

	 /**
	  * This is a handler for renaming design object event.
	  *
	  * @param object is the object which has been renamed
	  * @param oldName is the old name of the object
	  */
	 public void handlerRenameDesignObject(DesignObject object, String oldName)
	 {
		 if(object.getDesignType() == DesignObject.TYPE_ACTION) {
			 if(openActionCombo != null)
				 renameComboItem(openActionCombo, Parameters.PARAM_OPEN_ACTION, object, oldName);
			 if(openActionCombo != null)
				 renameComboItem(saveActionCombo, Parameters.PARAM_SAVE_ACTION, object, oldName);
		 }
		 else if(object.getDesignType() == DesignObject.TYPE_PAGE) {
			 if(parentPageCombo != null)
				 renameComboItem(parentPageCombo, Parameters.PARAM_PARENT_PAGE, object, oldName);
		 }
		 //    if(object.getDesignType() == DesignObject.TYPE_ACTION) {
		 //      if(openActionCombo != null) {
		 //        boolean isChanged = false;
		 //        if(openActionCombo.getText().equalsIgnoreCase(oldName))
		 //          isChanged = true;
		 //  
		 //        int oldIndex = getIndex(oldName, openActionCombo.getItems());
		 //        openActionCombo.setItem(oldIndex, object.getName());
		 //        
		 //        if(isChanged) {
		 //          openActionCombo.select(oldIndex);
		 //          SWTUtil.sortCombo(openActionCombo);
		 //          propsDirty = true;
		 //          updateDirty();
		 //        }
		 //      }
		 //      if(saveActionCombo != null) {
		 //        boolean isChanged = false;
		 //        if(saveActionCombo.getText().equalsIgnoreCase(oldName))
		 //          isChanged = true;
		 //  
		 //        int oldIndex = getIndex(oldName, saveActionCombo.getItems());
		 //        saveActionCombo.setItem(oldIndex, object.getName());
		 //        
		 //        if(isChanged) {
		 //          saveActionCombo.select(oldIndex);
		 //          SWTUtil.sortCombo(saveActionCombo);
		 //          propsDirty = true;
		 //          updateDirty();
		 //        }
		 //      }
		 //    }
	 }

	 private void renameComboItem(Combo combo, String paramName, DesignObject object, String oldName)
	 {
		 //    String currentValue = object.getParameterValue(paramName);

		 String[] vals = combo.getItems();
		 int newPos = Arrays.binarySearch(vals, oldName, new Comparator<Object>() {
			 public int compare(Object o1, Object o2)
			 {
				 String s1 = (String) o1, s2 = (String) o2;
				 return s1.compareToIgnoreCase(s2);
			 }
		 });
		 // CHECK IF THE ITEM IS IN THE COMBO
		 if(newPos < 0)
			 return;

		 disableNotification = true;

		 combo.setItem(newPos, object.getName());
		 //    if(currentValue != null && currentValue.length() > 0) {
		 //      vals = combo.getItems();
		 //      for(int i = 0; i < vals.length; ++i) {
		 //        if(vals[i].equals(currentValue)) {
		 //          combo.select(i);
		 //          break;
		 //        }
		 //      }
		 //    }

		 disableNotification = false;
	 }

	 /**
	  * Updates dirty status of the whole editor.
	  */
	 private void updateDirty()
	 {
		 editor.updateDirty();
	 }

	 /**
	  * Finds case sensitively string in the array, and returns index of that string
	  * in the array.
	  *
	  * @param str string to find
	  * @param array array in which we are loking for string
	  * @return index of string in the array
	  */
	 private int getIndex(String str, String[] array)
	 {
		 for(int i = 0; i < array.length; ++i) {
			 if(array[i].equals(str))
				 return i;
		 }

		 return -1;
	 }

	 public void step(int percentageComplete, String currentOperation)
	 {
		 if(monitor == null)
			 return;

		 if(percentageComplete == 100) {
			 monitor = null;
			 isDirty = false;
			 propsDirty = false;
		 }
	 }

	 /**
	  * Displays error if something goes wrong when updating design object.
	  *
	  * @param message is the error message to display
	  */
	 public void handleError(final String message)
	 {
		 Display.getDefault().asyncExec(new Runnable() {
			 public void run()
			 {
				 MessageDialog.openError(getShell(), "Error when uploading design object properties",
						 "Error occured while uploading design object properties.\nReason:\n"
								 + message);
			 }
		 });
	 }

	 public void disposePage()
	 {
		 propsEditor.dispose();
		 object.getApplication().removeListener(appListener);
	 }
}
