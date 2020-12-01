package puakma.vortex.views.console;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Server;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;

/**
 * This is a console view displaying the Tornado Server log.
 * 
 * @author Martin Novak
 */
public class TornadoConsoleView extends ViewPart implements PropertyChangeListener, IPropertyChangeListener
{
	public static final String VIEW_ID = "puakma.vortex.views.TornadoConsoleView";

	private Text commandLine;

	private Text logText;

	private Action clearLogAction;

	private Combo serverCombo;

	private Server server;

	private Map<String, Server> serversComboMap = new HashMap<String, Server>();

	private Button executeButton;

	private boolean clearAfterExec;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent)
	{
		createUI(parent);
		initializeUI();
		makeActions();
		contributeToActionBars();
	}

	/**
	 * Creates a user interface of this view. There is serverCombo which selects
	 * what server we want to monitor commandLine into which you write commands,
	 * and also logText where you will see the server's output.
	 */
	private void createUI(Composite parent)
	{
		GridLayout gl = new GridLayout(4, false);
		gl.marginWidth = gl.marginHeight = 0;
		gl.verticalSpacing = gl.horizontalSpacing = 0;
		parent.setLayout(gl);

		Label l = new Label(parent, SWT.NULL);
		l.setText("Server:");
		GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd.horizontalIndent = 5;
		l.setLayoutData(gd);

		serverCombo = new Combo(parent, SWT.READ_ONLY);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.minimumWidth = 30;
		serverCombo.setLayoutData(gd);

		commandLine = new Text(parent, SWT.BORDER | SWT.SINGLE);
		commandLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		commandLine.addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event)
			{
				if(event.character == '\r' || event.character == '\n')
					execute();
			}
		});

		executeButton = new Button(parent, SWT.PUSH);
		executeButton.setText("Execute");
		executeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		executeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				execute();
			}
		});

		logText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 4;
		logText.setLayoutData(gd);
		logText.setEditable(false);
		//    logText.setForeground()
	}

	private void initializeUI()
	{
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(this);

		Color bg = new Color(null, PreferenceConverter.getColor(store, PreferenceConstants.PREF_CONSOLE_BG_COLOR));
		Color fg = new Color(null, PreferenceConverter.getColor(store, PreferenceConstants.PREF_CONSOLE_FG_COLOR));

		//    commandLine.setForeground(fg);
		//    commandLine.setBackground(bg);

		logText.setForeground(fg);
		logText.setBackground(bg);

		clearAfterExec = store.getBoolean(PreferenceConstants.CONSOLE_CLEAR_AFTER_EXECUTE);

		// AT FIRST WE SHOULD FILL UP THE SERVER COMBO
		Server[] s = ServerManager.listConnectedServers();
		if(s.length == 0) {
			commandLine.setEnabled(false);
			executeButton.setEnabled(false);
		}

		for(int i = 0; i < s.length; ++i)
			addServer(s[i]);
		// AND ADD LISTENER
		ServerManager.addListener(this);

		serverCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				serverSelected();
			}
		});
	}

	private void addServer(Server server)
	{
		synchronized (server)
		{
			String text = server.getUserName() + "@" + server.getHost();
			serversComboMap.put(text, server);
			serverCombo.add(text);
			if(serverCombo.getItemCount() == 1)
			{
				serverCombo.select(0);
			}

			commandLine.setEnabled(true);
			executeButton.setEnabled(true);
		}
	}

	private void removeServer(Server server)
	{
		Server[] s = ServerManager.listConnectedServers();
		if(s.length == 0) {
			commandLine.setEnabled(false);
			executeButton.setEnabled(false);
		}

		String text = server.getUserName() + "@" + server.getHost();
		String[] items = serverCombo.getItems();
		for(int i = 0; i < items.length; ++i) {
			if(items[i].equals(text)) {
				int currentIndex = serverCombo.getSelectionIndex();
				if(i == currentIndex) {
					if(serverCombo.getItemCount() > 0)
						serverCombo.select(0);
				}

				serversComboMap.remove(text);
				serverCombo.remove(i);
				logText.setText("");
				return;
			}
		}
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(clearLogAction);
		manager.add(new Separator());
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(clearLogAction);
	}

	private void makeActions()
	{
		clearLogAction = new Action() {
			public void run()
			{
				clearLog();
			}
		};
		clearLogAction.setText("Clear log");
		clearLogAction.setToolTipText("Clears Server Log");
		clearLogAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
	{
		if(commandLine.isEnabled())
			commandLine.setFocus();
		else
			serverCombo.setFocus();
	}

	private void execute()
	{
		if(server == null)
			return;
		String command = commandLine.getText();
		if(command.length() == 0)
			return;

		if(clearAfterExec)
			logText.setText("");

		try {
			String response = server.executeCommand(command);
			logText.append(response);
			logText.append(logText.getLineDelimiter());
			logText.setSelection(logText.getCharCount());
		}
		catch(Exception e) {
			logText.append(e.getLocalizedMessage());
			logText.append(logText.getLineDelimiter());
			logText.setSelection(logText.getCharCount());
		}
		commandLine.selectAll();
	}

	private void clearLog()
	{
		logText.setText("");
	}

	private void serverSelected()
	{ 
		// AT FIRST WE SHOULD GET THE NEW SERVER
		String serverText = serverCombo.getText();
		Server s = (Server) serversComboMap.get(serverText);
		if(s == null)
			throw new IllegalStateException("Cannot find the proper server!");

		this.server = s;
		logText.setText("");
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		String prop = evt.getPropertyName();
		if(ServerManager.PROP_SERVER_CONNECTED.equals(prop)) {
			addServer((Server) evt.getNewValue());
		}
		else if(ServerManager.PROP_SERVER_CLOSE.equals(prop)) {
			removeServer((Server) evt.getOldValue());
		}
	}

	public void dispose()
	{
		super.dispose();

		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		store.removePropertyChangeListener(this);

		ServerManager.removeListener(this);
	}

	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event)
	{
		String prop = event.getProperty();
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();

		if(PreferenceConstants.PREF_CONSOLE_BG_COLOR.equals(prop)) {
			Color fg = new Color(null, PreferenceConverter.getColor(store, PreferenceConstants.PREF_CONSOLE_FG_COLOR));
			logText.setForeground(fg);
		}
		else if(PreferenceConstants.PREF_CONSOLE_FG_COLOR.equals(prop)) {
			Color bg = new Color(null, PreferenceConverter.getColor(store, PreferenceConstants.PREF_CONSOLE_BG_COLOR));
			logText.setBackground(bg);  
		}
		else if(PreferenceConstants.CONSOLE_CLEAR_AFTER_EXECUTE.equals(prop)) {
			clearAfterExec = store.getBoolean(PreferenceConstants.CONSOLE_CLEAR_AFTER_EXECUTE);
		}
	}
}
