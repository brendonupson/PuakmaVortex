/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 13, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import puakma.utils.lang.StringUtil;

/**
 * Some utility class for manipulation with SWT controls.
 *
 * @author Martin Novak
 */
public class SWTUtil
{
	private static IntVerifyListener ivl = new IntVerifyListener();

	/**
	 * Returns safe String. If text == null, then it is impossible to set it
	 * as text into control, so we have to provide some safe wrapper as this.
	 *
	 * @param text is the checked parameter
	 * @return if text == null, then "" otherwise parameter text
	 */
	public static String getSafeString(String text)
	{
		if(text == null)
			return "";
		return text;
	}

	/**
	 * This function sorts combo box, and selects previously selected item.
	 *
	 * @param combo is the Combo box which should be sorted
	 */
	public static void sortCombo(Combo combo)
	{
		int index = combo.getSelectionIndex();
		String text = combo.getItem(index);
		String[] items = combo.getItems();
		Arrays.sort(items, new Comparator<Object>() {
			public int compare(Object o1, Object o2)
			{
				String s1 = (String) o1, s2 = (String) o2;
				return s1.compareToIgnoreCase(s2);
			}
		});
		// FIND THE SELECTED INDEX
		for(int i = 0; i < items.length; ++i)
			if(text.equals(items[i])) {
				index = i;
				break;
			}
		// SETUP THE COMBO
		combo.setItems(items);
		combo.select(index);
	}

	public static int getTableHeightHint(Table table, int rows)
	{
		if(table.getFont().equals(JFaceResources.getDefaultFont()))
			table.setFont(JFaceResources.getDialogFont());
		int result = table.getItemHeight() * rows + table.getHeaderHeight();
		if(table.getLinesVisible())
			result += table.getGridLineWidth() * (rows - 1);
		return result;
	}

	/**
	 * This function computes width of some general text in control to pixels.
	 *
	 * @param c is the control from which we take the font to measure
	 * @param numChars is the number of general characters to measure
	 * @return width of general text in pixels
	 */
	public static int computeWidthOfChars(Control c, int numChars) {
		GC gc = new GC(c);
		gc.setFont(c.getFont());
		FontMetrics fm = gc.getFontMetrics();
		gc.dispose();
		return Dialog.convertWidthInCharsToPixels(fm, numChars);
	}

	/**
	 * This function computes height of some general text in control to pixels.
	 *
	 * @param c is the control from which we take the font to measure
	 * @param numChars is the number of general characters to measure
	 * @return height of general text in pixels
	 */
	public static int computeHeightOfChars(Control c, int numChars) {
		GC gc = new GC(c);
		gc.setFont(c.getFont());
		FontMetrics fm = gc.getFontMetrics();
		gc.dispose();
		return Dialog.convertHeightInCharsToPixels(fm, numChars);
	}

	/**
	 * Setups Text control to accept only integer numbers in input.
	 * @param control is the text control to be checked
	 */
	public static void setIntValidation(Text control)
	{
		control.addVerifyListener(ivl);
	}

	/**
	 * Creates a {@link TextCellEditor} object which input is being guarded by
	 * validator validating if the input is convertable int.
	 */
	public static TextCellEditor createNumericTextCellEditor(Composite parent)
	{
		TextCellEditor editor = new TextCellEditor(parent);
		editor.setValidator(new IntVerifyListener());
		return editor;
	}

	/**
	 * Safely sets text to the control.
	 */
	public static void setText(Control c, String label)
	{
		label = StringUtil.safeString(label);
		if(c instanceof Label)
			((Label) c).setText(label);
		else
			throw new IllegalArgumentException("Invalid control type");
	}
}

/**
 * Used for controlling input to be numerical.
 *
 * @author Martin Novak
 */
class IntVerifyListener implements VerifyListener, ICellEditorValidator
{
	public void verifyText(VerifyEvent e)
	{
		try {
			Integer.parseInt(e.text);
		}
		catch(NumberFormatException exc) {
			if(e.keyCode != SWT.DEL && e.keyCode != SWT.BS)
				e.doit = false;
		}
	}

	public String isValid(Object value)
	{
		try {
			Integer.parseInt(value.toString());
			return null;
		}
		catch(NumberFormatException e) {
			return value.toString() + " cannot be converted to int value";
		}
	}
}
