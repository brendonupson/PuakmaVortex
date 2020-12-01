/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 19, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.browsing.LogicalPackage;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.internal.ui.viewsupport.TreeHierarchyLayoutProblemsDecorator;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.views.port.PmaAppearanceAwareLabelProvider;

public class PmaPackagesViewLabelProvider extends PmaAppearanceAwareLabelProvider
{
  private ImageDescriptorRegistry fRegistry;

  private TreeHierarchyLayoutProblemsDecorator fDecorator;
  
  public PmaPackagesViewLabelProvider()
  {
    this(JavaElementLabels.T_TYPE_PARAMETERS
        | JavaElementLabels.M_PARAMETER_TYPES
        | JavaElementLabels.M_APP_TYPE_PARAMETERS
        | JavaElementLabels.M_APP_RETURNTYPE
        | JavaElementLabels.REFERENCED_ROOT_POST_QUALIFIED
        | JavaElementLabels.P_COMPRESSED,
        AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS
            | JavaElementImageProvider.SMALL_ICONS);
  }

  public PmaPackagesViewLabelProvider(long textFlags, int imageFlags)
  {
    super(textFlags, imageFlags);
    
    fRegistry = JavaPlugin.getImageDescriptorRegistry();

    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    boolean useFlatPackages = store.getBoolean(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES);
    fDecorator = new TreeHierarchyLayoutProblemsDecorator(useFlatPackages);
    addLabelDecorator(fDecorator);
  }

  /*
   * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
   */
  public Image getImage(Object element)
  {
    if(element instanceof LogicalPackage) {
      LogicalPackage cp = (LogicalPackage) element;
      return getLogicalPackageImage(cp);
    }
    return super.getImage(element);
  }

  /**
   * Decoration is only concerned with error ticks
   */
  private Image getLogicalPackageImage(LogicalPackage cp)
  {
    IPackageFragment[] fragments = cp.getFragments();
    for(int i = 0; i < fragments.length; i++) {
      IPackageFragment fragment = fragments[i];
      if(!isEmpty(fragment)) {
        return decorateCompoundElement(
            JavaPluginImages.DESC_OBJS_LOGICAL_PACKAGE, cp);
      }
    }
    return decorateCompoundElement(
        JavaPluginImages.DESC_OBJS_EMPTY_LOGICAL_PACKAGE, cp);
  }

  private Image decorateCompoundElement(ImageDescriptor imageDescriptor,
      LogicalPackage cp)
  {
    Image image = fRegistry.get(imageDescriptor);
    return decorateImage(image, cp);
  }

  private boolean isEmpty(IPackageFragment fragment)
  {
    try {
      return (fragment.getCompilationUnits().length == 0)
          && (fragment.getClassFiles().length == 0);
    }
    catch(JavaModelException e) {
      JavaPlugin.log(e);
    }
    return false;
  }

  /*
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
   */
  public String getText(Object element)
  {
    if(element instanceof IPackageFragment)
      return getText((IPackageFragment) element);
    else if(element instanceof LogicalPackage)
      return getText((LogicalPackage) element);
    else
      return super.getText(element);
  }

  private String getText(IPackageFragment fragment)
  {
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    boolean useFlatPackages = store.getBoolean(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES);
    
    if(useFlatPackages)
      return getFlatText(fragment);
    else
      return getHierarchicalText(fragment);
  }

  private String getText(LogicalPackage logicalPackage)
  {
    IPackageFragment[] fragments = logicalPackage.getFragments();
    return getText(fragments[0]);
  }

  private String getFlatText(IPackageFragment fragment)
  {
    return super.getText(fragment);
  }

  private String getHierarchicalText(IPackageFragment fragment)
  {
    if(fragment.isDefaultPackage()) {
      return super.getText(fragment);
    }
    IResource res = fragment.getResource();
    if(res != null && !(res.getType() == IResource.FILE))
      return decorateText(res.getName(), fragment);
    else
      return decorateText(calculateName(fragment), fragment);
  }

  private String calculateName(IPackageFragment fragment)
  {

    String name = fragment.getElementName();
    if(name.indexOf(".") != -1) //$NON-NLS-1$
      name = name.substring(name.lastIndexOf(".") + 1); //$NON-NLS-1$
    return name;

  }
}
