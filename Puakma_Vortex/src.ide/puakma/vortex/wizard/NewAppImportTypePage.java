/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:      
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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;

public class NewAppImportTypePage extends WizardPage
{
	private List templatesList;

	private Browser description;

	private Label preview;

	private TemplateDefinition[] defs;

	private TemplateDefinition selectedTemplate;

	protected NewAppImportTypePage()
	{
		super("chooseTemplatePage");

		setTitle("Choose Template");
		setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
		setDescription("Select template for new application.");
	}

	public void createControl(Composite parent)
	{
		DialogBuilder2 builder = new DialogBuilder2(parent);
		Composite content = builder.createComposite(2);

		templatesList = new List(content, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true);
		gd.verticalSpan = 3;
		gd.widthHint = 150;
		gd.heightHint = 330;
		templatesList.setLayoutData(gd);
		defs = listAllTemplates();
		for(int i = 0; i < defs.length; ++i) {
			templatesList.add(defs[i].getTemplateName());
		}
		templatesList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {  }
			public void widgetSelected(SelectionEvent e)
			{
				updateSelection(e);
			}
		});

		description = new Browser(content, SWT.BORDER);
		gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		description.setLayoutData(gd);

		Label l = new Label(content, SWT.NULL);
		l.setText("Preview:");

		preview = new Label(content, SWT.NULL);
		gd = new GridData(GridData.FILL, GridData.FILL, true, false);
		gd.heightHint = 150;
		preview.setLayoutData(gd);

		builder.closeComposite();
		builder.finishBuilder();

		setControl(content);
		templatesList.select(0);

		updateSelection(null);
	}

	/**
	 * This updates form (description, and preview) after selecting some item.
	 *
	 * @param e is the source event
	 */
	protected void updateSelection(SelectionEvent e)
	{
		int index = templatesList.getSelectionIndex();
		if(index == -1)
			return;

		String template = templatesList.getItem(index);
		for(int i = 0; i < defs.length; ++i) {
			if(defs[i].getTemplateName().equals(template)) {
				updateDialog(defs[i]);
				break;
			}
		}
	}

	/**
	 * This function updates dialog from template definition. It loads all
	 * the necessary resources, and displays them in controls.
	 *
	 * @param definition is the template definition to display
	 */
	private void updateDialog(TemplateDefinition definition)
	{
		this.selectedTemplate = definition;

		// LOAD BROWSER
		File html = definition.getHtmlFile();
		String descText;
		try {
			descText = loadTextFileContent(html, "UTF-8");
			description.setText(descText);
		}
		catch(IOException e) {
			description.setText("Unable to load description file");
		}

		// LOAD PICTURE
		Image im = new Image(Display.getDefault(), new ImageData(definition.getPictureFile().getAbsolutePath()));
		preview.setImage(im);
	}

	/**
	 * Loads the content of the file to String which is returned. Note that this
	 * function is not capable to load really large files! Only some small size
	 * files can be load using this, so be caution about that. If file is too large,
	 * throws some unchecked exception.
	 *
	 * @param file
	 * @param charset
	 * @return String with the textual content of the file
	 * @throws IOException 
	 */
	public static String loadTextFileContent(File file, String charset) throws IOException
	{
		FileChannel in = null;
		try {
			in = new FileInputStream(file).getChannel();
			long fileLength = file.length();
			byte[] b = new byte[(int) fileLength];
			ByteBuffer bb = ByteBuffer.wrap(b);
			in.read(bb);
			String str = new String(b, charset);
			return str;
		}
		finally {
			if(in != null)
				try 
			{ 
					in.close(); 
			}
			catch(IOException e)
			{}
		}
	}

	/**
	 * Lists all the definitions of Puakma application templates in the templates
	 * directory under plugin's directory.
	 *
	 * @return array with template definitions
	 */
	private TemplateDefinition[] listAllTemplates()
	{
		IPath tplPath = VortexPlugin.getPluginDirectory().append("templates");
		File tpl = tplPath.toFile();
		File[] files = tpl.listFiles();
		ArrayList<TemplateDefinition> pmxFiles = new ArrayList<TemplateDefinition>();
		for(int i = 0; i < files.length; ++i) {
			File file = files[i];
			if(file.isFile() && file.canRead()) {
				String fName = file.getName();
				int dot = fName.lastIndexOf(".");
				if(dot != -1) {
					String ext = fName.substring(dot + 1);
					if("pmx".equalsIgnoreCase(ext)) {
						String name = fName.substring(0, dot);
						TemplateDefinition def = new TemplateDefinition(name);
						pmxFiles.add(def);
					}
				}
			}
		}

		return (TemplateDefinition[]) pmxFiles.toArray(new TemplateDefinition[pmxFiles.size()]);
	}

	public String getName()
	{
		int index = templatesList.getSelectionIndex();
		if(index == -1)
			return null;
		return defs[index].getTemplateName();
	}

	public TemplateDefinition getTemplate()
	{
		return selectedTemplate;
	}
}
