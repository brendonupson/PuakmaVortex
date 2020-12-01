/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 20, 2005
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
import java.util.List;

import puakma.coreide.designer.AppDesigner;
import puakma.coreide.objects2.Keyword;
import puakma.utils.lang.CollectionsUtil;

/**
 * @author Martin Novak
 */
class KeywordImpl extends ApplicationObjectImpl implements Keyword
{
  private List<String> data = new ArrayList<String>();

  public KeywordImpl(ApplicationImpl application)
  {
    super(application);
  }

  public int addData(String value)
  {
    if(isRemoved())
      throw new IllegalStateException("Cannot add data to removed object");
    if(isWorkingCopy() == false && isNew() == false)
      throw new IllegalStateException("You can add data only to working copy or to new object");

    synchronized(data) {
      data.add(value);
      return data.size();
    }
  }

  public int addData(String value, int index)
  {
    if(isRemoved())
      throw new IllegalStateException("Cannot add data to nonexisting object");
    if(isWorkingCopy() == false && isNew() == false)
      throw new IllegalStateException("You can add data only to working copy or to new object");

    synchronized(data) {
      data.add(index, value);
      return data.size();
    }
  }

  public int removeData(int index)
  {
    if(isRemoved())
      throw new IllegalStateException("Cannot remove data from nonexisting object");
    if(isWorkingCopy() == false && isNew() == false)
      throw new IllegalStateException("You can remove data only from working copy or from new object");
    
    synchronized(data) {
      data.remove(index);
      return data.size();
    }
  }

  public String[] listData()
  {
    synchronized(data) {
      String[] arr = new String[data.size()];
      arr = data.toArray(arr);
      return arr;
    }
  }

  public String getData(int index)
  {
    synchronized(data) {
      return data.get(index);
    }
  }

  public Keyword makeWorkingCopy()
  {
    KeywordImpl kw = new KeywordImpl(application);
    makeCopy(kw);
    setupAsWorkingCopy(kw);
    // well, this is special kinda case - because string is not our object
    kw.data = new ArrayList<String>(data);
    return kw;
  }

  public void commit() throws PuakmaCoreException
  {
    if(isRemoved())
      throw new PuakmaCoreException("Cannot commit removed object");
    if(isNew() == false && isWorkingCopy() == false)
      throw new PuakmaCoreException("Cannot commit server object, it has to be working copy");

    synchronized(this) {
      try {
        AppDesigner designer = application.getAppDesigner();
        long newId = designer.saveKeyword(getId(), application.getId(), getName());
        if(isNew())
          id = newId;
        if(isNew() == false)
          original.copyFromWorkingCopy(this);
        setValid();
        
        if(isNew()) {
          synchronized(data) {
            designer.saveKeywordData(getId(), data.toArray(new String[data.size()]));
          }
        }
      }
      catch(Exception e) {
        throw new PuakmaCoreException("Cannot save keyword to server", e);
      }
    }
  }
  
  public void remove() throws PuakmaCoreException
  {
    synchronized(this) {
      if(isNew())
        throw new PuakmaCoreException("Cannot remove new nonexisting object");
      if(isWorkingCopy())
        throw new PuakmaCoreException("Cannot remove working copy");

      try {
        if(isRemoved() == false) {
          AppDesigner designer = application.getAppDesigner();
          designer.removeKeyword(getId());
        }
        setRemoved();
        application.notifyRemove(this);
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot remove keyword", e);
      }
    }
  }

  public void setData(String value, int index)
  {
    if(isRemoved())
      throw new IllegalStateException("Cannot set data in nonexisting object");
    if(isWorkingCopy() == false && isNew() == false)
      throw new IllegalStateException("You can set data only in working copy or in new object");
    
    synchronized(data) {
      data.set(index, value);
    }
  }

  public void commitData() throws PuakmaCoreException
  {
    if(isWorkingCopy() == false)
      throw new PuakmaCoreException("To commit data, you have to have working copy");
    if(isValid() == false)
      throw new PuakmaCoreException("Cannot commit data on non-existing object");

    synchronized(data) {
      try {
        AppDesigner designer = application.getAppDesigner();
        designer.saveKeywordData(getId(), data.toArray(new String[data.size()]));

        ((KeywordImpl)original).data.clear();
        ((KeywordImpl)original).data.addAll(data);
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot commit data change", e);
      }
    }
  }

  /**
   * Initializes the object which exists on the server with some properties.
   *
   * @param application is the application to which is this keyword assigned
   * @param id is the identifier of the keyword on the server
   * @param data is the list with datas (they have to be already sorted)
   */
  public void initialize(ApplicationImpl application, long id, List<String> data)
  {
    setApplication(application);
    setId(id);
    this.data.addAll(data);
  }

  public void close()
  {
    setApplication(null);
    synchronized(data) {
      data.clear();
    }
    // TODO: make new status closed???
    setRemoved();
  }

  public void shiftValue(int srcIndex, int destIndex)
  {
    if(isRemoved())
      throw new IllegalStateException("Cannot shift data to nonexisting object");
    if(isWorkingCopy() == false && isNew() == false)
      throw new IllegalStateException("You can shift data only in working copy or in new object");
    
    synchronized(data) {
      CollectionsUtil.shiftObjects(data, destIndex, srcIndex);
    }
  }
  
  /**
   * Refreshes the current object from the another object. Note that this has to be externally
   * synchronized, and then called update event. Sample:
   * 
   * <pre>
   * synchronized(kw) {
   *   kw.refreshFrom(anotherKw);
   *   if(kw.isDirty()) {
   *     kw.setDirty(false);
   *     kw.fireUpdateEvent();
   *   }
   * }
   * </pre>
   *
   * @param kw is the keyword from which we will refresh this object
   */
  void refreshFrom(KeywordImpl kw)
  {
    assert getId() == kw.getId() : "Identifier has to be the same in the both objects when refreshing";
    
    super.refreshFrom(kw);

    synchronized(data) {
      this.data.clear();
      // TODO: zkontrolovat jestli je to skutecne stejne
      this.data.addAll(kw.data);
      setDirty(true);
    }
  }
}
