package puakma.vortex.editors.pma.parser2;

import org.eclipse.wst.html.core.internal.encoding.HTMLDocumentCharsetDetector;
import org.eclipse.wst.html.core.internal.modelhandler.ModelHandlerForHTML;
import org.eclipse.wst.sse.core.internal.document.IDocumentCharsetDetector;
import org.eclipse.wst.sse.core.internal.document.IDocumentLoader;
import org.eclipse.wst.sse.core.internal.provisional.IModelLoader;

public class ModelHandlerForPma extends ModelHandlerForHTML
{
  /**
   * Needs to match what's in plugin registry. In fact, can be overwritten at
   * run time with what's in registry! (so should never be 'final')
   */
  static String AssociatedContentTypeID = "puakma.editors.pmasource"; //$NON-NLS-1$

  /**
   * Needs to match what's in plugin registry. In fact, can be overwritten at
   * run time with what's in registry! (so should never be 'final')
   */
  private static String ModelHandlerID_HTML = "puakma.editor.modelhandler"; //$NON-NLS-1$

  public ModelHandlerForPma()
  {
    super();

    setId(ModelHandlerID_HTML);
    setAssociatedContentTypeId(AssociatedContentTypeID);
  }

  public IModelLoader getModelLoader()
  {
    return new PmaModelLoader();
    //return new HTMLModelLoader();
  }

  public IDocumentCharsetDetector getEncodingDetector()
  {
    return new HTMLDocumentCharsetDetector();
  }

  public IDocumentLoader getDocumentLoader()
  {
    return new PmaDocumentLoader();
  }

}
