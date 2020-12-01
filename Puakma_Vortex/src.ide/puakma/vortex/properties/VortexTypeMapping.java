package puakma.vortex.properties;

import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

public class VortexTypeMapping implements ITypeMapper
{

  public Class<?> mapType(Object object)
  {
//    if(object.getClass() == TablePart.class) {
//      return ((TablePart) object).getTable();
//    }
    return null;
  }

}
