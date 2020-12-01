package puakma.vortex.editors.dbeditor;

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.swt.widgets.Composite;

import puakma.coreide.objects2.Database;
import puakma.vortex.editors.dbschema.BaseDatabaseSchemaEditor;

public interface DbEditorController
{
  /**
   * Creates and hooks up the graphical viewer.
   */
  public GraphicalViewer createGraphicalViewer(Composite parent);

  public void configureGraphicalViewer(GraphicalViewer viewer);

  public void configurePalleteViewer(PaletteViewer viewer);

  public DefaultEditDomain getEditDomain();

  /**
   * Unhooks all listeners, and destroys all resources.
   */
  public void dispose();

  public SelectionSynchronizer getSelectionSynchronizer();

  public PaletteRoot getPaletteRoot();

  /**
   * Returns {@link Database} object. Note that the database which is hold by the
   * controller is always working copy.
   */
  public Database getDatabase();

  public ContentOutlinePage createTreeOutlinePage(BaseDatabaseSchemaEditor editor);

  /**
   * This function initializes viewer's content
   */
  public void initializeViewer(GraphicalViewer viewer);

  public CommandStack getCommandStack();

  /**
   * When this is called, the dirty status has been changed, and controller must inform
   * all the listeners.
   */
  public void dirtyChanged();

  /**
   * This sets up the position properties for the model.
   */
  public void setupGraphicalPropertiesForModel(Properties props);

  /**
   * Returns {@link Properties} in which are stored all information about the viewer,
   * about the positions in the viewer, etc..
   */
  public Properties getProperties();

  /**
   * Loads properties from the existing connection from the server.
   */
  public void loadPreferences();

  /**
   * Saves the whole model to the server.
   */
  public void saveAllStuffToServer(IProgressMonitor monitor);

  public void saveAllStuffToFile(IFile file) throws CoreException;
}
