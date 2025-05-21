package org.rocex.datadict.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.rocex.datadict.vo.ComponentVO;
import org.rocex.datadict.vo.ModuleVO;
import org.rocex.db.SQLExecutor;
import org.rocex.db.processor.BeanListProcessor;
import org.rocex.db.processor.MapProcessor;
import org.rocex.utils.Logger;

public class SyncDBMDNccAction extends SyncDBMDAction
{
    /***************************************************************************
     * @param strVersion
     * @author Rocex Wang
     * @since 2025-05-19 11:05:04
     ***************************************************************************/
    public SyncDBMDNccAction(String strVersion)
    {
        super(strVersion);
    }
    
    /***************************************************************************
     * 修复一些数据库数据问题
     * @author Rocex Wang
     * @since 2020-5-9 14:05:43
     ***************************************************************************/
    @Override
    public void afterSyncMetaData()
    {
        Logger.getLogger().begin("after sync metadata in ncc");
        
        // @formatter:off
        String[] strSQLs = {
                //"insert into md_module(id,display_name,model_type,name,parent_module_id,ddc_version,version_type) values ('pca', 'pca','md', 'pca', 'mm','${version}',0)",
                
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__ae','${version}','db',0,'数据处理','ae','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__bq','${version}','db',0,'商业分析','bq','db_table')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__other','${version}','db',0,'未归类','other','db_table')",
                
                "update md_module set display_name='财务' where id='gl'",
                "update md_module set parent_module_id='md_clazz' where model_type='md' and parent_module_id is null",
                "update md_module set parent_module_id='md__'||parent_module_id where model_type='md' and parent_module_id not in('md_clazz','db_table')",

                "update md_component set own_module='uapbd' where own_module='UAP_BD'",
                
                "update md_class set name='Memo' where id='BS000010000100001030' and name='MEMO'",
                "update md_class set name='MultiLangText' where id='BS000010000100001058' and name='MULTILANGTEXT'",
                "update md_class set name='Custom' where id in('BS000010000100001056','BS000010000100001059') and name='CUSTOM'"
        };
        // @formatter:on
        
        strSQLs = replace("${version}", strVersion, strSQLs);
        
        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        Logger.getLogger().end("after sync metadata in ncc");
    }
    
    @Override
    public void beforeSyncMetaData()
    {
        Logger.getLogger().begin("before sync metadata in ncc");
        
        super.beforeSyncMetaData();
        
        //@formatter:off
        String[] strModuleSQLs = {
            "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ae','${version}','md',0,'数据处理','ae','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__am','${version}','md',0,'资产管理','am','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__basedoc','${version}','md',0,'基础档案','basedoc','md_clazz')",
            "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__bq','${version}','md',0,'商业分析','bq','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__co','${version}','md',0,'管理会计','co','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__epm','${version}','md',0,'企业绩效','epm','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__fi','${version}','md',0,'财务管理','fi','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__hr','${version}','md',0,'人力资源','hr','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ipm','${version}','md',0,'投资管理','ipm','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__lightapp','${version}','md',0,'轻量化平台','lightapp','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__mm','${version}','md',0,'生产制造','mm','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__pm','${version}','md',0,'项目管理','pm','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ria','${version}','md',0,'应用平台','ria','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__scap','${version}','md',0,'国资服务','scap','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__scct','${version}','md',0,'供应链控制塔','scct','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__scm','${version}','md',0,'供应链','scm','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ssc','${version}','md',0,'共享服务','ssc','md_clazz')",
            // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__tm','${version}','md',0,'财资管理','tm','md_clazz')",
            "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__uap','${version}','md',0,'UAP Server','uap','md_clazz')",
            "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__uapbd','${version}','md',0,'基础数据','uapbd','md_clazz')",
            "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ufofr','${version}','md',0,'自由报表','ufofr','md_clazz')"
        };
        //@formatter:on
        
        // 把以上的ddc_version替换成正确的值
        strModuleSQLs = replace("${version}", strVersion, strModuleSQLs);
        
        try
        {
            sqlExecutorTarget.executeUpdate(strModuleSQLs);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        
        Logger.getLogger().end("before sync metadata in ncc");
    }
    
    @Override
    public void syncDBMetaData()
    {
        Logger.getLogger().begin("sync db metadata in ncc");
        
        if (!isSyncDB)
        {
            Logger.getLogger().end("sync db metadata in ncc", "isSyncDB=%s, skip...", isSyncDB);
            return;
        }
        
        try (SQLExecutor sqlExecutorSource = new SQLExecutor(propDBSource); Connection connSource = sqlExecutorSource.getConnection())
        {
            initTableNameFilters(sqlExecutorSource);
            
            String strTableInModuleSQL = "select lower(a.defaulttablename),c.id from md_class a left join md_component b on a.componentid=b.id"
                    + " left join md_module c on b.ownmodule=c.id where a.classtype=201 order by defaulttablename";
            
            String strModuleDBSQL = "select 'db__'||id as id,'" + strVersion
                    + "' as ddc_version,0 as version_type,displayname as display_name,'db' as model_type,name"
                    + ",'db_table' as parent_module_id from md_module where parentmoduleid is null order by id";
            
            String strComponentDBSQL = "select 'db__'||id as id,'" + strVersion
                    + "' as ddc_version,0 as version_type,displayname as display_name,'db' as model_type,name"
                    + ",'db__'||parentmoduleid as own_module from md_module where parentmoduleid is not null order by id";
            
            mapTableInModule = (Map<String, String>) sqlExecutorSource.executeQuery(strTableInModuleSQL, new MapProcessor<>());
            
            List<ModuleVO> listModule = (List<ModuleVO>) sqlExecutorSource.executeQuery(strModuleDBSQL, new BeanListProcessor<>(ModuleVO.class));
            sqlExecutorTarget.insertVO(listModule.toArray(new ModuleVO[0]));
            
            List<ComponentVO> listComponent = (List<ComponentVO>) sqlExecutorSource.executeQuery(strComponentDBSQL, new BeanListProcessor<>(ComponentVO.class));
            sqlExecutorTarget.insertVO(listComponent.toArray(new ComponentVO[0]));
            
            syncDBTable(sqlExecutorSource, connSource.getCatalog(), connSource.getSchema(), null);
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        Logger.getLogger().end("sync db metadata in ncc");
    }
    
    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-11 14:00:20
     ***************************************************************************/
    @Override
    public void syncMDMetaData()
    {
        Logger.getLogger().begin("sync md metadata in ncc");
        
        if (!isSyncMD)
        {
            Logger.getLogger().end("sync md metadata in ncc", "isSyncMD=%s, skip...", isSyncMD);
            return;
        }
        
        String strOtherField = "'" + strVersion + "' as ddc_version,'" + strTs + "' as ts";
        
        String strModuleSQL = "select distinct id,name,displayname,parentmoduleid,help,versiontype," + strOtherField + " from md_module order by name";
        
        String strComponentSQL = "select distinct id,name,displayname,ownmodule,namespace,help,isbizmodel as biz_model,version,versiontype," + strOtherField
                + " from md_component";
        
        String strClassSQL = "select id,name,displayname,defaulttablename as table_name,fullclassname,keyattribute,componentid,classtype"
                + ",isprimary as primary_class,help,accessorclassname,bizitfimpclassname,refmodelname,returntype,isauthen,versiontype," + strOtherField
                + " from md_class"
                // + " where id like '%aaa%'"
                + " order by defaulttablename";
                
        String strPropertySQL = "select a.id id,a.name as name,a.displayname as displayname,attrlength,attrminvalue"
                + ",attrmaxvalue,attrsequence,customattr,datatype,datatypestyle,a.defaultvalue as defaultvalue"
                + ",a.nullable as nullable,a.precise as precise,a.refmodelname,classid,accesspowergroup,a.accessorclassname,dynamictable"
                + ",a.help,accesspower,calculation,dynamicattr,a.fixedlength,a.hided as hidden,a.notserialize,a.readonly,c.sqldatetype data_type_sql"
                + ",c.pkey key_prop,a.versiontype," + strOtherField
                + " from md_property a left join md_class b on a.classid=b.id left join md_column c on a.name=c.name and c.tableid=b.defaulttablename"
                // + " where a.classid like '%aaa%'"
                + " order by a.classid,c.pkey desc,a.attrsequence";
                
        String strEnumValueSQL = "select id as class_id,enumsequence as enum_sequence,name,value enum_value,versiontype," + strOtherField
                + " from md_enumvalue order by id,enumsequence";
        
        String strDomainSQL = "select 'md__'||id as id,0 as version_type,displayname as display_name,'md' as model_type,name"
                + ",'md_clazz' as parent_module_id," + strOtherField + " from md_module where parentmoduleid is null order by name";
        
        try (SQLExecutor sqlExecutorSource = new SQLExecutor(propDBSource))
        {
            try
            {
                List<ModuleVO> listMDModule = (List<ModuleVO>) sqlExecutorSource.executeQuery(strDomainSQL, new BeanListProcessor<>(ModuleVO.class));
                sqlExecutorTarget.insertVO(listMDModule.toArray(new ModuleVO[0]));
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
            
            syncMetaData(sqlExecutorSource, strModuleSQL, strComponentSQL, null, strClassSQL, strPropertySQL, null, strEnumValueSQL);
        }
        
        Logger.getLogger().end("sync md metadata in ncc");
    }
}
