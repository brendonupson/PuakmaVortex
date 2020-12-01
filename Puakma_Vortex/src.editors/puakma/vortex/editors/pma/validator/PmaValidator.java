package puakma.vortex.editors.pma.validator;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.wst.html.core.internal.validation.HTMLValidationReporter;
import org.eclipse.wst.html.core.internal.validation.HTMLValidator;
import org.eclipse.wst.sse.core.internal.validate.ValidationMessage;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

public class PmaValidator extends HTMLValidator
{
	public PmaValidator()
	{

	}

	protected HTMLValidationReporter getReporter(IReporter reporter, IFile file, IDOMModel model)
	{
		//return new HTMLValidationReporter(this, reporter, file, model);
		// TODO Auto-generated method stub
		return new HTMLValidationReporter(this, reporter, file, model) {
			public void report(ValidationMessage message)
			{
				if(message.getMessage().startsWith("Invalid location of tag (P@"))
					return;

				super.report(message);
			}

		};
	}

	public void validate(IRegion dirtyRegion, IValidationContext helper, final IReporter reporter) {
		IReporter r = new IReporter() {
			public void removeMessageSubset(IValidator validator, Object obj, String groupName) {
				reporter.removeMessageSubset(validator, obj, groupName);

			}

			public void removeAllMessages(IValidator origin, Object object) {
				reporter.removeAllMessages(origin, object);
			}

			public void removeAllMessages(IValidator origin) {
				reporter.removeAllMessages(origin);
			}

			public boolean isCancelled() {
				return reporter.isCancelled();
			}

			public List getMessages() {
				return reporter.getMessages();
			}

			public void displaySubtask(IValidator validator, IMessage message) {
				if(message.getText().startsWith("Invalid location"))
					return;
				reporter.displaySubtask(validator, message);
			}

			public void addMessage(IValidator origin, IMessage message) {
				if(message.getText().startsWith("Invalid location"))
					return;
				reporter.addMessage(origin, message);
			}
		};
	}
}
