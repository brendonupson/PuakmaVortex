package puakma.vortex.editors.pma.parser2;

import java.util.Iterator;
import java.util.List;

import org.eclipse.wst.html.core.internal.encoding.HTMLDocumentLoader;
import org.eclipse.wst.html.core.internal.modelquery.ModelQueryAdapterFactoryForHTML;
import org.eclipse.wst.sse.core.internal.document.IDocumentLoader;
import org.eclipse.wst.sse.core.internal.document.StructuredDocumentFactory;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapterFactory;
import org.eclipse.wst.sse.core.internal.provisional.document.IEncodedDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.text.BasicStructuredDocument;
import org.eclipse.wst.xml.core.internal.parser.XMLStructuredDocumentReParser;

public class PmaDocumentLoader extends HTMLDocumentLoader implements IDocumentLoader
{
  protected IEncodedDocument newEncodedDocument()
  {
    IStructuredDocument structuredDocument = StructuredDocumentFactory
        .getNewStructuredDocumentInstance(getParser());
    ((BasicStructuredDocument) structuredDocument)
        .setReParser(new XMLStructuredDocumentReParser());

    return structuredDocument;
  }

  public List getAdapterFactories()
  {
    List result = super.getAdapterFactories();

    // WE HAVE TO REMOVE ModelQueryAdapterFactoryForHTML OBJECT AND REPLACE IT
    // WITH ModelQueryAdapterFactoryForPma INSTANCE
    Iterator it = result.iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(o instanceof ModelQueryAdapterFactoryForHTML) {
        it.remove();
        INodeAdapterFactory factory = new ModelQueryAdapterFactoryForPma();
        result.add(factory);
        break;
      }
    }

    return result;
  }
}
