package puakma.vortex.editors.pma.parser2;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegionList;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.document.NodeImpl;
import org.eclipse.wst.xml.core.internal.document.XMLExtendedModelParser;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PmaModelParser extends XMLExtendedModelParser
{
  private DOMStyleModelImplForPma model;

  protected PmaModelParser(DOMStyleModelImplForPma model)
  {
    super(model);

    this.model = model;
  }

  /**
   * Tries to parse document region to detect tornado tag. If the document
   * region is not detected as tornado tag, we call parent to process it
   * further.
   */
  protected void insertStructuredDocumentRegion(IStructuredDocumentRegion flatNode)
  {
    // String regionType =
    // StructuredDocumentRegionUtil.getFirstRegionType(flatNode);
    // if(regionType == DOMRegionContext.XML_TAG_OPEN) {
    // insertStartTag(flatNode);
    // }
    boolean isPmaTag = detectPmaTag(flatNode);
    if(isPmaTag) {
      insertPmaTag(flatNode);
    }
    else
      super.insertStructuredDocumentRegion(flatNode);
  }

  /**
   * This detects if the flat node is Tornado markup node. If yes, then returns
   * true. Note that Tornado markup node starts with "&lt;P@" and ends with
   * "@P&gt;".
   */
  private boolean detectPmaTag(IStructuredDocumentRegion flatNode)
  {
    String text = flatNode.getFullText();
    if(text.startsWith("<P@"))
      return true;

    return false;
  }

  /**
   * This function inserts pma tag from the current document region.
   */
  private void insertPmaTag(IStructuredDocumentRegion flatNode)
  {
    ITextRegionList regions = flatNode.getRegions();
    if(regions == null)
      return;

    String tagName = null;
    boolean isEmptyTag = false;
    AttrImplForPma attr = null;
    Vector<AttrImplForPma> attrNodes = null;
    Iterator e = regions.iterator();
    // OPEN BRACKET
    ITextRegion r = (ITextRegion) e.next();
    String rType = r.getType();
    if(rType != DOMRegionContext.XML_TAG_OPEN)
      return;
    // P TAG
    r = (ITextRegion) e.next();
    rType = r.getType();
    if(rType != DOMRegionContext.XML_TAG_NAME)
      return;
    String text = flatNode.getText(r);
    if("P".equals(text) == false)
      return;
    // UNDEFINED SYMBOL - '@'
    r = (ITextRegion) e.next();
    rType = r.getType();
    if(rType != DOMRegionContext.UNDEFINED)
      return;
    text = flatNode.getText(r);
    if("@".equals(text) == false)
      return;
    // NOW THE NAME OF OUR TAG
    if(e.hasNext() == false) {
      insertText(flatNode);
      return;
    }
    r = (ITextRegion) e.next();
    rType = r.getType();
    if(rType != DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
      insertText(flatNode);
      return;
    }
    String pmaTagName = flatNode.getText(r);
    boolean isValidTag = isValidPmaTag(pmaTagName);
    // IF THE TAG IS NOT VALID, WE CAN STILL CONTINUE SINCE THIS IS ERROR WHICH
    // WE CAN RECOVER FROM

    // SO NOW WE CAN START THE LOOP WITH PROPERTIES AND THE STUFF THERE
    while(e.hasNext()) {
      ITextRegion region = (ITextRegion) e.next();
      String regionType = region.getType();

      if(regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
        String name = flatNode.getText(region);
        // IGNORE ENDING @P
        if("@P".equals(name))
          continue;
        
        attr = (AttrImplForPma) this.model.getDocument().createAttribute(name);
        if(attr != null) {
          attr.setNameRegion(region);
          if(attrNodes == null)
            attrNodes = new Vector<AttrImplForPma>();
          attrNodes.addElement(attr);
        }
      }
      else if(regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_EQUALS) {
        if(attr != null) {
          attr.setEqualRegion(region);
        }
      }
      else if(regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE) {
        if(attr != null) {
          attr.setValueRegion(region);
          attr = null;
        }
      }
    }

    ElementImplForPma element = null;
    try {
      element = (ElementImplForPma) model.getPmaDocument().createPmaElement(pmaTagName);
    }
    catch(DOMException ex) {
      // typically invalid name
    }
    if(element == null) { // invalid tag
      insertText(flatNode); // regard as invalid text
      return;
    }
    if(attrNodes != null) {
      Enumeration<AttrImplForPma> ae = attrNodes.elements();
      while(ae.hasMoreElements()) {
        Attr a = ae.nextElement();
        if(a == null)
          continue;
        element.appendAttributeNode(a);
      }
    }
    if(isEmptyTag)
      element.setEmptyTag(true);
    
    // TODO: what is this???
    element.setStartStructuredDocumentRegion(flatNode);
    insertStartTag(element);
  }

  private boolean isValidPmaTag(String text)
  {
    // TODO Auto-generated method stub
    return false;
  }

  protected boolean isNestedTagOpen(String regionType)
  {
    boolean result = false;
    return result;
  }

  protected Element createImplicitElement(Node parent, Node child)
  {
    // TODO Auto-generated method stub
    return super.createImplicitElement(parent, child);
  }
  
  public void changeRegion(IStructuredDocumentRegion flatNode, ITextRegion region)
  {
    super.changeRegion(flatNode, region);	  
  }
  
  public void replaceRegions(IStructuredDocumentRegion flatNode,
                             ITextRegionList newRegions, ITextRegionList oldRegions)
  {
    if(flatNode == null || getModel().getDocument() == null)
      return;
      
    if(detectPmaTag(flatNode)) {
      setupContext2(flatNode);
      super.getNextNode();
      NodeImpl nodeN = (NodeImpl) getNextNode();
      if(nodeN instanceof ElementImplForPma) {
        String fullNodeText = flatNode.getFullText();
        ElementImplForPma node = (ElementImplForPma) nodeN;
        resetNodeAttributes(node, flatNode);
        return;
//        Iterator it = oldRegions.iterator();
//        while(it.hasNext()) {
//          //AttrImpl attrImpl = (AttrImpl) it.next();
//          //node.removeAttributeNode(attrImpl);
//          ITextRegion region = (ITextRegion) it.next();
//          int start = region.getStart();
//          if(region.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
//            if(start == 3) {
//              String name = fullNodeText.substring(region.getStart(), region.getEnd());
//              node.remoAtt
//            }
//          }
//        }
//        
//        it = newRegions.iterator();
//        while(it.hasNext()) {
//          ITextRegion region = (ITextRegion) it.next();
//          int start = region.getStart();
//          if(region.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
//            if(start == 3) {
//              String name = fullNodeText.substring(region.getStart(), region.getEnd());
//              node.setTagName("P@" + name);
//            }
//            else {
//              
//            }
//          }
//        }
      }
    }
    super.replaceRegions(flatNode, newRegions, oldRegions);
  }

  private void resetNodeAttributes(ElementImplForPma node, IStructuredDocumentRegion flatNode)
  {
    AttrImplForPma attr = null;
    Vector<AttrImplForPma> attrNodes = null;
    Iterator e = flatNode.getRegions().iterator();
    // OPEN BRACKET
    if(e.hasNext() == false)
      return;
    ITextRegion r = (ITextRegion) e.next();
    String rType = r.getType();
    if(rType != DOMRegionContext.XML_TAG_OPEN)
      return;
    // P TAG
    if(e.hasNext() == false)
      return;
    r = (ITextRegion) e.next();
    rType = r.getType();
    if(rType != DOMRegionContext.XML_TAG_NAME)
      return;
    String text = flatNode.getText(r);
    if("P".equals(text) == false)
      return;
    // UNDEFINED SYMBOL - '@'
    if(e.hasNext() == false)
      return;
    r = (ITextRegion) e.next();
    rType = r.getType();
    if(rType != DOMRegionContext.UNDEFINED)
      return;
    text = flatNode.getText(r);
    if("@".equals(text) == false)
      return;
    // NOW THE NAME OF OUR TAG
    if(e.hasNext() == false)
      return;
    r = (ITextRegion) e.next();
    rType = r.getType();
    if(rType != DOMRegionContext.XML_TAG_ATTRIBUTE_NAME)
      return;
    String pmaTagName = flatNode.getText(r);
    boolean isValidTag = isValidPmaTag(pmaTagName);
    // IF THE TAG IS NOT VALID, WE CAN STILL CONTINUE SINCE THIS IS ERROR WHICH
    // WE CAN RECOVER FROM

    // SO NOW WE CAN START THE LOOP WITH PROPERTIES AND THE STUFF THERE
    while(e.hasNext()) {
      ITextRegion region = (ITextRegion) e.next();
      String regionType = region.getType();

      if(regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
        String name = flatNode.getText(region);
        // IGNORE ENDING @P
        if("@P".equals(name))
          continue;
        
        attr = (AttrImplForPma) this.model.getDocument().createAttribute(name);
        if(attr != null) {
          attr.setNameRegion(region);
          if(attrNodes == null)
            attrNodes = new Vector<AttrImplForPma>();
          attrNodes.addElement(attr);
        }
      }
      else if(regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_EQUALS) {
        if(attr != null) {
          attr.setEqualRegion(region);
        }
      }
      else if(regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE) {
        if(attr != null) {
          attr.setValueRegion(region);
          attr = null;
        }
      }
    }
    
    node.removeAttributes();
    node.setTagName(pmaTagName);
    if(attrNodes != null) {
      for(AttrImplForPma att : attrNodes)
        node.appendAttributeNode(attr);
    }
  }

  public void replaceStructuredDocumentRegions(IStructuredDocumentRegionList newStructuredDocumentRegions,
                                               IStructuredDocumentRegionList oldStructuredDocumentRegions)
  {
    super.replaceStructuredDocumentRegions(newStructuredDocumentRegions,
                                           oldStructuredDocumentRegions);
  }
}
