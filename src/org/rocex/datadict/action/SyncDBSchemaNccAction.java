package org.rocex.datadict.action;

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
    public void afterSyncData()
    {
        Logger.getLogger().begin("after sync data");

        String[] strSQLs = {"update md_class set name='Memo' where id='BS000010000100001030' and name='MEMO'",
            "update md_class set name='MultiLangText' where id='BS000010000100001058' and name='MULTILANGTEXT'",
            "update md_class set name='Custom' where id in('BS000010000100001056','BS000010000100001059') and name='CUSTOM'",
            "update md_module set display_name='财务' where id='gl'",
            // "delete from md_module where id='FBM'",
            "update md_component set own_module='fbm' where own_module='FBM'", "update md_component set own_module='mmpac' where own_module='NC_MM_PAC6.31'",
            "update md_component set own_module='hryf' where own_module='HRYF'", "update md_component set own_module='ufob' where own_module='UFOB'",
            "update md_component set own_module='hrp' where own_module='hr_hrp'", "update md_component set own_module='uapbd' where own_module='UAP_BD'",
            "update md_component set own_module='uap' where own_module='ncwebpub'", "update md_component set own_module='nresa' where own_module='resa'",
            "insert into md_module(id, display_name, name, parent_module_id,ddc_version) values ('pca', 'pca', 'pca', 'mm','" + strVersion + "')"};

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

    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-11 14:00:20
     ***************************************************************************/
    public void syncMetaData()
    {
        String strOtherSQL = "'" + strVersion + "' as ddc_version,'" + strTs + "' as ts";

        String strModuleSQL = "select distinct id,name,displayname,parentmoduleid,help,versiontype," + strOtherSQL + " from md_module order by name";

        String strComponentSQL =
            "select id as original_id,name,displayname,ownmodule,namespace,help,isbizmodel as biz_model,version,versiontype," + strOtherSQL +
                " from md_component";

        String strClassSQL = "select id,name,displayname,defaulttablename as table_name,fullclassname,keyattribute,componentid,classtype,isprimary,help" +
            ",accessorclassname,bizitfimpclassname,refmodelname,returntype,isauthen,versiontype," + strOtherSQL + " from md_class order by defaulttablename";

        String strPropertySQL = "select a.id original_id,a.name as name,a.displayname as displayname,attrlength,attrminvalue" +
            ",attrmaxvalue,attrsequence,customattr,datatype,datatypestyle,a.defaultvalue as defaultvalue" +
            ",a.nullable as nullable,a.precise as precise,refmodelname,classid,accesspowergroup,accessorclassname,dynamictable" +
            ",a.help,accesspower,calculation,dynamicattr,a.fixedlength,a.hided as hidden,a.notserialize,a.readonly,b.sqldatetype data_type_sql" +
            ",b.pkey key_prop,a.versiontype," + strOtherSQL +
            " from md_property a left join md_column b on a.name=b.name and b.tableid=? where classid=? order by b.pkey desc,a.attrsequence";

        String strEnumValueSQL = "select id as class_id,enumsequence as enum_sequence,name,value enum_value,versiontype," + strOtherSQL +
            " from md_enumvalue order by id,enumsequence";

        Properties dbPropSource2 = (Properties) propDBSource.clone();

        try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2))
        {
            syncMetaData(sqlExecutorSource, strModuleSQL, strComponentSQL, null, strClassSQL, strPropertySQL, null, strEnumValueSQL);
        }
    }
}
