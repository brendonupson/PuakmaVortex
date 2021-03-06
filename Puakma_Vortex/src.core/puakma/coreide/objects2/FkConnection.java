/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    04/01/2007
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.objects2;

import java.beans.PropertyChangeListener;

public interface FkConnection
{
  Table getTargetTable();
  
  TableColumn getSourceColumn();
}
