package org.rocex.datadict;

import org.rocex.datadict.vo.*;
import org.rocex.db.param.SQLParameter;
import org.rocex.db.processor.BeanListProcessor;
import org.rocex.db.processor.SQLExecutor;
import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;
import org.rocex.utils.StringHelper;
import org.rocex.utils.TimerLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-22 14:10:00
 ***************************************************************************/
public abstract class CreateDataDictAction implements IAction
{
    protected static int iIdLength = 8; // 主键长度
    
    protected Map<String, ? extends MetaVO> mapClassVO = new HashMap<>();            // class id 和 class 的对应关系
    protected Map<String, List<ClassVO>> mapClassVOByComponent = new HashMap<>();     // component id 和 component 内所有 class 链接的对应关系
    protected Map<String, ? extends MetaVO> mapComponentVO = new HashMap<>();        // component id 和 component 的对应关系
    protected Map<String, String> mapEnumString = new HashMap<>();                    // enum id 和 enum name and value 的对应关系
    protected Map<String, String> mapId = new HashMap<>();                            // 为了减小生成的文件体积，把元数据长id和新生成的短id做个对照关系
    protected Map<String, ? extends MetaVO> mapModuleVO = new HashMap<>();           // module id 和 module 的对应关系

    protected SQLExecutor sqlExecutor = null;

    // 以下默认都是html格式数据字典的
    protected String strClassListHrefTemplate = "<a href=\"{0}\" class=\"{1}\">{2}</a>";    // 左上角实体列表链接
    protected String strCreateTime = DateFormat.getDateTimeInstance().format(new Date());

    // 自定义项字段名前缀
    protected String[] strCustomPatterns = { "def", "vdef", "vfree", "vbdef", "vsndef", "vbfree", "vbcdef", "defitem", "vpfree", "zyx", "vuserdef", "obmdef",
            "hvdef", "vbodyuserdef", "vhdef", "vgdef", "hdef", "vstbdef", "vpdef", "vbprodbatdef", "vheaduserdef", "bc_vdef", "vsdef", "vhodef", "vstdef",
            "vbatchdef", "bdef", "freevalue", "h_def", "vrcdef", "des_freedef", "src_freedef", "vprodbatdef", "factor", "free", "nfactor", "glbdef",
            "jobglbdef", "vcostfree" };

    protected String strOutputDictDir;      // 输出数据字典文件目录
    protected String strOutputRootDir;      // 输出文件根目录
    protected String strRefClassPathHrefTemplate = "<a href=\"{0}\">{1}</a>";               // 引用模型 链接模板
    protected String strTreeDataClassTemplate = "'{'id:\"{0}\",pId:\"{1}\",name:\"{2} {3}\",url:\"{4}\",target:\"{5}\"'}',";    // 左树实体 链接模板
    protected String strTreeDataModuleTemplate = "'{'id:\"{0}\",name:\"{1} {2}\"'}',";  // 左树模块 链接模板
    protected String strVersion;            // 数据字典版本

    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-26 14:52:18
     ***************************************************************************/
    public CreateDataDictAction(String strVersion)
    {
        super();

        this.strVersion = strVersion;

        strOutputRootDir = DataDictCreator.settings.getProperty(strVersion + ".OutputDir");
        strOutputDictDir = strOutputRootDir + File.separator + "dict";

        Properties dbProp = new Properties();

        // dbProp.setProperty("jdbc.url", "jdbc:sqlite:" + strOutputRootDir + File.separator +
        // "datadict.sqlite");
        dbProp.setProperty("jdbc.url", "jdbc:sqlite:C:/datadict/datadict.sqlite");
        dbProp.setProperty("jdbc.driver", "org.sqlite.JDBC");

        sqlExecutor = new SQLExecutor(dbProp);

        // 补齐正则表达式
        for (int i = 0; i < strCustomPatterns.length; i++)
        {
            strCustomPatterns[i] = "(" + strCustomPatterns[i] + ")[0-9]+";
        }
    }

    /***************************************************************************
     * component id 和 component 内所有 class 链接的对应关系，用于数据字典左上角实体列表
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
    protected void buildEnumMap()
    {
        TimerLogger.getLogger().begin("query and build enum map");

        String strEnumValueSQL = "select class_id as id,name,enum_value from md_enumvalue order by class_id,enum_sequence";

        List<EnumVO> listEnumValueVO = null;

        try
        {
            listEnumValueVO = (List<EnumVO>) sqlExecutor.executeQuery(strEnumValueSQL, new BeanListProcessor<>(EnumVO.class));
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

            strEnum = new StringBuilder(strEnum).append(enumVO.getEnumValue()).append("=").append(enumVO.getName()).append(";<br>").toString();

            mapEnumString.put(enumVO.getId(), strEnum);
        }

        TimerLogger.getLogger().end("query and build enum map");
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
    protected void copyStaticHtmlFiles()
    {
        TimerLogger.getLogger().begin("copy html files");

        try
        {
            FileHelper.copyFolder(Paths.get("settings", DataDictCreator.settings.getProperty("createType", "json"), "static"), Paths.get(strOutputRootDir),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        TimerLogger.getLogger().end("copy html files");
    }

    /***************************************************************************
     * 生成数据字典文件
     * @param classVO
     * @author Rocex Wang
     * @version 2020-4-26 10:19:45
     ***************************************************************************/
    protected void createDataDictFile(ClassVO classVO)
    {
        if (classVO.getClassType() != 201 && classVO.getClassType() != 999)
        {
            return;
        }

        String strPropertySQL = "select distinct a.id id,a.id originalId,a.name name,a.displayname displayname,attrlength,attrminvalue"
                + ",attrmaxvalue,attrsequence,customattr,datatype,datatypestyle,a.defaultvalue defaultvalue"
                + ",a.nullable nullable,a.precise precise,refmodelname,classid,b.sqldatetype data_type_sql,b.pkey"
                + " from md_property a left join md_column b on a.name=b.name where classid=? and b.tableid=? order by b.pkey desc,a.attrsequence";

        strPropertySQL = sqlExecutor.getSQLSelect(PropertyVO.class) + " where class_id=? order by key_prop desc,attr_sequence";

        // 取实体所有属性
        SQLParameter para = new SQLParameter();
        para.addParam(classVO.getId());
        // para.addParam(classVO.getDefaultTableName());

        List<PropertyVO> listPropertyVO = null;

        try
        {
            listPropertyVO = (List<PropertyVO>) sqlExecutor.executeQuery(strPropertySQL, para, new BeanListProcessor<>(PropertyVO.class));
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
        if (listPropertyVO == null || listPropertyVO.isEmpty())
        {
            return;
        }

        listPropertyVO = sortPropertyVO(classVO, listPropertyVO);

        classVO.setPropertyVO(listPropertyVO);

        classVO.setClassListUrl(getClassListUrl(classVO));

        for (int i = 0; i < listPropertyVO.size(); i++)
        {
            PropertyVO propertyVO = listPropertyVO.get(i);

            // 引用实体模型
            propertyVO.setRefClassPathHref(getRefClassPathHref(propertyVO));

            // 枚举/取值范围
            propertyVO.setDataScope(getDataScope(propertyVO));

            // 默认值
            propertyVO.setDefaultValue(propertyVO.getDefaultValue() == null ? "" : propertyVO.getDefaultValue());

            createDataDictFileRow(classVO, propertyVO, i);
        }
    }

    /***************************************************************************
     * @param classVO
     * @param propertyVO
     * @param iRowIndex
     * @author Rocex Wang
     * @since 2021-10-25 10:43:57
     ***************************************************************************/
    protected void createDataDictFileRow(ClassVO classVO, PropertyVO propertyVO, int iRowIndex)
    {
    }

    /***************************************************************************
     * 生成构造实体树的json数据
     * @param listModuleVO
     * @param listClassVO
     * @param listAllTableVO
     * @author Rocex Wang
     * @version 2020-4-29 11:21:07
     ***************************************************************************/
    protected void createDataDictTree(List<ModuleVO> listModuleVO, List<ClassVO> listClassVO, List<ClassVO> listAllTableVO)
    {
        TimerLogger.getLogger().begin("create data dict tree data: " + (listClassVO.size() + listAllTableVO.size()));

        StringBuilder strModuleRows = new StringBuilder(); // 所有模块
        StringBuilder strClassRows = new StringBuilder();  // 所有实体

        List<String> listUsedClassModule = new ArrayList<>();// 只生成含有实体的Module

        for (ClassVO classVO : listClassVO)
        {
            if (classVO.getClassType() != 201)
            {
                continue;
            }

            ModuleVO moduleVO = getModuleVO(classVO);

            String strUrl = getClassUrl(classVO);

            strClassRows.append(MessageFormat.format(strTreeDataClassTemplate, getMappedClassId(classVO), getMappedModuleId(moduleVO),
                    classVO.getDefaultTableName(), classVO.getDisplayName(), strUrl, "ddc"));

            String strModuleId = getModuleId(classVO);

            if (!listUsedClassModule.contains(strModuleId))
            {
                listUsedClassModule.add(strModuleId);
            }
        }

        for (ModuleVO moduleVO : listModuleVO)
        {
            if (!listUsedClassModule.contains(moduleVO.getId()))
            {
                continue;
            }

            strModuleRows.append(MessageFormat.format(strTreeDataModuleTemplate, getMappedModuleId(moduleVO), moduleVO.getName(), moduleVO.getDisplayName()));

            listUsedClassModule.remove(moduleVO.getId());
        }

        // 所有表都按字母顺序挂在一个节点下，不再分级
        if (!listAllTableVO.isEmpty())
        {
            strModuleRows.append(MessageFormat.format(strTreeDataModuleTemplate, "all", "all", "所有表"));

            char[] chars = new char[26];

            for (ClassVO classVO : listAllTableVO)
            {
                String strUrl = getClassUrl(classVO);

                String strTableName = classVO.getDefaultTableName().toLowerCase();

                char char0 = strTableName.charAt(0);

                chars[char0 - 'a'] = char0;

                strClassRows.append(MessageFormat.format(strTreeDataClassTemplate, getMappedClassId(classVO), "char_" + char0, strTableName,
                        classVO.getDisplayName(), strUrl, "ddc"));
            }

            for (int i = 0; i < chars.length; i++)
            {
                char charAt = chars[i];

                if (charAt == 0)
                {
                    continue;
                }

                String strDisplay = new String(new char[] { charAt, charAt, charAt }).toUpperCase();

                strModuleRows.append(MessageFormat.format("'{'id:\"{0}\",pId:\"{1}\",name:\"{2} {3}\"'}',", "char_" + charAt, "all", strDisplay, strDisplay));
            }
        }

        FileHelper.writeFileThread(Paths.get(strOutputRootDir, "scripts", "data-dict-tree.js"),
                "var dataDictIndexData=[" + strModuleRows + strClassRows + "];");

        TimerLogger.getLogger().end("create data dict tree data: " + (listClassVO.size() + listAllTableVO.size()));
    }

    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-5-18 9:42:59
     ***************************************************************************/
    protected void createIndexHtmlFile()
    {
        TimerLogger.getLogger().begin("create index html file");

        String strHtml = MessageFormat.format(DataDictCreator.settings.getProperty("HtmlIndexFile"),
                DataDictCreator.settings.get(strVersion + ".DataDictVersion"), strCreateTime);

        FileHelper.writeFileThread(Paths.get(strOutputRootDir, "index.html"), strHtml);

        TimerLogger.getLogger().end("create index html file");
    }

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datadict.IAction#doAction(EventObject)
     * @author Rocex Wang
     * @since 2021-10-14 18:39:54
     ****************************************************************************/
    @Override
    public void doAction(EventObject evt)
    {
        emptyTargetDir();

        createIndexHtmlFile();

        copyStaticHtmlFiles();

        String strModuleSQL = "select id,display_name,name,parent_module_id from md_module order by lower(name)";
        String strComponentSQL = "select original_id as id,display_name,name,own_module from md_component";
        String strClassSQL = "select id,class_type,component_id,default_table_name,display_name,full_classname,help,is_primary,key_attribute,name from md_class where class_type<>999 order by default_table_name";
        String strClassSQL2 = "select id,class_type,component_id,default_table_name,display_name,full_classname,help,is_primary,key_attribute,name from md_class where class_type=999 order by default_table_name";

        try
        {
            List<ModuleVO> listModuleVO = (List<ModuleVO>) queryMetaVO(ModuleVO.class, strModuleSQL, null, null);
            List<ComponentVO> listComponentVO = (List<ComponentVO>) queryMetaVO(ComponentVO.class, strComponentSQL, null, null);
            List<ClassVO> listClassVO = (List<ClassVO>) queryMetaVO(ClassVO.class, strClassSQL, null, null);

            mapModuleVO = buildMap(listModuleVO);
            mapComponentVO = buildMap(listComponentVO);
            mapClassVO = buildMap(listClassVO);

            buildEnumMap();

            buildClassVOMapByComponentId(listClassVO);

            List<ClassVO> listAllTableVO = (List<ClassVO>) queryMetaVO(ClassVO.class, strClassSQL2, null, null);

            createDataDictTree(listModuleVO, listClassVO, listAllTableVO);

            TimerLogger.getLogger().begin("create data dict file: " + listClassVO.size());

            for (ClassVO classVO : listClassVO)
            {
                createDataDictFile(classVO);
            }

            TimerLogger.getLogger().end("create data dict file: " + listClassVO.size());

            TimerLogger.getLogger().begin("create all table data dict file: " + listAllTableVO.size());

            for (ClassVO classVO : listAllTableVO)
            {
                createDataDictFile(classVO);
            }

            TimerLogger.getLogger().end("create all table data dict file: " + listAllTableVO.size());
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
    protected void emptyTargetDir()
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
    protected abstract Path getClassFilePath(ClassVO classVO);

    /***************************************************************************
     * @param currentClassVO
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

            String strClassLink = MessageFormat.format(strClassListHrefTemplate, getClassUrl2(classVO), strClassStyle, classVO.getDisplayName());

            // 主实体放在首位
            strClassLinks = "Y".equals(classVO.getIsPrimary()) ? strClassLink + " / " + strClassLinks : strClassLinks + " / " + strClassLink;
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
     * 枚举/取值范围
     * @param propertyVO
     * @return
     * @author Rocex Wang
     * @since 2021-10-25 10:52:50
     ***************************************************************************/
    protected String getDataScope(PropertyVO propertyVO)
    {
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

        return strDataScope;
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

        String strMapId = StringHelper.getId();

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
     * @param propertyVO
     * @return 引用实体模型
     * @author Rocex Wang
     * @since 2021-10-25 10:54:10
     ***************************************************************************/
    protected String getRefClassPathHref(PropertyVO propertyVO)
    {
        String strRefClassPathHref = "";
        ClassVO refClassVO = (ClassVO) mapClassVO.get(propertyVO.getDataType());

        if (refClassVO != null)
        {
            strRefClassPathHref = refClassVO.getDisplayName();

            if (refClassVO.getClassType() == 201)
            {
                String strRefClassPath = getClassUrl2(refClassVO);

                strRefClassPathHref = MessageFormat.format(strRefClassPathHrefTemplate, strRefClassPath, refClassVO.getDisplayName());
            }

            strRefClassPathHref = strRefClassPathHref + " (" + refClassVO.getName() + ")";
        }

        return strRefClassPathHref;
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

        for (String strCustomPattern : strCustomPatterns)
        {
            if (strFieldCode.matches(strCustomPattern))
            {
                return true;
            }
        }

        return false;
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

            listMetaVO = (List<? extends MetaVO>) sqlExecutor.executeQuery(strSQL, param, processor);
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
     * @param classVO
     * @param listPropertyVO
     * @author Rocex Wang
     * @version 2020-11-3 17:21:23
     ***************************************************************************/
    protected List<PropertyVO> sortPropertyVO(ClassVO classVO, List<PropertyVO> listPropertyVO)
    {
        List<PropertyVO> listPkPropertyVO = new ArrayList<>();  // 主键列
        List<PropertyVO> listNormalPropertyVO = new ArrayList<>();  // 普通字段列
        List<PropertyVO> listCustomPropertyVO = new ArrayList<>(); // 自定义项列
        List<PropertyVO> listPropertyFinalVO = new ArrayList<>();  // dr、ts列

        List<String> listPk = Arrays.asList(classVO.getKeyAttribute().split(";"));

        for (PropertyVO propertyVO : listPropertyVO)
        {
            String strPropKey = propertyVO.getName();

            if (listPk.contains(propertyVO.getOriginalId()))
            {
                propertyVO.setKeyProp(true);
                listPkPropertyVO.add(propertyVO);
            }
            else if ("Y".equalsIgnoreCase(propertyVO.getCustomAttr()) || isCustomProperty(strPropKey))
            {
                listCustomPropertyVO.add(propertyVO);
            }
            else if ("dr".equalsIgnoreCase(strPropKey) || "ts".equalsIgnoreCase(strPropKey) || "creator".equalsIgnoreCase(strPropKey)
                    || "creationtime".equalsIgnoreCase(strPropKey) || "modifier".equalsIgnoreCase(strPropKey) || "modifiedtime".equalsIgnoreCase(strPropKey))
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
