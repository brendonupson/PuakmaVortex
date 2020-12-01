package puakma.vortex.editors.pma.parser2;

import org.eclipse.wst.html.core.internal.contentmodel.HTMLCMDataType;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;

import puakma.vortex.editors.pma.schema.PmaCMNodeImpl;

public class PmaCMDataTypeImpl extends PmaCMNodeImpl implements HTMLCMDataType
{
  private int impliedValueKind = IMPLIED_VALUE_NONE;

  private String impliedValue = null;

  private final static String[] emptyArray = new String[0];

  private String[] enumValues = emptyArray;

  private String instanceValue = null;

  public PmaCMDataTypeImpl(String typeName)
  {
    super(typeName);
  }

  public PmaCMDataTypeImpl(String typeName, String instanceValue)
  {
    super(typeName);
    this.instanceValue = instanceValue;
  }

  /**
   * getTypeName method
   * 
   * @return java.lang.String
   * 
   * This method returns a suitable default value that can be used when an
   * instance of the data type is created. This returns null of a suitable
   * default is not available.
   */
  public String generateInstanceValue()
  {
    return instanceValue;
  }

  public String getDataTypeName()
  {
    return getNodeName();
  }

  public String[] getEnumeratedValues()
  {
    return enumValues;
  }

  /**
   * Returns the implied value or null if none exists.
   */
  public String getImpliedValue()
  {
    return impliedValue;
  }

  /**
   * Returns one of : IMPLIED_VALUE_NONE, IMPLIED_VALUE_FIXED,
   * IMPLIED_VALUE_DEFAULT.
   */
  public int getImpliedValueKind()
  {
    return impliedValueKind;
  }

  public int getNodeType()
  {
    return CMNode.DATA_TYPE;
  }

  void setEnumValues(String[] values)
  {
    enumValues = new String[values.length];
    for(int i = 0; i < values.length; i++) {
      enumValues[i] = values[i];
    }
  }

  /**
   * package scope.
   */
  void setImpliedValue(int kind, String value)
  {
    switch(kind) {
      case IMPLIED_VALUE_FIXED:
      case IMPLIED_VALUE_DEFAULT:
        impliedValueKind = kind;
        impliedValue = value;
      break;
      case IMPLIED_VALUE_NONE:
      default:
        impliedValueKind = IMPLIED_VALUE_NONE;
        impliedValue = null; // maybe a null string?
      break;
    }
  }
}
