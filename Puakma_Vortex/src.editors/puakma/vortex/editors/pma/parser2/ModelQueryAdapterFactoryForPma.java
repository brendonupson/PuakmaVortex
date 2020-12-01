package puakma.vortex.editors.pma.parser2;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.html.core.internal.modelquery.ModelQueryAdapterFactoryForHTML;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.util.Debug;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.contentmodel.util.CMDocumentCache;
import org.eclipse.wst.xml.core.internal.modelquery.XMLCatalogIdResolver;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.ssemodelquery.ModelQueryAdapterImpl;


public class ModelQueryAdapterFactoryForPma extends ModelQueryAdapterFactoryForHTML
{
  ModelQueryAdapterImpl modelQueryAdapter;
  
  public INodeAdapter createAdapter(INodeNotifier target)
  {
    if(modelQueryAdapter == null) {
      Object o  = super.createAdapter(target);
      //ModelQueryAdapterImpl adapter = (ModelQueryAdapterImpl) o;
      //HTMLModelQueryImpl htmlMQ = (HTMLModelQueryImpl) o;
      
      if(target instanceof IDOMNode) {
        IDOMNode xmlNode = (IDOMNode) target;

        IStructuredModel model = xmlNode.getModel();
        String baseLocation = null;
        String modelsBaseLocation = model.getBaseLocation();
        if(modelsBaseLocation != null) {
          File file = new Path(modelsBaseLocation).toFile();
          if(file.exists()) {
            baseLocation = file.getAbsolutePath();
          }
          else {
            IPath basePath = new Path(model.getBaseLocation());
            IPath derivedPath = null;
            if(basePath.segmentCount() > 1)
              derivedPath = ResourcesPlugin.getWorkspace().getRoot().getFile(basePath)
                  .getLocation();
            else
              derivedPath = ResourcesPlugin.getWorkspace().getRoot().getLocation()
                  .append(basePath);
            if(derivedPath != null) {
              baseLocation = derivedPath.toString();
            }
          }
        }
        if(Debug.displayInfo)
          System.out.println("----------------ModelQueryAdapterFactoryForPma... baseLocation : " + baseLocation); //$NON-NLS-1$

        CMDocumentCache cmDocumentCache = new CMDocumentCache();
        URIResolver idResolver = null;

        org.eclipse.wst.sse.core.internal.util.URIResolver resolver = model.getResolver();
        if(baseLocation != null || resolver != null) {
          idResolver = new XMLCatalogIdResolver(baseLocation, resolver);
        }
        ModelQuery modelQuery = new PmaModelQueryImpl(cmDocumentCache, idResolver);
        modelQuery.setEditMode(ModelQuery.EDIT_MODE_UNCONSTRAINED);
        modelQueryAdapter = new ModelQueryAdapterImpl(cmDocumentCache, modelQuery, idResolver);
      }
    }
    return modelQueryAdapter;
  }
}
