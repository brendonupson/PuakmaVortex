/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 23, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import org.eclipse.swt.widgets.Composite;

public interface PropertiesPage extends PropertiesPageLifecycle
{
  /**
   * This function creates a composite for the properties page which can accept some events.
   * 
   * @param parent is the parent composite of the page
   * @return {@link Composite} object which was created for the tab.
   */
  public Composite createComposite(Composite parent);
  
  /**
   * Initializes the page with the tabbed properties controller
   * @param controller is the controller we belong to
   */
  public void init(TabbedPropertiesController controller);

  /**
   * Gets the name of this property page. It should appear in the tab view.
   * @return name of the property view
   */
  public String getName();
  
  /**
   * Gets the tool tip for this page. It appears as a tooltip on the tab view.
   * @return tooltip for this page
   */
  public String getTooltip();
}
