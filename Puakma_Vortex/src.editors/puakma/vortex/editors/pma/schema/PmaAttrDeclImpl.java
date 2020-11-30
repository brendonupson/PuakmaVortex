package puakma.vortex.editors.pma.schema;

import java.util.Enumeration;

import org.eclipse.wst.html.core.internal.contentmodel.HTMLAttributeDeclaration;
import org.eclipse.wst.html.core.internal.contentmodel.HTMLCMDataType;
import org.eclipse.wst.html.core.internal.provisional.HTMLCMProperties;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;

import puakma.vortex.editors.pma.parser2.PmaCMDataTypeImpl;

public class PmaAttrDeclImpl extends PmaCMNodeImpl implements HTMLAttributeDeclaration
{
  private PmaCMDataTypeImpl type = null;

  private int usage = 0;

  public PmaAttrDeclImpl(String name, HTMLCMDataType valueType, int valueUsage)
  {
    super(name);
    
    this.type = (PmaCMDataTypeImpl) valueType;

    switch (valueUsage) {
        case OPTIONAL :
        case REQUIRED :
        case FIXED :
        case PROHIBITED :
            this.usage = valueUsage;
            break;
        default :
            // should warn...
            this.usage = OPTIONAL; // fall back
            break;
    }
  }

  public String getAttrName()
  {
    return getNodeName();
  }

  public CMDataType getAttrType()
  {
    return type;
  }

  public String getDefaultValue()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Enumeration getEnumAttr()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public int getUsage()
  {
    return usage;
  }

  public int getNodeType()
  {
    return CMNode.ATTRIBUTE_DECLARATION;
  }

  public Object getProperty(String propertyName)
  {
    if(propertyName.equals(HTMLCMProperties.SHOULD_IGNORE_CASE))
      return new Boolean(false);
    else if(propertyName.equals(HTMLCMProperties.IS_XHTML))
      return new Boolean(false);
    return null;
  }

  public boolean supports(String propertyName)
  {
    if(propertyName.equals(HTMLCMProperties.SHOULD_IGNORE_CASE))
      return true;
    else if(propertyName.equals(HTMLCMProperties.IS_XHTML))
      return true;
    return false;
  }
  
  void setUsage(int usage)
  {
    this.usage = usage;
  }
}
