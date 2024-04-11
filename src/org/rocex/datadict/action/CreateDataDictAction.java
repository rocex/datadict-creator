package org.rocex.datadict.action;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.ComponentVO;
import org.rocex.datadict.vo.Context;
import org.rocex.datadict.vo.DictJsonVO;
import org.rocex.datadict.vo.EnumValueVO;
import org.rocex.datadict.vo.MetaVO;
import org.rocex.datadict.vo.ModuleVO;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.db.SQLExecutor;
import org.rocex.db.param.SQLParameter;
import org.rocex.db.processor.BeanListProcessor;
import org.rocex.utils.FileHelper;
import org.rocex.utils.JacksonHelper;
import org.rocex.utils.Logger;
import org.rocex.utils.ResHelper;
import org.rocex.utils.StringHelper;
import org.rocex.vo.IAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/***************************************************************************
 * 生成数据字典类，支持json和html格式，默认json格式<br>
 * @author Rocex Wang
 * @since 2020-4-22 14:10:00
 ***************************************************************************/
public class CreateDataDictAction implements IAction
{
    protected Boolean isBIP = true;

    protected Map<String, ? extends MetaVO> mapClassVO = new HashMap<>();            // class id 和 class 的对应关系
    protected Map<String, List<ClassVO>> mapClassVOByComponent = new HashMap<>();    // component id 和 component 内所有 class 链接的对应关系
    protected Map<String, String> mapComponentIdPrimaryClassId = new HashMap<>();    // component id 和 主实体 id 的对应关系
    protected Map<String, ? extends MetaVO> mapComponentVO = new HashMap<>();        // component id 和 component 的对应关系
    protected Map<String, String> mapEnumString = new HashMap<>();                   // enum id 和 enum name and value 的对应关系
    protected Map<String, String> mapId = new HashMap<>();                           // 为了减小生成的文件体积，把元数据长id和新生成的短id做个对照关系
    protected Map<String, ? extends MetaVO> mapModuleVO = new HashMap<>();           // module id 和 module 的对应关系

    protected SQLExecutor sqlExecutor;

    // 以下默认都是html格式数据字典的
    protected String strClassListHrefTemplate = "<a href=\"javascript:void(0);\" onClick=loadDataDict(\"{0}\"); class=\"{1}\">{2}</a>";    // 左上角实体列表链接
    protected String strCreateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    // 自定义项字段名前缀
    protected String[] strCustomPatterns = {"def", "vdef", "vfree", "vbdef", "vsndef", "vbfree", "vbcdef", "defitem", "vpfree", "zyx", "vuserdef", "obmdef", "hvdef",
        "vbodyuserdef", "vhdef", "vgdef", "hdef", "vstbdef", "vpdef", "vbprodbatdef", "vheaduserdef", "bc_vdef", "vsdef", "vhodef", "vstdef", "vbatchdef", "bdef", "freevalue",
        "h_def", "vrcdef", "des_freedef", "src_freedef", "vprodbatdef", "factor", "free", "nfactor", "glbdef", "jobglbdef", "vcostfree"};

    protected String strOutputDictDir;      // 输出数据字典文件目录
    protected String strOutputRootDir;      // 输出文件根目录
    protected String strPropertySQL;
    protected String strTreeDataClassTemplate = "'{'id:\"{0}\",pId:\"{1}\",name:\"{2} {3}\",path:\"{4}\"'}',";           // 左树实体 链接实体
    protected String strTreeDataModuleTemplate = "'{'id:\"{0}\",name:\"{1} {2}\",isDdcClass:false,path:\"{4}\"'}',";     // 左树模块 链接模板

    protected String strVersion;            // 数据字典版本

    /***************************************************************************
     * @author Rocex Wang
     * @since 2020-4-26 14:52:18
     ***************************************************************************/
    public CreateDataDictAction(String strVersion)
    {
        super();

        this.strVersion = strVersion;

        isBIP = Boolean.valueOf(Context.getInstance().getSetting(strVersion + ".isBIP", Context.getInstance().getSetting("isBIP")));

        strOutputRootDir = Context.getInstance().getSetting(strVersion + ".OutputDir");
        strOutputDictDir = Path.of(strOutputRootDir, "dict").toString();

        Properties dbProp = new Properties();

        String strTargetUrl = Context.getInstance().getSetting(strVersion + ".target.jdbc.url", Context.getInstance().getSetting("target.jdbc.url"));
        String strTargetUser = Context.getInstance().getSetting(strVersion + ".target.jdbc.user", Context.getInstance().getSetting("target.jdbc.user"));
        String strTargetDriver = Context.getInstance().getSetting(strVersion + ".target.jdbc.driver", Context.getInstance().getSetting("target.jdbc.driver"));
        String strTargetPassword = Context.getInstance().getSetting(strVersion + ".target.jdbc.password", Context.getInstance().getSetting("target.jdbc.password"));

        dbProp.setProperty("jdbc.url", strTargetUrl);
        dbProp.setProperty("jdbc.user", strTargetUser);
        dbProp.setProperty("jdbc.driver", strTargetDriver);
        dbProp.setProperty("jdbc.password", strTargetPassword);

        sqlExecutor = new SQLExecutor(dbProp);

        strPropertySQL = sqlExecutor.getSQLSelect(PropertyVO.class) + " where class_id=? and ddc_version=? order by key_prop desc,attr_sequence";

        // 补齐正则表达式
        for (int i = 0; i < strCustomPatterns.length; i++)
        {
            strCustomPatterns[i] = "(" + strCustomPatterns[i] + ")[0-9]+";
        }
    }

    /***************************************************************************
     * component id 和 component 内所有 class 链接的对应关系，用于数据字典左上角实体列表
     * @param listClassVO List<ClassVO>
     * @author Rocex Wang
     * @since 2020-4-24 15:41:49
     ***************************************************************************/
    protected void buildClassVOMapByComponentId(List<ClassVO> listClassVO)
    {
        Logger.getLogger().begin("build ClassVO map by componentId");

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

            if (classVO.isPrimaryClass())
            {
                listClassVO2.add(0, classVO);

                mapComponentIdPrimaryClassId.put(classVO.getComponentId(), classVO.getId());
            }
            else
            {
                listClassVO2.add(classVO);
            }

            mapClassVOByComponent.put(classVO.getComponentId(), listClassVO2);
        }

        Logger.getLogger().end("build ClassVO map by componentId");
    }

    /***************************************************************************
     * enum id 和 enum name and value 的对应关系
     * @author Rocex Wang
     * @since 2020-4-26 10:18:30
     ***************************************************************************/
    protected void buildEnumMap()
    {
        Logger.getLogger().begin("query and build enum map");

        String strEnumValueSQL = "select class_id as id,name,enum_value,ddc_version from md_enumvalue where ddc_version=? order by class_id,enum_sequence";

        List<EnumValueVO> listEnumValueVO = null;

        try
        {
            SQLParameter param = new SQLParameter();
            param.addParam(strVersion);

            listEnumValueVO = (List<EnumValueVO>) sqlExecutor.executeQuery(strEnumValueSQL, param, new BeanListProcessor<>(EnumValueVO.class));
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        if (listEnumValueVO == null || listEnumValueVO.isEmpty())
        {
            return;
        }

        for (EnumValueVO enumValueVO : listEnumValueVO)
        {
            String strEnum = mapEnumString.get(enumValueVO.getId());

            if (strEnum == null)
            {
                strEnum = "";
            }

            strEnum = strEnum + enumValueVO.getEnumValue() + "=" + enumValueVO.getName() + ";<br>";

            mapEnumString.put(enumValueVO.getId(), strEnum);
        }

        Logger.getLogger().end("query and build enum map");
    }

    /***************************************************************************
     * 建立 metaId 和 metaVO 的关系
     * @param listMetaVO
     * @return Map
     * @author Rocex Wang
     * @since 2020-4-26 10:18:51
     ***************************************************************************/
    protected Map<String, ? extends MetaVO> buildMap(List<? extends MetaVO> listMetaVO)
    {
        if (listMetaVO == null || listMetaVO.isEmpty())
            return new HashMap<String, MetaVO>();

        Logger.getLogger().begin("build map: " + listMetaVO.get(0).getClass().getSimpleName());

        Map<String, MetaVO> mapMetaVO = listMetaVO.stream().collect(Collectors.toMap(MetaVO::getId, Function.identity(), (key1, key2) -> key2));

        Logger.getLogger().end("build map: " + listMetaVO.get(0).getClass().getSimpleName());

        return mapMetaVO;
    }

    /***************************************************************************
     * 拷贝静态文件、css、js 等
     * @author Rocex Wang
     * @since 2020-4-29 10:33:18
     ***************************************************************************/
    protected void copyStaticHtmlFiles()
    {
        Logger.getLogger().begin("copy html files");

        try
        {
            FileHelper.copyFolderThread(Path.of("data", "template", "static"), Path.of(strOutputRootDir), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("copy html files");
    }

    /***************************************************************************
     * 生成数据字典文件
     * @param classVO
     * @author Rocex Wang
     * @since 2020-4-26 10:19:45
     ***************************************************************************/
    protected void createDataDictFile(ClassVO classVO)
    {
        if (classVO.getClassType() != 201 && classVO.getClassType() != 999)
        {
            return;
        }

        // 取实体所有属性
        SQLParameter para = new SQLParameter();
        para.addParam(classVO.getId());
        para.addParam(strVersion);

        List<PropertyVO> listPropertyVO = null;

        try
        {
            listPropertyVO = (List<PropertyVO>) sqlExecutor.executeQuery(strPropertySQL, para, new BeanListProcessor<>(PropertyVO.class));
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

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

            // 枚举/取值范围
            propertyVO.setDataScope(getDataScope(propertyVO));

            // 默认值
            propertyVO.setDefaultValue(propertyVO.getDefaultValue() == null ? "" : propertyVO.getDefaultValue());

            // 引用实体模型，这个放到最后，因为会改变dataType的值
            ClassVO refClassVO = (ClassVO) mapClassVO.get(propertyVO.getDataType());

            if (refClassVO != null)
            {
                propertyVO.setDataTypeName(refClassVO.getName());
                propertyVO.setDataType(getMappedClassId(refClassVO));
                propertyVO.setDataTypeDisplayName(refClassVO.getDisplayName());
            }
        }

        writeDataDictFile(classVO);
    }

    /***************************************************************************
     * 生成数据字典文件
     * @param listClassVO
     * @author Rocex Wang
     * @since 2024-04-07 10:53:10
     ***************************************************************************/
    protected void createDataDictFiles(List<ClassVO> listClassVO)
    {
        if (listClassVO == null || listClassVO.isEmpty())
        {
            return;
        }

        Logger.getLogger().begin("create data dict file: " + listClassVO.size());

        int[] iCount = {0, listClassVO.size()};

        // ExecutorService executorService = Executors.newFixedThreadPool(ResHelper.getThreadCount());
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        listClassVO.forEach(classVO ->
        {
            executorService.execute(() ->
            {
                createDataDictFile(classVO);

                Logger.getLogger().log2(Logger.iLoggerLevelDebug, ++iCount[0] + "/" + iCount[1]);
            });
        });

        executorService.shutdown();

        while (!executorService.isTerminated())
        {
            ResHelper.sleep(100);
        }

        Logger.getLogger().log2(Logger.iLoggerLevelDebug, (iCount[0]) + "/" + iCount[1] + "  done!\n");

        Logger.getLogger().end("create data dict file: " + listClassVO.size());
    }

    /***************************************************************************
     * 生成构造实体树的json数据
     * @param listModuleVO
     * @param listClassVO
     * @param listAllTableVO
     * @author Rocex Wang
     * @since 2020-4-29 11:21:07
     ***************************************************************************/
    protected void createDataDictTree(List<ModuleVO> listModuleVO, List<ComponentVO> listComponentVO, List<ClassVO> listClassVO, List<ClassVO> listAllTableVO)
    {
        Logger.getLogger().begin("create data dict tree: " + (listClassVO.size() + listAllTableVO.size()));

        StringBuilder strModuleRows = new StringBuilder(); // 所有模块
        StringBuilder strComponentRows = new StringBuilder(); // 所有组件
        StringBuilder strClassRows = new StringBuilder();  // 所有实体

        List<String> listClassUsedModule = new ArrayList<>();// 只生成含有实体的Module
        List<String> listClassUsedComponent = new ArrayList<>();// 只生成含有实体的Component

        for (ClassVO classVO : listClassVO)
        {
            if (classVO.getClassType() != 201)
            {
                continue;
            }

            ModuleVO moduleVO = getModuleVO(classVO);

            String strUrl = getClassUrl(classVO);
            String strClassname = classVO.getFullClassname();
            strClassname = strClassname.contains(".") ? strClassname.substring(strClassname.lastIndexOf(".") + 1) : strClassname;

            if (classVO.isPrimaryClass())
            {
                boolean blHasChildren = mapClassVOByComponent.get(classVO.getComponentId()).size() > 1;

                String strDdc = MessageFormat.format(strTreeDataClassTemplate, getMappedClassId(classVO), getMappedModuleId(moduleVO), classVO.getDefaultTableName(),
                    classVO.getDisplayName() + " " + strClassname, strUrl, "ddc");

                if (blHasChildren)
                {
                    strClassRows.insert(0, strDdc);
                }
                else
                {
                    strClassRows.append(strDdc);
                }
            }
            else
            {
                String strPrimaryClassId = mapComponentIdPrimaryClassId.get(classVO.getComponentId());
                ClassVO primaryClassVO = strPrimaryClassId == null ? null : (ClassVO) mapClassVO.get(strPrimaryClassId);
                String strPid = primaryClassVO == null ? getMappedModuleId(moduleVO) : getMappedClassId(primaryClassVO);

                strClassRows.append(
                    MessageFormat.format(strTreeDataClassTemplate, getMappedClassId(classVO), strPid, classVO.getDefaultTableName(), classVO.getDisplayName() + " " + strClassname,
                        strUrl, "ddc"));
            }

            String strModuleId = getModuleId(classVO);

            if (!listClassUsedModule.contains(strModuleId))
            {
                listClassUsedModule.add(strModuleId);
            }
        }

        for (ModuleVO moduleVO : listModuleVO)
        {
            if (!listClassUsedModule.contains(moduleVO.getId()))
            {
                continue;
            }

            strModuleRows.append(MessageFormat.format(strTreeDataModuleTemplate, getMappedModuleId(moduleVO), moduleVO.getName(), moduleVO.getDisplayName()));

            listClassUsedModule.remove(moduleVO.getId());
        }

        listClassUsedModule.clear();

        // 所有表都按字母顺序挂在一个节点下，不再分级
        if (!listAllTableVO.isEmpty())
        {
            strModuleRows.append(MessageFormat.format(strTreeDataModuleTemplate, ModuleVO.strDBTablesRootId, "Tables", "数据库字典"));

            for (ClassVO classVO : listAllTableVO)
            {
                String strUrl = getClassUrl(classVO);

                ModuleVO moduleVO = getModuleVO(classVO);

                String strTableName = classVO.getDefaultTableName().toLowerCase();

                char char0 = strTableName.charAt(0);

                if (moduleVO == null)
                {
                    String string0 = String.valueOf((char0 >= 'a' && char0 <= 'z') ? (char0 - 'a') : '0');

                    moduleVO = new ModuleVO();
                    moduleVO.setId("char_" + string0);
                    moduleVO.setName(string0);
                    moduleVO.setParentModuleId(ModuleVO.strDBTablesRootId);
                    moduleVO.setDisplayName((char0 >= 'a' && char0 <= 'z') ? (string0.toUpperCase() + " 开头") : "其它");
                }

                moduleVO.setPath(ModuleVO.strDBTablesRootId + "," + moduleVO.getId());

                ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());
                if (!listClassUsedComponent.contains(componentVO.getId()))
                {
                    listClassUsedComponent.add(componentVO.getId());
                }

                String strModuleId = moduleVO.getId();

                if (!listClassUsedModule.contains(strModuleId))
                {
                    listClassUsedModule.add(strModuleId);
                }

                strClassRows.append(
                    MessageFormat.format(strTreeDataClassTemplate, classVO.getId(), classVO.getComponentId(), strTableName, Objects.toString(classVO.getDisplayName(), ""),
                        ModuleVO.strDBTablesRootId + "," + moduleVO.getId() + "," + componentVO.getId() + "," + classVO.getId()));
            }

            for (ModuleVO moduleVO : listModuleVO)
            {
                if (!listClassUsedModule.contains(moduleVO.getId()))
                {
                    continue;
                }

                strModuleRows.append(
                    MessageFormat.format("'{'id:\"{0}\",pId:\"{1}\",name:\"{2} {3}\",isDdcClass:false,path:\"{4}\"'}',", moduleVO.getId(), moduleVO.getParentModuleId(),
                        moduleVO.getName(), Objects.toString(moduleVO.getDisplayName(), ""), ModuleVO.strDBTablesRootId + "," + moduleVO.getId()));

                listClassUsedModule.remove(moduleVO.getId());
            }

            listClassUsedModule.clear();

            for (ComponentVO componentVO : listComponentVO)
            {
                if (!listClassUsedComponent.contains(componentVO.getId()))
                {
                    continue;
                }

                strComponentRows.append(
                    MessageFormat.format("'{'id:\"{0}\",pId:\"{1}\",name:\"{2} {3}\",isDdcClass:false,path:\"{4}\"'}',", componentVO.getId(), componentVO.getOwnModule(),
                        componentVO.getName(), Objects.toString(componentVO.getDisplayName(), ""),
                        ModuleVO.strDBTablesRootId + "," + componentVO.getOwnModule() + "," + componentVO.getId()));
            }
        }

        FileHelper.writeFileThread(Path.of(strOutputRootDir, "scripts", "data-dict-tree.js"), "var dataDictIndexData=[" + strModuleRows + strComponentRows + strClassRows + "];");

        Logger.getLogger().end("create data dict tree: " + (listClassVO.size() + listAllTableVO.size()));
    }

    /***************************************************************************
     * @author Rocex Wang
     * @since 2020-5-18 9:42:59
     ***************************************************************************/
    protected void createIndexHtmlFile()
    {
        Logger.getLogger().begin("create index html file");

        try
        {
            String strHtmlIndexFile = Files.readString(Path.of("data", "template", "index.html"));

            String strHtml = MessageFormat.format(strHtmlIndexFile, Context.getInstance().getSetting(strVersion + ".DataDictVersion"), strVersion, strCreateTime);

            FileHelper.writeFileThread(Path.of(strOutputRootDir, "index.html"), strHtml);
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("create index html file");
    }

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.vo.IAction#doAction(EventObject)
     * @author Rocex Wang
     * @since 2021-10-14 18:39:54
     ****************************************************************************/
    @Override
    public void doAction(EventObject evt)
    {
        emptyTargetDir();

        createIndexHtmlFile();

        copyStaticHtmlFiles();

        String strVersionSQL = "ddc_version='" + strVersion + "'";

        String strModuleSQL = sqlExecutor.getSQLSelect(ModuleVO.class) + " where " + strVersionSQL + " and name in(select own_module from md_component where " + strVersionSQL +
            " and id in(select component_id from md_class where " + strVersionSQL + " and component_id is not null)) order by lower(display_name)";
        String strComponentSQL = sqlExecutor.getSQLSelect(ComponentVO.class) + " where " + strVersionSQL + " and id in(select component_id from md_class where " + strVersionSQL +
            " and component_id is not null)";
        String strClassSQL1 = sqlExecutor.getSQLSelect(ClassVO.class) + " where " + strVersionSQL +
            " and component_id is not null and class_type<>999 order by primary_class desc,default_table_name";
        String strClassSQL2 = sqlExecutor.getSQLSelect(ClassVO.class) + " where " + strVersionSQL + " and component_id is not null and class_type=999 order by default_table_name";

        List<ModuleVO> listModuleVO = (List<ModuleVO>) queryMetaVO(ModuleVO.class, strModuleSQL, null, null);
        List<ComponentVO> listComponentVO = (List<ComponentVO>) queryMetaVO(ComponentVO.class, strComponentSQL, null, null);
        List<ClassVO> listClassVO = (List<ClassVO>) queryMetaVO(ClassVO.class, strClassSQL1, null, null);

        mapModuleVO = buildMap(listModuleVO);
        mapComponentVO = buildMap(listComponentVO);
        mapClassVO = buildMap(listClassVO);

        buildEnumMap();

        buildClassVOMapByComponentId(listClassVO);

        List<ClassVO> listAllTableVO = (List<ClassVO>) queryMetaVO(ClassVO.class, strClassSQL2, null, null);

        createDataDictTree(listModuleVO, listComponentVO, listClassVO, listAllTableVO);

        createDataDictFiles(listClassVO);
        createDataDictFiles(listAllTableVO);

        Logger.getLogger().begin("save data dict json file to db: " + (listClassVO.size() + listAllTableVO.size()));

        saveToDictJson(listClassVO);
        saveToDictJson(listAllTableVO);

        Logger.getLogger().end("save data dict json file to db: " + (listClassVO.size() + listAllTableVO.size()));
    }

    /***************************************************************************
     * @author Rocex Wang
     * @since 2020-5-16 9:44:24
     ***************************************************************************/
    protected void emptyTargetDir()
    {
        Logger.getLogger().begin("empty target folder " + strOutputRootDir);

        try
        {
            FileHelper.deleteFolder(Path.of(strOutputRootDir));

            sqlExecutor.executeUpdate("delete from ddc_dict_json where ddc_version=?", new SQLParameter().addParam(strVersion));
        }
        catch (IOException | SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("empty target folder " + strOutputRootDir);
    }

    /***************************************************************************
     * 实体文件全路径的绝对路径
     * @param classVO
     * @return Path
     * @author Rocex Wang
     * @since 2020-4-26 10:21:59
     ***************************************************************************/
    protected Path getClassFilePath(ClassVO classVO)
    {
        return Path.of(strOutputDictDir, getMappedClassId(classVO) + ".json");
    }

    /***************************************************************************
     * @param currentClassVO
     * @return String
     * @author Rocex Wang
     * @since 2020-5-6 14:42:20
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

            if (currentClassVO.getId().equals(classVO.getId()) && classVO.isPrimaryClass())
            {
                strClassStyle = "classList-master-current";
            }
            else if (currentClassVO.getId().equals(classVO.getId()))
            {
                strClassStyle = "classList-current";
            }
            else if (classVO.isPrimaryClass())
            {
                strClassStyle = "classList-master";
            }

            String strClassLink = MessageFormat.format(strClassListHrefTemplate, getClassUrl2(classVO), strClassStyle, classVO.getDisplayName());

            // 主实体放在首位
            strClassLinks = classVO.isPrimaryClass() ? strClassLink + " / " + strClassLinks : strClassLinks + " / " + strClassLink;
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
     * @since 2020-5-2 14:26:12
     ***************************************************************************/
    protected String getClassUrl(ClassVO classVO)
    {
        return getMappedClassId(classVO);
    }

    /***************************************************************************
     * 实体的访问url，相对于当前目录
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @since 2020-5-2 14:30:53
     ***************************************************************************/
    protected String getClassUrl2(ClassVO classVO)
    {
        return getMappedClassId(classVO);
    }

    /***************************************************************************
     * 枚举/取值范围
     * @param propertyVO
     * @return String
     * @author Rocex Wang
     * @since 2021-10-25 10:52:50
     ***************************************************************************/
    protected String getDataScope(PropertyVO propertyVO)
    {
        String strDataScope;

        if (propertyVO.getDataType().length() > 20 && propertyVO.getRefModelName() == null && propertyVO.getDataTypeStyle() == 300 &&
            mapEnumString.containsKey(propertyVO.getDataType()))
        {
            strDataScope = mapEnumString.get(propertyVO.getDataType());
        }
        else
        {
            strDataScope = "[" + StringHelper.getIfEmpty(propertyVO.getAttrMinValue(), "") + " , " + StringHelper.getIfEmpty(propertyVO.getAttrMaxValue(), "") + "]";

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
     * @since 2020-4-30 13:47:11
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
     * @since 2020-4-30 13:47:06
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
     * @since 2020-4-30 13:46:54
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
     * @since 2020-4-26 10:23:41
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
     * @since 2020-4-29 11:46:50
     ***************************************************************************/
    protected ModuleVO getModuleVO(ClassVO classVO)
    {
        ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());

        ModuleVO moduleVO = (ModuleVO) mapModuleVO.get(componentVO.getOwnModule());

        if (moduleVO == null)
        {
            moduleVO = new ModuleVO();
            moduleVO.setId(componentVO.getOwnModule());
            moduleVO.setName(componentVO.getOwnModule());
            moduleVO.setDisplayName(componentVO.getOwnModule());
        }

        return moduleVO;
    }

    /***************************************************************************
     * @param strFieldCode
     * @return 从字段名判断是否自定义项
     * @author Rocex Wang
     * @since 2020-10-22 17:31:02
     ***************************************************************************/
    protected boolean isCustomProperty(String strFieldCode)
    {
        if (strFieldCode == null || strFieldCode.trim().isEmpty())
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
     * @param metaVOClass Class<? extends MetaVO>
     * @param strSQL String
     * @param param SQLParameter
     * @param pagingAction IAction
     * @return List<? extends MetaVO>
     * @author Rocex Wang
     * @since 2020-5-9 11:20:25
     ***************************************************************************/
    protected List<? extends MetaVO> queryMetaVO(Class<? extends MetaVO> metaVOClass, String strSQL, SQLParameter param, IAction pagingAction)
    {
        String strMessage = "query " + metaVOClass.getSimpleName();

        Logger.getLogger().begin(strMessage);

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

        Logger.getLogger().end(strMessage);

        return listMetaVO;
    }

    /***************************************************************************
     * @param listClassVO
     * @author Rocex Wang
     * @since 2022-08-02 13:27:39
     ***************************************************************************/
    protected void saveToDictJson(List<ClassVO> listClassVO)
    {
        if (listClassVO == null || listClassVO.isEmpty())
        {
            return;
        }

        List<DictJsonVO> listDictJsonVO = new ArrayList<>();

        for (ClassVO classVO : listClassVO)
        {
            byte[] objReadAllBytes = null;

            try
            {
                Path pathClassFile = getClassFilePath(classVO);

                if (Files.exists(pathClassFile))
                {
                    objReadAllBytes = Files.readAllBytes(pathClassFile);
                }
            }
            catch (IOException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }

            if (objReadAllBytes == null || objReadAllBytes.length == 0)
            {
                continue;
            }

            DictJsonVO dictJsonVO = new DictJsonVO();
            dictJsonVO.setName(classVO.getName());
            dictJsonVO.setDdcVersion(strVersion);
            dictJsonVO.setClassId(classVO.getId());
            dictJsonVO.setDictJson(objReadAllBytes);
            dictJsonVO.setId(getMappedClassId(classVO));
            dictJsonVO.setDisplayName(classVO.getDisplayName());

            listDictJsonVO.add(dictJsonVO);

            if (listDictJsonVO.size() > 100)
            {
                try
                {
                    sqlExecutor.insertVO(listDictJsonVO.toArray(new DictJsonVO[0]));
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }

                listDictJsonVO.clear();
            }
        }

        if (!listDictJsonVO.isEmpty())
        {
            try
            {
                sqlExecutor.insertVO(listDictJsonVO.toArray(new DictJsonVO[0]));
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }

            listDictJsonVO.clear();
        }
    }

    /***************************************************************************
     * 整理表的字段，顺序：主键列 -> 普通字段列 -> 自定义项列 -> dr、ts列
     * @param classVO
     * @param listPropertyVO
     * @author Rocex Wang
     * @since 2020-11-3 17:21:23
     ***************************************************************************/
    protected List<PropertyVO> sortPropertyVO(ClassVO classVO, List<PropertyVO> listPropertyVO)
    {
        List<PropertyVO> listPkPropertyVO = new ArrayList<>();  // 主键列
        List<PropertyVO> listNormalPropertyVO = new ArrayList<>();  // 普通字段列
        List<PropertyVO> listCustomPropertyVO = new ArrayList<>(); // 自定义项列
        List<PropertyVO> listPropertyFinalVO = new ArrayList<>();  // dr、ts列

        List<String> listPk = Arrays.asList(StringHelper.isEmpty(classVO.getKeyAttribute()) ? new String[]{} : classVO.getKeyAttribute().split(";"));

        for (PropertyVO propertyVO : listPropertyVO)
        {
            String strPropKey = propertyVO.getName();

            if (listPk.contains(strPropKey))
            {
                propertyVO.setKeyProp(true);
                listPkPropertyVO.add(propertyVO);
            }
            else if (propertyVO.isCustomAttr() || isCustomProperty(strPropKey))
            {
                listCustomPropertyVO.add(propertyVO);
            }
            else if ("dr".equalsIgnoreCase(strPropKey) || "ts".equalsIgnoreCase(strPropKey) || "creator".equalsIgnoreCase(strPropKey) ||
                "creationtime".equalsIgnoreCase(strPropKey) || "modifier".equalsIgnoreCase(strPropKey) || "modifiedtime".equalsIgnoreCase(strPropKey))
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

    /***************************************************************************
     * @param classVO
     * @author Rocex Wang
     * @since 2021-11-24 09:40:38
     ***************************************************************************/
    protected void writeDataDictFile(ClassVO classVO)
    {
        new JacksonHelper()
            .exclude(ClassVO.class, "accessorClassname", "bizItfImpClassname", "classType", "componentId", "ddcVersion", "help", "id", "keyAttribute", "name", "refModelName",
                "returnType", "ts", "versionType")
            .exclude(PropertyVO.class, "accessorClassname", "accessPower", "accessPowerGroup", "attrLength", "attrSequence", "calculation", "classId", "customAttr", "ddcVersion",
                "dynamicAttr", "fixedLength", "hidden", "notSerialize", "id", "precise", "readOnly", "refModelName", "ts", "versionType", "refClassPathHref")
            .serializeThread(classVO, getClassFilePath(classVO));
    }
}
