/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 14, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import puakma.coreide.designer.AppDesigner;
import puakma.coreide.designer.ApplicationStructureBean;
import puakma.coreide.designer.ApplicationStructureBean.ParamNameValue;
import puakma.coreide.objects2.Parameters;
import puakma.utils.lang.ArrayUtils;

class ParametersImpl implements Parameters
{
  private static final String[] PROPS_LIST = {
    PARAM_OPEN_ACTION, PARAM_OPEN_ACTION_1,
    PARAM_SAVE_ACTION, PARAM_SAVE_ACTION_1,
    PARAM_DEFAULT_CHARSET, PARAM_DEFAULT_OPEN,
    PARAM_LOGIN_PAGE, PARAM_PAGE_MODE, PARAM_PARENT_PAGE,
  };
  
  /**
   * List of parameters reserved for pages
   */
  private static final String[] PAGE_PROPS_LIST = {
    PARAM_OPEN_ACTION, PARAM_SAVE_ACTION, PARAM_PARENT_PAGE, PARAM_PAGE_MODE,
  };
  
  /**
   * List of parameters reserved for application's use
   */
  private static final String[] APP_PROPS_LIST = {
    PARAM_OPEN_ACTION, PARAM_OPEN_ACTION_1,
    PARAM_SAVE_ACTION, PARAM_SAVE_ACTION_1,
    PARAM_DEFAULT_CHARSET, PARAM_DEFAULT_OPEN,
    PARAM_LOGIN_PAGE,
  };
  
  private ApplicationImpl application;
  private DesignObjectImpl obj;
  private boolean workingCopy;
  private ParametersImpl original;
  private Map<String, String[]> params = new HashMap<String, String[]>();
  private boolean isDirty;
  private int status = ServerObjectImpl.STATUS_NEW;

  ParametersImpl(ApplicationImpl impl)
  {
    this.application = impl;
  }
  
  ParametersImpl(DesignObjectImpl impl)
  {
    this.obj = impl;
  }

  public void addParameter(String name, String value)
  {
    name = equalizeParameter(name);
    
    synchronized(this) {
      String[] strs = params.get(name);
      if(strs != null) {
        strs = (String[]) ArrayUtils.append(strs, value);
        params.put(name, strs);
      }
      else if(strs == null) {
        params.put(name, new String[] { value } );
      }
      else
        throw new IllegalStateException("Invalid object in design object params");
    }
  }

  /**
   * This function changes case of predefined parameters to it's predictable value.
   * It means from openaction makes OpenAction, etc... Just as defined in
   * Parameters#PROP_OPEN_ACTION.
   * 
   * @param name is the name of the parameter to check
   * @return converted value of the parameter
   */
  private String equalizeParameter(String name)
  {
    for(int i = 0; i < ParametersImpl.PROPS_LIST.length; ++i)
      if(ParametersImpl.PROPS_LIST[i].equalsIgnoreCase(name))
        return ParametersImpl.PROPS_LIST[i];

    return name;
  }

  public void setParameter(String name, String value)
  {
    name = equalizeParameter(name);

    synchronized(this) {
      if(value == null || value.length() == 0)
        params.remove(name);
      else
        params.put(name, new String[] { value });
    }
  }

  public void setParameters(String name, String[] values)
  {
    name = equalizeParameter(name);

    synchronized(this) {
      params.put(name, values.clone());
    }
  }

  public String getParameterValue(String name)
  {
    synchronized(this) {
      String[] strs = params.get(name);
      if(strs == null || strs.length == 0)
        return null;
      
      return strs[0];
    }
  }

  public String[] getParameterValues(String name)
  {
    synchronized(this) {
      String[] s = params.get(name);
      return s == null ? null : s.clone();
    }
  }

  public String[] listParameters()
  {
    synchronized(this) {
      Set<String> keys = params.keySet();
      return keys.toArray(new String[keys.size()]);
    }
  }
  
  public void removeParameter(String name)
  {
    name = equalizeParameter(name);
    
    synchronized(this) {
      params.remove(name);
    }
  }

  public void removeParameterValue(String name, String value)
  {
    name = equalizeParameter(name);
    
    synchronized(this) {
      String[] vals = params.get(name);
      if(vals == null || vals.length == 0)
        return;

      List<String> l = new ArrayList<String>();
      
      for(int i = 0; i < vals.length; ++i) {
        if(value.equals(vals[i]) == false) {
          l.add(vals[i]);
        }
      }
      
      String[] newVals = l.toArray(new String[l.size()]);
      if(newVals.length == 0)
        params.remove(name);
      else
        params.put(name, newVals);
    }
  }

  public void commitParams() throws PuakmaCoreException
  {
    if(isRemoved())
      throw new PuakmaCoreException("Cannot commit removed object");
    if(isNew() == false && isWorkingCopy() == false)
      throw new PuakmaCoreException("Cannot commit parameters object, it has to be working copy");
    
    intCommitParams();
  }
  
  /**
   * This commits parameters without checking any status.
   */
  void intCommitParams() throws PuakmaCoreException
  {
    synchronized(this) {
      ApplicationImpl localApp = application;
      if(application == null)
        localApp = (ApplicationImpl) obj.getApplication();
      // CREATE ARRAYS OF UPDATES DATA
      List<String> namesList = new ArrayList<String>();
      List<Object> valuesList = new ArrayList<Object>();
      for(String name : params.keySet()) {
        String[] ss = params.get(name);
        for(String s : ss) {
          namesList.add(name);
          valuesList.add(s);
        }
      }
      String[] paramNames = namesList.toArray(new String[namesList.size()]);
      String[] paramValues = valuesList.toArray(new String[valuesList.size()]);
      
      try {
        // NOW SEND DATA TO SERVER
        AppDesigner designer = localApp.getAppDesigner();
        if(application != null) {
          designer.setAppParams(application.getId(), paramNames, paramValues);
        }
        else {
          designer.setParams(obj.getId(), paramNames, paramValues);
        }
        
        // WELL, AND NOW WE HAVE TO UPDATE ORIGINAL
        if(isWorkingCopy())
          original.copyFromWorkingCopy(this);
        
        setValid();
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot commit data to server", e);
      }
    }
  }
  
  private void copyFromWorkingCopy(ParametersImpl copy)
  {
    synchronized(this) {
      params.clear();
      for(String name : copy.params.keySet()) {
        String[] val = copy.params.get(name);
        params.put(name, val);
      }
    }
  }

  public ParametersImpl makeWorkingCopy()
  {
    ParametersImpl impl;
    if(application != null)
      impl  = new ParametersImpl(application);
    else
      impl = new ParametersImpl(obj);
    
    synchronized(this) {
      impl.workingCopy = true;
      impl.application = application;
      impl.original = this;
      impl.obj = obj;
      // COPY THE PARAMETERS
      impl.params = new HashMap<String, String[]>();
      for(String name : params.keySet()) {
        String[] value = params.get(name);
        impl.params.put(name, value);
      }
    }
    
    return impl;
  }
  
  /**
   * Copies this shit.
   * @param copyObj is the designobject to which we want to assign this copy. If you want to
   * assign parameters to application, set this to null.
   * @param copyApp is the application to which this is gonna be assigned
   * @return ParametersImpl object assigned to the application or design object
   */
  public ParametersImpl copy(DesignObjectImpl copyObj, ApplicationImpl copyApp)
  {
    assert copyObj != null && copyApp != null : "Cannot copy parameters to the both";
    
    ParametersImpl impl = makeWorkingCopy();
    if(copyObj != null) {
      impl.application = null; impl.obj = copyObj;
    }
    else {
      impl.application = copyApp; impl.obj = null;
    }
    impl.workingCopy = false;
    impl.original = null;
    impl.status = ServerObjectImpl.STATUS_NEW;
    return impl;
  }

  
  public boolean isWorkingCopy()
  {
      return workingCopy;
  }

  /**
   * Refreshes list of parameters from the bean source which we got from xml
   * from the server.
   *
   * @param beansList is the list of parameters. Items are of the type
   *                  <code>ApplicationStructureBean.ParamNameValue</code>
   */
  public void refreshFrom(List<ApplicationStructureBean.ParamNameValue> beansList)
  {
    synchronized(this) {
      // CLEAR ALL THE PARAMS BECAUSE WE ARE DOING CLEAN REFRESH
      params.clear();
      
      Iterator<ApplicationStructureBean.ParamNameValue> it = beansList.iterator();
      while(it.hasNext()) {
        ApplicationStructureBean.ParamNameValue param = it.next();
        addParameter(param.name, param.value);
      }
      
      setDirty(false);
    }
  }
  
  public boolean isDirty()
  {
    return isDirty;
  }

  private void setDirty(boolean newDirty)
  {
    this.isDirty = newDirty;
  }
  
  public boolean isNew()
  {
    return status  == ServerObjectImpl.STATUS_NEW;
  }

  public void setValid()
  {
    status = ServerObjectImpl.STATUS_VALID;
  }
  
  public boolean isRemoved()
  {
    return status == ServerObjectImpl.STATUS_REMOVED;
  }
  
  public boolean isReservedPageProperty(String name)
  {
    for(int i = 0; i < PAGE_PROPS_LIST.length; ++i) {
      if(PAGE_PROPS_LIST[i].equalsIgnoreCase(name))
        return true;
    }
    
    return false;
  }
  
  public boolean isReservedAppProperty(String name)
  {
    for(int i = 0; i < APP_PROPS_LIST.length; ++i) {
      if(APP_PROPS_LIST[i].equalsIgnoreCase(name))
        return true;
    }
    
    return false;
  }

  public int getParameterCount()
  {
    synchronized(this) {
      return params.size();
    }
  }
}
