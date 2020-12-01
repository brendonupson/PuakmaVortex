/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    26/08/2006
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

public interface TornadoDatabaseConstraints
{
  public int getMaxApplication_AppNameLen();
  
  public int getMaxApplication_AppGroupLen();
  
  public int getMaxApplication_InheritLen();
  
  public int getMaxApplication_TemplateNameLen();
  
  public int getMaxAppParam_ParamNameLen();
  
  public int getMaxAppParam_ParamValueLen();
  
  public int getMaxDbCon_DbConNameLen();
  
  public int getMaxDbCon_DbNameLen();
  
  public int getMaxDbCon_DbUrlLen();
  
  public int getMaxDbCon_DbUrlOptionsLen();
  
  public int getMaxDbCon_DbUserNameLen();
  
  public int getMaxDbCon_CreatedByLen();
  
  public int getMaxDbCon_DbPasswordLen();
  
  public int getMaxDbCon_DbDriverLen();
  
  public int getMaxDbCon_OptionsLen();
  
  public int getMaxDbCon_InheritFromLen();
  
  public int getMaxDObj_NameLen();
  
  public int getMaxDObj_ContentTypeLen();
  
  public int getMaxDObj_UpdatedByLen();
  
  public int getMaxDObj_OptionsLen();
  
  public int getMaxDObj_InheritFromLen();
  
  public int getMaxDOParams_ParamNameLen();
  
  public int getMaxDOParams_ParamValueLen();
  
  public int getMaxKeyword_NameLen();
  
  public int getMaxKeywordData_DataLen();
  
  public int getMaxPermission_NameLen();
  
  public int getMaxRole_NameLen();
}
