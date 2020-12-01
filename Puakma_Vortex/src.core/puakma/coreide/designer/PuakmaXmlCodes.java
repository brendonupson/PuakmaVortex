/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 13, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.designer;



/**
 * This interface defines some xml node, attribute names...
 *
 * @author Martin Novak
 */
public interface PuakmaXmlCodes
{
  /**
   * Root node for retrieving of all application properties
   */
  String ELEM_PUAKMA_APPLICATION = "puakmaApplication",
         ELEM_APP_PARAM = "appParam",
         ELEM_PUAKMA_APPLICATION_DESC = "pmaAppDescription",
         ELEM_PUAKMA_SERVER = "puakmaServer",
         ELEM_SERVER_INFO_STRING = "serverInfo";
  String ELEM_DESIGN_ELEMENT = "designElement",
         ELEM_PUAKMA_DESIGN_ELEM_COMMENT = "designElemComment",
         ELEM_DESIGN_PARAM = "designParam";
  String ELEM_KEYWORD = "keyword",
         ELEM_KEYWORD_DATA = "keywordData";
  String ELEM_ROLE = "role",
         ELEM_ROLE_DESC = "roleDesc",
         ELEM_PERMISSION = "permiss",
         ELEM_PERMISSION_DESC = "permDesc",
         ELEM_DATABASE = "database",
         ELEM_DATABASE_DESC = "dbDesc",
         ELEM_SYSTEM_PROPERTY = "sysProp";
  String ELEM_RESULT_SET = "resultSet",
         ELEM_GENERATED_KEYS = "generatedKeys",
         ELEM_RS_METADATA = "metadata",
         ELEM_ROW = "row",
         ELEM_COLUMN_VALUE = "colVal",
         ELEM_EXCEPTION = "exception";
  String ELEM_RESULT = "result";
  String ELEM_COLUMN = "column";
  String ELEM_TABLE = "table";
  String ELEM_SQL_KEYWORD = "sqlKw",
         ELEM_NUMERIC_FUNCTION = "numFunc",
         ELEM_DATETIME_FUNCTION = "dateTimeFunc",
         ELEM_STACKTRACE = "stackTrace",
         ELEM_ERROR_CODE = "errorCode",
         ELEM_SQL_STATE = "sqlState",
         ELEM_SQL_STATE_TYPE = "sqlStateType",
         ELEM_JDBC = "jdbc",
         ELEM_PUAMA_SYSTEM_DATABASE = "puakmaSystemDb";
  String ELEM_LOG_ITEM = "logItem";
  String ELEM_CUSTOM = "custom";
  
  String ATT_WRITABLE = "writable";
  String ATT_SIGNED = "signed";
  String ATT_SEARCHABLE = "searchable";
  String ATT_READ_ONLY = "readOnly";
  String ATT_NULLABLE = "nullable";
  String ATT_PRECISION = "precision";
  String ATT_SCALE = "scale";
  String ATT_SCHEMA = "schema";
  String ATT_CURRENCY = "currency";
  String ATT_TABLE = "table";
  String ATT_CASE_SENSITIVE = "caseSensitive";
  String ATT_DEFINITELY_WRITABLE = "definitelyWritable";
  String ATT_LABEL = "label";
  String ATT_TYPE = "type";
  String ATT_TYPE_NAME = "typeName";
  String ATT_DISPLAY_SIZE = "displaySize";
  String ATT_AUTO_INCREMENT = "autoIncrement";
  String ATT_CATALOG_NAME = "catalog";
  String ATT_INDEX = "index";
  
  String ATT_ID = "id";
  String ATT_NAME = "name";
  String ATT_GROUP = "group";
  String ATT_INHERIT = "inherit";
  String ATT_TEMPLATE = "template";
  String ATT_APP_ID = "appId";
  String ATT_DESIGN_TYPE = "designType";
  String ATT_CONTENT_TYPE = "contentType";
  String ATT_UPDATED = "updated";
  String ATT_UPDATED_BY = "updatedBy";
  String ATT_OPTIONS = "options";
  String ATT_EXTRA_OPTIONS = "extraOptions";
  String ATT_SOURCE_LEN = "sourceLen";
  String ATT_DATA_LEN = "dataLen";
  String ATT_VALUE = "value";
  String ATT_ORDER = "order";
  String ATT_DATA = "data";
  String ATT_DESC = "desc";
  String ATT_DATA_CRC32 = "dataCrc32";
  String ATT_SOURCE_CRC32 = "sourceCrc32";
  String ATT_DATA_TYPE = "dataType";
  String ATT_COLUMN_SIZE = "columnSize";
  String ATT_DECIMAL_DIGITS = "decimalDigits";
  String ATT_NUM_PREC_RADIX = "numPrecRadix";
  String ATT_COLUMN_DEF = "columnDef";
  String ATT_POSITION = "position";
  String ATT_ITEM_SOURCE = "itemSource",
         ATT_SERVER_NAME = "serverName",
         ATT_DATE = "date";
  
  String ATT_DB_NAME = "dbName";
  String ATT_URL = "url", ATT_URL_OPTIONS = "dbUrlOptions", 
         ATT_USERNAME = "userName", ATT_DRIVER = "driver",
         ATT_PWD = "pwd", ATT_CREATED = "created",
         ATT_CREATEDBY = "createdBy", ATT_CLASS_NAME = "className",
         ATT_PACKAGE = "package", ATT_LIBRARY = "library",
         ATT_X500NAME = "x500Name",
         ATT_MESSAGE = "message",
         ATT_TYPE_SIZE = "typeSize";
  String ATT_DEFAULT = "defaultValue";
  String ATT_FK_TABLE = "refTable";
  String ATT_FK_TABLE_NAME = "refTableName";
  String ATT_IS_PK = "isPk";
  String ATT_IS_UNIQ = "unique";
  String ATT_CASCADE_DEL = "cascadeDel";
  String ATT_CASCADE_UPD = "cascadeUpd";
  
  // ATTRIBUTES TABLE
  String COL_ATT_NAME = "AttributeName",
         COL_ATT_ID = "AttributeID",
         COL_POSITION = "Position",
         COL_TYPE = "Type",
         COL_TYPE_SIZE = "TypeSize",
         COL_IS_PK = "IsPrimaryKey",
         COL_AUTO_INCREMENT = "AutoIncrement",
         COL_IS_UNIQUE = "IsUnique",
         COL_FK_TABLE = "RefTable",
         COL_FK_COLUMN = "RefColumn",
         COL_CASCADE_DEL = "CascadeDelete",
         COL_CASCADE_UPD = "CascadeDelete";
  String COL_ALLOW_NULL = "AllowNull";
  String COL_EXTRA_OPTIONS = "ExtraOptions";
  String COL_APP_ID = "AppID";
  String COL_APP_NAME = "AppName";
  String COL_APP_GROUP = "AppGroup";
  String COL_INHERIT_FROM = "InheritFrom";
  String COL_TEMPLATE_NAME = "TemplateName";
  String COL_DESCRIPTION = "Description";
  String COL_DESIGN_BUCKET_ID = "DesignBucketID";
  String COL_NAME = "Name";
  String COL_DESIGN_TYPE = "DesignType";
  String COL_DESIGN_DATA = "DesignData";
  String COL_DESIGN_SOURCE = "DesignSource";
  String COL_CONTENT_TYPE = "ContentType";
  String COL_UPDATED = "Updated";
  String COL_UPDATED_BY = "UpdatedBy";
  String COL_OPTIONS = "Options";
  String COL_COMMENT = "Comment";
  
  String COL_APP_PARAM_ID = "AppParamID";
  String COL_DESIGN_BUCKET_PARAM_ID = "DesignBucketParamID";
  String COL_PARAM_NAME = "ParamName";
  String COL_PARAM_VALUE = "ParamValue";
  
  String COL_KEYWORD_ID = "KeywordID";
  
  String COL_SOURCE_LEN = "SourceLen";
  String COL_DATA_LEN = "DataLen";
  String COL_HAS_SOURCE = "HasSource";
  String COL_HAS_DATA = "HasData";
  
  String COL_KEYWORD_DATA_ID = "KeywordDataID";
  String COL_KEYWORD_ORDER = "KeywordOrder";
  String COL_DATA = "Data";
  
  String COL_ROLE_ID = "RoleID", COL_ROLE_NAME = "RoleName";
  
  String COL_DBCONN_ID = "DBConnectionID", COL_DBCONN_NAME = "DBConnectionName",
         COL_DB_NAME = "DBName", COL_DB_URL = "DBURL",
         COL_DBURL_OPTIONS = "DBURLOptions", COL_DB_USERNAME = "DBUserName",
         COL_DB_DRIVER = "DBDriver", COL_DB_PWD = "DBPassword",
         COL_CREATED = "Created", COL_CREATEDBY = "CreatedBy",
         COL_PERMISSION_ID = "PermissionID";
  
  String COL_TABLE_NAME = "TableName",
         COL_TABLE_ID = "TableID",
         COL_BUILD_ORDER = "BuildOrder",
         COL_DEFAULT_VALUE = "DefaultValue";
  String COL_LOG_ID = "LogID",
         COL_LOG_DATE = "LogDate",
         COL_LOG_STRING = "LogString",
         COL_SOURCE = "Source",
         COL_SERVER_NAME = "ServerName",
         COL_USERNAME = "UserName";
  
  int COLT_BOOL      = 1;
  int COLT_BYTE      = 2;
  int COLT_CHAR      = 3;
  int COLT_SHORT     = 4;
  int COLT_INT       = 5;
  int COLT_LONG      = 6;
  int COLT_FLOAT     = 7;
  int COLT_DOUBLE    = 8;
  int COLT_STRING    = 9;
  int COLT_BYTEARRAY = 10;
  // IF ADD SOME TYPE, ALSO CHANGE SOMETHING IN XmlDatabaseResultParser
  
  /*
   * Table generation types
   */
  int GENERATING_SYSTEM = 0;
  int GENERATING_USER = 1;
  int GENERATING_DERIVED = 2;
  
  int[] GENER_TYPES = { GENERATING_SYSTEM, GENERATING_USER, GENERATING_DERIVED };
  String[] GENER_NAMES = { "SYSTEM", "USER", "DERIVED" };
  
  /*
   * Table types
   */
  int TYPE_TABLE = 0;
  int TYPE_VIEW = 1;
  int TYPE_SYSTEM_TABLE = 2;
  int TYPE_GLOBAL_TEMPORARY = 3;
  int TYPE_LOCAL_TEMPORARY = 4;
  int TYPE_ALIAS = 5;
  int TYPE_SYNONYM = 6;
  
  int[] TYPES = { TYPE_TABLE, TYPE_VIEW, TYPE_SYSTEM_TABLE,
    TYPE_GLOBAL_TEMPORARY, TYPE_LOCAL_TEMPORARY, TYPE_ALIAS, TYPE_SYNONYM };
  
  String[] TYPE_NAMES = { "TABLE", "VIEW", "SYSTEM TABLE",
      "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM" };
  
  /**
   * This is token which identifies invalid table.
   */
  String INVALID_TABLE_TOKEN = "##INVALID_ATT_TABLE_TOKEN##";
  
  //
  // TABLES IN THE SYSTEM DATABASE
  //
  String TABLE_ATTRIBUTE = "PMATABLE";
  String TABLE_PMATABLE = "ATTRIBUTE";
}
