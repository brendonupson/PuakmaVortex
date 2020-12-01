/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    04.06.2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.SOAP.designer;

import java.io.IOException;

import puakma.SOAP.SOAPFaultException;
import puakma.SOAP.SoapProxy;
import puakma.coreide.designer.DownloadDesigner;

public class TestDatabaseDesignerWidget implements Runnable
{
  public static void main(String[] args)
  {
    TestDatabaseDesignerWidget t = new TestDatabaseDesignerWidget();
    t.run();
//    for(int i = 0; i < 10; ++i) {
//      TestDatabaseDesignerWidget t = new TestDatabaseDesignerWidget();
//      Thread th = new Thread(t, "Runner[" + 1 + "]");
//      th.start();
//    }
  }

  public void run()
  {
    String url = "http://localhost:8080/system/SOAPDesigner.pma/DownloadDesigner?WidgetExecute";
    String user = "Sysadmin";
    String pwd = "gagarin;";
    DownloadDesigner client = (DownloadDesigner) SoapProxy.createSoapClient(DownloadDesigner.class, url, user, pwd);
    long id = 4526;
    try {
      byte[] v = client.downloadDesign(id, true);
      System.out.println(v);
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    catch(SOAPFaultException e) {
      e.printStackTrace();
    }
    
//    DownloadDesignerImpl d = new DownloadDesignerImpl(url, user, pwd);
//    EmptyOutputStream os = new EmptyOutputStream();
//    
//    for(int i = 0; i < 5; ++i) {
//      try {
//        d.downloadFile(id, false, os);
//      }
//      catch(Exception e) {
//        e.printStackTrace();
//      }
//    }
  }
}
