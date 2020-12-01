package puakma.vortex.editors.pma.parser2;

import java.lang.reflect.Field;

import org.eclipse.wst.html.core.internal.document.ElementStyleImpl;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.core.internal.document.NodeImpl;
import org.w3c.dom.Document;

// TODO: implement this according to JSP thing
public class ElementImplForPma extends ElementStyleImpl
{
  public ElementImplForPma()
  {
    super();
    
    setJSPTag(true);
  }
  
  protected void setOwnerDocument(Document ownerDocument)
  {
    super.setOwnerDocument(ownerDocument);
  }

  /**
   * Sets the tag name. Note that this adds P@ in front of the tag name.
   */
  protected void setTagName(String tagName)
  {
    super.setTagName("P@" + tagName);
  }

  void setStartStructuredDocumentRegion(IStructuredDocumentRegion flatNode)
  {
    try {
      Field f = NodeImpl.class.getDeclaredField("flatNode");
      f.setAccessible(true);
      f.set(this, flatNode);
    }
    catch(Exception e) {
      throw new RuntimeException("ALl is fucked!!!");
    }
//    Method methodSetValueRegion = null;
//
//    try {
//      methodSetValueRegion = ElementImpl.class
//          .getMethod("setStartStructuredDocumentRegion",
//                     new Class[] { IStructuredDocumentRegion.class });
//      methodSetValueRegion.setAccessible(true);
//      methodSetValueRegion.invoke(this, new Object[] { flatNode });
//    }
//    catch(Exception ex) {
//      throw new IllegalStateException("Cannot call some function");
//    }
//    finally {
//      // if(methodSetValueRegion != null)
//      // methodSetValueRegion.setAccessible(false);
//    }
  }
  
  public boolean isContainer()
  {
    return false;
  }

  public boolean isClosed()
  {
    return true;
  }

  public boolean isEmptyTag()
  {
    return true;
  }

  public boolean isEndTag()
  {
    return super.isEndTag();
  }

  public boolean isJSPContainer()
  {
    return super.isJSPContainer();
  }

  public boolean isStartTagClosed()
  {
    return true;
  }

//  protected CMElementDeclaration getDeclaration()
//  {
//    return super.getDeclaration();
//  }
}
