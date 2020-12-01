/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    28/08/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.design;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.ServerObject;
import puakma.vortex.IdeException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.pma.PmaStructuredTextEditor;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.swt.MultiPageEditorPart2;

public class PuakmaEditor extends MultiPageEditorPart2 implements IReusableEditor, PropertyChangeListener
{
	public static final String EDITOR_ID = "puakma.vortex.editors.PuakmaEditor";

	/**
	 * Is the managed design object
	 */
	private DesignObject obj;

	/**
	 * Text editor
	 */
	IEditorPart editorPage = null;

	/**
	 * Object used as resource uploader/downloader
	 */
	private UploaderPage resourceEditor;

	/**
	 * This is control for managing design object properties
	 */
	private DesignObjectPropertiesArea propertiesArea;

	/**
	 * Page with browser control
	 */
	private BrowserEditorArea browserArea;

	private int editorPageIndex = -1;

	private int resourceEditorIndex = -1;

	private int propertiesAreaIndex = -1;

	private int browserPageIndex = -1;

	protected void createPages()
	{
		try {
			setPartName(obj.getName());
			setTitleImage(VortexPlugin.getImage("puakma.gif"));

			editorPage = createEditor();
			if(editorPage != null) {
				PuakmaEditorInput pmaInput = (PuakmaEditorInput) getEditorInput();
				editorPageIndex = addPage(editorPage, new FileEditorInput(pmaInput.getFile()));
				setPageText(editorPageIndex, "Editor");
			}

			resourceEditor = createResourceEditor();
			if(resourceEditor != null) {
				resourceEditorIndex = addPage(resourceEditor);
				setPageText(resourceEditorIndex, "Edit");
			}

			propertiesArea = createPropertiesArea();
			if(propertiesArea != null) {
				propertiesAreaIndex = addPage(propertiesArea);
				setPageText(propertiesAreaIndex, "Properties");
			}

			browserArea = createBrowserArea();
			if(browserArea != null) {
				browserPageIndex = addPage(browserArea);
				setPageText(browserPageIndex, "Preview");
			}
		}
		catch(PartInitException ex) {
			throw new RuntimeException(ex);
		}
	}

	private IEditorPart createEditor() throws PartInitException
	{
		IEditorPart ret = null;

		if(obj.getDesignType() == DesignObject.TYPE_PAGE) {
			// MAKE DIRECTLY PHTML EDITOR IN THIS CASE HERE
			ret = new PmaStructuredTextEditor();
		}
		else if(isValidTextDesignObject(obj)) {
			String fileName = ((IFileEditorInput) getEditorInput()).getFile().getName();
			IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor[] editorDescriptor = editorRegistry.getEditors(fileName);

			IEditorDescriptor descriptor = null;
			IEditorDescriptor defDesc = editorRegistry.findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);
			if(editorDescriptor.length == 0)
				descriptor = defDesc;
			else
				descriptor = editorDescriptor[0];
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] confEls = registry.getConfigurationElementsFor("org.eclipse.ui",
					"editors", descriptor.getId());
			try {
				ret = (IEditorPart) confEls[0].createExecutableExtension("class");
			}
			catch(Exception e) {
				// well well well, editor hasn't been found, so try to create the default text editor
				try {
					ret = new TextEditor();
				}
				catch(Exception ex) {
					VortexPlugin.log(ex);
					VortexPlugin.log(e);
					throw new PartInitException("Cannot open editor: " + e.getLocalizedMessage(), e);
				}
			}
		}

		if(ret != null) {
			createSite(ret);
		}

		return ret;
	}

	private boolean isValidTextDesignObject(DesignObject obj2)
	{
		if(obj.getDesignType() != DesignObject.TYPE_RESOURCE
				&& obj.getDesignType() != DesignObject.TYPE_DOCUMENTATION)
			return false;
		String contentType = obj.getContentType();
		if(contentType.startsWith("text/"))
			return true;

		return false;
	}

	/**
	 * Creates a new resources editor, but doesn't add it to the page structures.
	 */
	private UploaderPage createResourceEditor()
	{
		if(obj.getDesignType() != DesignObject.TYPE_RESOURCE
				&& obj.getDesignType() != DesignObject.TYPE_DOCUMENTATION
				&& obj.getDesignType() != DesignObject.TYPE_JAR_LIBRARY
				&& obj.getDesignType() != DesignObject.TYPE_CONFIGURATION)
			return null;

		UploaderPage p = new UploaderPage(getContainer(), this, obj);
		return p;
	}

	private DesignObjectPropertiesArea createPropertiesArea()
	{
		DesignObjectPropertiesArea a = new DesignObjectPropertiesArea(getContainer(), this, obj);
		return a;
	}

	private BrowserEditorArea createBrowserArea()
	{
		// TODO: maybe enable this
		if(0 == 1 && obj.getDesignType() == DesignObject.TYPE_PAGE) {
			BrowserEditorArea a = new BrowserEditorArea(getContainer(), this);
			return a;
		}

		return null;
	}

	public void setInput(IEditorInput input)
	{
		if(input instanceof PuakmaEditorInput == false)
			throw new IllegalArgumentException("Editor input has to be of instance PuakmaEditorInput");

		PuakmaEditorInput pInput = (PuakmaEditorInput) input;
		obj = pInput.getDesignObject();
		setPartName(input.getName());

		if(editorPage != null && editorPage instanceof IReusableEditor)
			((IReusableEditor) editorPage).setInput(new FileEditorInput(((PuakmaEditorInput) input).getFile()));
		if(browserArea != null) {
			browserArea.interrupt();
			browserArea.refresh();
		}
		setPartName(input.getName());

		super.setInput(input);
	}

	public void doSave(IProgressMonitor monitor)
	{
		if(monitor == null)
			monitor = new NullProgressMonitor();

		monitor.beginTask("Saving...", 2);

		try {
			if(propertiesArea.isDirty()) {
				propertiesArea.doSave(new SubProgressMonitor(monitor, 1));
			}

			if(editorPage != null && editorPage.isDirty()) {
				editorPage.doSave(new SubProgressMonitor(monitor, 1));

				// IN THE CASE OF JAVA OBJECT, WE DON'T NEED TO UPLOAD THE SOURCE,
				// BACKGROUND CHANGE LISTENER DOES IT FOR US
				if(obj instanceof JavaObject == false) {
					try {
						boolean isSource = false;
						ProjectUtils.uploadFile(obj, isSource);
					}
					catch(IdeException e) {
						VortexPlugin.log(e);
					}
				}
			}
		}
		finally {
			monitor.done();
		}
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException
	{
		if(!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");

		super.init(site, editorInput);
	}

	protected void pageChange(int newPageIndex)
	{
		getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);//$NON-NLS-1$

		if(browserArea != null)
			browserArea.interrupt();

		super.pageChange(newPageIndex);

		if(newPageIndex == browserPageIndex) {
			getEditorSite().getActionBarContributor().setActiveEditor(this);
			browserArea.refresh();
		}
		else if(newPageIndex == editorPageIndex) {
			getEditorSite().getActionBarContributor().setActiveEditor(editorPage);
		}
		else {
			getEditorSite().getActionBarContributor().setActiveEditor(this);
		}
	}

	public boolean isDirty()
	{
		return (editorPage != null ? editorPage.isDirty() : false) || 
				(propertiesArea != null ? propertiesArea.isDirty() : false);
	}

	public Object getAdapter(Class key)
	{
		Object adapter = super.getAdapter(key);
		if(adapter == null && editorPage != null)
			adapter = editorPage.getAdapter(key);
		return adapter;
	}

	public DesignObject getDesignObject()
	{
		return obj;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		String propName = evt.getPropertyName();
		if(propName == DesignObject.PROP_NAME) {
			if(editorPageIndex != -1) {
				boolean isSource = obj instanceof JavaObject;
				IFile file = ProjectUtils.getIFile(obj, isSource);
				String fileName = file.getName();
				setPageText(editorPageIndex, fileName);
				setPartName(obj.getName());
			}
		}
		else if(propName == ServerObject.PROP_CLOSE) {
			getSite().getPage().closeEditor(this, false);
		}
		// TODO: implement this event in core
		//else if(propName == ServerObject.PROP_REMOVE) {
		//  
		//}
	}
}
