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
package puakma.vortex.views.port;

import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

public class PmaAppearanceAwareLabelProvider extends PmaJavaUILabelProvider
    implements IPropertyChangeListener
{
  public final static long DEFAULT_TEXTFLAGS = JavaElementLabels.ROOT_VARIABLE
      | JavaElementLabels.T_TYPE_PARAMETERS
      | JavaElementLabels.M_PARAMETER_TYPES
      | JavaElementLabels.M_APP_TYPE_PARAMETERS
      | JavaElementLabels.M_APP_RETURNTYPE
      | JavaElementLabels.REFERENCED_ROOT_POST_QUALIFIED;

  public final static int DEFAULT_IMAGEFLAGS = JavaElementImageProvider.OVERLAY_ICONS;

  private long fTextFlagMask;

  private int fImageFlagMask;

  /**
   * Constructor for AppearanceAwareLabelProvider.
   */
  public PmaAppearanceAwareLabelProvider(long textFlags, int imageFlags)
  {
    super(textFlags, imageFlags);
    initMasks();
    PreferenceConstants.getPreferenceStore().addPropertyChangeListener(this);
  }

  /**
   * Creates a labelProvider with DEFAULT_TEXTFLAGS and DEFAULT_IMAGEFLAGS
   */
  public PmaAppearanceAwareLabelProvider()
  {
    this(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS);
  }

  private void initMasks()
  {
    IPreferenceStore store = PreferenceConstants.getPreferenceStore();
    fTextFlagMask = -1;
    if(!store.getBoolean(PreferenceConstants.APPEARANCE_METHOD_RETURNTYPE)) {
      fTextFlagMask ^= JavaElementLabels.M_APP_RETURNTYPE;
    }
    if(!store.getBoolean(PreferenceConstants.APPEARANCE_METHOD_TYPEPARAMETERS)) {
      fTextFlagMask ^= JavaElementLabels.M_APP_TYPE_PARAMETERS;
    }
    if(!store.getBoolean(PreferenceConstants.APPEARANCE_COMPRESS_PACKAGE_NAMES)) {
      fTextFlagMask ^= JavaElementLabels.P_COMPRESSED;
    }

    fImageFlagMask = -1;
  }

  /*
   * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    if(property.equals(PreferenceConstants.APPEARANCE_METHOD_RETURNTYPE)
        || property
            .equals(PreferenceConstants.APPEARANCE_METHOD_TYPEPARAMETERS)
        || property
            .equals(PreferenceConstants.APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW)
        || property
            .equals(PreferenceConstants.APPEARANCE_COMPRESS_PACKAGE_NAMES)) {
      initMasks();
      LabelProviderChangedEvent lpEvent = new LabelProviderChangedEvent(this,
          null); // refresh all
      fireLabelProviderChanged(lpEvent);
    }
  }

  /*
   * @see IBaseLabelProvider#dispose()
   */
  public void dispose()
  {
    PreferenceConstants.getPreferenceStore().removePropertyChangeListener(this);
    super.dispose();
  }

  /*
   * @see JavaUILabelProvider#evaluateImageFlags()
   */
  protected int evaluateImageFlags(Object element)
  {
    return getImageFlags() & fImageFlagMask;
  }

  /*
   * @see JavaUILabelProvider#evaluateTextFlags()
   */
  protected long evaluateTextFlags(Object element)
  {
    return getTextFlags() & fTextFlagMask;
  }

}
