/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 9, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import puakma.coreide.objects2.ILogger;
import puakma.vortex.preferences.PreferenceConstants;


/**
 * Main logger for the IDE
 * 
 * @author Martin Novak
 */
public class IdeLogger implements ILogger
{
	/**
	 * Logs some message. Saves the message in the eclipse log, and also if the exception
	 * was defined, then writes stack trace to the console.
	 *
	 * @param message is the message to be written
	 * @param e is Exception to be logged
	 * @param level is the level of exception - see ILogger interface
	 */
	public void log(String message, Throwable e, int level)
	{
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		boolean logToConsoleOnly = store.getBoolean(PreferenceConstants.PREF_DEBUG_LOG_TO_CONSOLE_ONLY);
		ILog log = VortexPlugin.getDefault().getLog();

		if(e != null) {
			if(message != null && message.length() > 0)
				message = message + "\nReason: " + e.getLocalizedMessage();
			else
				message = e.getLocalizedMessage();

			if(message == null)
				message = "Unknown error - " + e.getClass().getName();
		}

		System.out.println(message);
		if(e != null)
			e.printStackTrace(System.err);

		if(logToConsoleOnly == false) {
			int error = 0;
			switch(level) {
			case ERROR_OK: error = IStatus.OK; break;
			case ERROR_INFO: error = IStatus.INFO; break;
			case ERROR_WARNING: error = IStatus.WARNING; break;
			case ERROR_ERROR:
			case ERROR_FATAL: error = IStatus.ERROR; break;
			}
			IStatus status = null;
			if(e instanceof CoreException)
				status = ((CoreException) e).getStatus();
			else
				status = new Status(error, "puakma.vortex", error, message, e);

			log.log(status);
		}

		if(level == ERROR_FATAL) {
			final String msg = message;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow();
					MessageDialog.openError(window.getShell(), "Fatal error", msg);
				}
			});
		}
	}
}
