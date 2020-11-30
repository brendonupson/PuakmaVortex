package puakma.vortex.editors.pma.schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.wst.html.core.internal.provisional.HTML40Namespace.ElementName;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.basic.CMNamedNodeMapImpl;


public class PmaElementCollection extends CMNamedNodeMapImpl implements ElementName
{
	private static PmaElementCollection instance;

	private static final String DESC = "desc";

	private static final String ATTRIBS = "attribs";

	private static final String REQ_ATTRIBS = "reqatts";

	private PmaAttributeCollection attributeCollection;

	public PmaElementCollection()
	{
		this.attributeCollection = new PmaAttributeCollection();
	}

	public CMNode getNamedItem(String name)
	{
		// TODO Auto-generated method stub
		return super.getNamedItem(name);
	}

	public Iterator<?> iterator()
	{
		// TODO Auto-generated method stub
		return super.iterator();
	}

	public PmaAttributeCollection getAttributesCollection()
	{
		return attributeCollection;
	}

	public static PmaElementCollection getInstance()
	{
		synchronized(PmaElementCollection.class) {
			if(instance == null) {
				instance = new PmaElementCollection();
				instance.loadTags();
			}
		}
		return instance;
	}

	/**
	 * This loads tags definition from tags.props resource in the same package as this class.
	 */
	void loadTags()
	{
		InputStream is = null;
		Map<String, TagDescriptor> m = new HashMap<String, TagDescriptor>();

		try {
			is = PmaElementCollection.class.getResourceAsStream("tags.properties");
			if(is != null) {
				Properties p = new Properties();
				p.load(is);
				int round = 0;

				Iterator<?> it = p.keySet().iterator();
				while(it.hasNext()) {
					String key = (String) it.next();
					String value = p.getProperty(key);
					insertKeyValueToTmpMap(m, key, value, round);
				}
				round++;

				// IN THE SECOUND PASS WE WILL GET INFORMATION ABOUT REQUIRED ATTRIBUTES
				// SINCE IT IS JUST BUILD LIKE THAT
				it = p.keySet().iterator();
				while(it.hasNext()) {
					String key = (String) it.next();
					String value = p.getProperty(key);
					insertKeyValueToTmpMap(m, key, value, round);
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		// SO NOW INITIALIZE THIS OBJECT
		Iterator<TagDescriptor> it = m.values().iterator();
		while(it.hasNext()) {
			TagDescriptor desc = (TagDescriptor) it.next();
			table.put(desc.getName(), desc);
		}
	}

	/**
	 * This function inserts tag property to the map m.
	 */
	private void insertKeyValueToTmpMap(Map<String, TagDescriptor> m, String key, String value, int round)
	{
		String name = getNameFromKey(key);
		if(name == null) {
			System.out.println("Invalid key: " + key);
			return;
		}

		String info = getInfoFromKey(key);
		TagDescriptor desc = m.get(name);
		if(round == 0) {
			if(desc == null) {
				desc = new BasePmaTag(name, this);
				m.put(name, desc);
			}
			if(DESC.equals(info)) {
				desc.setDescription(value);
			}
			else if(ATTRIBS.equals(info)) {
				desc.parseAttribs(value, false);
			}
		}
		else if(round == 1) {
			if(REQ_ATTRIBS.equals(info)) {
				desc.parseAttribs(value, true);
			}
		}
	}

	private String getInfoFromKey(String key)
	{
		int index = key.indexOf('.');
		if(index == -1)
			return null;
		return key.substring(index + 1, key.length());    
	}

	private String getNameFromKey(String key)
	{
		int index = key.indexOf('.');
		if(index == -1)
			return null;
		return key.substring(0, index);
	}

	public TagDescriptor getTag(String tagName)
	{
		return (TagDescriptor) getNamedItem(tagName);
	}

	public List<TagDescriptor> listAllTags()
	{
		List<TagDescriptor> l = new ArrayList<TagDescriptor>(table.values());
		return l;
	}

	public List<TagDescriptor> getTagsStarting(String tagBeginning)
	{
		List<TagDescriptor> l = new ArrayList<TagDescriptor>();
		Iterator<TagDescriptor> it = table.values().iterator();
		while(it.hasNext()) {
			TagDescriptor tag = (TagDescriptor) it.next();
			if(tag.getName().startsWith(tagBeginning))
				l.add(tag);
		}
		return l;
	}
}
