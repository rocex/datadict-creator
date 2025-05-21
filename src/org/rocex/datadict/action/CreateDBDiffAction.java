package org.rocex.datadict.action;

import java.nio.file.Path;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.rocex.datadict.vo.Context;
import org.rocex.datadict.vo.DiffColumn;
import org.rocex.datadict.vo.DiffTable;
import org.rocex.datadict.vo.IndexVO;
import org.rocex.db.SQLExecutor;
import org.rocex.db.param.SQLParameter;
import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;
import org.rocex.vo.IAction;
import org.rocex.vo.SuperVO;

public class CreateDBDiffAction implements IAction
{
    private final SQLExecutor sqlExecutor;
    
    private final String strOutputDiffDir;
    
    private final String strSeperator = "\t";
    
    private final String strVersion1;
    private final String strVersion2;
    
    public CreateDBDiffAction()
    {
        strVersion1 = Context.getInstance().getSetting("diff.version1");
        strVersion2 = Context.getInstance().getSetting("diff.version2");
        
        String strOutputRootDir = Path.of(Context.getInstance().getSetting("WorkDir"), "datadict-" + strVersion1).toString();
        strOutputDiffDir = Path.of(strOutputRootDir, "diff").toString();
        
        Properties dbProp = new Properties();
        
        String strDiffUrl = Context.getInstance().getSetting("diff.jdbc.url");
        String strDiffUser = Context.getInstance().getSetting("diff.jdbc.user");
        String strDiffDriver = Context.getInstance().getSetting("diff.jdbc.driver");
        String strDiffPassword = Context.getInstance().getSetting("diff.jdbc.password");
        
        dbProp.setProperty("jdbc.url", strDiffUrl);
        dbProp.setProperty("jdbc.user", strDiffUser);
        dbProp.setProperty("jdbc.driver", strDiffDriver);
        dbProp.setProperty("jdbc.password", strDiffPassword);
        
        sqlExecutor = new SQLExecutor(dbProp);
    }
    
    /**
     * 给 md_property 表增加3个字段，把一些信息合并起来方便后续操作
     **/
    private void adjustData()
    {
        // @formatter:off
        String[] strSQLs = {
                "alter table md_property add column schema2 varchar(128)",
                "update md_property set schema2=(select b.name from md_component b,md_class c where b.id=c.component_id and md_property.class_id=c.id)",
                "create index i_md_property_schema on md_property(schema2)",
                
                "alter table md_property add column merge1 varchar(256)",
                "update md_property set merge1=(select schema2||'.'||b.table_name||'.'||md_property.name from md_class b where md_property.class_id=b.id)",
                "create index i_md_property_merge1 on md_property(merge1)",
                
                "alter table md_property add column merge2 varchar(256)",
                "update md_property set merge2=(coalesce(data_type_sql,'')||(case when nullable=0 then ' not null' when nullable=1 then '' end)||(case when default_value is not null and data_type in(-16,-15,-9,-1,1,12) then ' default '||''''||default_value||'''' when default_value is not null then ' default '||default_value else '' end))",
                "create index i_md_property_merge2 on md_property(merge2)",
                
                "create table md_property2 as select * from md_property where ddc_version='" + strVersion1 + "'",
                "create table md_property3 as select * from md_property where ddc_version='" + strVersion2 + "'"};
        // @formatter:on
        
        for (String strSQL : strSQLs)
        {
            try
            {
                sqlExecutor.executeUpdate(strSQL);
            }
            catch (Exception ex)
            {
                Logger.getLogger().error("execute sql error -> " + strSQL);
            }
        }
    }
    
    private <T extends DiffTable> List<T> createDiff(Class<T> clazz, String strDiffSQL, SQLParameter sqlParam, DiffToString<T> diffToString, String strFileName)
    {
        List<T> listDiff = queryDiff(clazz, strDiffSQL, sqlParam);
        
        if (listDiff == null || listDiff.isEmpty())
        {
            return listDiff;
        }
        
        List<String> listContent = new ArrayList<>();
        listContent.add(diffToString.getTitle());
        
        for (T diff : listDiff)
        {
            listContent.add(diffToString.toString(diff));
        }
        
        writeFile(listContent, strFileName);
        
        return listDiff;
    }
    
    private void createDropTableSQLFile(List<DiffTable> listTable, String strFileName)
    {
        if (listTable == null || listTable.isEmpty())
        {
            return;
        }
        
        List<String> listContent = new ArrayList<>();
        
        for (DiffTable diffTable : listTable)
        {
            listContent.add("drop table %s.%s".formatted(diffTable.getSchema2(), diffTable.getTableName()));
        }
        
        writeFile(listContent, strFileName);
    }
    
    private void createNewColumnSQLFile(List<DiffColumn> listDiffColumn, String strFileName)
    {
        if (listDiffColumn == null || listDiffColumn.isEmpty())
        {
            return;
        }
        
        List<String> listContent = new ArrayList<>();
        
        for (DiffColumn diff : listDiffColumn)
        {
            String strSQL = "alter table %s.%s add column %s %s comment '%s';".formatted(diff.getSchema2(), diff.getTableName(), diff.getColumnName(),
                    diff.getMerge1(), Objects.toString(diff.getColumnShowName(), ""));
            listContent.add(strSQL);
        }
        
        writeFile(listContent, strFileName);
    }
    
    private void createNewIndexSQLFile(String strNewIndexSQL, SQLParameter sqlParam, String strFileName)
    {
        List<IndexVO> listIndexVO = queryDiff(IndexVO.class, strNewIndexSQL, sqlParam);
        
        if (listIndexVO == null || listIndexVO.isEmpty())
        {
            return;
        }
        
        List<String> listContent = new ArrayList<>();
        
        for (IndexVO indexVO : listIndexVO)
        {
            listContent.add(indexVO.getIndexSql());
        }
        
        writeFile(listContent, strFileName);
    }
    
    private void createNewTableSQLFile(String strNewTableColumn, SQLParameter sqlParam, String strFileName)
    {
        List<DiffColumn> listDiff = queryDiff(DiffColumn.class, strNewTableColumn, sqlParam);
        
        if (listDiff == null || listDiff.isEmpty())
        {
            return;
        }
        
        List<String> listContent = new ArrayList<>();
        
        Map<String, List<DiffColumn>> mapTable = listDiff.stream().filter(diffColumn -> diffColumn.getTableName() != null && diffColumn.getColumnName() != null)
                .collect(Collectors.groupingBy(DiffColumn::getTableName));
        
        for (Map.Entry<String, List<DiffColumn>> entry : mapTable.entrySet())
        {
            String strTableName = entry.getKey();
            
            String strColumns = entry.getValue().stream()
                    .map(column -> "\t%s %s comment '%s'".formatted(column.getColumnName().toLowerCase(), column.getMerge2(), column.getColumnShowName()))
                    .collect(Collectors.joining(",\n"));
            
            // 临时在merge1里面放着主键列表
            String strPkColumns = entry.getValue().get(0).getMerge1().replace(";", ",");
            String strSchema = entry.getValue().get(0).getSchema2();
            String strTableShowName = entry.getValue().get(0).getTableShowName();
            
            String strSQL = "create table %s.%s\n(\n%s,\n\tprimary key (%s)\n) comment='%s';\n".formatted(strSchema, strTableName, strColumns, strPkColumns,
                    strTableShowName);
            
            listContent.add(strSQL);
        }
        
        writeFile(listContent, strFileName);
    }
    
    @Override
    public void doAction(EventObject evt)
    {
        adjustData();
        
        // 新增表，schema+tablename
        String strTableSQL = """
                select replace(a.component_id,'db__','') as schema2,a.display_name as table_show_name,a.table_name from md_class a where a.ddc_version=?
                 and a.component_id||a.table_name not in(select b.component_id||b.table_name from md_class b where b.ddc_version=?)
                 order by a.component_id,a.table_name
                """;
        
        // 新增表的字段，不包含已存在表的新增字段
        String strNewTableColumn = """
                select a.schema2,b.display_name as table_show_name,a.display_name as column_show_name,b.table_name,a.name as column_name,a.data_type_sql,a.nullable,a.default_value,a.merge2,b.key_attribute merge1
                 from md_property a,md_class b where a.ddc_version=? and a.ddc_version=b.ddc_version and a.class_id=b.id
                 and a.merge1 not in(select c.merge1 from md_property c,md_class d where c.ddc_version=? and c.ddc_version=d.ddc_version and c.class_id=d.id)
                 and b.id in(select a.id from md_class a where a.ddc_version=? and a.component_id||a.table_name not in(select b.component_id||b.table_name from md_class b where b.ddc_version=?))
                 order by b.component_id,b.table_name,a.name
                """;
        
        // 新增字段，schema+tablename+columnname，并且不在新增表里的字段
        String strColumnSQL = """
                select a.schema2,b.display_name as table_show_name,a.display_name as column_show_name,b.table_name,a.name as column_name
                ,a.data_type_sql,a.nullable,a.default_value,a.merge2 merge1
                 from md_property a,md_class b where a.ddc_version=? and a.ddc_version=b.ddc_version and a.class_id=b.id
                 and a.merge1 not in(select c.merge1 from md_property c,md_class d where c.ddc_version=? and c.ddc_version=d.ddc_version and c.class_id=d.id)
                 and b.id not in(select a.id from md_class a where a.ddc_version=? and a.component_id||a.table_name not in(select b.component_id||b.table_name from md_class b where b.ddc_version=?))
                 order by b.component_id,b.table_name,a.name;
                """;
        
        // 变更字段，schema+tablename+columnname相等，但是data_type_sql||nullable||default_value不等
        String strChangedColumnSQL = """
                select a.schema2,b.display_name as table_show_name,b.table_name,a.display_name as column_show_name,a.name as column_name,a.merge2 merge1,c.merge2
                 from md_property2 a,md_class b inner join md_property3 c on a.merge1=c.merge1 and c.ddc_version=? and a.merge2!=c.merge2
                 where a.ddc_version=b.ddc_version and a.class_id=b.id and a.ddc_version=? order by a.schema2,b.table_name,a.name
                """;
        
        // 新增索引，schema+tablename+indexname
        String strNewIndexSQL = """
                select * from md_index where class_id in(select a.id from md_class a where a.ddc_version=?
                 and a.component_id||a.table_name not in(select b.component_id||b.table_name from md_class b where b.ddc_version=?)) order by schema2,table_name
                """;
        
        // 01 新增表
        // 03 删除表
        // 05 新增字段
        // 07 删除字段
        // 09 变更字段
        // 11 新增索引
        // 13 删除索引
        // 15 变更索引
        String strFileName = strVersion1 + " 比 " + strVersion2 + " ";
        
        createDiff(DiffTable.class, strTableSQL, new SQLParameter(strVersion1, strVersion2), new DiffToStringTable(), "01 " + strFileName + "新增表.txt");
        createNewTableSQLFile(strNewTableColumn, new SQLParameter(strVersion1, strVersion2, strVersion1, strVersion2), "02 " + strFileName + "新增表.sql");
        
        List<DiffTable> listDiffTable1 = createDiff(DiffTable.class, strTableSQL, new SQLParameter(strVersion2, strVersion1), new DiffToStringTable(),
                "03 " + strFileName + "删除表.txt");
        createDropTableSQLFile(listDiffTable1, "04 " + strFileName + "删除表.sql");
        
        List<DiffColumn> listDiffColumn1 = createDiff(DiffColumn.class, strColumnSQL, new SQLParameter(strVersion1, strVersion2, strVersion1, strVersion2),
                new DiffToStringColumn(), "05 " + strFileName + "新增字段.txt");
        createNewColumnSQLFile(listDiffColumn1, "06 " + strFileName + "新增字段.sql");
        
        createDiff(DiffColumn.class, strColumnSQL, new SQLParameter(strVersion2, strVersion1, strVersion2, strVersion1), new DiffToStringColumn4(),
                "07 " + strFileName + "删除字段.txt");
        
        createDiff(DiffColumn.class, strChangedColumnSQL, new SQLParameter(strVersion2, strVersion1), new DiffToStringColumn5(),
                "09 " + strFileName + "变更字段.txt");
        
        createNewIndexSQLFile(strNewIndexSQL, new SQLParameter(strVersion1, strVersion2), "12 " + strFileName + "新增索引.sql");
        
        // todo recoverData();
    }
    
    private <T extends SuperVO> List<T> queryDiff(Class<T> clazz, String strDiffSQL, SQLParameter sqlParam)
    {
        List<T> listDiff = null;
        
        try
        {
            listDiff = (List<T>) sqlExecutor.executeQuery(clazz, strDiffSQL, sqlParam);
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error("query diff error", ex);
        }
        
        return listDiff;
    }
    
    /**
     * 恢复数据库结构，删除额外增加的表及字段
     **/
    void recoverData()
    {
        // @formatter:off
        String[] strSQLs = {
                "drop table if exists md_property2"
                , "drop table if exists md_property3"
                , "drop index i_md_property_merge1"
                , "drop index i_md_property_merge2"
                , "drop index i_md_property_schema"
                , "alter table md_property drop column schema2"
                , "alter table md_property drop column merge1"
                , "alter table md_property drop column merge2"
                , "vacuum"};
        // @formatter:on
        
        for (String strSQL : strSQLs)
        {
            try
            {
                sqlExecutor.executeUpdate(strSQL);
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error("execute sql error -> " + strSQL, ex);
            }
        }
    }
    
    private void writeFile(List<String> listContent, String strFileName)
    {
        Path pathFile = Path.of(strOutputDiffDir, strFileName);
        FileHelper.writeFile(pathFile, listContent);
    }
    
    interface DiffToString<T extends SuperVO>
    {
        String getTitle();
        
        String toString(T t);
    }
    
    class DiffToStringColumn implements DiffToString<DiffColumn>
    {
        @Override
        public String getTitle()
        {
            return MessageFormat.format("数据库schema{0}表显示名称{0}表名{0}字段显示名称{0}字段名{0}字段类型定义({1})", strSeperator, strVersion1);
        }
        
        @Override
        public String toString(DiffColumn diffColumn)
        {
            return MessageFormat.format("{1}{0}{2}{0}{3}{0}{4}{0}{5}{0}{6}", strSeperator, diffColumn.getSchema2(), diffColumn.getTableShowName(),
                    diffColumn.getTableName(), diffColumn.getColumnShowName(), diffColumn.getColumnName(), diffColumn.getMerge1());
        }
    }
    
    class DiffToStringColumn4 extends DiffToStringColumn
    {
        @Override
        public String getTitle()
        {
            return super.getTitle().replace(strVersion1, strVersion2);
        }
    }
    
    class DiffToStringColumn5 extends DiffToStringColumn
    {
        @Override
        public String getTitle()
        {
            return super.getTitle() + strSeperator + "字段类型定义(" + strVersion2 + ")";
        }
        
        @Override
        public String toString(DiffColumn diffColumn)
        {
            return super.toString(diffColumn) + strSeperator + diffColumn.getMerge2();
        }
    }
    
    class DiffToStringTable implements DiffToString<DiffTable>
    {
        @Override
        public String getTitle()
        {
            return MessageFormat.format("数据库schema{0}表显示名称{0}表名", strSeperator);
        }
        
        @Override
        public String toString(DiffTable diffTable)
        {
            return MessageFormat.format("{1}{0}{2}{0}{3}", strSeperator, diffTable.getSchema2(), diffTable.getTableShowName(), diffTable.getTableName());
        }
    }
}
