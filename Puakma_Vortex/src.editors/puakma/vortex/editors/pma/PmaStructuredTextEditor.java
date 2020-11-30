package puakma.vortex.editors.pma;

import java.lang.reflect.Field;

import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.pma.parser2.PmaStructuredTextViewerConfiguration;

public class PmaStructuredTextEditor extends StructuredTextEditor
{
	public PmaStructuredTextEditor()
	{
		super();
		
		try {
			Field f = AbstractTextEditor.class.getDeclaredField("fConfiguration");
			f.setAccessible(true);
			
			
			//Instead of custom PmaStructuredTextViewerConfiguration class, built-in StructuredTextViewerConfigurationHTML
			//can be used. But ContentAssist will not be added to the editor.
			SourceViewerConfiguration conf = new PmaStructuredTextViewerConfiguration();//StructuredTextViewerConfigurationHTML();
			f.set(this, conf);
			
		}
		catch(Exception ex) {
			Logger.logException(ex);
		}
	}

	
	@Override
	protected StructuredTextViewer createStructedTextViewer(Composite parent, IVerticalRuler verticalRuler, int styles)
	{
		return new PmaStructuredTextViewer(parent, verticalRuler, getOverviewRuler(), isOverviewRulerVisible(), styles);
	}

	@Override
	public IDocumentProvider getDocumentProvider()
	{ 
		return super.getDocumentProvider();
	}

}
