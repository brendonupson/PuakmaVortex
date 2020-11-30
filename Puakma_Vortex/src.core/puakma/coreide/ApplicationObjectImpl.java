/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 17, 2005
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

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationObject;

/**
 * @author Martin Novak
 */
abstract class ApplicationObjectImpl extends ServerObjectImpl
                                            implements ApplicationObject
{
  protected ApplicationImpl application;
  
  protected boolean closing = false;
  
  /**
   * @param application
   */
  public ApplicationObjectImpl(ApplicationImpl application)
  {
    super((ServerImpl) (application == null ? null : application.getServer()));
    this.application = application;
  }
  
  public Application getApplication()
  {
    return application;
  }
  
  /**
   * Internal function which sets application to the application object.
   *
   * @param application is the newly assigned application
   */
  void setApplication(ApplicationImpl application)
  {
    this.application = application;

    if(application != null)
      setServer((ServerImpl) application.getServer());
//    else if(PuakmaLibraryManager.DEBUG_MODE) {
//      try {
//        if(closing == false)
//          throw new Exception("CLOSING Object id(" + getId() + ") name(" + getName() + ") " + toString());
//      }
//      catch(Exception ex) {
//        PuakmaLibraryManager.log(ex);
//      }
//      RandomAccessFile f= null;
//      try {
//        File file = new File("vortex-special.log");
//        f = new RandomAccessFile(file, "rw");
//        f.seek(file.length());
//        
//        if(closing) {
//          f.writeUTF("CLOSING Object id(" + getId() + ") name(" + getName() + ") " + toString());
//        }
//      //if(status == STATUS_VALID)
//        try {
//          throw new Exception("APPLICATION HAS BEEN SET TO NULL");
//        }
//        catch(Exception ex) {
//          StringWriter sw = new StringWriter();
//          ex.printStackTrace(new PrintWriter(sw));
//          f.writeUTF("Object id(" + getId() + ") name(" + getName() + ") " + toString());
//          f.writeUTF(sw.toString());
//          f.writeUTF("\n");
//        }
//      }
//      catch(Exception ex) {
//        PuakmaLibraryManager.log(ex);
//      }
//      finally {
//        if(f != null)
//          try {
//            f.close();
//          }
//          catch(Exception ex) {}
//      }
//    }
  }
  
  protected void makeCopy(ApplicationObjectImpl copy)
  {
    super.makeCopy(copy);
    
    copy.application = application;
  }
  
  protected void copyFromWorkingCopy(ApplicationObjectImpl workingCopy)
  {
    super.copyFromWorkingCopy(workingCopy);
  }
  
  public boolean isClosed()
  {
    return isOpen() == false ? true : false;
  }

  public boolean isOpen()
  {
    if(application != null)
      return application.isOpen();

    return false;
  }

  /**
   * @param impl
   */
  public void refreshFrom(ApplicationObjectImpl impl)
  {
    super.refreshFrom(impl);
  }
}
