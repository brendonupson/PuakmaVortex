package puakma.vortex.editors.pma.parser2;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class PuakmaRuleScanner extends RuleBasedScanner
{
	private final Color DEFAULT_TAG_COLOUR = new Color(Display.getCurrent(), new RGB(200, 0, 0));
	private final Color DEFAULT_COMMENT_COLOUR = new Color(Display.getCurrent(), new RGB(0, 200, 0));
	
	
	public PuakmaRuleScanner()
	{
		IToken tagToken = new Token(new TextAttribute(DEFAULT_TAG_COLOUR));
		IToken commentToken = new Token(new TextAttribute(DEFAULT_COMMENT_COLOUR));
		
		IRule[] rules = new IRule[2];
		rules[0] = new SingleLineRule("<", ">", tagToken);
		rules[1] = new EndOfLineRule("//", commentToken);
		setRules(rules);
		
	}
}
