/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    04/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema.generator;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.datatools.sqltools.sqleditor.internal.PreferenceConstants;
import org.eclipse.datatools.sqltools.sqleditor.internal.SQLEditorPlugin;
import org.eclipse.datatools.sqltools.sqleditor.internal.sql.SQLPartitionScanner;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import puakma.coreide.database.DatabaseGenerator2;
import puakma.coreide.database.SQLCommandDescriptor;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;

public class GeneratorMainPage extends WizardPage
{
  private DatabaseGenerator2 generator;
  private SourceViewer viewer;
  private SashForm sash;
  private List errorLog;
  private SQLCommandDescriptor[] regions;

  public GeneratorMainPage()
  {
    super("generatorMainPage");
    
    setTitle("Execute SQL");
    setDescription("Execute sql script to create database schema");
  }

  public void createControl(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    sash = builder.createSashForm(false);
    viewer = builder.createSourceViewer();
    builder.createComposite();
    errorLog = builder.appendList(SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    errorLog.setLayoutData(gd);
    builder.closeComposite();
    
    sash.setWeights(new int[] {4, 1});
    sash.setMaximizedControl(viewer.getControl());
    builder.closeSashForm();
    builder.finishBuilder();
    
    setControl(sash);
    
    initialize();
  }
  
  /**
   * This function should setup the source viewer as a sql editor.
   */
  private void initialize()
  {
    IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
//    ISharedTextColors sharedColors= EditorsPlugin.getDefault().getSharedTextColors();
//    ProjectionSupport projSupport = new ProjectionSupport(viewer, annotationAccess, sharedColors);
//    projSupport.addSummarizableAnnotationType( "org.eclipse.ui.workbench.texteditor.error" ); //$NON-NLS-1$
//    projSupport.addSummarizableAnnotationType( "org.eclipse.ui.workbench.texteditor.warning" ); //$NON-NLS-1$
//    projSupport.install();
    
    viewer.doOperation(ProjectionViewer.TOGGLE);
    
    IDocument doc = viewer.getDocument();
    if(doc instanceof IDocumentExtension3) {
      IDocumentExtension3 extension3 = (IDocumentExtension3) doc;
      IPartitionTokenScanner sqlPartScanner = SQLEditorPlugin.getDefault().getSQLPartitionScanner();
      IDocumentPartitioner part = new FastPartitioner(sqlPartScanner,  SQLPartitionScanner.SQL_PARTITION_TYPES);
      part.connect(doc);
      //extension3.setDocumentPartitioner(SQLEditorPlugin.SQL_PARTITIONING, part);
    }
    
    SQLSimpleSourceViewerConfiguration conf = new SQLSimpleSourceViewerConfiguration();
    viewer.configure(conf);
    
    IPreferenceStore preferenceStore = SQLEditorPlugin.getDefault().getPreferenceStore();
    boolean closeSingleQuotes = preferenceStore.getBoolean(PreferenceConstants.SQLEDITOR_CLOSE_SINGLE_QUOTES);
    boolean closeDoubleQuotes = preferenceStore.getBoolean(PreferenceConstants.SQLEDITOR_CLOSE_DOUBLE_QUOTES);
    boolean closeBrackets = preferenceStore.getBoolean(PreferenceConstants.SQLEDITOR_CLOSE_BRACKETS);
    
    errorLog.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        showCurrentError();
      }
    });
    errorLog.addMouseListener(new MouseAdapter() {
      public void mouseDoubleClick(MouseEvent e)
      {
        showCurrentError();
      }
    });
  }

  /**
   * Selects the current SQL error inside the viewer.
   */
  protected void showCurrentError()
  {
    int index = errorLog.getSelectionIndex();
    if(index == -1)
      return;
    
    SQLCommandDescriptor reg = regions[index];
    int start = reg.getStart();
    int end = reg.getEnd();
    viewer.setSelectedRange(start, end - start);
    viewer.getTextWidget().setFocus();
  }

  public void setGenerator(DatabaseGenerator2 generator)
  {
    this.generator = generator;
    try {
      String sql = generator.generateSql();
      Document d = new Document(sql);
      viewer.setInput(d);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }
  
  public DatabaseGenerator2 getGenerator()
  {
    return generator;
  }
  
  /**
   * Returns the text in the sql editor field
   */
  public String getSqlText()
  {
    return viewer.getTextWidget().getText();
  }
  
  /**
   * This function shows sql region inside the viewer.
   */
  public void setLogMessage(SQLCommandDescriptor region)
  {
    if(sash.getMaximizedControl() != null)
      sash.setMaximizedControl(null);

    viewer.setSelectedRange(region.getStart(), region.getEnd());
  }

  /**
   * Returns the document used in source viewer.
   */
  public IDocument getSQLDocument()
  {
    return viewer.getDocument();
  }
  
  public void displayErrors(java.util.List commandsList)
  {
    errorLog.removeAll();
    
    if(commandsList.size() > 0) {
      // ADD ALL REGIONS WITH ERROR TO THE LIST OF ERRORS
      java.util.List<SQLCommandDescriptor> l = new ArrayList<SQLCommandDescriptor>();
      Iterator<SQLCommandDescriptor> it = commandsList.iterator();
      while(it.hasNext()) {
        SQLCommandDescriptor res = (SQLCommandDescriptor) it.next();
        if(res.exceptionStackTrace != null) {
          l.add(res);
        }
      }
      this.regions = (SQLCommandDescriptor[]) l.toArray(new SQLCommandDescriptor[l.size()]);
      
      // FILL THE ERROR LOG
      for(int i = 0; i < regions.length; ++i) {
        String error = StringUtil.safeString(regions[i].exceptionMessage);
        errorLog.add(error);
      }
      
      // AND THEN SHOW SOME ERROR
      if(regions.length > 0) {
        errorLog.select(0);
        sash.setMaximizedControl(null);
        showCurrentError();
      }
    }
  }
}
