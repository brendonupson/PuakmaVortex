/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 11, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.templates;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

import puakma.utils.NameValuePair;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectUtils;


/**
 * @author Martin Novak
 */
public class TemplateCreator
{
	private static VelocityEngine engine;

	static {
		initialize();
	}

	/**
	 * Initializes the whole engine.
	 */
	public static void initialize()
	{
		try {
			engine = new VelocityEngine();
			Properties p = new Properties();
			IPath path = VortexPlugin.getPluginDirectory();
			path = path.append("templates");
			p.put("file.resource.loader.path", path.toString());
			engine.init(p);
		}
		catch(Exception e) {
			VortexPlugin.log(e);
		}
	}

	/**
	 * Processes template, and creates a new file from it.
	 *
	 * @param templateName is the file name of the template. It has to be relatively
	 * to plugin's templates directory.
	 * @param file is the workspace file to write in
	 * @param parameters are template parameters
	 * @throws Exception if some error occurs
	 */
	public static void processTemplate(String templateName, IFile file, NameValuePair[] parameters) throws Exception
	{
		// puts all names and values to the context of template
		VelocityContext context = new VelocityContext();
		if(parameters != null)
			for(int i = 0; i < parameters.length; ++i)
				context.put(parameters[i].getName(), parameters[i].getValue());

		Template template = engine.getTemplate(templateName);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
		template.merge(context, writer);
		writer.flush();
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

		// CREATE THE WORKSPACE FILE
		ProjectUtils.createFolder((IFolder) file.getParent());
		if(file.exists() == false)
			file.create(is, true, null);
		else
			file.setContents(is, true, true, null);

		writer.close();
		os.close();
		is.close();
	}
}
