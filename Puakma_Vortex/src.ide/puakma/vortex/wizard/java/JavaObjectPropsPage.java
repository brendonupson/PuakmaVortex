/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Apr 5, 2005
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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.wizard.AbstractWizardPage2;

/**
 * This class makes a page for specifying properties of the design objects.
 *
 * @author Martin Novak
 */
public class JavaObjectPropsPage extends AbstractWizardPage2 implements ModifyListener
{
  private int designType;
  
  private Text nameText;
  private Text commentText;
  private ClazzPage clazzPage;
  private boolean javaPageInitialized;
  private int javaType;

  protected JavaObjectPropsPage(Application application, int designType, int javaType, String pageName)
  {
    super(pageName, application);
    
    this.designType = designType;
    this.javaType = javaType;
    
    String title;
    String msg = "Create New ";
    switch(designType) {
      case DesignObject.TYPE_ACTION:
        msg += "Action";
      break;
      case DesignObject.TYPE_SCHEDULEDACTION:
        msg += "Sceduled Action";
      break;
      case DesignObject.TYPE_WIDGET:
        msg += "SOAP Widget";
      break;
    }
    title = msg;
    setTitle(title);
    setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
    setDescription(msg);
  }

  protected void createContent(DialogBuilder2 builder)
  {    
    nameText = builder.createEditRow("Name");
    nameText.addModifyListener(this);
    commentText = builder.createMemoRow("Comment", 3);
    commentText.addModifyListener(this);
    
    if(designType == DesignObject.TYPE_SCHEDULEDACTION) {
      // TODO: add some more controls here... but later dude!
    }
    
    setPageComplete(false);
  }
  
  public String getName()
  {
    return nameText.getText();
  }
  
  public String getComment()
  {
    return commentText.getText();
  }

  public void modifyText(ModifyEvent e)
  {
    String msg = findErrorMsg();
    setErrorMessage(msg);
    setPageComplete(msg == null);
  }

  /**
   * Checks dialog for possible error, and returns it.
   *
   * @return String with error message or null if there is no error
   */
  private String findErrorMsg()
  {
    String str = super.checkError();
    if(str != null)
      return str;

    if(nameText.getText().length() == 0)
      return "You have to type the name of the design object";
    if(getApplication() != null && getApplication().getDesignObject(nameText.getText()) != null)
      return "Design object with that name already exists";
    return null;
  }

  public IWizardPage getNextPage()
  {
    if(javaPageInitialized == false) {
      JavaObjectWizard wizard = (JavaObjectWizard) getWizard();
      clazzPage = new ClazzPage(getApplication(), designType, javaType, "clazz");
      wizard.addPage(clazzPage);
      wizard.setupClazzPage(clazzPage);
      javaPageInitialized = true;
    }
    
    clazzPage.setupActionName(nameText.getText());
    clazzPage.setupApplication(getApplication());
    
    return clazzPage;
  }
}
