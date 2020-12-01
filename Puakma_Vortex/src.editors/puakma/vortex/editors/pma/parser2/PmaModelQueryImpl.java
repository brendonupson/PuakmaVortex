package puakma.vortex.editors.pma.parser2;

import java.util.List;

import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.html.core.internal.modelquery.HTMLModelQueryImpl;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.util.CMDocumentCache;
import org.w3c.dom.Element;

import puakma.vortex.editors.pma.schema.PmaElementCollection;

public class PmaModelQueryImpl extends HTMLModelQueryImpl
{
  public PmaModelQueryImpl(CMDocumentCache cache, URIResolver idResolver)
  {
    super(cache, idResolver);
  }

  public CMElementDeclaration getCMElementDeclaration(Element element)
  {
    String name = element.getNodeName();
    PmaElementCollection col = PmaElementCollection.getInstance();
    CMNode node = col.getNamedItem(name);
    if(node != null)
      return (CMElementDeclaration) node;
//    if(name.startsWith("P@text")) {
//      PmaAttributeCollection attributeCollection = new PmaAttributeCollection();
//      PmaElementCollection collection = new PmaElementCollection(attributeCollection);
//      return new TagPmaText(collection);
//    }
    return super.getCMElementDeclaration(element);
  }

  public List getAvailableContent(Element element, CMElementDeclaration ed, int includeOptions)
  {
    List candidates = super.getAvailableContent(element, ed, includeOptions);
    
    // TODO: cosi dodelat - melo by se to udelat tovarnou???
    String name = element.getNodeName();
    PmaElementCollection manager = new PmaElementCollection();
//    manager.getTag(name)
//    TagPmaText pmaTag = new TagPmaText(collection);
//    candidates.add(pmaTag);
    
    return candidates;
  }
}
