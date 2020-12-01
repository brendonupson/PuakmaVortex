package puakma.vortex.editors.pma;

import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

import puakma.vortex.editors.pma.parser2.PmaStructuredTextViewerConfiguration;

public class PmaStructuredTextViewer extends StructuredTextViewer
{
	public PmaStructuredTextViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles)
	{
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
	}

	@Override
	public void configure(SourceViewerConfiguration configuration)
	{
		if(configuration instanceof PmaStructuredTextViewerConfiguration)
		{
			((PmaStructuredTextViewerConfiguration) configuration).setupReconciler(this);
		}
		super.configure(configuration);
	}
}
