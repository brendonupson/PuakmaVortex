package puakma.vortex.editors.pma.schema;

import org.eclipse.wst.html.core.internal.provisional.HTMLCMProperties;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;

abstract public class PmaCMNodeImpl implements CMNode
{
  private java.lang.String name = null;

  /**
   * CMNodeImpl constructor comment.
   */
  public PmaCMNodeImpl(String nm)
  {
    super();
    name = nm;
  }

  /**
   * getNodeName method
   * 
   * @return java.lang.String
   */
  public String getNodeName()
  {
    return name;
  }

  /**
   * getProperty method
   * 
   * @return java.lang.Object
   * 
   * Returns the object property desciped by the propertyName
   * 
   */
  public Object getProperty(String propertyName)
  {
    if(propertyName.equals(HTMLCMProperties.IS_XHTML))
      return new Boolean(false);
    return null;
  }

  /**
   * supports method
   * 
   * @return boolean
   * 
   * Returns true if the CMNode supports a specified property
   * 
   */
  public boolean supports(String propertyName)
  {
    if(propertyName.equals(HTMLCMProperties.IS_XHTML))
      return true;
    return false;
  }
}
