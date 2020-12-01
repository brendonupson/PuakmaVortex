package puakma.vortex.editors.pma.schema;

import puakma.vortex.editors.pma.ExtendedTestCase;

public class TestTagsManagerForPma extends ExtendedTestCase
{
	public void testLoad()
	{
		PmaElementCollection m = PmaElementCollection.getInstance();
		m.loadTags();
		TagDescriptor desc = m.getTag("P@Text");
		assertNotNull(desc);
		String[] atts = desc.listAttributes();
		assertArrayContainsValue(atts, "name");
		assertArrayContainsValue(atts, "value");
	}
}
