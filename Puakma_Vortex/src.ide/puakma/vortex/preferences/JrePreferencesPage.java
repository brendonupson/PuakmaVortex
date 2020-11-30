/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    14/06/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.application.JavaPropertiesPage;
import puakma.vortex.swt.DialogBuilder2;

public class JrePreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	public static final String PAGE_ID = "puakma.preferences.JavaVersionPreferencesPage";

	public JrePreferencesPage()
	{
		super(GRID);

		setPreferenceStore(VortexPlugin.getDefault().getPreferenceStore());
		setDescription("Assign Eclipse JREs to Java versions for Tornado Applications");
	}

	public void init(IWorkbench workbench)
	{

	}

	//  protected Control createContents(Composite parent)
	//  {
	//    DialogBuilder2 builder = new DialogBuilder2(parent);
	//    Composite ret = builder.createComposite(2);
	//    
	//    jdk14Combo = builder.createComboRow("Java 1.4 default JRE", true);
	//    jdk15Combo = builder.createComboRow("Java 1.5 default JRE", true);
	//    jdk16Combo = builder.createComboRow("Java 1.6 default JRE", true);
	//    
	//    // NOW CREATE LINK TO THE ECLIPSE JRE MANIPULATION PAGE
	//    StringBuffer sb = new StringBuffer();
	//    sb.append("<form>\n");
	//    sb.append("<p><a href=\"prefs://org.eclipse.jdt.debug.ui.preferences.VMPreferencePage\">Open Eclipse Installed JREs page.</a></p>");
	//    sb.append(" Please note that if you want to change something on the Eclipse JREs page,<br/>");
	//    sb.append("you will need then to click Ok, ");
	//    sb.append("and reopen this dialog to make further changes here");
	//    sb.append("</form>\n");
	//    
	//    builder.createFormText(sb.toString());
	//
	//    
	//    // NOW INIT THAT
	//    updateContentOfCombos();
	//    
	//    return ret;
	//  }

	//  private void updateContentOfCombos()
	//  {
	//    updateContentOfCombo(jdk14Combo, JavaPropertiesPage.ENV_JAVA_1_4);
	//    updateContentOfCombo(jdk15Combo, JavaPropertiesPage.ENV_JAVA_1_5);
	//    updateContentOfCombo(jdk16Combo, JavaPropertiesPage.ENV_JAVA_1_6);
	//  }
	//
	//  private void updateContentOfCombo(Combo combo, String envName)
	//  {
	//    IExecutionEnvironmentsManager envM = JavaRuntime.getExecutionEnvironmentsManager();
	//    IExecutionEnvironment env = envM.getEnvironment(envName);
	//    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
	//    String defId = store.getString(envName);
	//    IVMInstall[] vms = env.getCompatibleVMs();
	//    
	//    // CHECK IF THE DEFID IS VALID, IF NOT, REMOVE IT
	//    if(defId.length() > 0) {
	//      boolean ok = false;
	//      for(int i = 0; i < vms.length; ++i) {
	//        if(env.isStrictlyCompatible(vms[i]) && defId.equals(vms[i].getId())) {
	//          ok = true;
	//          break;
	//        }
	//      }
	//      
	//      if(ok == false) {
	//        defId = "";
	//        store.setToDefault(envName);
	//      }
	//    }
	//      
	//    combo.removeAll();
	//    
	//    IVMInstall defaultVm = env.getDefaultVM();
	//    
	//    for(int j = 0; j < vms.length; ++j)
	//      if(env.isStrictlyCompatible(vms[j])) {
	//        combo.add(vms[j].getName());
	//
	//        if(defId.length() > 0) {
	//          if(vms[j].getId().equals(defId))
	//            combo.select(combo.getItemCount());
	//        }
	//        else if(vms[j].equals(defaultVm)) {
	//          combo.select(combo.getItemCount());
	//          // ALSO SAVE ID TO PREFERENCE STORE
	//          store.setValue(envName, vms[j].getId());
	//        }
	//      }
	//    
	//    if(defId.length() == 0 && defaultVm == null) {
	//      if(vms.length > 0) {
	//        combo.select(0);
	//        // ALSO SAVE ID TO PREFERENCE STORE
	//        store.setValue(envName, vms[0].getId());
	//      }
	//    }
	//  }
	//
	//  public void setVisible(boolean visible)
	//  {
	//    super.setVisible(visible);
	//    
	////    if(visible)
	////      updateContentOfCombos();
	//  }
	//
	//  public boolean performOk()
	//  {
	//    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
	//    saveVmFor(JavaPropertiesPage.ENV_JAVA_1_4, jdk14Combo, store);
	//    saveVmFor(JavaPropertiesPage.ENV_JAVA_1_5, jdk15Combo, store);
	//    saveVmFor(JavaPropertiesPage.ENV_JAVA_1_6, jdk16Combo, store);
	//    return true;
	//  }
	//
	//  /**
	//   * Saves configuration for JDK to the preference store
	//   */
	//  private void saveVmFor(String key, Combo combo, IPreferenceStore store)
	//  {
	//    if(combo.getText().length() == 0) {
	//      store.setToDefault(key);
	//    }
	//    else {
	//      String name = combo.getText();
	//      
	//      // FIND THE VM
	//      IExecutionEnvironmentsManager envM = JavaRuntime.getExecutionEnvironmentsManager();
	//      IExecutionEnvironment env = envM.getEnvironment(key);
	//      IVMInstall[] installs = env.getCompatibleVMs();
	//      for(int i = 0; i < installs.length; ++i) {
	//        if(name.equals(installs[i].getName())) {
	//          // NOW STORE ID TO PREFERENCESTORE
	//          store.setValue(key, installs[i].getId());
	//          return;
	//        }
	//      }
	//      
	//      // IF WE HAVEN'T FOUND ANYTHING, SET TO EMPTY
	//      store.setToDefault(key);
	//      VortexPlugin.log("Unknown JRE: " + combo.getText());
	//    }
	//  }

	protected void createFieldEditors()
	{
		addEnvComboBox(JavaPropertiesPage.ENV_JAVA_1_4, "Java 1.4");
		addEnvComboBox(JavaPropertiesPage.ENV_JAVA_1_5, "Java 1.5");
		addEnvComboBox(JavaPropertiesPage.ENV_JAVA_1_6, "Java 1.6");

		DialogBuilder2 builder = new DialogBuilder2(getFieldEditorParent());
		// NOW CREATE LINK TO THE ECLIPSE JRE MANIPULATION PAGE
		StringBuffer sb = new StringBuffer();
		sb.append("<form>\n");
		sb.append("<p><a href=\"prefs://org.eclipse.jdt.debug.ui.preferences.VMPreferencePage\">Open Eclipse Installed JREs page.</a></p>");
		sb.append(" Please note that if you want to change something on the Eclipse JREs page,<br/>");
		sb.append("you will need then to click Ok, ");
		sb.append("and reopen this dialog to make further changes here");
		sb.append("</form>\n");

		builder.createFormText(sb.toString());
	}

	private void addEnvComboBox(String envName, String label)
	{
		String[][] namesAndValues;
		namesAndValues = generateNamesAndValues(envName);
		addField(new ComboFieldEditor(envName, label, namesAndValues, getFieldEditorParent()));
	}

	private String[][] generateNamesAndValues(String envName)
	{
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment env = manager.getEnvironment(envName);
		IVMInstall[] vms = env.getCompatibleVMs();
		List<String> ret = new ArrayList<String>();
		for(int i = 0; i < vms.length; ++i) {
			if(env.isStrictlyCompatible(vms[i])) {
				ret.add(vms[i].getName());
				ret.add(vms[i].getId());
			}
		}

		String[][] s = new String[ret.size() / 2][2];
		Iterator<String> it = ret.iterator();
		for(int i = 0; i < s.length; ++i) {
			s[i][0] = (String) it.next();
			s[i][1] = (String) it.next();
		}
		return s;
	}
}
