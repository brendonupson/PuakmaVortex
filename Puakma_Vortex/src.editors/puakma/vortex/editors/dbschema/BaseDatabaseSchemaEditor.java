/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 20, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SaveAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.Splitter;

public abstract class BaseDatabaseSchemaEditor extends EditorPart implements CommandStackListener,
ISelectionListener
{
	private static final int PALETTE_SIZE = 120;

	private PaletteViewer paletteViewer;

	private GraphicalViewer graphicalViewer;

	private ActionRegistry actionRegistry;

	private List<String> selectionActions = new ArrayList<String>();

	private List<String> stackActions = new ArrayList<String>();

	private List<String> propertyActions = new ArrayList<String>();

	private DbEditorController controller;

	private SashForm form;

	private Splitter viewerSplitter;

	/**
	 * When the command stack changes, the actions interested in the command stack are
	 * updated.
	 * 
	 * @param event the change event
	 */
	public void commandStackChanged(EventObject event)
	{
		Display.getDefault().asyncExec(new Runnable() {
			public void run()
			{
				firePropertyChange(PROP_DIRTY);
				updateActions(stackActions);
			}
		});
	}

	/**
	 * Called to configure the graphical viewer before it receives its contents. This is
	 * where the root editpart should be configured. Subclasses should extend or override
	 * this method as needed.
	 */
	protected void configureGraphicalViewer()
	{
		GraphicalViewer viewer = getGraphicalViewer();

		viewer.getControl().setBackground(ColorConstants.listBackground);
		controller.configureGraphicalViewer(viewer);
	}

	/**
	 * Creates actions for this editor. Subclasses should override this method to create and
	 * register actions with the {@link ActionRegistry}.
	 */
	protected void createActions()
	{
		ActionRegistry registry = getActionRegistry();
		IAction action;

		action = new UndoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new RedoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new SelectAllAction(this);
		registry.registerAction(action);

		action = new DeleteAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new SaveAction(this);
		registry.registerAction(action);
		getPropertyActions().add(action.getId());

		action = new PrintAction(this);
		registry.registerAction(action);
	}

	/**
	 * Creates the GraphicalViewer on the specified <code>Composite</code>.
	 * 
	 * @param parent the parent composite
	 */
	protected void createGraphicalViewer(Composite parent)
	{
		// GraphicalViewer viewer = new ScrollingGraphicalViewer();
		// viewer.createControl(parent);
		GraphicalViewer viewer = getGraphicalViewerController().createGraphicalViewer(parent);
		setGraphicalViewer(viewer);

		configureGraphicalViewer();
		hookGraphicalViewer();
		initializeGraphicalViewer();
	}

	/**
	 * Realizes the Editor by creating it's Control.
	 * <P>
	 * WARNING: This method may or may not be called by the workbench prior to {@link
	 * #dispose()}.
	 * 
	 * @param parent the parent composite
	 */
	public void createPartControl(Composite parent)
	{
		form = new SashForm(parent, SWT.SMOOTH | SWT.VERTICAL);
		if(parent.getLayout() instanceof GridLayout) {
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			form.setLayoutData(gd);
		}
		// TODO: let client setup somehow proporions here...
		createNonGraphicalControl(form);
		createGraphicalControl(form);
		if(form.getChildren().length != 1)
			form.setWeights(new int[] { 30, 70 });
	}

	/**
	 * Maximizes/restores the graphical viewer.
	 */
	public void maximizeGraphicalViewer(boolean show)
	{
		if(show == false)
			form.setMaximizedControl(null);
		else
			form.setMaximizedControl(viewerSplitter);
	}

	/**
	 * Returns true if the graphical viewer is maximized.
	 */
	public boolean isGraphicalViewerMaximized()
	{
		return form.getMaximizedControl() == viewerSplitter;
	}

	/**
	 * This function creates a non-graphical control on the editor. So at the end there will
	 * be graphical editor, and also non graphical part. Those two part will be splited up
	 * on some Sash.
	 * 
	 * @param parent is the parent Composite
	 */
	protected abstract void createNonGraphicalControl(Composite parent);

	/**
	 * This creates a graphical control on the editor.
	 * 
	 * @param parent is the parent composite of the graphical control
	 */
	protected void createGraphicalControl(Composite parent)
	{
		viewerSplitter = new Splitter(parent, SWT.HORIZONTAL);
		createPaletteViewer(viewerSplitter);
		createGraphicalViewer(viewerSplitter);
		viewerSplitter.maintainSize(getPaletteViewer().getControl());
		viewerSplitter.setFixedSize(getInitialPaletteSize());
		viewerSplitter.addFixedSizeChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt)
			{
				handlePaletteResized(((Splitter) evt.getSource()).getFixedSize());
			}
		});
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose()
	{
		getCommandStack().removeCommandStackListener(this);
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);

		controller.dispose();
		getActionRegistry().dispose();
		super.dispose();
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#firePropertyChange(int)
	 */
	protected void firePropertyChange(int property)
	{
		super.firePropertyChange(property);
		updateActions(propertyActions);
	}

	/**
	 * Lazily creates and returns the action registry.
	 * 
	 * @return the action registry
	 */
	protected ActionRegistry getActionRegistry()
	{
		if(actionRegistry == null)
			actionRegistry = new ActionRegistry();
		return actionRegistry;
	}

	/**
	 * Returns the adapter for the specified key.
	 * 
	 * <P>
	 * <EM>IMPORTANT</EM> certain requests, such as the property sheet, may be made before
	 * or after {@link #createPartControl(Composite)} is called. The order is unspecified by
	 * the Workbench.
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class type)
	{
		if(type == GraphicalViewer.class)
			return getGraphicalViewer();
		else if(type == CommandStack.class)
			return getCommandStack();
		else if(type == ActionRegistry.class)
			return getActionRegistry();
		else if(type == IContentOutlinePage.class)
			return getGraphicalViewerController().createTreeOutlinePage(this);
		else if(type == ZoomManager.class)
			return getGraphicalViewer().getProperty(ZoomManager.class.toString());
		else if(type == EditPart.class && getGraphicalViewer() != null)
			return getGraphicalViewer().getRootEditPart();
		else if(type == IFigure.class && getGraphicalViewer() != null)
			return ((GraphicalEditPart) getGraphicalViewer().getRootEditPart()).getFigure();
		return super.getAdapter(type);
	}

	/**
	 * Returns the command stack.
	 */
	protected CommandStack getCommandStack()
	{
		return getEditDomain().getCommandStack();
	}

	/**
	 * Returns the edit domain.
	 */
	protected DefaultEditDomain getEditDomain()
	{
		return controller.getEditDomain();
	}

	/**
	 * Returns the graphical viewer.
	 */
	protected GraphicalViewer getGraphicalViewer()
	{
		return graphicalViewer;
	}

	public DbEditorController getGraphicalViewerController()
	{
		return controller;
	}

	/**
	 * Returns the list of {@link IAction IActions} dependant on property changes in the
	 * Editor. These actions should implement the {@link UpdateAction} interface so that
	 * they can be updated in response to property changes. An example is the "Save" action.
	 * 
	 * @return the list of property-dependant actions
	 */
	protected List<String> getPropertyActions()
	{
		return propertyActions;
	}

	/**
	 * Returns the list of {@link IAction IActions} dependant on changes in the workbench's
	 * {@link ISelectionService}. These actions should implement the {@link UpdateAction}
	 * interface so that they can be updated in response to selection changes. An example is
	 * the Delete action.
	 * 
	 * @return the list of selection-dependant actions
	 */
	protected List<String> getSelectionActions()
	{
		return selectionActions;
	}

	/**
	 * Returns the selection syncronizer object. The synchronizer can be used to sync the
	 * selection of 2 or more EditPartViewers.
	 */
	protected SelectionSynchronizer getSelectionSynchronizer()
	{
		return getGraphicalViewerController().getSelectionSynchronizer();
	}

	/**
	 * Returns the list of {@link IAction IActions} dependant on the CommmandStack's state.
	 * These actions should implement the {@link UpdateAction} interface so that they can be
	 * updated in response to command stack changes. An example is the "undo" action.
	 * 
	 * @return the list of stack-dependant actions
	 */
	protected List<String> getStackActions()
	{
		return stackActions;
	}

	/**
	 * Hooks the GraphicalViewer to the rest of the Editor. By default, the viewer is added
	 * to the SelectionSynchronizer, which can be used to keep 2 or more EditPartViewers in
	 * sync. The viewer is also registered as the ISelectionProvider for the Editor's
	 * PartSite.
	 */
	protected void hookGraphicalViewer()
	{
		getSite().setSelectionProvider(getGraphicalViewer());
	}

	/**
	 * Sets the site and input for this editor then creates and initializes the actions.
	 * Subclasses may extend this method, but should always call
	 * <code>super.init(site, input)
	 * </code>.
	 * 
	 * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		setSite(site);
		setInput(input);
		initializeController();
		getCommandStack().addCommandStackListener(this);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		initializeActionRegistry();
	}

	protected abstract void initializeController();

	protected void setController(DbEditorController controller)
	{
		this.controller = controller;
	}

	/**
	 * Initializes the ActionRegistry. This registry may be used by {@link
	 * ActionBarContributor ActionBarContributors} and/or {@link ContextMenuProvider
	 * ContextMenuProviders}.
	 * <P>
	 * This method may be called on Editor creation, or lazily the first time {@link
	 * #getActionRegistry()} is called.
	 */
	protected void initializeActionRegistry()
	{
		createActions();
		updateActions(propertyActions);
		updateActions(stackActions);
	}

	/**
	 * Override to set the contents of the GraphicalViewer after it has been created.
	 * 
	 * @see #createGraphicalViewer(Composite)
	 */
	protected void initializeGraphicalViewer()
	{
		getGraphicalViewerController().initializeViewer(getGraphicalViewer());
	}

	/**
	 * Returns <code>true</code> if the command stack is dirty
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty()
	{
		return getCommandStack().isDirty();
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{
		// If not the active editor, ignore selection changed.
		if(this.equals(getSite().getPage().getActiveEditor()))
			updateActions(selectionActions);
	}

	/**
	 * Sets the ActionRegistry for this EditorPart.
	 * 
	 * @param registry the registry
	 */
	protected void setActionRegistry(ActionRegistry registry)
	{
		actionRegistry = registry;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		getGraphicalViewer().getControl().setFocus();
	}

	/**
	 * Sets the graphicalViewer for this EditorPart.
	 * 
	 * @param viewer the graphical viewer
	 */
	protected void setGraphicalViewer(GraphicalViewer viewer)
	{
		this.graphicalViewer = viewer;
	}

	/**
	 * A convenience method for updating a set of actions defined by the given List of
	 * action IDs. The actions are found by looking up the ID in the
	 * {@link #getActionRegistry() action registry}. If the corresponding action is an
	 * {@link UpdateAction}, it will have its <code>update()</code> method called.
	 * 
	 * @param actionIds the list of IDs to update
	 */
	protected void updateActions(List<String> actionIds)
	{
		ActionRegistry registry = getActionRegistry();
		Iterator<String> iter = actionIds.iterator();
		while(iter.hasNext()) {
			IAction action = registry.getAction(iter.next());
			if(action instanceof UpdateAction)
				((UpdateAction) action).update();
		}
	}

	/**
	 * Creates the palette on the given composite.
	 * 
	 * @param parent the composite
	 */
	protected void createPaletteViewer(Composite parent)
	{
		PaletteViewer viewer = new PaletteViewer();
		setPaletteViewer(viewer);
		viewer.createControl(parent);

		configurePaletteViewer();
		hookPaletteViewer();
		initializePaletteViewer();
	}

	/**
	 * Called to configure the viewer before it receives its contents.
	 */
	protected void configurePaletteViewer()
	{
		getGraphicalViewerController().configurePalleteViewer(getPaletteViewer());
	}

	/**
	 * Returns the initial palette size in pixels. Subclasses may override this method to
	 * return a persisted value.
	 * 
	 * @see #handlePaletteResized(int)
	 * @return the initial size of the palette in pixels.
	 */
	protected int getInitialPaletteSize()
	{
		return PALETTE_SIZE;
	}

	/**
	 * Returns the PaletteViewer.
	 * 
	 * @return the palette viewer
	 */
	protected PaletteViewer getPaletteViewer()
	{
		return paletteViewer;
	}

	/**
	 * Called whenever the user resizes the palette.
	 * 
	 * @param newSize the new size in pixels
	 */
	protected void handlePaletteResized(int newSize)
	{
	}

	/**
	 * Called when the palette viewer is set. By default, the EditDomain is given the
	 * palette viewer.
	 */
	protected void hookPaletteViewer()
	{

	}

	/**
	 * Called to populate the palette viewer.
	 */
	protected void initializePaletteViewer()
	{
	}

	/**
	 * Sets the palette viewer
	 * 
	 * @param paletteViewer the palette viewer
	 */
	protected void setPaletteViewer(PaletteViewer paletteViewer)
	{
		this.paletteViewer = paletteViewer;
	}
}
