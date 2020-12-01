/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    08/06/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.designer;

import java.io.IOException;

import puakma.SOAP.SOAPFaultException;

public interface DatabaseDesigner
{
  public String executeQuery(long dbConnectionId, String sql, boolean isUpdate) throws IOException,
                                                                               SOAPFaultException;

  public String[] executeBatch(long dbConnectionId, String[] sqlCommands, boolean failOnError,
                               boolean transaction) throws IOException, SOAPFaultException;

  public String getDdl(long dbConnectionId) throws IOException, SOAPFaultException;

  public String[] puakmaDatabase_TableVersionCheck(boolean force) throws IOException,
                                                                 SOAPFaultException;

  public int deletePuakmaAttribute(long attId) throws IOException, SOAPFaultException;

  public long savePuakmaAttribute(long attId, long tableId, String attName, String type,
                                  String typeSize, boolean allowNull, boolean isPk,
                                  boolean isAutoIncrement, boolean isUnique, boolean isFtIndex,
                                  String refTable, long refColumn, String extraOptions,
                                  boolean cascadeDelete, String description, String defaultValue,
                                  int position, boolean cascadeUpdate) throws IOException,
                                                                      SOAPFaultException;

  public long[] savePuakmaAttributes(long[] attId, long[] tableId, String[] attName, String[] type,
                                     String[] typeSize, boolean[] allowNull, boolean[] isPk,
                                     boolean[] isAutoIncrement, boolean[] isUnique,
                                     boolean[] isFtIndex, String[] refTable, long[] refColumn,
                                     String[] extraOptions, boolean[] cascadeDelete,
                                     String[] description, String[] defaultValue, int[] position,
                                     boolean[] cascadeUpdate) throws IOException,
                                                             SOAPFaultException;

  public void deletePuakmaTable(long tableId) throws IOException, SOAPFaultException;

  public long savePuakmaTable(long idTable, long idDbCon, String tableName, String description,
                              int buildOrder) throws IOException, SOAPFaultException;

  public void savePuakmaTableFKs(long columnId, long refTableId, long refColumnId) throws IOException,
                                                                                  SOAPFaultException;

  long savePuakmaAttribute2(long attId, long tableId, String attName, String type, String typeSize,
                            boolean allowNull, boolean isPk, boolean isAutoIncrement,
                            boolean isUnique, boolean isFtIndex, String refTable, long refColumn,
                            String extraOptions, int fkDeleteAction, int fkUpdateAction,
                            String description, String defaultValue, int position) throws IOException,
                                                                                  SOAPFaultException;

  long[] savePuakmaAttributes2(long[] attId, long[] tableId, String[] attName, String[] type,
                               String[] typeSize, boolean[] allowNull, boolean[] isPk,
                               boolean[] isAutoIncrement, boolean[] isUnique, boolean[] isFtIndex,
                               String[] refTable, long[] refColumn, String[] extraOptions,
                               int[] fkDeleteAction, int[] fkUpdateAction, String[] description,
                               String[] defaultValue, int[] position) throws IOException,
                                                                     SOAPFaultException;
}
