/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 14, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.lang.ListenersList;

/**
 * This is a faked connection model, so we can implement connections much
 * easier. The deal is to set some column here as the fk, and then we compute
 * everything else on the fly. So that's it. Note that this also checks the
 * column fk creation/removal, and notifies appropriately the Part.
 * 
 * @author Martin Novak
 */
public class ConnectionFakedModel implements PropertyChangeListener
{
	/**
	 * This event is being fired whenever we disconnect the property
	 */
	public static final String PROP_REMOVE = "remove";

	private static final String KEY_REFERENCING_CONNECTION = "referencingConnection";

	/**
	 * This is the column which is pointing at something
	 */
	private TableColumn fkColumn;
	private ListenersList listeners = new ListenersList();

	public ConnectionFakedModel() {  }

	public ConnectionFakedModel(TableColumn fkColumn)
	{
		setSourceColumn(fkColumn);
	}

	public void setSourceColumn(TableColumn fkColumn)
	{
		this.fkColumn = fkColumn;

		fkColumn.addListener(this);

		// HOOK REFERENCINF APPLICATION OBJECT
		ApplicationObject ao = fkColumn.getRefTable();
		if(ao == null)
			throw new IllegalArgumentException("Invalid column passed - it is not referencing anything");
		hookApplicationObject(ao);
	}

	public void addListener(PropertyChangeListener listener)
	{
		listeners.addListener(listener);
	}

	public void removeListener(PropertyChangeListener listener)
	{
		listeners.removeListener(listener);
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		String p = evt.getPropertyName();
		if(TableColumn.PROP_REFERENCED_TABLE.equals(p)) {
			if(evt.getNewValue() == null) {
				fireRemove(evt.getOldValue());
			}
		}
		else if(Database.PROP_REMOVE_TABLE.equals(p)) {
			fireRemove(evt.getOldValue());
		}
	}

	/**
	 * Handles unlinking of the referenced column reference from the referenced column. 
	 */
	private void fireRemove(Object oldReference)
	{
		fkColumn.removeListener(this);
		// REMOVE THIS FROM THE LIST OF REFERENCING CONNECTION TO TABLE OR TABLE COLUMN OBJECT
		ApplicationObject ao = (ApplicationObject) oldReference;
		synchronized(ao) {
			List<?> l = (List<?>) ao.getData(KEY_REFERENCING_CONNECTION);
			if(l != null) {
				if(l.size() == 1)
					ao.setData(KEY_REFERENCING_CONNECTION, null);
				else {
					l.remove(this);
					ao.setData(KEY_REFERENCING_CONNECTION, l);
				}
			}
		}
		listeners.fireEvent(this, PROP_REMOVE, this, null);
		fkColumn = null;
	}

	/**
	 * Adds this to the list of referencing objects of some column/table.
	 * @param ao is the referenced table/column
	 */
	private void hookApplicationObject(ApplicationObject ao)
	{
		synchronized(ao) {
			List<ConnectionFakedModel> l = (List<ConnectionFakedModel>) ao.getData(KEY_REFERENCING_CONNECTION);
			if(l == null)
				l = new ArrayList<ConnectionFakedModel>();
			if(l.contains(this) == false) {
				l.add(this);
				ao.setData(KEY_REFERENCING_CONNECTION, l);
			}
		}
	}

	public static List<?> extractConnections(ApplicationObject ao)
	{
		synchronized(ao) {
			List<?> l = (List<?>) ao.getData(KEY_REFERENCING_CONNECTION);
			if(l == null)
				return Collections.EMPTY_LIST;
			return new ArrayList<Object>(l);
		}
	}

	/**
	 * Gets the referencing column.
	 */
	public TableColumn getReferencingColumn()
	{
		return fkColumn;
	}

	/**
	 * Gets the table to which is column referencing
	 */
	public Table getReferencedTable()
	{
		return fkColumn.getRefTable();
	}
}
