/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    08/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import puakma.coreide.PuakmaCoreException;
import puakma.utils.velohelp.VelocityArrayHelper;

public class VelocityProcessor
{
  private VelocityEngine engine;

  public VelocityProcessor(File baseDir) throws IOException, PuakmaCoreException
  {
    if(baseDir.exists() == false)
      throw new IOException("Base directory " + baseDir + " doesn't exist");
    if(baseDir.isDirectory() == false)
      throw new IOException("Base directory " + baseDir + " is not a directory");
    
    engine = new VelocityEngine();
    Properties p = new Properties();
    p.put("file.resource.loader.path", baseDir.getAbsolutePath());
    
    try {
      engine.init(p);
    }
    catch(Exception e) {
      throw new PuakmaCoreException(e);
    }
  }
  
  public VelocityProcessor(String baseDir) throws IOException, PuakmaCoreException
  {
    this(new File(baseDir));
  }

  public void processTemplate(String templateName, OutputStream os, NameValuePair[] parameters) throws PuakmaCoreException
  {
   // puts all names and values to the context of template
    VelocityContext context = new VelocityContext();
    context.put("HELPER", new VelocityArrayHelper());
    if(parameters != null)
      for(int i = 0; i < parameters.length; ++i)
        context.put(parameters[i].getName(), parameters[i].getValue());
    
    try {
      Template template = engine.getTemplate(templateName);
      OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
      template.merge(context, writer);
      writer.flush();
      writer.close();
    }
    catch(Exception ex) {
      throw new PuakmaCoreException(ex);
    }
  }
}
