/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 16, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator.actions;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IWorkbenchPart;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.views.navigator.PuakmaResourceView;

/**
 * This action drops table/column with/without dropping background shit...
 *
 * @author Martin Novak
 */
public class PNDropTableAction extends PNBaseAction
{
	public static final String ID_DROP_ALL = "#dropAll";
	public static final String ID_DROP_JDBC = "#dropJdbc";
	public static final String ID_DROP_SYS = "#dropSys";

	public PNDropTableAction(int mode, PuakmaResourceView view)
	{
		super(null, view);

		//assert PuakmaLibraryUtils.isValidDatabaseMode(mode) : "Invalid database manipulation mode";

		//    if((mode & Database.MODE_PMASYS_AND_JDBC) == Database.MODE_PMASYS_AND_JDBC) {
		//      setText("Drop from System and Jdbc");
		//      setId(ID_DROP_ALL);
		//    }
		//    else if((mode & Database.MODE_JDBC_DB) == Database.MODE_JDBC_DB) {
		//      setText("Drop from Puakma System Database");
		//      setId(ID_DROP_JDBC);
		//    }
		//    else if((mode & Database.MODE_PMASYS_DB) == Database.MODE_PMASYS_DB) {
		//      setText("Drop from Jdbc Database");
		//      setId(ID_DROP_SYS);
		//    }
	}

	public void run()
	{
		throw new IllegalStateException("Not implemented yet");
	}

	public boolean handleKeyEvent(KeyEvent event)
	{
		IStructuredSelection selection = getView().getSelection();
		Iterator it = selection.iterator();
		while(it.hasNext()) {
			Object o = it.next();
			if((o instanceof Table || o instanceof TableColumn) == false)
				return false;
		}

		return true;
	}

	public boolean qualifyForSelection()
	{
		throw new IllegalStateException("Not implemented yet");
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{
		throw new IllegalStateException("Not implemented yet");
	}

}
