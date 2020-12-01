/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 22, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project.queue;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ILogger;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.ResizableDialog;

public class DiffDialog extends ResizableDialog implements ISelectionChangedListener, IProgressMonitor
{
  private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
  private static final String DIALOG_ID = "DiffDialog";
  private final DiffController controller;
  private Table objectsTable;
  private CompareConfiguration compareConfiguration;
  private Label changedAuthorLabel;
  private Label changedTimeLabel;
  private Label localTimeLabel;
  private DiffNode[] diffs;
  private CompareViewerSwitchingPane pane;
  private TableViewer tableViewer;
  /**
   * {@link Composite} for progress
   */
  private Composite mainComposite;
  private Composite progressComposite;
  private Label progressLabel;
  private ProgressBar progressBar;
  private double work;
  private String taskName;
  private int worked;
  private String mainTaskName;
  private int totalWork;
  private Composite buttonsBar;
  /**
   * This is {@link Composite} which holds selection buttons. Every time
   * selection changes, we dispose this composite, and recreate it.
   */
  private Composite buttonsBarSubComposite;
  private Button uploadToServerButton;
  private Button downloadFromServerButton;
  private Button ignoreUploadButton;
  private DiffNode currentDiff;

  /**
   * Constructs the dialog on the top of the parent shell with the content of
   * changes.
   * 
   * @param shell is the parent shell
   * @param items is the array with all the changes which needs to be displayed
   */
  public DiffDialog(Shell shell, HistoryItem[] items)
  {
    super(shell, DIALOG_ID);
    
    this.controller = new DiffController();

    int x = items.length;
    for(int i = 0; i < items.length; ++i) {
      if(items[i].isSource() == false && items[i].getDesignObject() instanceof JavaObject)
        x--;
    }
    
    // NOW INITIALIZE ALL THE DIFFS IN changes VARIABLE
    diffs = new DiffNode[x];
    int j = 0;
    for(int i = 0; i < items.length; i++) {
      HistoryItem left = items[i];
      HistoryItem right = new HistoryItem(left.getDesignObject(), DesignObject.REV_CURRENT, ! left.isLocal());
      right.setSource(left.isSource());
      DiffNode node = new DiffNode(left, right);
      diffs[j] = node;
      ++j;
    }
  }

  protected Control createDialogArea(Composite parent)
  {
    getShell().setText("Upload Conflict");
    
    Composite container = (Composite) super.createDialogArea(parent);
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    DialogBuilder2 builder = new DialogBuilder2(container);
    SashForm form = builder.createSashForm(false);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    form.setLayoutData(gd);
    objectsTable = builder.createTable();

    mainComposite = builder.createComposite(1);
    createInfoPanel(builder);

    this.compareConfiguration = new CompareConfiguration();
    pane = new CompareViewerSwitchingPane(mainComposite, SWT.NONE, true) {
      protected Viewer getViewer(Viewer oldViewer, Object input)
      {
        return CompareUI.findContentViewer(oldViewer, (ICompareInput) input, this,
                                           DiffDialog.this.compareConfiguration);
      }
    };
    pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    builder.closeComposite();
    
    form.setWeights(new int[] { 2, 5 });
    builder.closeSashForm();
    builder.finishBuilder();

    // INITIALIZE VIEWERS
    DiffController.setupColumns(objectsTable);
    tableViewer = builder.setupTableViewer(objectsTable, controller, controller, DiffController.getProperties(),
                                           controller, DiffController.getEditors());
    tableViewer.addSelectionChangedListener(this);
    tableViewer.setInput(diffs);
    if(diffs.length >= 1)
      tableViewer.setSelection(new StructuredSelection(diffs[0]));
    objectsTable.setFocus();
    return container;
  }

  /**
   * This function creates a information panel with info about both local, and
   * server side stuff.
   * 
   * @param builder is the {@link DialogBuilder2} object supplying controls
   */
  private void createInfoPanel(DialogBuilder2 builder)
  {
    //SashForm form = builder.createSashForm(false);
    
    // CREATE LEFT PANE
    Composite c = builder.createComposite(2);
    localTimeLabel = builder.createTwoLabelRow("Local time:", "");
    builder.closeComposite();
    
    // CREATE RIGHT PANE
    c = builder.createComposite(2);
    changedTimeLabel = builder.createTwoLabelRow("Changed time:", "");
    changedAuthorLabel = builder.createTwoLabelRow("Changed author:", "");
    builder.closeComposite();
    
    // AND NOW CREATE BUTTONS BAR WITH BUTTONS WHICH ARE SELECTED BY USER WHAT
    // TO DO, BUT NOTE THAT WE USE TRICK HERE TO RENDER NICER BUTTONS
    buttonsBar = builder.createComposite();//FormsLFComposite(null, false, 1);
    builder.closeComposite();
    
    // DO SOME FINAL SETUP
    //form.setWeights(new int[] {1, 1});
    //builder.closeSashForm();
  }

  public void selectionChanged(SelectionChangedEvent event)
  {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    if(selection.size() != 1) {
      pane.setInput(null);
      
      if(this.buttonsBarSubComposite != null) {
        buttonsBarSubComposite.dispose();
        buttonsBarSubComposite = null;
      }
    }
    else {
      if(currentDiff == selection.getFirstElement())
        return;

      currentDiff = (DiffNode) selection.getFirstElement();
      HistoryItem left = (HistoryItem) currentDiff.getLeft();
      HistoryItem right = (HistoryItem) currentDiff.getRight();
      int workToBeDone = 0;
      if(left.isPrepared() == false)
        workToBeDone++;
      if(right.isPrepared() == false)
        workToBeDone++;
      
      if(workToBeDone > 0) {
        showProgressUI(true);
        
        try {
          left.prepareContent(new SubProgressMonitor(this, 1));
        }
        catch(Exception e) {
          VortexPlugin.log(e);
        }
        
        try {
          right.prepareContent(new SubProgressMonitor(this, 1));
        }
        catch(Exception e) {
          VortexPlugin.log(e);
        }
        finally {
          showProgressUI(false);
        }
      }
      
      this.changedAuthorLabel.setText(right.getAuthorName());
      this.changedTimeLabel.setText(formatDate(right.getModificationDate()));
      this.localTimeLabel.setText(formatDate(right.getLocalTime()));
      
      pane.setInput(currentDiff);
      
      setupButtons(currentDiff, left);
    }
  }

  /**
   * This function sets up buttons which are selected by user what to do with
   * the history item.
   */
  private void setupButtons(DiffNode node, final HistoryItem leftHistory)
  {
    // ALSO SETUP BUTTONS BAR
    
    // NA DODELANI: TADY SE MUSI NASTAVIT JAKA TLACITKA BUDEME POUZIVAT PRO ZVOLENI DALSIHO POSTUPU.
    // NAPRIKLAD MUZEME POUZIT
    if(this.buttonsBarSubComposite != null) {
      buttonsBarSubComposite.dispose();
      buttonsBarSubComposite = null;
    }
    DialogBuilder2 builder = new DialogBuilder2(buttonsBar);
    
    buttonsBarSubComposite = builder.createComposite(3);
    GridData gd = (GridData) buttonsBarSubComposite.getLayoutData();
    gd.grabExcessHorizontalSpace = gd.grabExcessVerticalSpace = false;
    buttonsBarSubComposite.setLayoutData(gd);
    // FOR UPLOAD WE HAVE ONLY THREE CHOICES:
    // (1) upload to the server -> default
    // (2) download from the server
    // (3) ignore
    // NOTE THAT IN THE FUTURE THERE MIGHT BE MERGE
    uploadToServerButton = builder.createSingleToogleButton("Upload To Server");
    downloadFromServerButton = builder.createSingleToogleButton("Download From Server");
    ignoreUploadButton = builder.createSingleToogleButton("Ignore Everything");
    builder.setupToogleButtonsAsRadios(new Button[] { uploadToServerButton,
        downloadFromServerButton, ignoreUploadButton });
    
    // CREATE INITIAL SETUP FOR BUTTONS
    SelectionAdapter adapter = new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        if(e.widget == uploadToServerButton)
          leftHistory.setUserAnswer(HistoryItem.ACTION_USE_LOCAL);
        else if(e.widget == downloadFromServerButton)
          leftHistory.setUserAnswer(HistoryItem.ACTION_USE_SERVER);
        else if(e.widget == ignoreUploadButton)
          leftHistory.setUserAnswer(HistoryItem.ACTION_DONT_CARE);
        else
          VortexPlugin.log("Invalid widget - " + e.widget, ILogger.ERROR_ERROR);
      }
    };
    uploadToServerButton.addSelectionListener(adapter);
    downloadFromServerButton.addSelectionListener(adapter);
    ignoreUploadButton.addSelectionListener(adapter);
    
    switch(leftHistory.getUserAnswer()) {
      case HistoryItem.ACTION_USE_LOCAL:
        uploadToServerButton.setSelection(true);
      break;
      case HistoryItem.ACTION_USE_SERVER:
        downloadFromServerButton.setSelection(true);
      break;
      case HistoryItem.ACTION_DONT_CARE:
        ignoreUploadButton.setSelection(true);
      break;
    }
    
    builder.closeComposite();
    builder.finishBuilder();
  }
  
  /**
   * Formats the time according to the requested time.
   *
   * @param time is the time we want to format
   * @return String with formated time
   */
  private String formatDate(long time)
  {
    return DATE_FORMAT.format(new Date(time));
  }

  private void showProgressUI(boolean show)
  {
    if(show) {
      if(progressComposite != null)
        return;
      
      DialogBuilder2 builder = new DialogBuilder2(mainComposite);
      progressComposite = builder.createComposite();
      
      progressLabel = builder.createTwoLabelRow("Progress:", "");
      progressBar = builder.createProgressBarRow(true);
      builder.closeComposite();
      
      builder.finishBuilder();
    }
    else {
      if(progressComposite == null)
        return;
      
      progressComposite.dispose();
      progressLabel = null;
      progressBar = null;
      progressComposite = null;
    }
  }

//  protected void okPressed()
//  {
////    final Object waitingFamily = new Object();
//    close();
//    
////    CompareRunnable runnable = new CompareRunnable(diffs);
////    PlatformUI.getWorkbench().getProgressService().showInDialog(null, runnable);
//    
//    diffs = null;
////    IRunnableContext context = PlatformUI.getWorkbench().getProgressService();
////    try {
////      context.run(true, false, runnable);
////    }
////    catch(InvocationTargetException e) {
////      VortexPlugin.log(e);
////    }
////    catch(InterruptedException e) {
////      // IF INTERRUPTED, SIMPLY IGNORE
////    }
//  }

  public void beginTask(String name, int totalWork)
  {
    this.mainTaskName = name;
    this.totalWork = totalWork;
    this.progressBar.setMinimum(0);
    this.progressBar.setMaximum(totalWork);
    this.progressLabel.setText(name);
  }

  public void done()
  {
    this.progressBar.setSelection(totalWork);
    this.progressLabel.setText("Done.");
    totalWork = 0;
    mainTaskName = null;
    taskName = null;
    worked = 0;
  }

  public void internalWorked(double work)
  {
    this.work += work;
  }

  public boolean isCanceled()
  {
    return false;
  }

  public void setCanceled(boolean value)
  {
    
  }

  public void setTaskName(String name)
  {
    this.taskName = name;
    progressLabel.setText(name);
  }

  public void subTask(String name)
  {
    
  }

  public void worked(int work)
  {
    this.worked += work;
  }

  protected void cancelPressed()
  {
    setupCancel();
    
    super.cancelPressed();
  }
  
  protected void handleShellCloseEvent()
  {
    setupCancel();
    
    super.handleShellCloseEvent();
  }

  /**
   * Setups all result items to {@link HistoryItem#ACTION_DONT_CARE}. THis
   * should be called only when user cancels dialog by the cancel button or
   * close button in the title bar.
   */
  private void setupCancel()
  {
    for(int i = 0; i < diffs.length; ++i) {
      HistoryItem item = (HistoryItem) diffs[i].getLeft();
      item.setUserAnswer(HistoryItem.ACTION_DONT_CARE);
    }
  }
}
