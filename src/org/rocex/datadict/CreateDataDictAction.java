package org.rocex.datadict;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.ComponentVO;
import org.rocex.datadict.vo.EnumVO;
import org.rocex.datadict.vo.MetaVO;
import org.rocex.datadict.vo.ModuleVO;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.db.param.SQLParameter;
import org.rocex.db.processor.BeanListProcessor;
import org.rocex.db.processor.SQLExecutor;
import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;
import org.rocex.utils.TimerLogger;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-22 14:10:00
 ***************************************************************************/
public class CreateDataDictAction
{
    private int iIndex = 1;

    private Map<String, ? extends MetaVO> mapClassVO = new HashMap<>();            // class id 和 class 的对应关系
    private Map<String, List<ClassVO>> mapClassVOByComponent = new HashMap<>();     // component id 和 component 内所有 class 链接的对应关系
    private Map<String, ? extends MetaVO> mapComponentVO = new HashMap<>();        // component id 和 component 的对应关系
    private Map<String, String> mapEnumString = new HashMap<>();                    // enum id 和 enum name and value 的对应关系
    private Map<String, String> mapId = new HashMap<>();                            // 为了减小生成的文件体积，把元数据id和序号做个对照关系
    private Map<String, ? extends MetaVO> mapModuleVO = new HashMap<>();           // module id 和 module 的对应关系
    private Map<String, String> mapTableNamePrimaryKeys = new HashMap<>();          // 表名和表主键列表的对应关系，多个主键用；分隔，表名用全小写

    private SQLExecutor sqlExecutor = null;

    private String strCreateTime = DateFormat.getDateTimeInstance().format(new Date());
    private String strOutputDdcDir;     // 输出数据字典文件目录
    private String strOutputRootDir;    // 输出文件根目录
    private String strSchema;
    private String strVersion;

    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-26 14:52:18
     ***************************************************************************/
    public CreateDataDictAction(String strVersion)
    {
        super();

        this.strVersion = strVersion;

        strOutputRootDir = DataDictCreator.settings.getProperty(strVersion + ".OutputDir");
        strOutputDdcDir = strOutputRootDir + File.separator + "ddc";

        strSchema = DataDictCreator.settings.getProperty(strVersion + ".jdbc.user").toUpperCase();

        Properties dbProp = new Properties();

        dbProp.setProperty("jdbc.driver", DataDictCreator.settings.getProperty(strVersion + ".jdbc.driver"));
        dbProp.setProperty("jdbc.url", DataDictCreator.settings.getProperty(strVersion + ".jdbc.url"));
        dbProp.setProperty("jdbc.user", DataDictCreator.settings.getProperty(strVersion + ".jdbc.user"));
        dbProp.setProperty("jdbc.password", DataDictCreator.settings.getProperty(strVersion + ".jdbc.password"));

        sqlExecutor = new SQLExecutor(dbProp);
    }

    /***************************************************************************
     * 修复一些数据库数据问题
     * @author Rocex Wang
     * @version 2020-5-9 14:05:43
     ***************************************************************************/
    protected void adjustData()
    {
        TimerLogger.getLogger().begin("fix data");

        String strSQLs[] = { "update md_class set name='Memo' where id='BS000010000100001030' and name='MEMO'",
                "update md_class set name='MultiLangText' where id='BS000010000100001058' and name='MULTILANGTEXT'",
                "update md_class set name='Custom' where id in('BS000010000100001056','BS000010000100001059') and name='CUSTOM'" };

        try
        {
            sqlExecutor.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        TimerLogger.getLogger().end("fix data");
    }

    /***************************************************************************
     * component id 和 component 内所有 class 链接的对应关系，用于数据字典右上角实体列表
     * @param listClassVO
     * @author Rocex Wang
     * @version 2020-4-24 15:41:49
     ***************************************************************************/
    protected void buildClassVOMapByComponentId(List<ClassVO> listClassVO)
    {
        TimerLogger.getLogger().begin("build ClassVO map by componentId");

        for (ClassVO classVO : listClassVO)
        {
            if (classVO.getClassType() != 201)
            {
                continue;
            }

            List<ClassVO> listClassVO2 = mapClassVOByComponent.get(classVO.getComponentId());

            if (listClassVO2 == null)
            {
                listClassVO2 = new ArrayList<>();
            }

            if ("Y".equals(classVO.getIsPrimary()))
            {
                listClassVO2.add(0, classVO);
            }
            else
            {
                listClassVO2.add(classVO);
            }

            mapClassVOByComponent.put(classVO.getComponentId(), listClassVO2);
        }

        TimerLogger.getLogger().end("build ClassVO map by componentId");
    }

    /***************************************************************************
     * enum id 和 enum name and value 的对应关系
     * @author Rocex Wang
     * @version 2020-4-26 10:18:30
     ***************************************************************************/
    protected void buildEnumString()
    {
        TimerLogger.getLogger().begin("build enum string");

        String strEnumValueSQL = "select id,name,value from md_enumvalue order by id,enumsequence";

        List<EnumVO> listEnumValueVO = null;

        try
        {
            listEnumValueVO = (List<EnumVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(EnumVO.class), strEnumValueSQL);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        if (listEnumValueVO == null || listEnumValueVO.size() == 0)
        {
            return;
        }

        for (EnumVO enumVO : listEnumValueVO)
        {
            String strEnum = mapEnumString.get(enumVO.getId());

            if (strEnum == null)
            {
                strEnum = "";
            }

            strEnum = new StringBuilder(strEnum).append(enumVO.getValue()).append("=").append(enumVO.getName()).append(";<br>").toString();

            mapEnumString.put(enumVO.getId(), strEnum);
        }

        TimerLogger.getLogger().end("build enum string");
    }

    /***************************************************************************
     * 建立 metaId 和 metaVO 的关系
     * @param listMetaVO
     * @return Map
     * @author Rocex Wang
     * @version 2020-4-26 10:18:51
     ***************************************************************************/
    protected Map<String, ? extends MetaVO> buildMap(List<? extends MetaVO> listMetaVO)
    {
        TimerLogger.getLogger().begin("build map: " + listMetaVO.get(0).getClass().getSimpleName());

        Map<String, MetaVO> mapMetaVO = new HashMap<>();

        mapMetaVO = listMetaVO.stream().collect(Collectors.toMap(MetaVO::getId, Function.identity(), (key1, key2) -> key2));

        TimerLogger.getLogger().end("build map: " + listMetaVO.get(0).getClass().getSimpleName());

        return mapMetaVO;
    }

    /***************************************************************************
     * 拷贝静态文件、css、js 等
     * @author Rocex Wang
     * @version 2020-4-29 10:33:18
     ***************************************************************************/
    protected void copyHtmlFiles()
    {
        TimerLogger.getLogger().begin("copy html files");

        try
        {
            FileHelper.copyFolder(Paths.get("settings", "html"), Paths.get(strOutputRootDir), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        TimerLogger.getLogger().end("copy html files");
    }

    /***************************************************************************
     * @param listAllClassVO
     * @throws SQLException
     * @throws Exception
     * @author Rocex Wang
     * @version 2020-5-13 16:22:33
     ***************************************************************************/
    protected void createAllTableDataDictFile(List<ClassVO> listAllClassVO) throws SQLException, Exception
    {
        TimerLogger.getLogger().begin("query all fields");

        Map<String, String> mapColumn = new HashMap<>();
        mapColumn.put("COLUMN_NAME", "Id");
        mapColumn.put("COLUMN_SIZE", "AttrLength");
        mapColumn.put("COLUMN_DEF", "DefaultValue");
        mapColumn.put("TABLE_NAME", "ClassId");
        mapColumn.put("TYPE_NAME", "SqlDateType");
        mapColumn.put("DECIMAL_DIGITS", "Precise");
        mapColumn.put("NULLABLE", "Nullable");

        DatabaseMetaData dbMetaData = sqlExecutor.getConnection().getMetaData();

        // 一次性查出所有表的字段，然后再按照表名分组
        ResultSet rsColumns = dbMetaData.getColumns(null, strSchema, null, null);

        Map<String, List<PropertyVO>> mapTableNamePropertyVO = new HashMap<>();

        new BeanListProcessor<>(PropertyVO.class, mapColumn, propertyVO ->
        {
            List<PropertyVO> listProp = mapTableNamePropertyVO.get(propertyVO.getClassId());

            if (listProp == null)
            {
                listProp = new ArrayList<>();
                mapTableNamePropertyVO.put(propertyVO.getClassId(), listProp);
            }

            listProp.add(propertyVO);

            return false;
        }).doAction(rsColumns);

        TimerLogger.getLogger().end("query all fields");

        TimerLogger.getLogger().begin("create all table data dict file: " + listAllClassVO.size());

        for (ClassVO classVO : listAllClassVO)
        {
            List<PropertyVO> listPropertyVO = mapTableNamePropertyVO.get(classVO.getDefaultTableName());

            listPropertyVO.sort((prop1, prop2) -> prop1.getId().compareToIgnoreCase(prop2.getId()));

            // 找到表的主键
            String strPks = getPrimaryKeys(dbMetaData, classVO.getDefaultTableName(), mapColumn);

            classVO.setKeyAttribute(strPks);
            classVO.setDefaultTableName(classVO.getDefaultTableName().toLowerCase());

            // 整理表的字段，把主键列放到首位，把自定义项、dr、ts放到末尾
            List<PropertyVO> listPropertyOrderVO = new ArrayList<>();  // 主键列
            List<PropertyVO> listPropertyCustomVO = new ArrayList<>(); // 自定义项
            List<PropertyVO> listPropertyFinalVO = new ArrayList<>();  // dr、ts

            List<String> listPk = Arrays.asList(strPks.split(";"));

            for (PropertyVO propertyVO : listPropertyVO)
            {
                propertyVO.setClassId(classVO.getId());

                propertyVO.setId(propertyVO.getId().toLowerCase());
                propertyVO.setName(propertyVO.getId());
                propertyVO.setDisplayName(propertyVO.getId());
                propertyVO.setNullable("1".equals(propertyVO.getNullable()) ? "Y" : "N");

                if (listPk.contains(propertyVO.getId()))
                {
                    listPropertyOrderVO.add(0, propertyVO);
                }
                else if ("dr".equals(propertyVO.getId()) || "ts".equals(propertyVO.getId()))
                {
                    listPropertyFinalVO.add(propertyVO);
                }
                else if (isCustomProperty(propertyVO.getId()))
                {
                    listPropertyCustomVO.add(propertyVO);
                }
                else
                {
                    listPropertyOrderVO.add(propertyVO);
                }
            }

            listPropertyOrderVO.addAll(listPropertyCustomVO);
            listPropertyOrderVO.addAll(listPropertyFinalVO);

            createDataDictFile(classVO, listPropertyOrderVO);
        }

        TimerLogger.getLogger().end("create all table data dict file: " + listAllClassVO.size());
    }

    /***************************************************************************
     * 生成数据字典文件
     * @param classVO
     * @author Rocex Wang
     * @version 2020-4-26 10:19:45
     ***************************************************************************/
    protected void createDataDictFile(ClassVO classVO)
    {
        if (classVO.getClassType() != 201)
        {
            return;
        }

        String strPropertySQL = "select distinct a.id id,a.name name,a.displayname displayname,attrlength,attrminvalue,attrmaxvalue,attrsequence,customattr"
                + ",datatype,datatypestyle,a.defaultvalue defaultvalue,a.nullable nullable,a.precise precise,refmodelname,classid,b.sqldatetype sqldatetype,b.pkey"
                + " from md_property a left join md_column b on a.name=b.name where classid=? and b.tableid=? order by b.pkey desc,a.attrsequence";

        // 取实体所有属性
        SQLParameter para = new SQLParameter();
        para.addParam(classVO.getId());
        para.addParam(classVO.getDefaultTableName());

        List<PropertyVO> listPropertyVO = null;

        try
        {
            listPropertyVO = (List<PropertyVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(PropertyVO.class), strPropertySQL, para);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        createDataDictFile(classVO, listPropertyVO);
    }

    /***************************************************************************
     * @param classVO
     * @param listPropertyVO
     * @author Rocex Wang
     * @version 2020-5-13 15:27:35
     ***************************************************************************/
    protected void createDataDictFile(ClassVO classVO, List<PropertyVO> listPropertyVO)
    {
        if (listPropertyVO == null)
        {
            return;
        }

        listPropertyVO = sortPropertyVO(classVO.getKeyAttribute(), listPropertyVO);

        StringBuilder strHtmlRows = new StringBuilder();
        String strHtmlDataDictRow = DataDictCreator.settings.getProperty("HtmlDataDictRow");

        List<String> listPk = Arrays.asList(classVO.getKeyAttribute().split(";"));

        for (int i = 0; i < listPropertyVO.size(); i++)
        {
            PropertyVO propertyVO = listPropertyVO.get(i);

            // 引用实体模型
            String strRefClassPathHref = "";
            ClassVO refClassVO = (ClassVO) mapClassVO.get(propertyVO.getDataType());

            if (refClassVO != null)
            {
                strRefClassPathHref = refClassVO.getDisplayName();

                if (refClassVO.getClassType() == 201)
                {
                    String strRefClassPath = getClassUrl2(refClassVO);

                    strRefClassPathHref = MessageFormat.format("<a href=\"{0}\">{1}</a>", strRefClassPath, refClassVO.getDisplayName());
                }

                strRefClassPathHref = strRefClassPathHref + " (" + refClassVO.getName() + ")";
            }

            // 数据库类型
            String strDbType = getDbType(propertyVO);

            // 默认值
            String strDefaultValue = propertyVO.getDefaultValue() == null ? "" : propertyVO.getDefaultValue();

            // 枚举/取值范围
            String strDataScope = "";
            if (propertyVO.getDataType().length() > 20 && propertyVO.getRefModelName() == null && propertyVO.getDataTypeStyle() == 300
                    && mapEnumString.containsKey(propertyVO.getDataType()))
            {
                strDataScope = mapEnumString.get(propertyVO.getDataType());
            }
            else
            {
                strDataScope = "[" + (propertyVO.getAttrMinValue() == null ? "" : propertyVO.getAttrMinValue()) + " , "
                        + (propertyVO.getAttrMaxValue() == null ? "" : propertyVO.getAttrMaxValue()) + "]";

                if ("[ , ]".equals(strDataScope))
                {
                    strDataScope = "";
                }
            }

            // 每行的css，主键行字体红色，其它行正常黑色
            String strRowStyle = listPk.contains(propertyVO.getId()) ? "pk-row" : "";

            // 是否必输
            String strMustInput = "N".equals(propertyVO.getNullable()) ? "√" : "";

            String strHtmlRow = MessageFormat.format(strHtmlDataDictRow, strRowStyle, i + 1, propertyVO.getName(),
                    propertyVO.getDisplayName(), propertyVO.getName(), strDbType, strMustInput, strRefClassPathHref, strDefaultValue,
                    strDataScope);

            strHtmlRows.append(strHtmlRow);
        }

        // 组件内实体列表链接
        String strClassList = getClassListUrl(classVO);

        if (strClassList.startsWith("/")) // 截掉最前面的斜杠
        {
            strClassList = strClassList.substring(1);
        }

        String strFullClassname = classVO.getFullClassname() == null ? "" : " / " + classVO.getFullClassname();

        String strHtml = MessageFormat.format(DataDictCreator.settings.getProperty("HtmlDataDictFile"), classVO.getDisplayName(),
                classVO.getDefaultTableName(), strFullClassname, DataDictCreator.settings.get(strVersion + ".DataDictVersion"),
                strClassList, strHtmlRows, getFooter());

        FileHelper.writeFileThread(getClassFilePath(classVO), strHtml);
    }

    /***************************************************************************
     * 生成构造实体树的json数据
     * @param listModuleVO
     * @param listClassVO
     * @param listAllClassVO
     * @author Rocex Wang
     * @version 2020-4-29 11:21:07
     ***************************************************************************/
    protected void createDataDictTreeData(List<ModuleVO> listModuleVO, List<ClassVO> listClassVO, List<ClassVO> listAllClassVO)
    {
        TimerLogger.getLogger().begin("create data dict tree data: " + (listClassVO.size() + listAllClassVO.size()));

        StringBuilder strModuleRows = new StringBuilder(); // 所有模块
        StringBuilder strClassRows = new StringBuilder();  // 所有实体

        String strLeafTemplate = "'{'id:\"{0}\",pId:\"{1}\",name:\"{2} {3}\",url:\"{4}\",target:\"{5}\"'}',";

        List<String> listUsedClassModule = new ArrayList<>();// 只生成含有实体的Module

        for (ClassVO classVO : listClassVO)
        {
            if (classVO.getClassType() != 201)
            {
                continue;
            }

            ModuleVO moduleVO = getModuleVO(classVO);

            String strUrl = getClassUrl(classVO);

            strClassRows.append(MessageFormat.format(strLeafTemplate, getMappedClassId(classVO), getMappedModuleId(moduleVO),
                    classVO.getDefaultTableName(), classVO.getDisplayName(), strUrl, "ddc"));

            String strModuleId = getModuleId(classVO);

            if (!listUsedClassModule.contains(strModuleId))
            {
                listUsedClassModule.add(strModuleId);
            }
        }

        // 单独拿出来是为了按照模块号排序
        String strModuleTemplate = "'{'id:\"{0}\",name:\"{1} {2} {3}\"'}',";

        for (ModuleVO moduleVO : listModuleVO)
        {
            if (!listUsedClassModule.contains(moduleVO.getId()))
            {
                continue;
            }

            strModuleRows.append(MessageFormat.format(strModuleTemplate, getMappedModuleId(moduleVO),
                    moduleVO.getModuleId() == null ? "" : moduleVO.getModuleId(), moduleVO.getName(), moduleVO.getDisplayName()));

            listUsedClassModule.remove(moduleVO.getId());
        }

        if (!listAllClassVO.isEmpty())
        {
            strModuleRows.append(MessageFormat.format(strModuleTemplate, "all", "all", "", "所有表"));

            for (ClassVO classVO : listAllClassVO)
            {
                String strUrl = getClassUrl(classVO);

                strClassRows.append(MessageFormat.format(strLeafTemplate, getMappedClassId(classVO), "all",
                        classVO.getDefaultTableName().toLowerCase(), classVO.getDisplayName(), strUrl, "ddc"));
            }
        }

        FileHelper.writeFileThread(Paths.get(strOutputRootDir, "scripts", "data-dict-tree.js"),
                "var dataDictIndexData=[" + strModuleRows + strClassRows + "];");

        TimerLogger.getLogger().end("create data dict tree data: " + (listClassVO.size() + listAllClassVO.size()));
    }

    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-5-18 9:42:59
     ***************************************************************************/
    protected void createIndexFile()
    {
        TimerLogger.getLogger().begin("create index file");

        String strHtml = MessageFormat.format(DataDictCreator.settings.getProperty("HtmlIndexFile"),
                DataDictCreator.settings.get(strVersion + ".DataDictVersion"));

        FileHelper.writeFileThread(Paths.get(strOutputRootDir, "index.html"), strHtml);

        TimerLogger.getLogger().end("create index file");
    }

    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-21 15:47:35
     ***************************************************************************/
    public void doAction()
    {
        emptyTargetFolder();

        createIndexFile();

        copyHtmlFiles();

        String strModuleSQL = "select lower(id) id,lower(name) name,displayname,b.moduleid moduleid from md_module a left join dap_dapsystem b on lower(a.id)=lower(b.devmodule) order by b.moduleid";
        String strComponentSQL = "select id,name,displayname,lower(ownmodule) ownmodule from md_component";
        String strClassSQL = "select id,name,displayname,defaulttablename,fullclassname,keyattribute,componentid,classtype,isprimary from md_class order by lower(defaulttablename)";

        try
        {
            adjustData();

            List<ModuleVO> listModuleVO = (List<ModuleVO>) queryMetaVO(ModuleVO.class, strModuleSQL);
            List<ComponentVO> listComponentVO = (List<ComponentVO>) queryMetaVO(ComponentVO.class, strComponentSQL);
            List<ClassVO> listClassVO = (List<ClassVO>) queryMetaVO(ClassVO.class, strClassSQL);

            mapModuleVO = buildMap(listModuleVO);
            mapComponentVO = buildMap(listComponentVO);
            mapClassVO = buildMap(listClassVO);

            buildEnumString();

            buildClassVOMapByComponentId(listClassVO);

            List<ClassVO> listAllClass = queryAllClass();

            createDataDictTreeData(listModuleVO, listClassVO, listAllClass);

            TimerLogger.getLogger().begin("create data dict file: " + listClassVO.size());

            for (ClassVO classVO : listClassVO)
            {
                createDataDictFile(classVO);
            }

            TimerLogger.getLogger().end("create data dict file: " + listClassVO.size());

            createAllTableDataDictFile(listAllClass);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        finally
        {
            sqlExecutor.closeConnection();
        }
    }

    /***************************************************************************
     * @throws IOException
     * @author Rocex Wang
     * @version 2020-5-16 9:44:24
     ***************************************************************************/
    protected void emptyTargetFolder()
    {
        TimerLogger.getLogger().begin("empty target folder " + strOutputRootDir);

        try
        {
            FileHelper.deleteFolder(Paths.get(strOutputRootDir));
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        TimerLogger.getLogger().end("empty target folder " + strOutputRootDir);
    }

    /***************************************************************************
     * 实体文件全路径的绝对路径
     * @param classVO
     * @return Path
     * @author Rocex Wang
     * @version 2020-4-26 10:21:59
     ***************************************************************************/
    protected Path getClassFilePath(ClassVO classVO)
    {
        return Paths.get(strOutputDdcDir, getMappedClassId(classVO) + ".html");
    }

    /***************************************************************************
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-5-6 14:42:20
     ***************************************************************************/
    protected String getClassListUrl(ClassVO currentClassVO)
    {
        List<ClassVO> listClassVO = mapClassVOByComponent.get(currentClassVO.getComponentId());

        String strClassLinks = "";

        if (listClassVO == null)
        {
            return strClassLinks;
        }

        for (ClassVO classVO : listClassVO)
        {
            if (classVO.getClassType() != 201)
            {
                continue;
            }

            String strClassStyle = "";

            if (currentClassVO.getId().equals(classVO.getId()) && "Y".equals(classVO.getIsPrimary()))
            {
                strClassStyle = "classList-master-current";
            }
            else if (currentClassVO.getId().equals(classVO.getId()))
            {
                strClassStyle = "classList-current";
            }
            else if ("Y".equals(classVO.getIsPrimary()))
            {
                strClassStyle = "classList-master";
            }

            String strClassLink = MessageFormat.format("<a href=\"{0}\" class=\"{1}\">{2}</a>", getClassUrl2(classVO), strClassStyle,
                    classVO.getDisplayName());

            // 主实体放在首位
            strClassLinks = "Y".equals(classVO.getIsPrimary()) ? strClassLink + " / " + strClassLinks
                    : strClassLinks + " / " + strClassLink;
        }

        strClassLinks = strClassLinks.trim().replace("/  /", "/");

        if (strClassLinks.endsWith("/"))
        {
            strClassLinks = strClassLinks.substring(0, strClassLinks.length() - 1);
        }

        return strClassLinks;
    }

    /***************************************************************************
     * 实体的访问url，相对于根目录
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-5-2 14:26:12
     ***************************************************************************/
    protected String getClassUrl(ClassVO classVO)
    {
        return "./ddc/" + getMappedClassId(classVO) + ".html";
    }

    /***************************************************************************
     * 实体的访问url，相对于当前目录
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-5-2 14:30:53
     ***************************************************************************/
    protected String getClassUrl2(ClassVO classVO)
    {
        return "./" + getMappedClassId(classVO) + ".html";
    }

    /***************************************************************************
     * 返回数据库类型定义
     * @param propertyVO
     * @return String
     * @author Rocex Wang
     * @version 2020-4-28 10:14:34
     ***************************************************************************/
    protected String getDbType(PropertyVO propertyVO)
    {
        String strDbType = propertyVO.getSqlDateType().toLowerCase();

        if (strDbType.contains("char") || strDbType.contains("text"))
        {
            strDbType = strDbType + "(" + propertyVO.getAttrLength() + ")";
        }
        else if (strDbType.contains("decimal") || strDbType.contains("number"))
        {
            strDbType = strDbType + "(" + propertyVO.getAttrLength() + ", " + propertyVO.getPrecise() + ")";
        }

        return strDbType;
    }

    /***************************************************************************
     * 页脚版权信息
     * @return String
     * @author Rocex Wang
     * @version 2020-5-2 15:24:39
     ***************************************************************************/
    protected String getFooter()
    {
        String strFooter = DataDictCreator.settings.getProperty("HtmlDataDictFooterFile");

        strFooter = MessageFormat.format(strFooter, strCreateTime);

        return strFooter;
    }

    /***************************************************************************
     * @param classVO
     * @return mappedId
     * @author Rocex Wang
     * @version 2020-4-30 13:47:11
     ***************************************************************************/
    protected synchronized String getMappedClassId(ClassVO classVO)
    {
        return getMappedId("class_", classVO.getId());
    }

    /***************************************************************************
     * @param strType
     * @param strId
     * @return mappedId
     * @author Rocex Wang
     * @version 2020-4-30 13:47:06
     ***************************************************************************/
    protected synchronized String getMappedId(String strType, String strId)
    {
        String strKey = strType + strId;

        if (mapId.containsKey(strKey))
        {
            return mapId.get(strKey);
        }

        String strMapId = String.valueOf(iIndex++);

        mapId.put(strKey, strMapId);

        return strMapId;
    }

    /***************************************************************************
     * @param moduleVO
     * @return mappedId
     * @author Rocex Wang
     * @version 2020-4-30 13:46:54
     ***************************************************************************/
    protected synchronized String getMappedModuleId(ModuleVO moduleVO)
    {
        return getMappedId("module_", moduleVO.getId());
    }

    /***************************************************************************
     * 得到 class 对应的 module
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-4-26 10:23:41
     ***************************************************************************/
    protected String getModuleId(ClassVO classVO)
    {
        ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());

        ModuleVO moduleVO = (ModuleVO) mapModuleVO.get(componentVO.getOwnModule());

        return moduleVO == null ? componentVO.getOwnModule() : moduleVO.getId();
    }

    /***************************************************************************
     * @param classVO
     * @return ModuleVO
     * @author Rocex Wang
     * @version 2020-4-29 11:46:50
     ***************************************************************************/
    protected ModuleVO getModuleVO(ClassVO classVO)
    {
        ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());

        ModuleVO moduleVO = (ModuleVO) mapModuleVO.get(componentVO.getOwnModule());

        return moduleVO;
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
    protected String getPrimaryKeys(DatabaseMetaData dbMetaData, String strTableName, Map<String, String> mapColumn) throws Exception
    {
        String strPks = "";

        if (dbMetaData.getDatabaseProductName().toLowerCase().contains("oracle"))
        {
            if (mapTableNamePrimaryKeys.isEmpty())
            {
                TimerLogger.getLogger().begin("oracle query all primary keys");

                String strSQL = "select column_name id,table_name classid from user_cons_columns where constraint_name in(select constraint_name from user_constraints where constraint_type='P')";

                sqlExecutor.executeQuery(new BeanListProcessor<>(PropertyVO.class, null, propertyVO ->
                {

                    String strPk = mapTableNamePrimaryKeys.get(propertyVO.getClassId().toLowerCase());

                    if (strPk == null)
                    {
                        strPk = propertyVO.getId().toLowerCase();
                    }
                    else
                    {
                        strPk = strPk + ";" + propertyVO.getId().toLowerCase();
                    }

                    mapTableNamePrimaryKeys.put(propertyVO.getClassId().toLowerCase(), strPk);

                    return false;
                }), strSQL);

                TimerLogger.getLogger().end("oracle query all primary keys");
            }

            strPks = mapTableNamePrimaryKeys.get(strTableName.toLowerCase());

            return strPks == null ? "" : strPks;
        }
        else
        {
            // 找到表的主键
            ResultSet rsPkColumns = dbMetaData.getPrimaryKeys(null, strSchema, strTableName);

            List<PropertyVO> listPkPropertyVO = (List<PropertyVO>) new BeanListProcessor<>(PropertyVO.class, mapColumn, "id")
                    .doAction(rsPkColumns);

            for (PropertyVO propertyVO : listPkPropertyVO)
            {
                strPks += propertyVO.getId().toLowerCase() + ";";
            }
        }

        return strPks;
    }

    /***************************************************************************
     * @param strFieldCode
     * @return 从字段名判断是否自定义项
     * @author Rocex Wang
     * @version 2020-10-22 17:31:02
     ***************************************************************************/
    protected boolean isCustomProperty(String strFieldCode)
    {
        if (strFieldCode == null || strFieldCode.trim().length() == 0)
        {
            return false;
        }

        return strFieldCode.matches("(def)[0-9]+") || strFieldCode.matches("(vdef)[0-9]+") || strFieldCode.matches("(vfree)[0-9]+")
                || strFieldCode.matches("(hvdef)[0-9]+") || strFieldCode.matches("(free)[0-9]+") || strFieldCode.matches("(vbfree)[0-9]+")
                || strFieldCode.matches("(vbdef)[0-9]+") || strFieldCode.matches("(defitem)[0-9]+")
                || strFieldCode.matches("(vuserdef)[0-9]+") || strFieldCode.matches("(freevalue)[0-9]+")
                || strFieldCode.matches("(hdef)[0-9]+") || strFieldCode.matches("(bdef)[0-9]+") || strFieldCode.matches("(nfactor)[0-9]+")
                || strFieldCode.matches("(vcostfree)[0-9]+");
    }

    /***************************************************************************
     * 查询所有表
     * @param listClassVO
     * @author Rocex Wang
     * @version 2020-5-11 11:19:19
     * @throws Exception
     ***************************************************************************/
    protected List<ClassVO> queryAllClass() throws Exception
    {
        TimerLogger.getLogger().begin("query all table");

        ResultSet rsTable = sqlExecutor.getConnection().getMetaData().getTables(null, strSchema, "%", new String[] { "TABLE" });

        Map<String, String> mapTable = new HashMap<>();
        mapTable.put("TABLE_NAME", "DefaultTableName");

        // 排除一些表
        String strFilters[] = new String[] { "aqua_explain_", "hr_temptable", "ic_temp_", "iufo_measpub_", "iufo_measure_data_",
                "sm_securitylog_", "tb_fd_sht", "tb_tmp_tcheck", "tb_tt_", "temp000", "temppkts", "temptable_oa", "temp_", "temq_",
                "tmpbd_", "tmpub_calog_temp", "tmp_", "tm_mqsend_success_", "uidbcache_temp_", "uidbcache_temp_", "wa_temp_", "zdp_" };

        List<ClassVO> listAllClassVO = (List<ClassVO>) new BeanListProcessor<>(ClassVO.class, mapTable, classVO ->
        {
            String strDefaultTableName = classVO.getDefaultTableName().toLowerCase();

            // 表名长度小于6、不包含下划线 的都认为不是合法要生成数据字典的表
            if (strDefaultTableName.length() < 6 || !strDefaultTableName.contains("_"))
            {
                return false;
            }

            for (String strFilter : strFilters)
            {
                if (strDefaultTableName.startsWith(strFilter))
                {
                    return false;
                }
            }

            classVO.setId(strDefaultTableName);
            classVO.setName(strDefaultTableName);
            classVO.setDisplayName(strDefaultTableName);

            return true;
        }, "DefaultTableName").doAction(rsTable);

        TimerLogger.getLogger().end("query all table");

        return listAllClassVO;
    }

    /***************************************************************************
     * @param metaVOClass
     * @param strSQL
     * @return List<? extends MetaVO>
     * @author Rocex Wang
     * @version 2020-5-9 11:20:25
     ***************************************************************************/
    protected List<? extends MetaVO> queryMetaVO(Class<? extends MetaVO> metaVOClass, String strSQL)
    {
        TimerLogger.getLogger().begin("query " + metaVOClass.getSimpleName());

        List<? extends MetaVO> listMetaVO = null;

        try
        {
            listMetaVO = (List<? extends MetaVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(metaVOClass), strSQL);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        TimerLogger.getLogger().end("query " + metaVOClass.getSimpleName());

        return listMetaVO;
    }

    /***************************************************************************
     * 整理表的字段，顺序：主键列 -> 普通字段列 -> 自定义项列 -> dr、ts列
     * @param strPks
     * @param listPropertyVO
     * @author Rocex Wang
     * @version 2020-11-3 17:21:23
     ***************************************************************************/
    protected List<PropertyVO> sortPropertyVO(String strPks, List<PropertyVO> listPropertyVO)
    {
        List<PropertyVO> listPkPropertyVO = new ArrayList<>();  // 主键列
        List<PropertyVO> listNormalPropertyVO = new ArrayList<>();  // 普通字段列
        List<PropertyVO> listCustomPropertyVO = new ArrayList<>(); // 自定义项列
        List<PropertyVO> listPropertyFinalVO = new ArrayList<>();  // dr、ts列

        List<String> listPk = Arrays.asList(strPks.split(";"));

        for (PropertyVO propertyVO : listPropertyVO)
        {
            String strPropKey = propertyVO.getName();

            if (listPk.contains(strPropKey))
            {
                listPkPropertyVO.add(propertyVO);
            }
            else if ("Y".equalsIgnoreCase(propertyVO.getCustomAttr()) || isCustomProperty(strPropKey))
            {
                listCustomPropertyVO.add(propertyVO);
            }
            else if ("dr".equalsIgnoreCase(strPropKey) || "ts".equalsIgnoreCase(strPropKey))
            {
                listPropertyFinalVO.add(propertyVO);
            }
            else
            {
                listNormalPropertyVO.add(propertyVO);
            }
        }

        listPkPropertyVO.addAll(listNormalPropertyVO);
        listPkPropertyVO.addAll(listCustomPropertyVO);
        listPkPropertyVO.addAll(listPropertyFinalVO);

        return listPkPropertyVO;
    }
}
