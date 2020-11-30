/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 20, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.design;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import puakma.coreide.objects2.DesignObject;


/**
 * TODO: pridat
 * @author Martin Novak
 */
public class BrowserEditorArea extends Composite
{
	private Text urlControl;
	private Browser browser;
	private ToolItem backward;
	private ToolItem forward;
	private ToolItem refresh;
	private ToolItem stop;
	private ToolItem home;

	private DesignObject obj;
	private boolean notStarted = false;

	BrowserEditorArea(Composite parent, PuakmaEditor editor)
	{
		super(parent, SWT.NULL);

		this.obj = editor.getDesignObject();

		GridData gd = new GridData(GridData.FILL_BOTH);
		setLayoutData(gd);

		GridLayout gl = new GridLayout(2, false);
		gl.horizontalSpacing = gl.verticalSpacing = 0;
		gl.marginWidth = gl.marginHeight = 0;
		setLayout(gl);

		ToolBar tb = new ToolBar(this, SWT.FLAT);

		// back item
		backward = new ToolItem(tb, SWT.PUSH);
		backward.setText("Back");
		backward.setToolTipText("Go Backward One Page");
		backward.setEnabled(false);
		backward.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browser.back();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		forward = new ToolItem(tb, SWT.FLAT);
		forward.setText("Forward");
		forward.setToolTipText("Go Forward One Page");
		forward.setEnabled(false);
		forward.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browser.forward();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		refresh = new ToolItem(tb, SWT.FLAT);
		refresh.setText("Reload");
		refresh.setToolTipText("Reload Current Page");
		refresh.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browser.refresh();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		stop = new ToolItem(tb, SWT.FLAT);
		stop.setText("Stop");
		stop.setToolTipText("Stop Loading This Page");
		stop.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browser.refresh();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		stop.setEnabled(false);

		// url text
		urlControl = new Text(this, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		urlControl.setLayoutData(gd);
		urlControl.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) { }
			public void widgetDefaultSelected(SelectionEvent e)
			{
				browser.setUrl(urlControl.getText());
			}
		});

		// now make browser component
		browser = new Browser(this, SWT.NULL);
		browser.setFocus();
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		browser.setLayoutData(gd);
		browser.addLocationListener(new LocationListener() {
			public void changing(LocationEvent event) {
				stop.setEnabled(true);
			}
			public void changed(LocationEvent event) {
				backward.setEnabled(browser.isBackEnabled());
				forward.setEnabled(browser.isForwardEnabled());
				stop.setEnabled(false);

				String url = browser.getUrl();
				if(url.equals(urlControl.getText()) == false) {
					int pos = urlControl.getCaretPosition();
					urlControl.setText(url);
					urlControl.setSelection(pos);
				}
			}
		});
	}

	/**
	 * The default focus should be passed to browser.
	 *
	 * @return true if focus can be granted to the browser.
	 */
	public boolean setFocus()
	{
		return browser.setFocus();
	}

	public void refresh()
	{
		if(notStarted = false) {
			String url = obj.getUrl();

			if(url == null)
				return;

			urlControl.setText(url);
			browser.setUrl(url);
		}
	}

	/**
	 * Interrupts loading of the page to the browser.
	 */
	public void interrupt()
	{
		browser.stop();
	}
}
