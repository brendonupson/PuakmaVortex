/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 14, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.wizard;

import java.io.File;

import org.eclipse.core.runtime.IPath;

import puakma.vortex.VortexPlugin;

/**
 * This class is a bean holding definition of templates.
 *
 * @author Martin Novak
 */
public class TemplateDefinition
{
  private String templateName;
  
  private File pictureFile;
  
  private File htmlFile;
  
  private File pmxFile;
  
  public TemplateDefinition(String templateName)
  {
    this.templateName = templateName;

    IPath templatesPath = VortexPlugin.getPluginDirectory().append("templates");
    pictureFile = templatesPath.append(templateName + ".png").toFile();
    if(pictureFile.exists() == false) {
      pictureFile = templatesPath.append(templateName + ".gif").toFile();
      if(pictureFile.exists() == false) {
        pictureFile = templatesPath.append(templateName + ".png").toFile();
      }
    }
    
    htmlFile = templatesPath.append(templateName + ".html").toFile();
    pmxFile = templatesPath.append(templateName + ".pmx").toFile();
  }

  public File getHtmlFile()
  {
    return htmlFile;
  }

  public File getPictureFile()
  {
    return pictureFile;
  }

  public File getPmxFile()
  {
    return pmxFile;
  }

  public String getTemplateName()
  {
    return templateName;
  }
}
