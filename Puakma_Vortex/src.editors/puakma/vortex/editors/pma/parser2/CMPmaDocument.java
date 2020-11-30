package puakma.vortex.editors.pma.parser2;

import org.eclipse.wst.html.core.internal.contentmodel.HTMLElementDeclaration;
import org.eclipse.wst.html.core.internal.contentmodel.chtml.CHCMDocImpl;
import org.eclipse.wst.html.core.internal.provisional.HTML40Namespace;
import org.eclipse.wst.html.core.internal.provisional.HTMLCMProperties;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamespace;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.provisional.contentmodel.CMDocType;

import puakma.vortex.editors.pma.schema.PmaAttributeCollection;
import puakma.vortex.editors.pma.schema.PmaElementCollection;

public class CMPmaDocument extends CHCMDocImpl implements CMDocument
{
  private PmaElementCollection elements;
  private PmaAttributeCollection attributes;
  
  public CMPmaDocument()
  {
    super(CMDocType.HTML_DOC_TYPE, new CMNamespace() {
      public boolean supports(String propertyName)
      {
        if(propertyName.equals(HTMLCMProperties.IS_XHTML))
          return true;
        return false;
      }

      public Object getProperty(String propertyName)
      {
        if(propertyName.equals(HTMLCMProperties.IS_XHTML))
          return new Boolean(false);
        return null;
      }

      public int getNodeType()
      {
        // TODO Auto-generated method stub
        return CMNode.NAME_SPACE;
      }

      public String getNodeName()
      {
        return HTML40Namespace.HTML40_URI;
      }

      public String getURI()
      {
        return getNodeName();
      }

      public String getPrefix()
      {
        return HTML40Namespace.HTML40_TAG_PREFIX;
      }
    });
    
    elements = PmaElementCollection.getInstance();
    attributes = elements.getAttributesCollection();
  }

  public CMNamedNodeMap getElements()
  {
    // TODO Auto-generated method stub
    return elements;
  }

  public HTMLElementDeclaration getElementDeclaration(String elementName)
  {
    if(elementName.startsWith("<P@")) {
      HTMLElementDeclaration decl = (HTMLElementDeclaration) elements.getNamedItem(elementName);
      return decl;
    }
    return super.getElementDeclaration(elementName);
  }
}
