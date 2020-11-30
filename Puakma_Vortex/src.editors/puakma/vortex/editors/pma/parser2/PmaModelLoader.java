package puakma.vortex.editors.pma.parser2;

import org.eclipse.wst.html.core.internal.document.DOMStyleModelImpl;
import org.eclipse.wst.html.core.internal.encoding.HTMLModelLoader;
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IModelHandler;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

public class PmaModelLoader extends HTMLModelLoader
{
  public IStructuredModel newModel()
  {
    DOMStyleModelImpl model = new DOMStyleModelImplForPma();
    return model;
  }

  public IStructuredModel createModel()
  {
    // TODO Auto-generated method stub
    return super.createModel();
  }

  public IStructuredModel createModel(IStructuredDocument structuredDocument, String baseLocation, IModelHandler handler)
  {
    // TODO Auto-generated method stub
    return super.createModel(structuredDocument, baseLocation, handler);
  }

  public IStructuredModel createModel(IStructuredModel oldModel)
  {
    return super.createModel(oldModel);
  }
}
