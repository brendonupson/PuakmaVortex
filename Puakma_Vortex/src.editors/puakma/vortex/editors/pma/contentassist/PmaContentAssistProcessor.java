package puakma.vortex.editors.pma.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.html.ui.internal.contentassist.HTMLContentAssistProcessor;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.pma.parser2.AttrImplForPma;
import puakma.vortex.editors.pma.schema.PmaElementCollection;
import puakma.vortex.editors.pma.schema.TagDescriptor;


public class PmaContentAssistProcessor extends HTMLContentAssistProcessor implements IContentAssistProcessor
{
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
	{
		//    IDocument doc = viewer.getDocument();
		//    String line;
		//    try {
		//      int startOffset = getLineBeginningIndex(doc, offset);
		//      line = doc.get(startOffset, offset - startOffset);
		//      
		//    }
		//    catch(BadLocationException e) {
		//      // TODO Auto-generated catch block
		//      e.printStackTrace();
		//    }
		//IDOMNode node = getCurrentNode(doc, offset);

		ICompletionProposal[] proposals = null;
		//    if()
		proposals = super.computeCompletionProposals(viewer, offset);
		List<ICompletionProposal> proposalsList;

		// REMOVE PROPOSALS FOR PMA TAGS
		if(proposals == null)
			proposalsList = new ArrayList<ICompletionProposal>();
		else
			proposalsList = removePmaTagProposals(proposals);

		String tagBeginning = getPmaTagBeginning(viewer, offset);
		int tagBeginningIndex = -1;
		if(tagBeginning != null) {
			if(tagBeginning.startsWith("<P@"))
				//proposals = super.computeCompletionProposals(viewer, offset);
				//proposalsList = removePmaTagProposals(proposals);
				//}
				proposalsList.clear();

			// ADD OUR PROPOSALS
			addPmaTagProposals(proposalsList, viewer, offset, tagBeginning);
		}
		else if((tagBeginningIndex = getPmaAttributeBeginningIndex(viewer, offset)) != -1) {
			addPmaAttributeProposal(proposalsList, viewer, tagBeginningIndex, offset);
		}
		else {
			// SHOW ALL TAG PROPOSALS
			addAllPmaTagProposals(proposalsList, viewer, offset);
		}

		// SORT ALL TAGS
		Collections.sort(proposalsList, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				ICompletionProposal p1 = (ICompletionProposal) o1, p2 = (ICompletionProposal) o2;
				return p1.getDisplayString().compareToIgnoreCase(p2.getDisplayString());
			}
		});

		return (ICompletionProposal[]) proposalsList.toArray(new ICompletionProposal[proposalsList.size()]);
	}

	/**
	 * Adds puakma attribute proposal.
	 */
	private void addPmaAttributeProposal(List<ICompletionProposal> proposalsList, ITextViewer viewer, int tagBeginningIndex, int offset)
	{
		IndexedRegion treeNode = getNodeAt((StructuredTextViewer) viewer, offset);

		Node node = (Node) treeNode;
		while (node != null && node.getNodeType() == Node.TEXT_NODE && node.getParentNode() != null)
			node = node.getParentNode();

		// IGNORE NON ELEMENT NODES BECAUSE WE WANT TO KNOW TAG NAME
		if(node instanceof IDOMElement == false)
			return;
		// IGNORE ALSO ALL TAGS NOT STARTING WITH P@
		IDOMElement xmlelem = (IDOMElement) node;
		String tagName = xmlelem.getTagName();
		if(tagName.startsWith("P@") == false)
			return;

		// IGNORE UNKNOWN TAG
		PmaElementCollection manager = PmaElementCollection.getInstance();
		TagDescriptor desc = manager.getTag(tagName);
		if(desc == null)
			return;

		String[] attributes = desc.listAttributes();
		List<AttrImplForPma> attNodes = new ArrayList<AttrImplForPma>();

		NamedNodeMap nodes = xmlelem.getAttributes();
		int len = nodes.getLength();
		for(int i = 0; i < len; ++i) {
			Node subnode = nodes.item(i);
			if(subnode == null)
				continue;
			if(subnode instanceof IDOMAttr) {
				AttrImplForPma pmaAttr = (AttrImplForPma) subnode;
				attNodes.add(pmaAttr);
			}
		}

		// SO NOW WE SHOULD HAVE ALL ATTRIBUTES WHICH ARE NOT IN THE CURRENT TAG,
		// SO WE CAN ADD ATTRIBUTES WHICH ARE NOT IN THE TAG, BUT WE HAVE
		// TO DISTINGUISH IF WE ARE AFTER SPACE OR SOMETHING OR IN THE MIDDLE
		// OF ATTRIBUTE - IF IN THE MIDDLE WE SHOULD IGNORE IT
		boolean isInsideAttributeName = false;
		boolean isInsideAttribute = false;
		String attributeHint = null;
		Iterator<AttrImplForPma> it = attNodes.iterator();
		while(it.hasNext()) {
			AttrImplForPma att = (AttrImplForPma) it.next();
			// CHECK IF WE ARE INSIDE THE NAME REGION
			int start = att.getNameRegion().getStart();
			int end = att.getNameRegion().getTextEnd();
			int relOffset = offset - xmlelem.getStartOffset();
			if(start < relOffset && relOffset <= end) {
				isInsideAttributeName = true;
				attributeHint = att.getNameRegionText().substring(0, end - start);
				break;
			}
			// CHECK IF WE ARE INSIDE THE TAG - IF YES, CANCEL THIS SHIT
			ITextRegion valueRegion = att.getValueRegion();
			if(valueRegion != null) {
				start = valueRegion.getStart();
				end = start + valueRegion.getTextLength();
				if(start < relOffset && relOffset <= end) {
					isInsideAttribute = true;
					break;
				}
			}
		}

		if(isInsideAttributeName) {
			// IF WE ARE INSIDE ATTRIBUTE NAME, WE SHOULD USE THE BEGINNING OF THE
			// ATTRIBUTE NAME FOR THE HINT
			addAllPmaTagAttributes(proposalsList, tagName, offset, attNodes, attributeHint);
		}
		else if(isInsideAttribute == false) {
			// BLOW THE WHOLE LIST OF ATTRIBUTES WHICH ARE NOT USED YET
			addAllPmaTagAttributes(proposalsList, tagName, offset, attNodes, null);
		}
	}

	/**
	 * This adds all attributes as a completion for the current node.
	 */
	private void addAllPmaTagAttributes(List<ICompletionProposal> proposalsList, String tagName, int offset,
			List<AttrImplForPma> usedNodes, String nameStart)
	{
		PmaElementCollection manager = PmaElementCollection.getInstance();
		TagDescriptor tag = manager.getTag(tagName);
		proposalsList.clear();

		if(tag == null)
			return;
		String[] attNames = tag.listAttributes();
		if(nameStart == null) {
			// CREATE FAST HASH MAP
			Map<String, AttrImplForPma> usedNamesMap = new HashMap<String, AttrImplForPma>();
			// MOVE USED ATTRIBUTE NAMES TO MAP
			Iterator<AttrImplForPma> it = usedNodes.iterator();
			while(it.hasNext()) {
				AttrImplForPma att = (AttrImplForPma) it.next();
				usedNamesMap.put(att.getName(), att);
			}

			for(int i = 0; i < attNames.length; ++i) {
				if(attNames[i] == null)
					continue;
				String attName = attNames[i];
				if(usedNamesMap.containsKey(attName) == false) {
					CompletionProposal proposal = createAttributeProposal(offset, tag, attName, "");
					proposalsList.add(proposal);
				}
			}
		}
		else {
			// TODO: write proposal with some hint for the user
			for(int i = 0; i < attNames.length; ++i) {
				if(attNames[i] == null)
					continue;
				String nameStartUpper = nameStart.toUpperCase();
				if(attNames[i].toUpperCase().startsWith(nameStartUpper)) {
					CompletionProposal proposal = createAttributeProposal(offset, tag, attNames[i], nameStart);
					proposalsList.add(proposal);
				}
			}
		}
	}

	private CompletionProposal createAttributeProposal(int offset, TagDescriptor tag, String attName, String attBegin)
	{
		String replacementString = attName + "=\"\"";
		String displayStr = attName;
		int replacementOffset = offset - attBegin.length();
		int replacementLen = attBegin.length();
		int cursorPos = replacementString.length() - 1;
		Image image = VortexPlugin.getDefault().getImage("tag-pma.png");
		IContextInformation contextInfo = null;
		// TODO:
		String additionalInfo = null; //desc.getDescription();

		CompletionProposal prop = new CompletionProposal(replacementString, replacementOffset,
				replacementLen, cursorPos, image,
				displayStr, contextInfo, additionalInfo);
		return prop;
	}

	private void addAllPmaTagProposals(List<ICompletionProposal> proposalsList, ITextViewer viewer, int offset)
	{
		PmaElementCollection manager = PmaElementCollection.getInstance();
		List<?> l = manager.listAllTags();
		Iterator<?> it = l.iterator();
		while(it.hasNext()) {
			TagDescriptor desc = (TagDescriptor) it.next();
			CompletionProposal prop = createProposal(offset, desc, "");
			proposalsList.add(prop);
		}
	}

	/**
	 * Adds Puakma tag proposals.
	 */
	private void addPmaTagProposals(List<ICompletionProposal> proposalsList, ITextViewer viewer, int offset, String tagBeginning)
	{
		// THIS STRING SHOULD ALWAYS START WITH '<'
		tagBeginning = tagBeginning.substring(1);

		PmaElementCollection manager = PmaElementCollection.getInstance();
		List<?> l = manager.getTagsStarting(tagBeginning);
		Iterator<?> it = l.iterator();
		while(it.hasNext()) {
			TagDescriptor desc = (TagDescriptor) it.next();
			CompletionProposal prop = createProposal(offset, desc, tagBeginning);
			proposalsList.add(prop);
		}
	}

	/**
	 * Creates a completion proposal for the given tag
	 */
	private CompletionProposal createProposal(int offset, TagDescriptor desc, String tagBeginning)
	{
		Object[] objs = composeRequiredAttrsTogether(desc);
		String requiredAtts = (String) objs[0];
		int requiredAttsOffset = ((Integer) objs[1]).intValue();
		String replacementString = '<' + desc.getName() + requiredAtts + " @P>";
		String displayStr = desc.getName();
		int replacementOffset = offset - tagBeginning.length() - 1;
		int replacementLen = tagBeginning.length() + 1;
		int cursorPos;
		if(requiredAttsOffset > 0)
			cursorPos = desc.getName().length() + 1 + requiredAttsOffset;
		else
			cursorPos = replacementString.length();
		Image image = VortexPlugin.getDefault().getImage("tag-pma.png");
		IContextInformation contextInfo = null;
		String additionalInfo = desc.getDescription();

		CompletionProposal prop = new CompletionProposal(replacementString, replacementOffset,
				replacementLen, cursorPos, image,
				displayStr, contextInfo, additionalInfo);
		return prop;
	}

	/**
	 * Creates a string with all required attributes to insert to &lt;P@ tag.
	 * Returns array where the first item is string with all attributes to
	 * complete, and the second item is offset of the first attribute value or 0
	 * for no attributes.
	 */
	public static Object[] composeRequiredAttrsTogether(TagDescriptor desc)
	{
		int offset = 0;
		StringBuffer sb = new StringBuffer();
		String[] atts = desc.listRequiredAttributes();
		if(atts.length > 0)
			offset = atts[0].length() + 3;
		for(int i = 0; i < atts.length; ++i) {
			sb.append(' ');
			sb.append(atts[i]);
			sb.append("=\"\"");
		}
		return new Object[] { sb.toString(), new Integer(offset) };
	}

	/**
	 * Removes all unwanted &lt;P@xxx tag proposals, and returns the list free of
	 * such proposals from parameter.
	 */
	private List<ICompletionProposal> removePmaTagProposals(ICompletionProposal[] proposals)
	{
		List<ICompletionProposal> ret = new ArrayList<ICompletionProposal>();

		if(proposals != null) {
			for(int i = 0; i < proposals.length; ++i) {
				if(proposals[i] instanceof CustomCompletionProposal) {
					CustomCompletionProposal prop = (CustomCompletionProposal) proposals[i];
					String text = prop.getDisplayString();
					if(text.startsWith("P@") == false) {
						ret.add(prop);
					}
				}
				else {
					System.out.println("Invalid proposal type. Proposal: " + proposals[i]);
				}
			}
		}

		return ret;
	}

	private String getPmaTagBeginning(ITextViewer viewer, int offset)
	{
		IDocument doc = viewer.getDocument();
		try {
			int lineStartIndex = getLineBeginningIndex(doc, offset);
			int len = offset - lineStartIndex;
			String ret = doc.get(lineStartIndex, len);

			// NOW FIND THE BEGINNING OF THE PROPOSAL
			int index = ret.lastIndexOf('<');
			if(index == -1)
				return null;
			String beginning = ret.substring(index);
			// WE SHOULD ALSO FILTER OUT STRINGS LONGER THAN 3 CHARS, BUT NOT STARTING WITH <P@
			if(beginning.length() > 3 && beginning.startsWith("<P@") == false)
				return null;
			// ALSO FILTER OUT STUFF HAVING SPACE SOMEWHERE
			if(beginning.indexOf(' ') != -1 || beginning.indexOf('\t') != -1 || beginning.indexOf('\n') != -1)
				return null;
			// SO NOW IT SHOULD BE FILTERED OUT A BIT
			return beginning;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if the proposal is for some puakma attribute. If yes, returns true.
	 */
	private int getPmaAttributeBeginningIndex(ITextViewer viewer, int offset)
	{
		try {
			IDocument doc = viewer.getDocument();
			int lastLineBeginningIndex = getLineBeginningIndex(doc, offset);
			String lastLine = doc.get(lastLineBeginningIndex, offset - lastLineBeginningIndex);
			int pmaTagStartIndex = lastLine.lastIndexOf("<P@");
			if(pmaTagStartIndex == -1)
				return -1;

			// DISABLE THE CASE WHEN THE PMA TAG IS CLOSED
			int pmaTagEndIndex = lastLine.lastIndexOf("@P>");
			if(pmaTagEndIndex < pmaTagStartIndex && pmaTagEndIndex != -1)
				return -1;

			return pmaTagStartIndex;
		}
		catch(BadLocationException ex) {
			ex.printStackTrace();
		}

		return -1;
	}


	private int getLineBeginningIndex(IDocument doc, int offset) throws BadLocationException
	{
		while(offset >= 0) {
			offset--;
			char c = doc.getChar(offset);
			if(c == '\n')
				return offset + 1;
			//offset--;
		}

		return offset;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		return new IContextInformation[0];
	}

	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters()
	{
		return new char[0];
	}

	public IContextInformationValidator getContextInformationValidator()
	{
		return null;
	}

	public String getErrorMessage()
	{
		return null;
	}

	/**
	 * Returns the node the cursor is currently on in the document. null if no
	 * node is selected
	 */
	public static IDOMNode getCurrentNode(IDocument document, int offset)
	{
		// get the current node at the offset (returns either: element,
		// doctype, text)
		IndexedRegion inode = null;
		IStructuredModel sModel = null;
		try {
			sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);
			inode = sModel.getIndexedRegion(offset);
			if(inode == null)
				inode = sModel.getIndexedRegion(offset - 1);
		}
		finally {
			if(sModel != null)
				sModel.releaseFromRead();
		}

		if(inode instanceof IDOMNode) {
			return (IDOMNode) inode;
		}
		return null;
	}

	/**
	 * Returns the closest IndexedRegion for the offset and viewer allowing
	 * for differences between viewer offsets and model positions. note: this
	 * method returns an IndexedRegion for read only
	 * 
	 * @param viewer
	 *            the viewer whose document is used to compute the proposals
	 * @param documentOffset
	 *            an offset within the document for which completions should
	 *            be computed
	 * @return an IndexedRegion
	 */
	public static IndexedRegion getNodeAt(StructuredTextViewer viewer, int documentOffset)
	{
		if(viewer == null)
			return null;

		IndexedRegion node = null;
		IModelManager mm = StructuredModelManager.getModelManager();
		IStructuredModel model = null;
		if(mm != null)
			model = mm.getExistingModelForRead(viewer.getDocument());
		try {
			if(model != null) {
				int lastOffset = documentOffset;
				node = model.getIndexedRegion(documentOffset);
				while(node == null && lastOffset >= 0) {
					lastOffset--;
					node = model.getIndexedRegion(lastOffset);
				}
			}
		}
		finally {
			if(model != null)
				model.releaseFromRead();
		}
		return node;
	}
}
