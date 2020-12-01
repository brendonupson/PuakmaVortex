package puakma.vortex.editors.pma.parser2;

import java.lang.reflect.Field;

import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.w3c.dom.Document;

public class AttrImplForPma extends AttrImpl
{
  public void setName(String name)
  {
    super.setName(name);
  }

  public void setOwnerDocument(Document ownerDocument)
  {
    super.setOwnerDocument(ownerDocument);
  }

  public void setNameRegion(ITextRegion nameRegion)
  {
    try {
      Field f = AttrImpl.class.getDeclaredField("nameRegion");
      f.setAccessible(true);
      f.set(this, nameRegion);
    }
    catch(Exception e) {
      throw new RuntimeException("ALl is fucked!!!");
    }
  }

  public void setEqualRegion(ITextRegion equalRegion)
  {
    try {
      Field f = AttrImpl.class.getDeclaredField("equalRegion");
      f.setAccessible(true);
      f.set(this, equalRegion);
    }
    catch(Exception e) {
      throw new RuntimeException("ALl is fucked!!!");
    }
  }

  public void setValueRegion(ITextRegion valueRegion)
  {
    try {
      Field f = AttrImpl.class.getDeclaredField("valueRegion");
      f.setAccessible(true);
      f.set(this, valueRegion);
      if(valueRegion != null) {
        f = AttrImpl.class.getDeclaredField("valueSource");
        f.setAccessible(true);
        f.set(this, null);
      }
    }
    catch(Exception e) {
      throw new RuntimeException("ALl is fucked!!!");
    }
  }
}
