package org.rocex.datadict.action;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.ComponentVO;
import org.rocex.datadict.vo.Context;
import org.rocex.datadict.vo.DictJsonVO;
import org.rocex.datadict.vo.EnumValueVO;
import org.rocex.datadict.vo.IndexVO;
import org.rocex.datadict.vo.MetaVO;
import org.rocex.datadict.vo.MetaVO.ModelType;
import org.rocex.datadict.vo.ModuleVO;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.db.SQLExecutor;
import org.rocex.db.SQLExecutor.DBType;
import org.rocex.db.param.SQLParameter;
import org.rocex.db.processor.BeanListProcessor;
import org.rocex.db.processor.PagingAction;
import org.rocex.db.processor.ResultSetProcessor;
import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;
import org.rocex.utils.ResHelper;
import org.rocex.utils.StringHelper;
import org.rocex.vo.IAction;

/***************************************************************************
 * 同步其它NC数据库的元数据和表结构到sqlite数据库<br>
 * @author Rocex Wang
 * @since 2021-10-28 02:17:34
 ***************************************************************************/
public abstract class SyncDBSchemaAction implements IAction, Closeable, ISyncDBSchemaAction
{
    protected Boolean isCreateDbDdc;
    
    protected Map<String, String> mapId = new HashMap<>();                          // 为了减小生成的文件体积，把元数据长id和新生成的短id做个对照关系
    protected Map<String, String> mapPrimaryKeyByTableName = new HashMap<>();       // 表名和表主键列表的对应关系，多个主键用；分隔，表名用全小写
    protected Map<String, PropertyVO> mapPropertyVOByTableName = new HashMap<>();   // 表名+属性名 - PropertyVO
    
    protected Pattern patternTableFilter;
    
    protected Properties propCodeName;
    protected Properties propDBSource;
    
    protected SQLExecutor sqlExecutorTarget;
    
    // 排除的一些表名前缀
    protected String[] strTableBeginFilters = { "aqua_explain_", "gltmp_verdtlbal", "gl_tmp_table", "hr_temptable", "ic_temp_", "iufo_measpub_",
            "iufo_measure_data_", "iufo_tmp_pub_", "ntb_tmp_formual_", "obsclass", "pitemid", "pkjkbx", "sm_securitylog_", "sm_securitylog_",
            "ssccm_adjustlog_b_", "ssccm_adjust_log_", "szxmid", "tb_cell_wbk", "tb_fd_sht", "tb_fd_sht", "tb_tmp_", "tb_tmp_tcheck", "tb_tmp_tcheck_",
            "tb_tt_", "tb_tt_gh_budgetmodle", "temp000", "temppkts", "temptable_", "temp_", "temp_bd_", "temp_fa_", "temp_ia_", "temp_ic_", "temp_pam_",
            "temp_scacosts_", "temp_scas", "temq_", "tempx_", "tmpbd_", "tmpin", "tmpina", "tmpinb", "tmpinpk_", "tmpins", "tmpinsrc_", "tmpintop_",
            "tmpub_calog_temp", "tmp_", "tmp_arap_", "tmp_gl_", "tmp_po_", "tmp_scmf", "tmp_so_", "tm_mqsend_success_", "transf2pcm", "t_ationid", "t_emplate",
            "t_laterow", "t_laterow", "uidbcache_temp_", "uidbcache_temp_", "wa_temp_", "zdp_" };
    
    protected String[] strTableEndFilters = {};     // 排除的一些表名后缀
    protected String[] strTableIncludeFilters = {}; // 排除的一些包含表名
    
    protected String strTs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(new Date());
    protected String strVersion;                    // 数据字典版本
    
    /***************************************************************************
     * @param strVersion
     * @author Rocex Wang
     * @since 2021-10-28 02:18:20
     ***************************************************************************/
    public SyncDBSchemaAction(String strVersion)
    {
        this.strVersion = strVersion;
        
        isCreateDbDdc = Boolean.parseBoolean(Context.getInstance().getSetting("createDbDdc", "true"));
        
        propCodeName = FileHelper.load("data" + File.separator + "code-name.properties");
        
        // source
        propDBSource = new Properties();
        propDBSource.setProperty("jdbc.url", Context.getInstance().getSetting("jdbc.url"));
        propDBSource.setProperty("jdbc.user", Context.getInstance().getSetting("jdbc.user"));
        propDBSource.setProperty("jdbc.driver", Context.getInstance().getSetting("jdbc.driver"));
        propDBSource.setProperty("jdbc.password", Context.getInstance().getSetting("jdbc.password"));
        
        // target
        Properties dbPropTarget = new Properties();
        
        dbPropTarget.setProperty("jdbc.url", Context.getInstance().getSetting("target.jdbc.url").replace("${version}", strVersion));
        dbPropTarget.setProperty("jdbc.user", Context.getInstance().getSetting("target.jdbc.user"));
        dbPropTarget.setProperty("jdbc.driver", Context.getInstance().getSetting("target.jdbc.driver"));
        dbPropTarget.setProperty("jdbc.password", Context.getInstance().getSetting("target.jdbc.password"));
        
        sqlExecutorTarget = new SQLExecutor(dbPropTarget);
        
        String strTableFilterPattern = Context.getInstance().getSetting("exclude.tablePattern");
        patternTableFilter = Pattern.compile(strTableFilterPattern, Pattern.CASE_INSENSITIVE);
    }
    
    @Override
    public void close()
    {
        mapId.clear();
        
        propDBSource.clear();
        propCodeName.clear();
        
        mapPrimaryKeyByTableName.clear();
        mapPropertyVOByTableName.clear();
        
        sqlExecutorTarget.close();
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see IAction#doAction(EventObject)
     * @author Rocex Wang
     * @since 2021-10-28 14:17:51
     ****************************************************************************/
    @Override
    public void doAction(EventObject evt)
    {
        Logger.getLogger().begin("sync db schema and meta data");
        
        if (!isNeedSyncData())
        {
            return;
        }
        
        sqlExecutorTarget.initDBSchema(ModuleVO.class, ComponentVO.class, ClassVO.class, PropertyVO.class, EnumValueVO.class, IndexVO.class, DictJsonVO.class);
        
        beforeSyncData();
        
        if (isCreateDbDdc)
        {
            syncDBMeta();
        }
        
        syncMetaData();
        
        afterSyncData();
        
        Logger.getLogger().end("sync db schema and meta data");
    }
    
    /***************************************************************************
     * 返回数据库类型定义
     * @param propertyVO
     * @return String
     * @author Rocex Wang
     * @since 2020-4-28 10:14:34
     ***************************************************************************/
    protected String getDataTypeSql(PropertyVO propertyVO)
    {
        if (propertyVO.getDataTypeSql() == null && (propertyVO.getDataType().length() == 20 || propertyVO.getDataType().length() == 36))
        {
            propertyVO.setDataTypeSql("char");
        }
        
        String strDbTypeSql = propertyVO.getDataTypeSql().toLowerCase();
        
        if ((strDbTypeSql.contains("char") || strDbTypeSql.contains("text")) && propertyVO.getAttrLength() != null)
        {
            strDbTypeSql = strDbTypeSql + "(" + propertyVO.getAttrLength() + ")";
        }
        else if ((strDbTypeSql.contains("decimal") || strDbTypeSql.contains("number")) && propertyVO.getAttrLength() != null)
        {
            strDbTypeSql = strDbTypeSql + "(" + propertyVO.getAttrLength() + ", " + Objects.toString(propertyVO.getPrecise(), "0") + ")";
        }
        
        propertyVO.setDataTypeSql(strDbTypeSql);
        
        return strDbTypeSql;
    }
    
    /***************************************************************************
     * @param classVO
     * @return mappedId
     * @author Rocex Wang
     * @since 2020-4-30 13:47:11
     ***************************************************************************/
    protected synchronized String getMappedClassId(ClassVO classVO)
    {
        return getMappedId("class", classVO.getId());
    }
    
    /***************************************************************************
     * @param strType
     * @param strId
     * @return mappedId
     * @author Rocex Wang
     * @since 2020-4-30 13:47:06
     ***************************************************************************/
    protected synchronized String getMappedId(String strType, String strId)
    {
        if (strId != null && strId.length() < 24)
        {
            return strId;
        }
        
        String strKey = strType + "_" + strId;
        
        String strValue = mapId.get(strKey);
        
        if (strValue != null)
        {
            return strValue;
        }
        
        String strMapId = StringHelper.getId();
        
        mapId.put(strKey, strMapId);
        
        return strMapId;
    }
    
    /***************************************************************************
     * @param moduleVO
     * @return mappedId
     * @author Rocex Wang
     * @since 2020-4-30 13:46:54
     ***************************************************************************/
    protected synchronized String getMappedModuleId(ModuleVO moduleVO)
    {
        return getMappedId("module", moduleVO.getId());
    }
    
    /***************************************************************************
     * 根据数据库特性一次性读取所有表的主键，如果不能确定数据库，还是按照jdbc的api每次只取一个表的主键
     * @param strTableName
     * @param mapPrimaryKey
     * @return String
     * @throws Exception
     * @author Rocex Wang
     * @since 2020-5-22 14:03:59
     ***************************************************************************/
    protected String getPrimaryKeys(Connection connSource, String strDBCatalog, String strDBSchema, String strTableName, Map<String, String> mapPrimaryKey)
            throws Exception
    {
        List<String> listPk = new ArrayList<>();
        
        // 找到表的主键
        try (ResultSet rsPkColumns = connSource.getMetaData().getPrimaryKeys(strDBCatalog, strDBSchema, strTableName))
        {
            List<PropertyVO> listPkPropertyVO = (List<PropertyVO>) new BeanListProcessor<>(PropertyVO.class, mapPrimaryKey, "name").doAction(rsPkColumns);
            
            for (PropertyVO propertyVO : listPkPropertyVO)
            {
                if (propertyVO.getName() != null)
                {
                    listPk.add(propertyVO.getName().toLowerCase());
                }
            }
        }
        
        return String.join(";", listPk);
    }
    
    /***************************************************************************
     * 获取数据库临时表并加到strTableFilters里
     * @throws Exception
     * @author Rocex Wang
     * @since 2020-5-22 14:03:59
     ***************************************************************************/
    protected void initTableFiltersWithTempTableName(SQLExecutor sqlExecutorSource) throws SQLException
    {
        if (sqlExecutorSource.getDBType() == DBType.Oracle)
        {
            Logger.getLogger().begin("oracle query all temporary tables");
            
            String strSQL = "select lower(table_name) from user_tables where temporary='Y' order by table_name";
            
            final List<String> listTempTableName = new ArrayList<>();
            
            sqlExecutorSource.executeQuery(strSQL, new ResultSetProcessor()
            {
                @Override
                protected Object processResultSet(ResultSet resultSet) throws SQLException
                {
                    while (resultSet.next())
                    {
                        listTempTableName.add(resultSet.getString(1));
                    }
                    
                    return null;
                }
            });
            
            listTempTableName.addAll(Arrays.asList(strTableBeginFilters));
            
            strTableBeginFilters = listTempTableName.toArray(new String[0]);
            
            Logger.getLogger().end("oracle query all temporary tables");
        }
    }
    
    /***************************************************************************
     * @return boolean
     * @author Rocex Wang
     * @since 2021-11-11 16:26:03
     ***************************************************************************/
    public boolean isNeedSyncData()
    {
        if (!sqlExecutorTarget.isTableExist("md_module"))
        {
            return true;
        }
        
        String strSQL = "select count(1) from md_module where ddc_version='" + strVersion + "'";
        
        try
        {
            int iRecordCount = (Integer) sqlExecutorTarget.executeQuery(strSQL, new ResultSetProcessor()
            {
                @Override
                protected Object processResultSet(ResultSet resultSet) throws SQLException
                {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            });
            
            return iRecordCount == 0;
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        return true;
    }
    
    /***************************************************************************
     * @param strTableName
     * @return true：表名合法；false：不合法
     * @author Rocex Wang
     * @since 2021-11-17 19:07:50
     ***************************************************************************/
    protected boolean isTableNameValid(String strTableName)
    {
        for (String strTableFilter : strTableBeginFilters)
        {
            if (strTableName.startsWith(strTableFilter))
            {
                return false;
            }
        }
        
        for (String strTableFilter : strTableEndFilters)
        {
            if (strTableName.endsWith(strTableFilter))
            {
                return false;
            }
        }
        
        for (String strTableFilter : strTableIncludeFilters)
        {
            if (strTableName.contains(strTableFilter))
            {
                return false;
            }
        }
        
        boolean blValid = patternTableFilter.matcher(strTableName).matches();
        
        return !blValid;
    }
    
    /***************************************************************************
     * @param metaVOClass
     * @param strSQL
     * @param param
     * @param pagingAction
     * @return List<? extends MetaVO>
     * @author Rocex Wang
     * @since 2020-5-9 11:20:25
     ***************************************************************************/
    protected List<? extends MetaVO> queryMetaVO(SQLExecutor sqlExecutorSource, Class<? extends MetaVO> metaVOClass, String strSQL, SQLParameter param,
            PagingAction pagingAction)
    {
        Logger.getLogger().begin("query " + metaVOClass.getSimpleName());
        
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
        
        Logger.getLogger().end("query " + metaVOClass.getSimpleName());
        
        return listMetaVO;
    }
    
    /***************************************************************************
     * @throws SQLException
     * @author Rocex Wang
     * @since 2021-11-15 13:59:54
     ***************************************************************************/
    protected void syncDBField(Connection connSource, String strDBCatalog, String strDBSchema, ClassVO classVO) throws SQLException
    {
        PagingAction pagingFieldAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                int iSequence = 0;
                List<PropertyVO> listPropertyVO = (List<PropertyVO>) evt.getSource();
                
                for (PropertyVO propertyVO : listPropertyVO)
                {
                    propertyVO.setClassId(classVO.getId());
                    
                    String strPropLowerName = propertyVO.getName().toLowerCase();
                    
                    propertyVO.setModelType(ModelType.db.name());
                    propertyVO.setDataTypeStyle(ClassVO.ClassType.db.value());
                    propertyVO.setName(strPropLowerName);
                    propertyVO.setDdcVersion(strVersion);
                    propertyVO.setId(StringHelper.getId());
                    propertyVO.setAttrSequence(++iSequence);
                    propertyVO.setDataTypeSql(getDataTypeSql(propertyVO));
                    propertyVO.setDefaultValue(StringHelper.isEmpty(propertyVO.getDefaultValue()) ? null : propertyVO.getDefaultValue().toLowerCase());
                    propertyVO.setDisplayName(
                            StringHelper.isEmpty(propertyVO.getRemarks()) ? strPropLowerName : StringHelper.removeCRLF(propertyVO.getRemarks()));
                }
                
                Map<String, PropertyVO> mapTableNamePropertyVO2 = listPropertyVO.stream()
                        .collect(Collectors.toMap(propertyVO -> propertyVO.getTableName() + "." + propertyVO.getName(), propertyVO -> propertyVO));
                
                mapPropertyVOByTableName.putAll(mapTableNamePropertyVO2);
                
                try
                {
                    sqlExecutorTarget.insertVO(listPropertyVO.toArray(new PropertyVO[0]));
                    
                    listPropertyVO.clear();
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        };
        
        Map<String, String> mapColumn = new HashMap<>();
        mapColumn.put("COLUMN_NAME", "Name");
        mapColumn.put("COLUMN_SIZE", "AttrLength");
        mapColumn.put("COLUMN_DEF", "DefaultValue");
        mapColumn.put("TABLE_NAME", "TableName");
        mapColumn.put("TYPE_NAME", "DataTypeSql");
        mapColumn.put("DECIMAL_DIGITS", "Precise");
        mapColumn.put("NULLABLE", "Nullable");
        
        BeanListProcessor<PropertyVO> processor = new BeanListProcessor<>(PropertyVO.class, mapColumn);
        
        try (ResultSet rsColumns = connSource.getMetaData().getColumns(strDBCatalog, strDBSchema, classVO.getTableName(), null))
        {
            processor.setPagingAction(pagingFieldAction);
            
            processor.doAction(rsColumns);
        }
    }
    
    protected void syncDBIndex(Connection connSource, String strSrcDBCatalog, String strSrcDBSchema, ClassVO classVO) throws SQLException
    {
        PagingAction pagingFieldAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                List<IndexVO> listNewIndexVO = new ArrayList<>();
                List<IndexVO> listIndexVO = (List<IndexVO>) evt.getSource();
                
                Map<String, List<IndexVO>> mapIndex = listIndexVO.stream().filter(indexVO -> indexVO.getIndexName() != null && indexVO.getColumnName() != null)
                        .collect(Collectors.groupingBy(IndexVO::getIndexName));
                
                for (Map.Entry<String, List<IndexVO>> entry : mapIndex.entrySet())
                {
                    AtomicBoolean isNonUnique = new AtomicBoolean(false);
                    
                    String strColumns = entry.getValue().stream().sorted(Comparator.comparingInt(IndexVO::getOrdinalPosition)).map(indexVO ->
                    {
                        isNonUnique.set(indexVO.isNonUnique());
                        
                        return indexVO.getColumnName().toLowerCase() + ("D".equalsIgnoreCase(indexVO.getAscOrDesc()) ? " desc" : "");
                    }).collect(Collectors.joining(","));
                    
                    String strIndexSQL = "create%s index %s on %s.%s (%s)".formatted(isNonUnique.get() ? "" : " unique", entry.getKey().toLowerCase(),
                            strSrcDBSchema, classVO.getTableName(), strColumns);
                    
                    // Logger.getLogger().debug(strIndexSQL);
                    
                    IndexVO indexVO = entry.getValue().get(0);
                    indexVO.setDdcVersion(strVersion);
                    indexVO.setSchema2(strSrcDBSchema);
                    indexVO.setId(StringHelper.getId());
                    indexVO.setClassId(classVO.getId());
                    indexVO.setTableName(classVO.getTableName());
                    indexVO.setIndexSql(strIndexSQL.toLowerCase());
                    indexVO.setIndexName(indexVO.getIndexName().toLowerCase());
                    
                    listNewIndexVO.add(indexVO);
                }
                
                try
                {
                    sqlExecutorTarget.insertVO(listNewIndexVO.toArray(new IndexVO[0]));
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        };
        
        pagingFieldAction.setPageSize(Integer.MAX_VALUE);
        
        BeanListProcessor<IndexVO> processor = new BeanListProcessor<>(IndexVO.class);
        
        try (ResultSet rsIndex = connSource.getMetaData().getIndexInfo(strSrcDBCatalog, strSrcDBSchema, classVO.getTableName(), false, true))
        {
            processor.setPagingAction(pagingFieldAction);
            
            processor.doAction(rsIndex);
        }
    }
    
    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-11 14:00:41
     ***************************************************************************/
    protected void syncDBMeta()
    {
        Logger.getLogger().begin("sync database meta");
        
        String strSourceUrl = Context.getInstance().getSetting("jdbc.url");
        
        String strSrcDBSchemaList = Context.getInstance().getSetting("jdbc.schemas");
        
        String[] strSrcDBSchemas = StringHelper.isEmpty(strSrcDBSchemaList) ? new String[] { Context.getInstance().getSetting("jdbc.user") }
                : strSrcDBSchemaList.split(",");
        
        for (String strSrcDBSchema : strSrcDBSchemas)
        {
            Properties dbPropSource2 = (Properties) propDBSource.clone();
            dbPropSource2.setProperty("jdbc.url", strSourceUrl.replace("${schema}", strSrcDBSchema));
            
            try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2); Connection connSource = sqlExecutorSource.getConnection())
            {
                String strSrcDBCatalog = connSource.getCatalog();
                
                initTableFiltersWithTempTableName(sqlExecutorSource);
                
                syncDBTable(connSource, strSrcDBCatalog, strSrcDBSchema, "db__" + strSrcDBSchema);
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        }
        
        Logger.getLogger().end("sync database meta");
    }
    
    /***************************************************************************
     * 查询所有表
     * @author Rocex Wang
     * @since 2020-5-11 11:19:19
     * @throws Exception
     ***************************************************************************/
    protected void syncDBTable(Connection connSource, String strSrcDBCatalog, String strSrcDBSchema, String strComponentId) throws SQLException
    {
        String strMsg = "sync all tables, fields and index from schema [%s]".formatted(strSrcDBSchema);
        
        Logger.getLogger().begin(strMsg);
        
        Map<String, String> mapTable = new HashMap<>();
        mapTable.put("TABLE_NAME", "TableName");
        mapTable.put("TABLE_TYPE", "ClassListUrl"); // 用 ClassListUrl 代替了，反正 ClassListUrl 也不保存
        
        Map<String, String> mapPrimaryKey = new HashMap<>();
        mapPrimaryKey.put("COLUMN_NAME", "name");
        mapPrimaryKey.put("KEY_SEQ", "AttrSequence");
        
        PagingAction pagingAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                List<ClassVO> listClassVO = (List<ClassVO>) evt.getSource();
                
                try
                {
                    sqlExecutorTarget.insertVO(listClassVO.toArray(new ClassVO[0]));
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
                
                ExecutorService executorService = Executors.newFixedThreadPool(ResHelper.getThreadCount());
                
                listClassVO.forEach(classVO -> executorService.execute(() ->
                {
                    try
                    {
                        syncDBField(connSource, strSrcDBCatalog, strSrcDBSchema, classVO);
                    }
                    catch (SQLException ex)
                    {
                        Logger.getLogger().error(ex.getMessage(), ex);
                    }
                    
                    try
                    {
                        syncDBIndex(connSource, strSrcDBCatalog, strSrcDBSchema, classVO);
                    }
                    catch (SQLException ex)
                    {
                        Logger.getLogger().error(ex.getMessage(), ex);
                    }
                }));
                
                executorService.shutdown();
                
                while (!executorService.isTerminated())
                {
                    ResHelper.sleep(100);
                }
                
                listClassVO.clear();
            }
        };
        
        BeanListProcessor<ClassVO> processor = new BeanListProcessor<>(ClassVO.class, mapTable, classVO ->
        {
            String strTableName = Objects.toString(classVO.getTableName(), "").toLowerCase();
            
            if (!"table".equalsIgnoreCase(classVO.getClassListUrl()) || !isTableNameValid(strTableName))
            {
                return false;
            }
            
            classVO.setDdcVersion(strVersion);
            classVO.setId(StringHelper.getId());
            classVO.setName(strTableName);
            classVO.setComponentId(strComponentId);
            classVO.setModelType(ModelType.db.name());
            classVO.setTableName(strTableName);
            classVO.setClassType(ClassVO.ClassType.db.value());
            classVO.setDisplayName(
                    propCodeName.getProperty(strTableName, StringHelper.isEmpty(classVO.getRemarks()) ? "" : StringHelper.removeCRLF(classVO.getRemarks())));
            
            try
            {
                String strPrimaryKeys = getPrimaryKeys(connSource, strSrcDBCatalog, strSrcDBSchema, strTableName, mapPrimaryKey);
                
                classVO.setKeyAttribute(strPrimaryKeys);
                
                mapPrimaryKeyByTableName.put(strTableName, strPrimaryKeys);
            }
            catch (Exception ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
            
            return true;
        }, "TableName", "ClassListUrl", "Remarks");
        
        try (ResultSet rsTable = connSource.getMetaData().getTables(strSrcDBCatalog, strSrcDBSchema, "%", new String[] { "TABLE" }))
        {
            processor.setPagingAction(pagingAction);
            processor.doAction(rsTable);
        }
        
        Logger.getLogger().end(strMsg);
    }
    
    protected void syncMetaData(SQLExecutor sqlExecutorSource, String strModuleSQL, String strComponentSQL, String strBizObjAsComponentSQL, String strClassSQL,
            String strPropertySQL, String strEnumAsClass, String strEnumValueSQL)
    {
        Logger.getLogger().begin("sync bip metadata");
        
        PagingAction pagingAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                List<MetaVO> listVO = (List<MetaVO>) evt.getSource();
                
                for (MetaVO metaVO : listVO)
                {
                    if (metaVO.getId() == null)
                    {
                        metaVO.setId(StringHelper.getId());
                    }
                    
                    metaVO.setDisplayName(StringHelper.removeCRLF(metaVO.getDisplayName()));
                    
                    if (metaVO instanceof ClassVO classVO && StringHelper.isBlank(classVO.getKeyAttribute()))
                    {
                        classVO.setKeyAttribute(mapPrimaryKeyByTableName.get(Objects.toString(classVO.getTableName(), "").toLowerCase()));
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
            }
        };
        
        PagingAction pagingPropertyAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                List<PropertyVO> listVO = (List<PropertyVO>) evt.getSource();
                
                for (PropertyVO propertyVO : listVO)
                {
                    propertyVO.setId(StringHelper.getId());
                    propertyVO.setDataTypeSql(getDataTypeSql(propertyVO));
                    propertyVO.setDisplayName(StringHelper.removeCRLF(propertyVO.getDisplayName()));
                }
                
                try
                {
                    sqlExecutorTarget.insertVO(listVO.toArray(new MetaVO[0]));
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        };
        
        queryMetaVO(sqlExecutorSource, ModuleVO.class, strModuleSQL, null, pagingAction);
        queryMetaVO(sqlExecutorSource, ComponentVO.class, strComponentSQL, null, pagingAction);
        queryMetaVO(sqlExecutorSource, ClassVO.class, strClassSQL, null, pagingAction);
        queryMetaVO(sqlExecutorSource, PropertyVO.class, strPropertySQL, null, pagingPropertyAction);
        
        if (StringHelper.isNotBlank(strBizObjAsComponentSQL))
        {
            queryMetaVO(sqlExecutorSource, ComponentVO.class, strBizObjAsComponentSQL, null, pagingAction);
        }
        
        if (StringHelper.isNotEmpty(strEnumAsClass))
        {
            queryMetaVO(sqlExecutorSource, ClassVO.class, strEnumAsClass, null, pagingAction);
        }
        
        queryMetaVO(sqlExecutorSource, EnumValueVO.class, strEnumValueSQL, null, pagingAction);
        
        Logger.getLogger().end("sync bip metadata");
    }
}
