//
// SCRIPT FOR CREATING HSQL DATABASE SQL CODE
//
def sql = '';
def tables = db.listTables();
for(table in tables) {
  def cols = table.listColumns()
  def colCount = cols.length
  def i = 0
  if(dropTablePolicy == 'dropTablesIfExists')
    sql += 'DROP TABLE IF EXISTS ' + table.name + ';\n'
  
  sql += 'CREATE'
  if(dropTablePolicy == 'createIfNotExists')
    sql += ' IF NOT EXISTS'
  sql += ' TABLE ' + table.name + ' (\n'
  for(col in cols) {
    sql += '\t' + col.name + ' ' + col.type;
    def type = col.type.toUpperCase()
    if(type == 'FLOAT' || type == 'DOUBLE' || type == 'NUMERIC' || type == 'DECIMAL') {
      sql += ' (' + typeSize
      if(col.floatDecimals > 0)
        sql += ', ' + col.floatDecimals;
      sql += ')'
    }
    else if(col.type != 'INTEGER') {
      sql += ' (' + col.typeSize + ')'
    }
    
    if(col.allowsNull() == false)
      sql += ' NOT NULL'
    if(col.defaultValue)
      sql += ' DEFAULT ' + col.defaultValue
    if(col.autoInc)
      sql += ' AUTO_INCREMENT'
    if(col.pk)
      sql += ' PRIMARY KEY'
    if(i < colCount - 1)
      sql += ','
    sql += '\n'
    i++
  }
  sql += ');\n'
}

for(table in tables) {  
  // ONE MORE LOOP FOR CONSTRAINTS
  def i = 0
  def cols = table.listColumns()
  for(col in cols) {
    if(col.fk) {
      sql += 'ALTER TABLE ' + table.name
      sql += ' ADD CONSTRAINT fk_' + table.name + '_' + col.name + ' FOREIGN KEY (' + col.name + ')'
      sql += ' REFERENCES ' + col.refTable.name + ';\n'
      //if(col.refColumn)
      //  sql += ' (' + col.refColumn + ')'
    }
  }
}


return sql

/*#foreach($col in $columns)
#if($col.isFk())
CONSTRAINT fk_${table.name}_$col.name FOREIGN KEY $col.name
           REFERENCES $col.refTable.name #if($col.refColumn) ($col.refColumn.name) #end
##             #if($col.) ON DELETE
##        [index_name] (index_col_name,...) [reference_definition]
##        REFERENCES tbl_name [(index_col_name,...)]
##               [MATCH FULL | MATCH PARTIAL]
##               [ON DELETE reference_option]
##               [ON UPDATE reference_option]
#end ## $col.isFk()
#end
*/
