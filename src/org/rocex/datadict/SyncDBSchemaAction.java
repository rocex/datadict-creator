package org.rocex.datadict;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.ComponentVO;
import org.rocex.datadict.vo.EnumVO;
import org.rocex.datadict.vo.MetaVO;
import org.rocex.datadict.vo.ModuleVO;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.db.param.SQLParameter;
import org.rocex.db.processor.BeanListProcessor;
import org.rocex.db.processor.SQLExecutor;
import org.rocex.utils.Logger;
import org.rocex.utils.StringHelper;
import org.rocex.utils.TimerLogger;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @since 2021-10-28 02:17:34
 ***************************************************************************/
public class SyncDBSchemaAction implements IAction
{
    protected Map<String, String> mapColumn = new HashMap<>();

    protected Map<String, String> mapTableNamePrimaryKeys = new HashMap<>();          // 表名和表主键列表的对应关系，多个主键用；分隔，表名用全小写

    protected SQLExecutor sqlExecutorSource = null;
    protected SQLExecutor sqlExecutorTarget = null;
    
    protected String strDBSchema;           // 数据库schema
    
    // 排除一些表，最前面的英文逗号必须保留
    protected String strTableFilters = ",aqua_explain_,hr_temptable,ic_temp_,iufo_measpub_,iufo_measure_data_,sm_securitylog_,tb_fd_sht,tb_tmp_tcheck,tb_tt_,temp000"
            + ",temppkts,temptable_oa,temp_,temq_,tmpbd_,tmpub_calog_temp,tmp_,tm_mqsend_success_,uidbcache_temp_,uidbcache_temp_,wa_temp_,zdp_";
    
    protected String strVersion;            // 数据字典版本
    
    /***************************************************************************
     * @param strVersion
     * @author Rocex Wang
     * @since 2021-10-28 02:18:20
     ***************************************************************************/
    public SyncDBSchemaAction(String strVersion)
    {
        super();
        
        this.strVersion = strVersion;
        
        strDBSchema = DataDictCreator.settings.getProperty(strVersion + ".jdbc.user").toUpperCase();

        //
        Properties dbPropSource = new Properties();
        
        dbPropSource.setProperty("jdbc.url", DataDictCreator.settings.getProperty(strVersion + ".jdbc.url"));
        dbPropSource.setProperty("jdbc.user", DataDictCreator.settings.getProperty(strVersion + ".jdbc.user"));
        dbPropSource.setProperty("jdbc.driver", DataDictCreator.settings.getProperty(strVersion + ".jdbc.driver"));
        dbPropSource.setProperty("jdbc.password", DataDictCreator.settings.getProperty(strVersion + ".jdbc.password"));
        
        sqlExecutorSource = new SQLExecutor(dbPropSource);

        //
        Properties dbPropTarget = new Properties();
        
        // dbPropTarget.setProperty("jdbc.url", "jdbc:sqlite:" + strOutputRootDir + File.separator +
        // "datadict.sqlite");
        dbPropTarget.setProperty("jdbc.url", "jdbc:sqlite:C:/datadict/datadict.sqlite");
        dbPropTarget.setProperty("jdbc.driver", "org.sqlite.JDBC");

        sqlExecutorTarget = new SQLExecutor(dbPropTarget);

        //
        mapColumn.put("COLUMN_NAME", "Name");
        mapColumn.put("COLUMN_SIZE", "AttrLength");
        mapColumn.put("COLUMN_DEF", "DefaultValue");
        mapColumn.put("TABLE_NAME", "ClassId");
        mapColumn.put("TYPE_NAME", "DataTypeSql");
        mapColumn.put("DECIMAL_DIGITS", "Precise");
        mapColumn.put("NULLABLE", "Nullable");
    }

    /***************************************************************************
     * 修复一些数据库数据问题
     * @author Rocex Wang
     * @version 2020-5-9 14:05:43
     ***************************************************************************/
    protected void adjustData()
    {
        TimerLogger.getLogger().begin("fix data");

        String[] strSQLs = { "update md_class set name='Memo' where id='BS000010000100001030' and name='MEMO'",
                "update md_class set name='MultiLangText' where id='BS000010000100001058' and name='MULTILANGTEXT'",
                "update md_class set name='Custom' where id in('BS000010000100001056','BS000010000100001059') and name='CUSTOM'",
                "update md_module set display_name='财务' where id='gl'" };

        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        TimerLogger.getLogger().end("fix data");
    }

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datadict.IAction#doAction(EventObject)
     * @author Rocex Wang
     * @since 2021-10-28 14:17:51
     ****************************************************************************/
    @Override
    public void doAction(EventObject evt)
    {
        if (!isNeedSyncData())
        {
            return;
        }

        sqlExecutorTarget.initDBSchema(ModuleVO.class, ModuleVO.class, ComponentVO.class, ClassVO.class, PropertyVO.class, EnumVO.class);
        
        syncMetaData();
        syncDatabaseMeta();

        adjustData();
    }
    
    /***************************************************************************
     * 返回数据库类型定义
     * @param propertyVO
     * @return String
     * @author Rocex Wang
     * @version 2020-4-28 10:14:34
     ***************************************************************************/
    protected String getDataTypeSql(PropertyVO propertyVO)
    {
        String strDbType = propertyVO.getDataTypeSql().toLowerCase();

        if (strDbType.contains("char") || strDbType.contains("text"))
        {
            strDbType = strDbType + "(" + propertyVO.getAttrLength() + ")";
        }
        else if (strDbType.contains("decimal") || strDbType.contains("number"))
        {
            strDbType = strDbType + "(" + propertyVO.getAttrLength() + ", " + propertyVO.getPrecise() + ")";
        }

        propertyVO.setDataTypeSql(strDbType);

        return strDbType;
    }

    /***************************************************************************
     * 根据数据库特性一次性读取所有表的主键，如果不能确定数据库，还是按照jdbc的api每次只取一个表的主键
     * @param dbMetaData
     * @param strTableName
     * @param mapColumn
     * @return String
     * @throws Exception
     * @author Rocex Wang
     * @version 2020-5-22 14:03:59
     ***************************************************************************/
    protected String getPrimaryKeys(String strTableName, Map<String, String> mapColumn) throws Exception
    {
        String strPks = "";

        if (sqlExecutorSource.getConnection().getMetaData().getDatabaseProductName().toLowerCase().contains("oracle"))
        {
            if (mapTableNamePrimaryKeys.isEmpty())
            {
                TimerLogger.getLogger().begin("oracle query all primary keys");

                String strSQL = "select column_name id,table_name classid from user_cons_columns where constraint_name in(select constraint_name from user_constraints where constraint_type='P')";

                sqlExecutorSource.executeQuery(strSQL, new BeanListProcessor<>(PropertyVO.class, null, propertyVO ->
                {
                    String strClassId = propertyVO.getClassId().toLowerCase();
                    String strField = propertyVO.getId().toLowerCase();

                    String strPk = mapTableNamePrimaryKeys.get(strClassId);

                    strPk = strPk == null ? strField : strPk + ";" + strField;

                    mapTableNamePrimaryKeys.put(strClassId, strPk);

                    return false;
                }));

                TimerLogger.getLogger().end("oracle query all primary keys");
            }

            strPks = mapTableNamePrimaryKeys.get(strTableName.toLowerCase());

            return strPks == null ? "" : strPks;
        }
        else
        {
            // 找到表的主键
            ResultSet rsPkColumns = sqlExecutorSource.getConnection().getMetaData().getPrimaryKeys(null, strDBSchema, strTableName);

            List<PropertyVO> listPkPropertyVO = (List<PropertyVO>) new BeanListProcessor<>(PropertyVO.class, mapColumn, "id").doAction(rsPkColumns);

            for (PropertyVO propertyVO : listPkPropertyVO)
            {
                strPks += propertyVO.getId().toLowerCase() + ";";
            }
        }

        return strPks;
    }

    /***************************************************************************
     * @return boolean
     * @author Rocex Wang
     * @since 2021-11-11 16:26:03
     ***************************************************************************/
    public boolean isNeedSyncData()
    {
        return !sqlExecutorTarget.isTableExist("md_module");
    }
    
    /***************************************************************************
     * @param metaVOClass
     * @param strSQL
     * @param param
     * @param pagingAction
     * @return List<? extends MetaVO>
     * @author Rocex Wang
     * @version 2020-5-9 11:20:25
     ***************************************************************************/
    protected List<? extends MetaVO> queryMetaVO(Class<? extends MetaVO> metaVOClass, String strSQL, SQLParameter param, IAction pagingAction)
    {
        TimerLogger.getLogger().begin("query " + metaVOClass.getSimpleName());

        List<? extends MetaVO> listMetaVO = null;

        try
        {
            BeanListProcessor<? extends MetaVO> processor = new BeanListProcessor<>(metaVOClass);
            processor.setPagingAction(pagingAction);

            listMetaVO = (List<? extends MetaVO>) sqlExecutorSource.executeQuery(strSQL, param, processor);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        TimerLogger.getLogger().end("query " + metaVOClass.getSimpleName());

        return listMetaVO;
    }

    /***************************************************************************
     * @throws SQLException
     * @author Rocex Wang
     * @since 2021-11-15 13:59:54
     ***************************************************************************/
    protected void syncAllDBField() throws SQLException
    {
        TimerLogger.getLogger().begin("sync all fields");

        Map<String, Integer> mapSequence = new HashMap<>();
        
        IAction pagingFieldAction = evt ->
        {
            List<PropertyVO> listVO = (List<PropertyVO>) evt.getSource();

            for (PropertyVO propertyVO : listVO)
            {
                propertyVO.setClassId(propertyVO.getClassId().toLowerCase());

                propertyVO.setDataTypeStyle(999);
                propertyVO.setId(StringHelper.getId());
                propertyVO.setDataTypeSql(getDataTypeSql(propertyVO));
                propertyVO.setName(propertyVO.getName().toLowerCase());
                propertyVO.setDisplayName(propertyVO.getName().toLowerCase());
                propertyVO.setNullable("1".equals(propertyVO.getNullable()) ? "Y" : "N");

                Integer iSequence = mapSequence.get(propertyVO.getClassId());

                if (iSequence == null)
                {
                    iSequence = 0;
                }

                propertyVO.setAttrSequence(++iSequence);
                
                mapSequence.put(propertyVO.getClassId(), iSequence);
            }

            try
            {
                sqlExecutorTarget.insertVO(listVO.toArray(new MetaVO[0]));
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        };

        DatabaseMetaData dbMetaData = sqlExecutorSource.getConnection().getMetaData();

        // 一次性查出所有表的字段
        ResultSet rsColumns = dbMetaData.getColumns(null, strDBSchema, null, null);
        
        BeanListProcessor<? extends MetaVO> processor = new BeanListProcessor<>(PropertyVO.class, mapColumn);
        processor.setPagingAction(pagingFieldAction);
        
        processor.doAction(rsColumns);

        TimerLogger.getLogger().end("sync all fields");
    }

    /***************************************************************************
     * 查询所有表
     * @param listClassVO
     * @author Rocex Wang
     * @version 2020-5-11 11:19:19
     * @throws Exception
     ***************************************************************************/
    protected void syncAllDBTable() throws Exception
    {
        TimerLogger.getLogger().begin("sync all table");
        
        ResultSet rsTable = sqlExecutorSource.getConnection().getMetaData().getTables(null, strDBSchema, "%", new String[] { "TABLE" });
        
        Map<String, String> mapTable = new HashMap<>();
        mapTable.put("TABLE_NAME", "DefaultTableName");
        
        IAction pagingAction = evt ->
        {
            List<MetaVO> listVO = (List<MetaVO>) evt.getSource();

            try
            {
                sqlExecutorTarget.insertVO(listVO.toArray(new MetaVO[0]));
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        };
        
        BeanListProcessor<ClassVO> processor = new BeanListProcessor<>(ClassVO.class, mapTable, classVO ->
        {
            String strDefaultTableName = classVO.getDefaultTableName().toLowerCase();
            
            // 表名长度小于6、不包含下划线、要排除 的都认为不是合法要生成数据字典的表
            if (strDefaultTableName.length() < 6 || !strDefaultTableName.contains("_") || strTableFilters.contains("," + strDefaultTableName))
            {
                return false;
            }
            
            classVO.setClassType(999);
            classVO.setId(strDefaultTableName);
            classVO.setName(strDefaultTableName);
            classVO.setDisplayName(strDefaultTableName);
            classVO.setDefaultTableName(strDefaultTableName);

            try
            {
                String strPrimaryKeys = getPrimaryKeys(strDefaultTableName, mapTable);
                
                classVO.setKeyAttribute(strPrimaryKeys);
            }
            catch (Exception ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
            
            return true;
        }, "DefaultTableName");

        processor.setPagingAction(pagingAction);
        processor.doAction(rsTable);
        
        TimerLogger.getLogger().end("sync all table");
    }

    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-11 14:00:41
     ***************************************************************************/
    protected void syncDatabaseMeta()
    {
        try
        {
            syncAllDBTable();

            syncAllDBField();
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }

    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-11 14:00:20
     ***************************************************************************/
    protected void syncMetaData()
    {
        String strModuleSQL = "select distinct id,name,displayname,parentmoduleid from md_module order by name";
        String strComponentSQL = "select id as original_id,name,displayname,ownmodule ownmodule from md_component";
        String strClassSQL = "select id,name,displayname,defaulttablename,fullclassname,keyattribute,componentid,classtype,isprimary,help from md_class order by defaulttablename";
        String strEnumValueSQL = "select id as class_id,enumsequence as enum_sequence,name,value enum_value from md_enumvalue order by id,enumsequence";
        String strPropertySQL = "select a.id original_id,a.name as name,a.displayname as displayname,attrlength,attrminvalue"
                + ",attrmaxvalue,attrsequence,customattr,datatype,datatypestyle,a.defaultvalue as defaultvalue"
                + ",a.nullable as nullable,a.precise as precise,refmodelname,classid,b.sqldatetype data_type_sql,b.pkey"
                + " from md_property a left join md_column b on a.name=b.name where classid=? and b.tableid=? order by b.pkey desc,a.attrsequence";

        IAction pagingAction = evt1 ->
        {
            List<MetaVO> listVO = (List<MetaVO>) evt1.getSource();

            for (MetaVO metaVO : listVO)
            {
                if (metaVO.getId() == null)
                {
                    metaVO.setId(StringHelper.getId());
                }
            }
            
            try
            {
                sqlExecutorTarget.insertVO(listVO.toArray(new MetaVO[0]));
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        };

        queryMetaVO(ModuleVO.class, strModuleSQL, null, pagingAction);
        queryMetaVO(ComponentVO.class, strComponentSQL, null, pagingAction);
        queryMetaVO(EnumVO.class, strEnumValueSQL, null, pagingAction);

        List<ClassVO> listClassVO = (List<ClassVO>) queryMetaVO(ClassVO.class, strClassSQL, null, pagingAction);

        int iEnableLevel = Logger.getLogger().getEnableLevel();

        IAction pagingPropertyAction = evt1 ->
        {
            List<PropertyVO> listVO = (List<PropertyVO>) evt1.getSource();

            for (PropertyVO propertyVO : listVO)
            {
                propertyVO.setId(StringHelper.getId());
                propertyVO.setDataTypeSql(getDataTypeSql(propertyVO));
            }

            try
            {
                sqlExecutorTarget.insertVO(listVO.toArray(new MetaVO[0]));
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        };

        TimerLogger.getLogger().begin("sync PropertyVO by class");
        Logger.getLogger().setEnableLevel(Logger.iLoggerLevelError);

        for (ClassVO classVO : listClassVO)
        {
            SQLParameter param = new SQLParameter();
            param.addParam(classVO.getId());
            param.addParam(classVO.getDefaultTableName());

            queryMetaVO(PropertyVO.class, strPropertySQL, param, pagingPropertyAction);
        }

        Logger.getLogger().setEnableLevel(iEnableLevel);

        TimerLogger.getLogger().end("sync PropertyVO by class");
    }
}
