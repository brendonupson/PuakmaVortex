package puakma.vortex.editors.pma.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.wst.html.core.internal.contentmodel.HTMLAttributeDeclaration;
import org.eclipse.wst.html.core.internal.contentmodel.HTMLCMDataType;
import org.eclipse.wst.html.core.internal.contentmodel.HTMLElementDeclaration;
import org.eclipse.wst.html.core.internal.contentmodel.HTMLPropertyDeclaration;
import org.eclipse.wst.html.core.internal.provisional.HTMLCMProperties;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMContent;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.basic.CMNamedNodeMapImpl;
import org.eclipse.wst.xml.core.internal.contentmodel.basic.CMNodeImpl;

import puakma.vortex.editors.pma.parser2.PmaCMDataTypeImpl;


public class BasePmaTag extends CMNodeImpl implements
HTMLElementDeclaration, HTMLPropertyDeclaration,
org.eclipse.wst.xml.core.internal.contentmodel.CMContent,
TagDescriptor
{
	public static final int UNBOUNDED = -1;

	/** -1: it's UNBOUNDED. */
	private int maxOccur = UNBOUNDED;

	/** 0: it's OPTIONAL, 1, it's REQUIRED. */
	private int minOccur = 0;

	protected CMNamedNodeMapImpl attributes = null;

	private String tagName;

	private PmaElementCollection elementCollection;

	protected PmaAttributeCollection attributeCollection;

	private String description;

	protected final static CMNamedNodeMap EMPTY_MAP = new CMNamedNodeMap() {
		public int getLength() {
			return 0;
		}

		public CMNode getNamedItem(String name) {
			return null;
		}

		public CMNode item(int index) {
			return null;
		}

		public Iterator iterator() {
			return new Iterator() {
				public boolean hasNext()
				{
					return false;
				}

				public Object next()
				{
					return null;
				}

				public void remove()
				{
				}
			};
		}
	};

	public BasePmaTag(String tagName, PmaElementCollection collection)
	{
		super();

		this.tagName = tagName;
		this.elementCollection = collection;
		this.attributeCollection = collection.getAttributesCollection();
		attributes = new CMNamedNodeMapImpl();
	}

	public HTMLAttributeDeclaration getAttributeDeclaration(String attrName)
	{
		if(attributes == null)
			return null; // fail to create

			CMNode cmnode = attributes.getNamedItem(attrName);
			if(cmnode == null) {
				return null;
			}
			else {
				return (HTMLAttributeDeclaration) cmnode; // already exists.
			}
	}

	/**
	 * This function should create attributes declaration.
	 */
	//protected abstract void createAttributeDeclarations();

	public CMNamedNodeMap getAttributes()
	{
		return attributes;
	}

	/**
	 * No tady nevim jestli je to dobre - viz HTMLElementDeclImpl. Jsou tam nejake
	 * komplexni typy, ale co to znamena, to neni nikde psano!
	 */
	public CMContent getContent()
	{
		return null;
	}

	public int getContentType()
	{
		return CMElementDeclaration.EMPTY;
	}

	public CMDataType getDataType()
	{
		return null;
	}

	public String getElementName()
	{
		return getNodeName();
	}

	public String getNodeName()
	{
		return tagName;
	}

	/**
	 * No HTML element has local elements. So, this method always returns an empty
	 * map.
	 * 
	 * @see org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration
	 */
	public CMNamedNodeMap getLocalElements()
	{
		return EMPTY_MAP;
	}

	public int getMaxOccur()
	{
		return maxOccur;
	}

	public int getMinOccur()
	{
		return minOccur;
	}

	public int getNodeType()
	{
		return CMNode.ELEMENT_DECLARATION;
	}

	public int getCorrectionType()
	{
		return CORRECT_NONE;
	}

	public CMContent getExclusion()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int getFormatType()
	{
		return FORMAT_HTML;
	}

	public CMContent getInclusion()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int getLayoutType()
	{
		return LAYOUT_NONE;
	}

	/**
	 * Line break hint is strongly related to layout type. Indeed, in the
	 * C++DOM, it is determined from layout type only. So, this implementation,
	 * as the default implementation for all declarations, also determines from
	 * layout type only.<br>
	 */
	public int getLineBreakHint()
	{
		switch(getLayoutType()) {
		case HTMLElementDeclaration.LAYOUT_BLOCK:
			return HTMLElementDeclaration.BREAK_BEFORE_START_AND_AFTER_END;
		case HTMLElementDeclaration.LAYOUT_BREAK:
			return HTMLElementDeclaration.BREAK_AFTER_START;
		case HTMLElementDeclaration.LAYOUT_HIDDEN:
			return HTMLElementDeclaration.BREAK_BEFORE_START_AND_AFTER_END;
		default:
			return HTMLElementDeclaration.BREAK_NONE;
		}
	}

	public int getOmitType()
	{
		return OMIT_NONE;
	}

	public CMNamedNodeMap getProhibitedAncestors()
	{
		return EMPTY_MAP;
	}

	public boolean isJSP()
	{
		return false;
	}

	/**
	 * In some elements, such as APPLET, a source generator should indent child
	 * elements that their parents.  That is, a source generator should generate
	 * source  of APPLET and PARAMS like this:
	 * <PRE>
	 *   &lt;APPLET ...&gt;
	 *     &lt;PARAM ... &gt;
	 *     &lt;PARAM ... &gt;
	 *   &lt;/APPLET&gt;
	 * <PRE>
	 * @return boolean
	 */
	public boolean shouldIndentChildSource() {
		return false;
	}

	/**
	 * Most of elements can compact spaces in their child text nodes. Some special
	 * elements should keep them in their source.
	 */
	public boolean shouldKeepSpaces() {
		return false;
	}

	/**
	 * Return element names which terminates this element.<br>
	 */
	protected Iterator getTerminators()
	{
		return null;
	}

	public boolean shouldTerminateAt(HTMLElementDeclaration nextElement)
	{
		Iterator i = getTerminators();
		if(i == null)
			return false;
		String nextName = nextElement.getElementName();
		while(i.hasNext()) {
			if(nextName.equals(i.next()))
				return true;
		}
		return false;
	}
	public boolean supports(String propertyName)
	{
		if(propertyName.equals(HTMLCMProperties.SHOULD_IGNORE_CASE)) {
			return true;
		}
		else if(HTMLCMProperties.IS_JSP.equals(propertyName)) {
			return true;
		}
		else if(HTMLCMProperties.IS_SSI.equals(propertyName)) {
			return true;
		}
		//    TODO: deal with that
		//    else if (propertyName.equals(HTMLCMProperties.CONTENT_HINT)) {
		//        ComplexTypeDefinition def = getComplexTypeDefinition();
		//        return (def != null);
		//    }
		else {
			//      TODO: implement here tag hints
			//        PropertyProvider pp = PropertyProviderFactory.getProvider(propertyName);
			//        if (pp == null)
			//            return false;
			//        return pp.supports(this);
		}
		return false;
	}

	public Object getProperty(String propertyName)
	{
		if (propertyName.equals(HTMLCMProperties.SHOULD_IGNORE_CASE)) {
			return new Boolean(false);
		}
		else if(HTMLCMProperties.IS_JSP.equals(propertyName)) {
			return new Boolean(false);
		}
		else if(HTMLCMProperties.OMIT_TYPE.equals(propertyName)) {
			return HTMLCMProperties.Values.OMIT_NONE;
		}
		else if(HTMLCMProperties.IS_SSI.equals(propertyName)) {
			return new Boolean(false);
		}
		// TODO: implement this here:
		//    else if (propertyName.equals(HTMLCMProperties.CONTENT_HINT)) {
		//        ComplexTypeDefinition def = getComplexTypeDefinition();
		//        return (def != null) ? def.getPrimaryCandidate() : null;
		//    }
		//    else {
		//        PropertyProvider pp = PropertyProviderFactory.getProvider(propertyName);
		//        if (pp == null)
		//            return null;
		//        return pp.get(this);
		//    }

		return null;
	}

	public String getName()
	{
		return tagName;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}

	public void parseAttribs(String atts, boolean required)
	{
		StringTokenizer tokenizer = new StringTokenizer(atts, ",");
		if(required) {
			while(tokenizer.hasMoreTokens()) {
				String attName = tokenizer.nextToken();
				PmaAttrDeclImpl att = (PmaAttrDeclImpl) attributes.getNamedItem(attName);
				att.setUsage(CMAttributeDeclaration.REQUIRED);
			}
		}
		else {
			HTMLAttributeDeclaration attr = null;
			HTMLCMDataType atype = null;

			while(tokenizer.hasMoreTokens()) {
				String name = tokenizer.nextToken();
				atype = new PmaCMDataTypeImpl(CMDataType.CDATA);
				attr = new PmaAttrDeclImpl(name, atype, CMAttributeDeclaration.OPTIONAL);
				attributes.put(attr);
			}
		}
	}

	/**
	 * Lists all attribute names.
	 */
	public String[] listAttributes()
	{
		String[] attrs = new String[attributes.getLength()];
		Iterator it = attributes.iterator();
		int i = 0;
		while(it.hasNext()) {
			HTMLAttributeDeclaration att = (HTMLAttributeDeclaration) it.next();
			attrs[i] = att.getAttrName();
			i++;
		}
		return attrs;
	}

	/**
	 * Lists all required attribute names.
	 */
	public String[] listRequiredAttributes()
	{
		List<String> l = new ArrayList<String>();
		Iterator it = attributes.iterator();
		while(it.hasNext()) {
			HTMLAttributeDeclaration att = (HTMLAttributeDeclaration) it.next();
			if(att.getUsage() == CMAttributeDeclaration.OPTIONAL == false)
				l.add(att.getAttrName());
		}
		return (String[]) l.toArray(new String[l.size()]);
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getName());
		sb.append("[");
		Iterator it = attributes.iterator();
		while(it.hasNext()) {
			HTMLAttributeDeclaration att = (HTMLAttributeDeclaration) it.next();
			sb.append(att.getAttrName());
			sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
}
