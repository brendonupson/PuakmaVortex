package ru.nlmk.editors.position;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import ru.nlmk.editors.position.preferences.PositionPreferencePage;
/**
 * @author lobas_av
 */
public class PositionPlugin extends AbstractUIPlugin implements IStartup, IPropertyChangeListener
{
	private static PositionPlugin m_plugin;
	private HashMap m_listeners = new HashMap();
	private HashMap m_positions;
	private HashMap m_counts;
	private String m_storeFile;
	private String m_countFile;
	private boolean m_disable = false;
	private boolean m_source = true;
	private boolean m_restoreAutoOpen = true;
	private int m_countLimit = 1000;
	//
	public PositionPlugin(IPluginDescriptor descriptor)
	{
		super(descriptor);
		m_plugin = this;
	}
	public static PositionPlugin getDefault()
	{
		return m_plugin;
	}
	public static void log(String message, Throwable e)
	{
		getDefault().getLog().log(new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), IStatus.ERROR, message, e)
		{
			public boolean isMultiStatus()
			{
				return true;
			}
		});
	}
	protected void loadPositions()
	{
        m_storeFile = Platform.getPluginStateLocation(this) + "/position.bin";
        File fileStore = new File(m_storeFile);
		//
		if(fileStore.exists())
		{
			try
			{
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(fileStore));
				m_positions = (HashMap)stream.readObject();
				stream.close();
			}
			catch(Exception e)
			{
				log("Error load position file: ", e);
				m_positions = new HashMap();
			}
		}
		else
		{
			m_positions = new HashMap();
		}
		//
		m_countFile = Platform.getPluginStateLocation(this) + "/counts.bin";
		File fileCount = new File(m_countFile);
		//
		if(fileStore.exists())
		{
			try
			{
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(fileCount));
				m_counts = (HashMap)stream.readObject();
				stream.close();
			}
			catch(Exception e)
			{
				log("Error load count file: ", e);
				m_counts = new HashMap();
			}
		}
		else
		{
			m_counts = new HashMap();
		}
	}
	protected void savePositionData()
	{
		try
		{
			ObjectOutputStream streamPosition = new ObjectOutputStream(new FileOutputStream(m_storeFile));
			streamPosition.writeObject(m_positions);
			streamPosition.close();
			//
			ObjectOutputStream streamCount = new ObjectOutputStream(new FileOutputStream(m_countFile));
			streamCount.writeObject(m_counts);
			streamCount.close();
		}
		catch(Exception e)
		{
			log("Error save position data: ", e);
		}
	}
	protected void saveOpenEditors()
	{
		for(Iterator I = m_listeners.entrySet().iterator(); I.hasNext();)
		{
			Map.Entry element = (Map.Entry)I.next();
			closeEditor((PartPositionHandler)element.getValue());
		}
	}
	protected void handlePreOpened(final IWorkbenchWindow window)
	{
		getWorkbench().getDisplay().syncExec(new Runnable()
		{
			public void run()
			{
				IWorkbenchPage[] pages = window.getPages();
				//
				for(int i = 0; i < pages.length; i++)
				{
					IEditorReference[] ref = pages[i].getEditorReferences();
					//
					for(int j = 0; j < ref.length; j++)
						activatedEditor(ref[j]);
				}
			}
		});
	}
	public void shutdown() throws CoreException
	{
		super.shutdown();
		saveOpenEditors();
		savePositionData();
	}
	public void clearPositionData()
	{
		m_positions.clear();
		m_counts.clear();
		savePositionData();
	}
	public int getFullCountEditors()
	{
		return m_positions.size();
	}
	public void propertyChange(PropertyChangeEvent event)
	{
		IPreferenceStore store = getPreferenceStore();
		m_disable = store.getBoolean(PositionPreferencePage.DISABLE);
		m_source = store.getBoolean(PositionPreferencePage.SOURCE);
		m_restoreAutoOpen = store.getBoolean(PositionPreferencePage.RESTORE_AUTOOPEN);
		m_countLimit = store.getInt(PositionPreferencePage.COUNT);
	}
	protected void initializeDefaultPreferences(IPreferenceStore store)
	{
		super.initializeDefaultPreferences(store);
		store.setDefault(PositionPreferencePage.DISABLE, false);
		store.setDefault(PositionPreferencePage.SOURCE, true);
		store.setDefault(PositionPreferencePage.RESTORE_AUTOOPEN, true);
		store.setDefault(PositionPreferencePage.COUNT, 1000);
	}
	public void earlyStartup()
	{
		IPreferenceStore store = getPreferenceStore();
		store.addPropertyChangeListener(this);
		m_disable = store.getBoolean(PositionPreferencePage.DISABLE);
		m_source = store.getBoolean(PositionPreferencePage.SOURCE);
		m_countLimit = store.getInt(PositionPreferencePage.COUNT);
		loadPositions();
		IWorkbenchWindow window = getWorkbench().getWorkbenchWindows()[0];
		handlePreOpened(window);
		window.getPartService().addPartListener(new IPartListener2()
		{
			public void partActivated(IWorkbenchPartReference ref)
			{
				activatedEditor(ref);
			}
			public void partBroughtToTop(IWorkbenchPartReference ref)
			{
			}
			public void partClosed(IWorkbenchPartReference ref)
			{
				closedEditor(ref);
			}
			public void partDeactivated(IWorkbenchPartReference ref)
			{
			}
			public void partOpened(IWorkbenchPartReference ref)
			{
			}
			public void partHidden(IWorkbenchPartReference ref)
			{
			}
			public void partVisible(IWorkbenchPartReference ref)
			{
			}
			public void partInputChanged(IWorkbenchPartReference ref)
			{
			}
		});
	}
	protected void activatedEditor(IWorkbenchPartReference ref)
	{
		if(m_listeners.containsKey(ref))
			return;
		//
		IWorkbenchPart part = ref.getPart(false);
		//
		if(part != null && part instanceof ITextEditor)
		{
			final ITextEditor editor = (ITextEditor)part;
			IEditorInput input = editor.getEditorInput();
			String key = null;
			//
			if(input instanceof FileEditorInput)
			{
				key = ((FileEditorInput)input).getFile().getFullPath().toString();
			}
			else
				if(input instanceof IClassFileEditorInput && m_source)
				{
					try
					{
						IClassFile classFile = ((IClassFileEditorInput)input).getClassFile();
						key = classFile.getPath().toString() + "/" + classFile.getType().getFullyQualifiedName();
					}
					catch(JavaModelException e)
					{
						log("error activate source editor: ", e);
						return;
					}
				}
				else
				{
					return;
				}
			//
			ISelectionProvider provider = editor.getSelectionProvider();
			//
			if(provider instanceof IPostSelectionProvider)
			{
				final PartPositionHandler handler = new PartPositionHandler(key, (IPostSelectionProvider)provider);
				m_listeners.put(ref, handler);
				if(!m_disable)
				{
					StringBuffer stack = null;
					//
					if(m_restoreAutoOpen)
					{
						try
						{
							throw new Exception();
						}
						catch(Exception e)
						{
							StringWriter writer = new StringWriter();
							e.printStackTrace(new PrintWriter(writer));
							stack = writer.getBuffer();
						}
					}
					if(stack != null && stack.indexOf("org.eclipse.jdt.internal.ui.actions.OpenTypeAction") != -1)
					{
						Display.getCurrent().asyncExec(new Runnable()
						{
							public void run()
							{
								handler.open(editor, m_positions, m_counts);
							}
						});
					}
					else
					{
						handler.open(editor, m_positions, m_counts);
					}
				}
			}
		}
	}
	protected void closedEditor(IWorkbenchPartReference ref)
	{
		PartPositionHandler handler = (PartPositionHandler)m_listeners.remove(ref);
		if(handler != null && !m_disable)
			closeEditor(handler);
	}
	protected void closeEditor(PartPositionHandler handler)
	{
		handler.close(m_positions, m_counts);
		//
		if(m_positions.size() > m_countLimit)
		{
			Iterator I = m_counts.entrySet().iterator();
			//
			if(I.hasNext())
			{
				Map.Entry first = (Map.Entry)I.next();
				Map.Entry minValue = first;
				Integer min = (Integer)first.getValue();
				//
				while(I.hasNext())
				{
					Map.Entry next = (Map.Entry)I.next();
					Integer nextValue = (Integer)first.getValue();
					//
					if(nextValue.compareTo(min) == -1)
					{
						min = nextValue;
						minValue = next;
					}
				}
				//
				String key = (String)minValue.getKey();
				m_positions.remove(key);
				m_counts.remove(key);
			}
		}
	}
}