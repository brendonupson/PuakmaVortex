package puakma.vortex.editors.pma.parser2;

import java.lang.reflect.Field;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.css.core.text.ICSSPartitions;
import org.eclipse.wst.css.ui.internal.contentassist.CSSContentAssistProcessor;
import org.eclipse.wst.html.core.text.IHTMLPartitions;
import org.eclipse.wst.html.ui.internal.contentassist.HTMLContentAssistProcessor;
import org.eclipse.wst.html.ui.internal.contentassist.NoRegionContentAssistProcessorForHTML;
//import org.eclipse.wst.javascript.ui.internal.common.contentassist.JavaScriptContentAssistProcessor;
import org.eclipse.wst.jsdt.debug.internal.ui.source.contentassist.JavaScriptContentAssistProcessor;
import org.eclipse.wst.sse.core.text.IStructuredPartitions;
import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;
import org.eclipse.wst.xml.core.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;

import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.pma.contentassist.PmaContentAssistProcessor;
import puakma.vortex.editors.pma.validator.PmaReconciler;

public class PmaStructuredTextViewerConfiguration extends StructuredTextViewerConfiguration//StructuredTextViewerConfigurationHTML
{
	public PmaStructuredTextViewerConfiguration()
	{
		super();
		
	}

	@Override
	protected IContentAssistProcessor[] getContentAssistProcessors(ISourceViewer sourceViewer,
			String partitionType)
	{

		//VortexPlugin.log(super.getClass().getCanonicalName() + partitionType);
		
		IContentAssistProcessor[] processors = null;

		if(partitionType == IHTMLPartitions.HTML_DEFAULT) {
			processors = new IContentAssistProcessor[] { new PmaContentAssistProcessor() };
		}
		else if(partitionType == IHTMLPartitions.HTML_COMMENT) {
			processors = new IContentAssistProcessor[] { new HTMLContentAssistProcessor() };
		}
		else if(partitionType == IHTMLPartitions.SCRIPT) {
			processors = new IContentAssistProcessor[] {
					new JavaScriptContentAssistProcessor(null), new PmaContentAssistProcessor() };
		}
		else if(partitionType == ICSSPartitions.STYLE) {
			processors = new IContentAssistProcessor[] { new CSSContentAssistProcessor(),
					new PmaContentAssistProcessor() };
		}
		else if(partitionType == PmaPartitions.PMA_PART) {
			processors = new IContentAssistProcessor[] { new PmaContentAssistProcessor() };
		}
		else if(partitionType == IXMLPartitions.XML_DEFAULT) {
			processors = new IContentAssistProcessor[] { new XMLContentAssistProcessor() };
		}
		else if(partitionType == IStructuredPartitions.UNKNOWN_PARTITION) {
			processors = new IContentAssistProcessor[] { new NoRegionContentAssistProcessorForHTML() };
		}

		return processors;
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType)
	{
		return super.getAutoEditStrategies(sourceViewer, contentType);
	}

	public void setupReconciler(ISourceViewer sourceViewer)
	{
		Field f;
		try {
			f = StructuredTextViewerConfiguration.class.getDeclaredField("fReconciler");
			f.setAccessible(true);
			PmaReconciler reconciler = new PmaReconciler();
			reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
			f.set(this, reconciler);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
