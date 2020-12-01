/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    10/06/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.application;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

import puakma.coreide.objects2.Application;
import puakma.utils.lang.JDKUtils;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.JrePreferencesPage;
import puakma.vortex.project.JavaProjectConfiguration;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.project.PuakmaProject2;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.MultiPageEditorPart2;
import puakma.vortex.swt.NewLFEditorPage;
import puakma.vortex.swt.NullStreamHandler;

public class JavaPropertiesPage extends NewLFEditorPage
{
	public static final String ENV_JAVA_1_6 = "JavaSE-1.6";
	public static final String ENV_JAVA_1_5 = "J2SE-1.5";
	public static final String ENV_JAVA_1_4 = "J2SE-1.4";
	public static final String JAVA_1_6 = "Java 1.6 (6.0)";
	public static final String JAVA_1_5 = "Java 1.5 (5.0)";
	public static final String JAVA_1_4_2 = "Java 1.4.2";
	private Application application;
	private Combo jdkVersionList;
	private Button jvmRadioBtnJdkVersion;
	private Button jvmRadioBtnServerJdk;
	private FormText formText;
	private Font boldFont;

	public JavaPropertiesPage(Composite parent, MultiPageEditorPart2 editor, final Application application)
	{
		super(parent, editor);

		this.application = application;

		DialogBuilder2 builder = new DialogBuilder2(this);
		builder.createFormsLFComposite("Java Properties", false, 1);

		PuakmaProject2 project = ProjectManager.getProject(application);
		if(project.javaStarted()) {
			builder.createSection("Used JDK", "Specify which JDK do you want to use in application", 2);
			Composite c = builder.getCurrentComposite();
			GridLayout gl = (GridLayout) c.getLayout();
			gl.makeColumnsEqualWidth = true;

			createSelectJdkComposite(application, builder);
			createInfoComposite(application, builder);
			builder.closeSection();
		}
		else {
			builder.createLabelRow("Start java in this project, and reopen the editor to see some results");
		}

		builder.closeComposite();
		builder.finishBuilder();

		if(project.javaStarted())
			initialize();
	}

	private void createSelectJdkComposite(final Application application, DialogBuilder2 builder)
	{
		builder.createComposite(2);
		jvmRadioBtnJdkVersion = builder.createRadioButtonRow("Use specific Java version");
		jdkVersionList = builder.createComboRow("", true);
		jdkVersionList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				jdkTypeChanged();
			}
		});

		String serverJDKName = application.getServer().getEnvironmentProperty("java.version");
		int jdkVersion;
		if(serverJDKName != null && (jdkVersion = JDKUtils.guessJdk(serverJDKName)) != JDKUtils.JDK_UNKNOWN) {
			if(jdkVersion < JDKUtils.JDK_VERSION_1_4_2) {
				jvmRadioBtnServerJdk = builder.createRadioButtonRow("Server is running on invalid JRE version (" + serverJDKName + ")");
				jvmRadioBtnServerJdk.setEnabled(false);
			}
			else {
				serverJDKName = JDKUtils.maxVersion(jdkVersion) + "." + JDKUtils.medVersion(jdkVersion)
						+ "." + JDKUtils.minVersion(jdkVersion);
				jvmRadioBtnServerJdk = builder.createRadioButtonRow("Use latest server compatible Java (" + serverJDKName + ")");
			}
		}
		else {
			jvmRadioBtnServerJdk = builder.createRadioButtonRow("Server JDK unknown");
			jvmRadioBtnServerJdk.setEnabled(false);
		}
		SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				jdkSourceSelectionChange(e.widget);
			}
		};
		jvmRadioBtnJdkVersion.addSelectionListener(adapter);
		jvmRadioBtnServerJdk.addSelectionListener(adapter);
		builder.closeComposite();
	}

	private void createInfoComposite(final Application application, DialogBuilder2 builder)
	{
		builder.createComposite();

		formText = builder.createFormText("<form></form>");
		formText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				try {
					Object obj = e.getHref();
					String str = (String) obj;
					if(str.startsWith("custom://") == false)
						return;

					URL url = new URL(null, str, new NullStreamHandler());
					// PREFS PROTOCOL IS A SPECIAL THING FOR OPENING PREFERENCE PAGES
					if("custom".equals(url.getProtocol())) {
						PuakmaProject2 project = ProjectManager.getProject(application);
						IJavaProject javaProject = project.getJavaProject();
						String propPageId = url.getHost();
						PreferenceDialog dlg = PreferencesUtil.createPropertyDialogOn(null, javaProject,
								propPageId, null, null);
						dlg.open();
						uploadJavaProperties();
					}
				}
				catch(MalformedURLException ex) {
					// IGNORE
				}
			}
		});

		boldFont = getFont();
		FontData[] fd = boldFont.getFontData();
		for(int i = 0; i < fd.length; ++i)
			fd[i].setStyle(fd[i].getStyle() | SWT.BOLD);
		boldFont = new Font(boldFont.getDevice(), fd);
		formText.setFont("bold", boldFont);

		builder.createLabelRow("");builder.createLabelRow("");

		builder.closeComposite();
	}

	/**
	 * Initializes the content of the page
	 */
	private void initialize()
	{
		jdkVersionList.add(JAVA_1_4_2);
		jdkVersionList.add(JAVA_1_5);
		jdkVersionList.add(JAVA_1_6);

		// LOAD THE SETTINGS FROM THE PROJECT
		PuakmaProject2 project = ProjectManager.getProject(application);
		JavaProjectConfiguration conf = project.getJavaConfiguration();
		String envName;
		if(conf.useLatestServerCompatibleJdk()) {
			envName = ProjectUtils.getServerEnvironmentName(application);
		}
		else
			envName = conf.getJavaVersion();

		if(ENV_JAVA_1_4.equals(envName))
			jdkVersionList.select(0);
		else if(ENV_JAVA_1_5.equals(envName))
			jdkVersionList.select(1);
		else if(ENV_JAVA_1_6.equals(envName))
			jdkVersionList.select(2);
		else
			jdkVersionList.select(0);

		if(conf.useLatestServerCompatibleJdk()) {
			jvmRadioBtnServerJdk.setSelection(true);
			jdkVersionList.setEnabled(false);
		}
		else {
			jvmRadioBtnJdkVersion.setSelection(true);
		}

		refreshInfoText();
	}

	/**
	 * Updates text information about what JRE are we using.
	 */
	private void refreshInfoText()
	{
		IVMInstall vm = getCurrentVm();

		StringBuffer sb = new StringBuffer();
		sb.append("<form>\n");
		if(vm != null) {
			sb.append("<p>You are using jre: ");
			sb.append(vm.getName());
			sb.append("</p>\n<p>Root path: <span font=\"bold\">");
			sb.append(vm.getInstallLocation().toString());
			sb.append("</span></p>");

			sb.append("<p><a href=\"prefs://");
			sb.append(JrePreferencesPage.PAGE_ID);
			sb.append("\">Open preference page to assign Eclipse JRE to Java version used by Vortex.</a>");
			sb.append("</p>");
		}
		else {
			sb.append("<p>Invalid JRE, please select different Java version or you can ");
			sb.append("manage JREs clicking ");
			sb.append("<a href=\"prefs://");
			sb.append(JrePreferencesPage.PAGE_ID);
			sb.append("\">here.</a></p>");
		}

		sb.append("<p><a href=\"custom://org.eclipse.jdt.ui.propertyPages.CompliancePreferencePage/project\">");
		sb.append("Open java compiler settings for Eclipse project</a>.");
		sb.append("Note that all changed preferences will be updated to the Tornado server.</p>");

		sb.append("</form>\n");

		formText.setText(sb.toString(), true, false);
	}

	private IVMInstall getCurrentVm()
	{
		String id = getCurrentVMInstallId();
		if(id == null)
			return null;

		return ProjectUtils.getVMInstallById(id);
	}

	private String getCurrentVMInstallId()
	{
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		String envName = null;

		if(jvmRadioBtnJdkVersion.getSelection() == true) {
			envName = getCurrentJavaEnvironment();
		}
		else
			envName = ProjectUtils.getServerEnvironmentName(application);

		String id = store.getString(envName);
		if(id == null || id.length() == 0) {
			IVMInstall install = ProjectUtils.getVMInstallByEnvName(envName);
			if(install == null)
				return null;
			return install.getId();
		}
		return id;
	}

	public void doSave(IProgressMonitor monitor)
	{
		try {
			PuakmaProject2 project = ProjectManager.getProject(application);
			JavaProjectConfiguration conf = project.getJavaConfiguration();
			boolean useServer = jvmRadioBtnServerJdk.getSelection();
			conf.setUseLatestServerCompatibleJdk(useServer);
			if(useServer == false)
				conf.setJavaVersion(getCurrentJavaEnvironment());

			project.setJavaConfiguration(conf);

			setDirty(false);
		}
		catch(Exception e) {
			VortexPlugin.log(e);
		}
	}

	/**
	 * Gets the current java environment string according to the selection in the combo box.
	 */
	private String getCurrentJavaEnvironment()
	{
		String envName = null;
		String name = jdkVersionList.getText();
		if(JAVA_1_4_2.equals(name))
			envName = ENV_JAVA_1_4;
		else if(JAVA_1_5.equals(name))
			envName = ENV_JAVA_1_5;
		else if(JAVA_1_6.equals(name))
			envName = ENV_JAVA_1_6;
		return envName;
	}

	/**
	 * Event handler for changing selection of java type.
	 */
	public void jdkTypeChanged()
	{
		String name = jdkVersionList.getText();
		if(JAVA_1_4_2.equals(name) == false && JAVA_1_5.equals(name) == false && JAVA_1_6.equals(name) == false)
			throw new IllegalStateException("Invalid jdk name has been chosen: " + name);

		refreshInfoText();
		setDirty(true);
	}

	private void jdkSourceSelectionChange(Widget selected)
	{
		// IF WE SELECT THAT WE WANT TO USE SERVER'S COMPATIBLE JRE
		if(jvmRadioBtnServerJdk == selected) {
			jdkVersionList.setEnabled(false);
			// NOW GET THE SERVER'S JAVA VERSION
			String serverJDKName = application.getServer().getEnvironmentProperty("java.version");
			int jdkVersion;
			if(serverJDKName != null && (jdkVersion = JDKUtils.guessJdk(serverJDKName)) != JDKUtils.JDK_UNKNOWN) {
				if(JDKUtils.maxVersion(jdkVersion) == 1 && JDKUtils.medVersion(jdkVersion) == 4) {
				} else if(jdkVersion == JDKUtils.JDK_VERSION_1_5_0) {
				} else {
				}

				//fillEclipseJresCombo(envName);
			}
		}
		else {
			jdkVersionList.setEnabled(true);
		}

		refreshInfoText();
		setDirty(true);
	}

	/**
	 * Uploads all java properties to Tornado server.
	 */
	protected void uploadJavaProperties()
	{
		Job j = new Job("Save java properties") {
			protected IStatus run(IProgressMonitor monitor) {
				PuakmaProject2 project = ProjectManager.getProject(application);
				try {
					project.uploadJavaConfiguration();
				}
				catch(Exception e) {
					return new Status(IStatus.ERROR, VortexPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e);
				}
				return Status.OK_STATUS;
			}
		};

		j.schedule();
	}

	public void disposePage()
	{
		boldFont.dispose();    
	}  
}
