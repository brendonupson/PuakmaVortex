package puakma.vortex.editors.pma.parser2;

import java.util.Collection;

import org.eclipse.wst.html.core.internal.document.DocumentStyleImpl;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.xml.core.internal.document.DocumentImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.ssemodelquery.ModelQueryAdapter;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DOMDocumentForPma extends DocumentStyleImpl
{
	private ModelQueryAdapter modelQuery;

	public DOMDocumentForPma()
	{
		super();
	}

	/**
	 * @param that
	 */
	protected DOMDocumentForPma(DocumentImpl that)
	{
		super(that);
	}

	/**
	 * cloneNode method
	 * 
	 * @return org.w3c.dom.Node
	 * @param deep boolean
	 */
	public Node cloneNode(boolean deep)
	{
		DOMDocumentForPma cloned = new DOMDocumentForPma(this);
		if(deep)
			cloned.importChildNodes(this, true);
		return cloned;
	}

	/**
	 * createElement method
	 * 
	 * @return org.w3c.dom.Element
	 * @param tagName java.lang.String
	 */
	public Element createPmaElement(String tagName) throws DOMException
	{
		ElementImplForPma element = new ElementImplForPma();
		element.setOwnerDocument(this);
		element.setTagName(tagName);
		return element;
	}

	/**
	 * createComment method
	 * 
	 * @return org.w3c.dom.Comment
	 * @param data java.lang.String
	 */
	//  public Comment createComment(String data)
	//  {
	//    CommentImplForPma comment = new CommentImplForPma();
	//    comment.setOwnerDocument(this);
	//    if(data != null)
	//      comment.setData(data);
	//    return comment;
	//  }

	/**
	 * createAttribute method
	 * 
	 * @return org.w3c.dom.Attr
	 * @param name java.lang.String
	 */
	public Attr createAttribute(String name) throws DOMException
	{
		AttrImplForPma attr = new AttrImplForPma();
		attr.setOwnerDocument(this);
		attr.setName(name);
		return attr;
	}

	//  public Attr createAttributeNS(String uri, String name) throws DOMException
	//  {
	//    AttrImplForPma attr = new AttrImplForPma();
	//    attr.setOwnerDocument(this);
	//    attr.setName(name);
	//    attr.setNamespaceURI(uri);
	//    return attr;
	//  }

	//  public Text createTextNode(String data)
	//  {
	//    TextImplForPma text = new TextImplForPma();
	//    text.setOwnerDocument(this);
	//    text.setData(data);
	//    return text;
	//  }

	protected void setModel(IDOMModel model)
	{
		super.setModel(model);
	}

	public int getAdapterCount()
	{
		return super.getAdapterCount() + 1;
	}

	public INodeAdapter getAdapterFor(Object type)
	{
		if(type == ModelQueryAdapter.class) {
			if(modelQuery == null) {
				ModelQueryAdapterFactoryForPma factory = new ModelQueryAdapterFactoryForPma();
				modelQuery = (ModelQueryAdapter) factory.createAdapter(this);
			}
			return modelQuery;
		}
		// TODO Auto-generated method stub
		return super.getAdapterFor(type);
	}

	public Collection<INodeAdapter> getAdapters()
	{
		// TODO Auto-generated method stub
		return super.getAdapters();
	}
}
