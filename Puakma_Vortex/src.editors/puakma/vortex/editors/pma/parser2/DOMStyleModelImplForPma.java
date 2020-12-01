package puakma.vortex.editors.pma.parser2;

import org.eclipse.wst.html.core.internal.document.DOMStyleModelImpl;
import org.eclipse.wst.xml.core.internal.document.XMLModelParser;
import org.eclipse.wst.xml.core.internal.document.XMLModelUpdater;
import org.w3c.dom.Document;

public class DOMStyleModelImplForPma extends DOMStyleModelImpl
{
  /**
   * 
   */
  public DOMStyleModelImplForPma()
  {
    super();
    // remember, the document is created in super constructor,
    // via internalCreateDocument
  }

  /**
   * createDocument method
   * 
   * @return org.w3c.dom.Document
   */
  protected Document internalCreateDocument()
  {
    DOMDocumentForPma document = new DOMDocumentForPma();
    document.setModel(this);
    return document;
  }

  protected XMLModelParser createModelParser()
  {
    return new PmaModelParser(this);
  }

  protected XMLModelUpdater createModelUpdater()
  {
    return super.createModelUpdater();
    //return new NestDOMModelUpdater(this);
  }

  /**
   * Gets the Puakma DOM document implementation.
   */
  public DOMDocumentForPma getPmaDocument()
  {
    return (DOMDocumentForPma) getDocument();
  }
}
