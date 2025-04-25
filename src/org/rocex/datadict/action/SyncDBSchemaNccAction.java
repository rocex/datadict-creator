package org.rocex.datadict.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

import org.rocex.db.SQLExecutor;
import org.rocex.utils.Logger;

public class SyncDBSchemaNccAction extends SyncDBSchemaAction
{
    public SyncDBSchemaNccAction(String strVersion)
    {
        super(strVersion);
    }
    
    /***************************************************************************
     * 修复一些数据库数据问题
     * @author Rocex Wang
     * @since 2020-5-9 14:05:43
     ***************************************************************************/
    @Override
    public void afterSyncData()
    {
        Logger.getLogger().begin("after sync data");
        
        String[] strSQLs = { "update md_class set name='Memo' where id='BS000010000100001030' and name='MEMO'",
                "update md_class set name='MultiLangText' where id='BS000010000100001058' and name='MULTILANGTEXT'",
                "update md_class set name='Custom' where id in('BS000010000100001056','BS000010000100001059') and name='CUSTOM'",
                "update md_module set display_name='财务' where id='gl'",
                // "delete from md_module where id='FBM'",
                "update md_component set own_module='fbm' where own_module='FBM'",
                "update md_component set own_module='mmpac' where own_module='NC_MM_PAC6.31'",
                "update md_component set own_module='hryf' where own_module='HRYF'", "update md_component set own_module='ufob' where own_module='UFOB'",
                "update md_component set own_module='hrp' where own_module='hr_hrp'", "update md_component set own_module='uapbd' where own_module='UAP_BD'",
                "update md_component set own_module='uap' where own_module='ncwebpub'", "update md_component set own_module='nresa' where own_module='resa'",
                "insert into md_module(id,display_name,model_type,name,parent_module_id,ddc_version,version_type) values ('pca', 'pca','md', 'pca', 'mm','"
                        + strVersion + "',0)",
                "update md_module set parent_module_id='md_clazz' where model_type='md' and parent_module_id is null",
                
                "update md_module set parent_module_id='md__'||parent_module_id where model_type='md' and parent_module_id not in('md_clazz','db_table')" };
        
        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        Logger.getLogger().end("after sync data");
    }
    
    @Override
    public void beforeSyncData()
    {
        String[] strModuleSQLs = {
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ae','2505','md',0,'数据处理','ae','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__am','2505','md',0,'资产管理','am','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__basedoc','2505','md',0,'基础档案','basedoc','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__bq','2505','md',0,'商业分析','bq','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__co','2505','md',0,'管理会计','co','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__epm','2505','md',0,'企业绩效','epm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__fi','2505','md',0,'财务管理','fi','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__hr','2505','md',0,'人力资源','hr','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ipm','2505','md',0,'投资管理','ipm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__lightapp','2505','md',0,'轻量化平台','lightapp','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__mm','2505','md',0,'生产制造','mm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__pm','2505','md',0,'项目管理','pm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ria','2505','md',0,'应用平台','ria','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__scap','2505','md',0,'国资服务','scap','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__scct','2505','md',0,'供应链控制塔','scct','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__scm','2505','md',0,'供应链','scm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ssc','2505','md',0,'共享服务','ssc','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__tm','2505','md',0,'财资管理','tm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__uap','2505','md',0,'UAP Server','uap','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__uapbd','2505','md',0,'基础数据','uapbd','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ufofr','2505','md',0,'自由报表','ufofr','md_clazz')",
                
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__ae','2505','db',0,'数据处理','ae','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__am','2505','db',0,'资产管理','am','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__basedoc','2505','db',0,'基础档案','basedoc','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__bq','2505','db',0,'商业分析','bq','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__co','2505','db',0,'管理会计','co','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__epm','2505','db',0,'企业绩效','epm','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__fi','2505','db',0,'财务管理','fi','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__hr','2505','db',0,'人力资源','hr','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__ipm','2505','db',0,'投资管理','ipm','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__lightapp','2505','db',0,'轻量化平台','lightapp','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__mm','2505','db',0,'生产制造','mm','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__pm','2505','db',0,'项目管理','pm','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__ria','2505','db',0,'应用平台','ria','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__scap','2505','db',0,'国资服务','scap','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__scct','2505','db',0,'供应链控制塔','scct','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__scm','2505','db',0,'供应链','scm','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__ssc','2505','db',0,'共享服务','ssc','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__tm','2505','db',0,'财资管理','tm','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__uap','2505','db',0,'UAP Server','uap','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__uapbd','2505','db',0,'基础数据','uapbd','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__ufofr','2505','db',0,'自由报表','ufofr','db_table')" };
        
        // 把以上的ddc_version替换成正确的值
        strModuleSQLs = Arrays.stream(strModuleSQLs).map(strSQL -> strSQL.replace("'bip__version'", "'" + strVersion + "'")).toArray(String[]::new);
        
        try
        {
            sqlExecutorTarget.executeUpdate(strModuleSQLs);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public boolean isNeedSyncData()
    {
        String strSQLs[] = { "drop table md_module", "drop table md_component", "drop table md_class", "drop table md_property", "drop table md_enumvalue",
                "drop table md_index", "drop table ddc_dict_json" };
        
        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        return super.isNeedSyncData();
    }
    
    @Override
    protected void syncDBMeta()
    {
        Logger.getLogger().begin("sync database meta");
        
        try (SQLExecutor sqlExecutorSource = new SQLExecutor(propDBSource); Connection connSource = sqlExecutorSource.getConnection())
        {
            initTableFiltersWithTempTableName(sqlExecutorSource);
            
            syncDBTable(sqlExecutorSource, connSource.getCatalog(), connSource.getSchema(), "db_table");
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        Logger.getLogger().end("sync database meta");
    }
    
    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-11 14:00:20
     ***************************************************************************/
    @Override
    public void syncMetaData()
    {
        String strOtherSQL = "'" + strVersion + "' as ddc_version,'" + strTs + "' as ts";
        
        String strModuleSQL = "select distinct id,name,displayname,parentmoduleid,help,versiontype," + strOtherSQL + " from md_module order by name";
        
        String strComponentSQL = "select distinct id,name,displayname,ownmodule,namespace,help,isbizmodel as biz_model,version,versiontype," + strOtherSQL
                + " from md_component";
        
        String strClassSQL = "select id,name,displayname,defaulttablename as table_name,fullclassname,keyattribute,componentid,classtype,isprimary,help"
                + ",accessorclassname,bizitfimpclassname,refmodelname,returntype,isauthen,versiontype," + strOtherSQL
                + " from md_class order by defaulttablename";
        
        String strPropertySQL = "select a.id id,a.name as name,a.displayname as displayname,attrlength,attrminvalue"
                + ",attrmaxvalue,attrsequence,customattr,datatype,datatypestyle,a.defaultvalue as defaultvalue"
                + ",a.nullable as nullable,a.precise as precise,a.refmodelname,classid,accesspowergroup,a.accessorclassname,dynamictable"
                + ",a.help,accesspower,calculation,dynamicattr,a.fixedlength,a.hided as hidden,a.notserialize,a.readonly,c.sqldatetype data_type_sql"
                + ",c.pkey key_prop,a.versiontype," + strOtherSQL
                + " from md_property a left join md_class b on a.classid=b.id left join md_column c on a.name=c.name and c.tableid=b.defaulttablename"
                + " order by a.classid,c.pkey desc,a.attrsequence";
        
        String strEnumValueSQL = "select id as class_id,enumsequence as enum_sequence,name,value enum_value,versiontype," + strOtherSQL
                + " from md_enumvalue order by id,enumsequence";
        
        Properties dbPropSource2 = (Properties) propDBSource.clone();
        
        try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2))
        {
            syncMetaData(sqlExecutorSource, strModuleSQL, strComponentSQL, null, strClassSQL, strPropertySQL, null, strEnumValueSQL);
        }
    }
}
