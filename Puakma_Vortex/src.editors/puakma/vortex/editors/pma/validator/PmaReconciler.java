package puakma.vortex.editors.pma.validator;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.eclipse.wst.html.core.internal.validation.HTMLValidator;
import org.eclipse.wst.sse.ui.internal.reconcile.StructuredRegionProcessor;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ValidatorMetaData;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ValidatorStrategy;

public class PmaReconciler extends StructuredRegionProcessor
{
	public PmaReconciler()
	{
		
	}

	@Override
	protected ValidatorStrategy getValidatorStrategy()
	{
		ValidatorStrategy validatorStrategy = super.getValidatorStrategy();
		if(validatorStrategy == null)
			return null;

		try
		{
			Field f = ValidatorStrategy.class.getDeclaredField("fMetaData");
			f.setAccessible(true);
			List l = (List) f.get(validatorStrategy);
			Iterator it = l.iterator();
			while(it.hasNext()) {
				ValidatorMetaData md = (ValidatorMetaData) it.next();
				if(md.getValidatorClass().equals(HTMLValidator.class.getName()))
					it.remove();
			}
			f.set(validatorStrategy, l);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return validatorStrategy;
	}




}
