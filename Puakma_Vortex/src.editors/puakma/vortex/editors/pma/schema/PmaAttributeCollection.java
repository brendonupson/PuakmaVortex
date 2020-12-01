package puakma.vortex.editors.pma.schema;

import java.util.Iterator;

import org.eclipse.wst.html.core.internal.contentmodel.HTMLAttributeDeclaration;
import org.eclipse.wst.html.core.internal.contentmodel.HTMLCMDataType;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;
import org.eclipse.wst.xml.core.internal.contentmodel.basic.CMNamedNodeMapImpl;

import puakma.vortex.editors.pma.parser2.PmaCMDataTypeImpl;
import puakma.vortex.editors.pma.parser2.PmaNamedNodeMap;

public class PmaAttributeCollection extends PmaNamedNodeMap
{
  public void getDeclarations(PmaNamedNodeMap declarations, Iterator names)
  {
    while(names.hasNext()) {
      String attrName = (String) names.next();
      HTMLAttributeDeclaration dec = getDeclaration(attrName);
      if(dec != null)
        declarations.putNamedItem(attrName, dec);
    }
  }

  private HTMLAttributeDeclaration getDeclaration(String attrName)
  {
    HTMLAttributeDeclaration attr = null;
    HTMLCMDataType atype = null;

    if(attrName.equals(PmaNamespace.ATTR_NAME)) {
      atype = new PmaCMDataTypeImpl(CMDataType.CDATA);
      attr = new PmaAttrDeclImpl(PmaNamespace.ATTR_NAME, atype,
                                 CMAttributeDeclaration.OPTIONAL);
    }
    return attr;
  }

  public void getDeclarations(CMNamedNodeMapImpl attributes, String[] names)
  {
    for(int i = 0; i < names.length; ++i) {
      String attrName = names[i];
      HTMLAttributeDeclaration dec = getDeclaration(attrName);
      if(dec != null) {
        attributes.put(dec);
      }
    }
  }
}
