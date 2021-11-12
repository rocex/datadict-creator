package org.rocex.datadict;

import java.nio.file.Path;
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
public class SyncDBSchemaAction extends CreateDataDictAction
{
    protected SQLExecutor sqlExecutorTarget = null;
    
    /***************************************************************************
     * @param strVersion
     * @author Rocex Wang
     * @since 2021-10-28 02:18:20
     ***************************************************************************/
    public SyncDBSchemaAction(String strVersion)
    {
        super(strVersion);

        Properties dbPropTarget = new Properties();

        // dbPropTarget.setProperty("jdbc.url", "jdbc:sqlite:" + strOutputRootDir + File.separator +
        // "datadict.sqlite");
        dbPropTarget.setProperty("jdbc.url", "jdbc:sqlite:C:/datadict/datadict.sqlite");
        dbPropTarget.setProperty("jdbc.driver", "org.sqlite.JDBC");

        sqlExecutorTarget = new SQLExecutor(dbPropTarget);
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
    }

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datadict.CreateDataDictAction#getClassFilePath(org.rocex.datadict.vo.ClassVO)
     * @author Rocex Wang
     * @since 2021-10-28 15:05:15
     ****************************************************************************/
    @Override
    protected Path getClassFilePath(ClassVO classVO)
    {
        return null;
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
     * @author Rocex Wang
     * @since 2021-11-11 14:00:41
     ***************************************************************************/
    protected void syncDatabaseMeta()
    {
        try
        {
            List<ClassVO> listAllDBTable = queryAllDBTable();

            sqlExecutorTarget.insertVO(listAllDBTable.toArray(new ClassVO[0]));

            //
            Map<String, Integer> mapSequence = new HashMap<>();
            
            IAction pagingFieldAction = evt ->
            {
                List<PropertyVO> listVO = (List<PropertyVO>) evt.getSource();

                for (PropertyVO propertyVO : listVO)
                {
                    propertyVO.setClassId(propertyVO.getClassId().toLowerCase());

                    propertyVO.setDataTypeStyle(999);
                    propertyVO.setId(StringHelper.getId());
                    propertyVO.setDisplayName(propertyVO.getName());
                    propertyVO.setDataTypeSql(getDataTypeSql(propertyVO));
                    propertyVO.setName(propertyVO.getName().toLowerCase());
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

            TimerLogger.getLogger().begin("query all fields");

            DatabaseMetaData dbMetaData = sqlExecutor.getConnection().getMetaData();

            // 一次性查出所有表的字段，然后再按照表名分组
            ResultSet rsColumns = dbMetaData.getColumns(null, strDBSchema, null, null);
            
            BeanListProcessor<? extends MetaVO> processor = new BeanListProcessor<>(PropertyVO.class, mapColumn);
            processor.setPagingAction(pagingFieldAction);
            
            processor.doAction(rsColumns);

            TimerLogger.getLogger().end("query all fields");
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
        String strModuleSQL = "select distinct id,name,displayname from md_module order by name";
        String strComponentSQL = "select distinct id,name,displayname,lower(ownmodule) ownmodule from md_component where versiontype=0";
        String strClassSQL = "select id,name,displayname,defaulttablename,fullclassname,keyattribute,componentid,classtype,isprimary from md_class order by lower(defaulttablename)";
        String strEnumValueSQL = "select id||'_'||versiontype||'_'||value||'_'||industry as id,id as class_id,cast(enumsequence as int) as enum_sequence,name,value enum_value from md_enumvalue order by id,enumsequence";
        String strPropertySQL = "select a.id original_id,a.name as name,a.displayname as displayname,attrlength,attrminvalue"
                + ",attrmaxvalue,attrsequence,customattr,datatype,datatypestyle,a.defaultvalue as defaultvalue"
                + ",a.nullable as nullable,a.precise as precise,refmodelname,classid,b.sqldatetype data_type_sql,b.pkey"
                + " from md_property a left join md_column b on a.name=b.name where classid=? and b.tableid=? order by b.pkey desc,a.attrsequence";

        IAction pagingAction = evt1 ->
        {
            List<MetaVO> listVO = (List<MetaVO>) evt1.getSource();

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
