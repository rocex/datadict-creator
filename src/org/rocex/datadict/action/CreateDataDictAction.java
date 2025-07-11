package org.rocex.datadict.action;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
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

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.ComponentVO;
import org.rocex.datadict.vo.Context;
import org.rocex.datadict.vo.DictJsonVO;
import org.rocex.datadict.vo.EnumValueVO;
import org.rocex.datadict.vo.FullText;
import org.rocex.datadict.vo.FullTextItem;
import org.rocex.datadict.vo.MetaVO;
import org.rocex.datadict.vo.ModuleVO;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.db.SQLExecutor;
import org.rocex.db.param.SQLParameter;
import org.rocex.db.processor.BeanListProcessor;
import org.rocex.db.processor.PageProcessor;
import org.rocex.db.processor.PagingAction;
import org.rocex.db.processor.PagingEventObject;
import org.rocex.utils.FileHelper;
import org.rocex.utils.JacksonHelper;
import org.rocex.utils.Logger;
import org.rocex.utils.ResHelper;
import org.rocex.utils.StringHelper;
import org.rocex.vo.IAction;

/***************************************************************************
 * 生成数据字典类，支持json格式<br>
 * @author Rocex Wang
 * @since 2020-4-22 14:10:00
 ***************************************************************************/
public class CreateDataDictAction implements IAction, Closeable
{
    protected boolean isCreateDB = true;        // 是否生成表结构字典
    protected boolean isCreateMD = true;        // 是否生成元数据字典
    protected boolean isSaveJsonToDB = false;   // 是否在数据库里保存生成的json文件

    protected JacksonHelper jacksonHelper = new JacksonHelper().exclude(ClassVO.class, "accessorClassname", "authen", "bizItfImpClassname", "bizObjectId",
            "classType", "componentId", "ddcVersion", "help", "id", "keyAttribute", "mainClassId", "name", "refModelName", "returnType", "ts", "versionType")
            .exclude(PropertyVO.class, "accessorClassname", "accessPower", "accessPowerGroup", "attrLength", "attrSequence", "calculation", "classId",
                    "customAttr", "ddcVersion", "dynamicAttr", "fixedLength", "hidden", "notSerialize", "id", "precise", "readOnly", "refModelName", "ts",
                    "versionType", "refClassPathHref");

    protected Map<String, List<ClassVO>> mapClassVOByComponent = new HashMap<>();       // component id 和 component 内所有 class 链接的对应关系

    protected Map<String, String> mapComponentIdPrimaryClassId = new HashMap<>();       // component id 和 主实体 id 的对应关系
    protected Map<String, String> mapEnumString = new HashMap<>();                      // enum id 和 enum name and value 的对应关系

    protected Map<String, ? extends MetaVO> mapIdClassVO = new HashMap<>();             // class id 和 class 的对应关系
    protected Map<String, ? extends MetaVO> mapIdComponentVO = new HashMap<>();         // component id 和 component 的对应关系
    protected Map<String, ? extends MetaVO> mapIdModuleVO = new HashMap<>();            // module id 和 module 的对应关系

    protected SQLExecutor sqlExecutor;

    protected String strClassListHrefTemplate = "<a href=\"javascript:void(0);\" onClick=loadDataDict(\"%s\"); class=\"%s\">%s</a>";    // 左上角实体列表链接

    protected String strCreateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    // 自定义项字段名前缀
    protected String[] strCustomPatterns = {"def", "vdef", "vfree", "vbdef", "vsndef", "vbfree", "vbcdef", "defitem", "vpfree", "zyx", "vuserdef", "obmdef",
            "hvdef", "vbodyuserdef", "vhdef", "vgdef", "hdef", "vstbdef", "vpdef", "vbprodbatdef", "vheaduserdef", "bc_vdef", "vsdef", "vhodef", "vstdef",
            "vbatchdef", "bdef", "freevalue", "h_def", "vrcdef", "des_freedef", "src_freedef", "vprodbatdef", "factor", "free", "nfactor", "glbdef",
            "jobglbdef", "vcostfree"};
    protected String strFullTextFileIndex;  // 全文检索文件名序号，英文逗号分割
    protected String strOutputDictDir;      // 输出数据字典文件目录
    protected String strOutputRootDir;      // 输出文件根目录
    protected String strPropertySQL;        // 在多线程中使用，提取出来
    protected String strVersion;            // 数据字典版本

    /***************************************************************************
     * @author Rocex Wang
     * @since 2020-4-26 14:52:18
     ***************************************************************************/
    public CreateDataDictAction(String strVersion)
    {
        this.strVersion = strVersion;

        isCreateMD = StringHelper.getBoolean(Context.getInstance().getSetting("createMD"), true);
        isCreateDB = StringHelper.getBoolean(Context.getInstance().getSetting("createDB"), true);
        isSaveJsonToDB = StringHelper.getBoolean(Context.getInstance().getSetting("saveJsonToDB"), false);

        strOutputRootDir = Path.of(Context.getInstance().getSetting("WorkDir"), "datadict-" + strVersion).toString();
        strOutputDictDir = Path.of(strOutputRootDir, "dict").toString();

        Properties dbProp = new Properties();

        String strTargetUrl = Context.getInstance().getSetting("target.jdbc.url").replace("${version}", strVersion);
        String strTargetUser = Context.getInstance().getSetting("target.jdbc.user");
        String strTargetDriver = Context.getInstance().getSetting("target.jdbc.driver");
        String strTargetPassword = Context.getInstance().getSetting("target.jdbc.password");

        dbProp.setProperty("jdbc.url", strTargetUrl);
        dbProp.setProperty("jdbc.user", strTargetUser);
        dbProp.setProperty("jdbc.driver", strTargetDriver);
        dbProp.setProperty("jdbc.password", strTargetPassword);

        sqlExecutor = new SQLExecutor(dbProp);

        // 补齐正则表达式
        for (int i = 0; i < strCustomPatterns.length; i++)
        {
            strCustomPatterns[i] = "(" + strCustomPatterns[i] + ")[0-9]+";
        }

        strPropertySQL = sqlExecutor.getSQLSelect(PropertyVO.class) + " where class_id=? and ddc_version=? order by key_prop desc,attr_sequence,name";
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
            if (classVO.getClassType() != ClassVO.ClassType.clazz.value())
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

        if (!isCreateMD)
        {
            Logger.getLogger().end("query and build enum map", "isCreateMD=%s, skip...", isCreateMD);
            return;
        }

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
        {
            return new HashMap<>();
        }

        Logger.getLogger().begin("build map: " + listMetaVO.get(0).getClass().getSimpleName());

        Map<String, MetaVO> mapMetaVO = listMetaVO.stream().collect(Collectors.toMap(MetaVO::getId, Function.identity(), (key1, key2) -> key2));

        Logger.getLogger().end("build map: " + listMetaVO.get(0).getClass().getSimpleName());

        return mapMetaVO;
    }

    @Override
    public void close()
    {
        mapClassVOByComponent.clear();
        mapComponentIdPrimaryClassId.clear();
        mapEnumString.clear();
        mapIdClassVO.clear();
        mapIdComponentVO.clear();
        mapIdModuleVO.clear();

        sqlExecutor.close();
    }

    /***************************************************************************
     * @author Rocex Wang
     * @since 2025-05-22 09:51:22
     ***************************************************************************/
    protected void copyDynamicFiles()
    {
        Logger.getLogger().begin("copy dynamic files");

        try
        {
            String strInfoFile = Files.readString(Path.of("data", "template", "info.json"));

            strInfoFile = strInfoFile.formatted(strCreateTime, Context.getInstance().getSetting("DataDictVersion"), strVersion, strFullTextFileIndex);

            FileHelper.writeFileThread(Path.of(strOutputRootDir, "info.json"), strInfoFile);
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("copy dynamic files");
    }

    /***************************************************************************
     * 拷贝静态文件、css、js 等
     * @author Rocex Wang
     * @since 2020-4-29 10:33:18
     ***************************************************************************/
    protected void copyStaticFiles()
    {
        Logger.getLogger().begin("copy static files");

        try
        {
            FileHelper.copyFolderThread(Path.of("data", "template", "static"), Path.of(strOutputRootDir), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("copy static files");
    }

    /***************************************************************************
     * 生成数据字典文件
     * @param classVO
     * @author Rocex Wang
     * @since 2020-4-26 10:19:45
     ***************************************************************************/
    protected void createDataDictFile(ClassVO classVO)
    {
        if (classVO.getClassType() != ClassVO.ClassType.clazz.value() && classVO.getClassType() != ClassVO.ClassType.db.value())
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

        if (listPropertyVO == null) // 旗舰版存在没有属性的元数据，如果不生成，会存在断链的引用
        {
            listPropertyVO = new ArrayList<>();
        }

        listPropertyVO = sortPropertyVO(classVO, listPropertyVO);

        classVO.setPropertyVO(listPropertyVO);
        classVO.setClassListUrl(getClassListUrl(classVO));

        for (PropertyVO propertyVO : listPropertyVO)
        {
            // 枚举/取值范围
            propertyVO.setDataScope(getDataScope(propertyVO));

            // 默认值
            propertyVO.setDefaultValue(Objects.toString(propertyVO.getDefaultValue(), ""));

            // 引用实体模型，这个放到最后，因为会改变dataType的值
            ClassVO refClassVO = (ClassVO) mapIdClassVO.get(propertyVO.getDataType());

            if (refClassVO != null)
            {
                propertyVO.setDataType(refClassVO.getId());
                propertyVO.setDataTypeName(refClassVO.getName());
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
        Logger.getLogger().begin("create data dict file:");

        if (listClassVO == null || listClassVO.isEmpty())
        {
            Logger.getLogger().end("create data dict file:", "listClassVO is empty, skip...");
            return;
        }

        int[] iCount = {0, listClassVO.size()};

        ExecutorService executorService = Executors.newFixedThreadPool(ResHelper.getThreadCount());

        listClassVO.forEach(classVO -> executorService.execute(() ->
        {
            createDataDictFile(classVO);

            Logger.getLogger().log2(Logger.iLoggerLevelDebug, ++iCount[0] + "/" + iCount[1]);
        }));

        executorService.shutdown();

        while (!executorService.isTerminated())
        {
            ResHelper.sleep(10);
        }

        Logger.getLogger().log2(Logger.iLoggerLevelDebug, iCount[0] + "/" + iCount[1] + "  done!\n");

        Logger.getLogger().end("create data dict file:");
    }

    /***************************************************************************
     * 生成构造实体树的json数据
     * @param listModuleVO
     * @param listClassVO
     * @param listTableVO
     * @author Rocex Wang
     * @since 2020-4-29 11:21:07
     ***************************************************************************/
    protected void createDataDictTree(List<ModuleVO> listModuleVO, List<ComponentVO> listComponentVO, List<ClassVO> listClassVO, List<ClassVO> listTableVO)
    {
        Logger.getLogger().begin("create data dict tree: " + (listClassVO.size() + listTableVO.size()));

        StringBuilder strModuleRows = new StringBuilder();          // 所有模块
        StringBuilder strComponentRows = new StringBuilder();       // 所有组件
        StringBuilder strClassRows = new StringBuilder();           // 所有实体

        List<String> listWithChildren = new ArrayList<>();          // 只生成含有实体的Module和Component

        String strTreeDataFolderTemplate = """
                {id:"%s",pId:"%s",name:"%s %s",isDdcClass:false,path:"%s"},""";     // 左树目录 虚节点
        String strTreeDataClassTemplate = """
                {id:"%s",pId:"%s",name:"%s %s",path:"%s"},""";                      // 左树实体 链接实体

        strModuleRows.append(strTreeDataFolderTemplate.formatted(ModuleVO.strMDRootId, null, "Class", "元数据字典", ModuleVO.strMDRootId));

        for (ClassVO classVO : listClassVO)
        {
            if (classVO.getClassType() != ClassVO.ClassType.clazz.value())
            {
                continue;
            }

            ModuleVO moduleVO = getModuleVO(classVO);

            if (moduleVO == null)
            {
                Logger.getLogger().debug("Unable to find the module to which the class belongs:" + classVO);
                continue;
            }

            String strModuleId = moduleVO.getId();
            String strParentModuleId = moduleVO.getParentModuleId();

            String strClassname = classVO.getFullClassname();
            strClassname = strClassname.contains(".") ? strClassname.substring(strClassname.lastIndexOf(".") + 1) : strClassname;

            if (classVO.isPrimaryClass())
            {
                String strDdc = strTreeDataClassTemplate.formatted(classVO.getId(), strModuleId, Objects.toString(classVO.getTableName(), ""),
                        classVO.getDisplayName() + " " + strClassname, ModuleVO.strMDRootId + "," + strParentModuleId + "," + strModuleId);

                boolean blHasChildren = mapClassVOByComponent.get(classVO.getComponentId()).size() > 1;

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
                ClassVO primaryClassVO = strPrimaryClassId == null ? null : (ClassVO) mapIdClassVO.get(strPrimaryClassId);
                String strPid = primaryClassVO == null ? strModuleId : primaryClassVO.getId();

                String strTreeDataClass = strTreeDataClassTemplate.formatted(classVO.getId(), strPid, Objects.toString(classVO.getTableName(), ""),
                        classVO.getDisplayName() + " " + strClassname, ModuleVO.strMDRootId + "," + strParentModuleId + "," + strModuleId + "," + strPid);
                strClassRows.append(strTreeDataClass);
            }

            if (!listWithChildren.contains(strParentModuleId))
            {
                listWithChildren.add(strParentModuleId);
            }

            if (!listWithChildren.contains(strModuleId))
            {
                listWithChildren.add(strModuleId);
            }
        }

        for (ModuleVO moduleVO : listModuleVO)
        {
            if (!listWithChildren.contains(moduleVO.getId()))
            {
                continue;
            }

            String strTreeDataModule = strTreeDataFolderTemplate.formatted(moduleVO.getId(), moduleVO.getParentModuleId(), moduleVO.getName(),
                    moduleVO.getDisplayName(), ModuleVO.strMDRootId + "," + moduleVO.getParentModuleId());
            strModuleRows.append(strTreeDataModule);
        }

        listWithChildren.clear();

        // 所有表都按字母顺序挂在一个节点下，不再分级
        if (!listTableVO.isEmpty())
        {
            strModuleRows.append(strTreeDataFolderTemplate.formatted(ModuleVO.strDBRootId, null, "Table", "数据库字典", ModuleVO.strDBRootId));

            for (ClassVO classVO : listTableVO)
            {
                ModuleVO moduleVO = getModuleVO(classVO);

                String strModuleId = null, strComponentId, strPath = ModuleVO.strDBRootId;

                if (moduleVO == null)
                {
                    Logger.getLogger()
                            .debug("cannot find module: class id=%s, name=%s, tablename=%s, componentid=%s, classtype=%s, modeltype=%s".formatted(
                                    classVO.getId(), classVO.getName(), classVO.getTableName(), classVO.getComponentId(), classVO.getClassType(),
                                    classVO.getModelType()));
                    // continue;
                }
                else
                {
                    strModuleId = moduleVO.getId();
                    strPath += "," + strModuleId;

                    if (!listWithChildren.contains(strModuleId))
                    {
                        listWithChildren.add(strModuleId);
                    }
                }

                ComponentVO componentVO = (ComponentVO) mapIdComponentVO.get(classVO.getComponentId());
                if (componentVO != null)
                {
                    strComponentId = componentVO.getId();
                    strPath += "," + strComponentId;

                    if (!listWithChildren.contains(strComponentId))
                    {
                        listWithChildren.add(strComponentId);
                    }
                }

                String strTableName = classVO.getTableName().toLowerCase();

                String strClassRow = strTreeDataClassTemplate.formatted(classVO.getId(), classVO.getComponentId(), strTableName,
                        Objects.toString(classVO.getDisplayName(), ""), strPath);
                strClassRows.append(strClassRow);
            }

            for (ModuleVO moduleVO : listModuleVO)
            {
                if (!listWithChildren.contains(moduleVO.getId()))
                {
                    continue;
                }

                String strModuleRow = strTreeDataFolderTemplate.formatted(moduleVO.getId(), moduleVO.getParentModuleId(), moduleVO.getName(),
                        Objects.toString(moduleVO.getDisplayName(), ""), ModuleVO.strDBRootId);
                strModuleRows.append(strModuleRow);
            }

            for (ComponentVO componentVO : listComponentVO)
            {
                if (!listWithChildren.contains(componentVO.getId()))
                {
                    continue;
                }

                String strComponentRow = strTreeDataFolderTemplate.formatted(componentVO.getId(), componentVO.getOwnModule(), componentVO.getName(),
                        Objects.toString(componentVO.getDisplayName(), ""), ModuleVO.strDBRootId + "," + componentVO.getOwnModule());
                strComponentRows.append(strComponentRow);
            }

            listWithChildren.clear();
        }

        FileHelper.writeFileThread(Path.of(strOutputRootDir, "scripts", "data-dict-tree.js"),
                "var dataDictIndexData=[" + strModuleRows + strComponentRows + strClassRows + "];");

        Logger.getLogger().end("create data dict tree: " + (listClassVO.size() + listTableVO.size()));
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
        Logger.getLogger().begin("create data dictionary " + strVersion);

        Logger.getLogger().debug("version=%s, createDB=%s, createMD=%s, saveJsonToDB=%s", strVersion, isCreateDB, isCreateMD, isSaveJsonToDB);

        if (!isCreateDB && !isCreateMD)
        {
            Logger.getLogger().end("create data dictionary " + strVersion, "isCreateDB=%s, isCreateMD=%s, skip...", isCreateDB, isCreateMD);
            return;
        }

        initTargetDir();

        copyStaticFiles();

        String strVersionSQL = "ddc_version='" + strVersion + "'";

        String strModuleSQL = sqlExecutor.getSQLSelect(ModuleVO.class) + " where " + strVersionSQL + " order by model_type,id";
        String strComponentSQL = sqlExecutor.getSQLSelect(ComponentVO.class) + " where " + strVersionSQL + " order by model_type,own_module,name";
        String strClassSQLMd = sqlExecutor.getSQLSelect(ClassVO.class) + " where " + strVersionSQL + " and component_id is not null and model_type='"
                + MetaVO.ModelType.md.name() + "' order by primary_class desc,table_name";
        String strClassSQLDb = sqlExecutor.getSQLSelect(ClassVO.class) + " where " + strVersionSQL + " and component_id is not null and model_type='"
                + MetaVO.ModelType.db.name() + "' order by table_name";

        List<ModuleVO> listModuleVO = (List<ModuleVO>) queryMetaVO(ModuleVO.class, strModuleSQL);
        List<ComponentVO> listComponentVO = (List<ComponentVO>) queryMetaVO(ComponentVO.class, strComponentSQL);

        List<ClassVO> listClassVO = isCreateMD ? (List<ClassVO>) queryMetaVO(ClassVO.class, strClassSQLMd) : new ArrayList<>();
        List<ClassVO> listTableVO = isCreateDB ? (List<ClassVO>) queryMetaVO(ClassVO.class, strClassSQLDb) : new ArrayList<>();

        mapIdModuleVO = buildMap(listModuleVO);
        mapIdComponentVO = buildMap(listComponentVO);
        mapIdClassVO = buildMap(listClassVO);

        buildEnumMap();
        buildClassVOMapByComponentId(listClassVO);

        createDataDictTree(listModuleVO, listComponentVO, listClassVO, listTableVO);

        createDataDictFiles(listClassVO);
        createDataDictFiles(listTableVO);

        String strMsg = "save data dict json file to db: %s md + %s db = %s".formatted(listClassVO.size(), listTableVO.size(),
                listClassVO.size() + listTableVO.size());
        Logger.getLogger().begin(strMsg);

        saveToDictJson(listClassVO);
        saveToDictJson(listTableVO);

        Logger.getLogger().end(strMsg);

        exportFullText();

        copyDynamicFiles();

        Logger.getLogger().end("create data dictionary " + strVersion);
    }

    /***************************************************************************
     * 生成全文检索文件
     * @author Rocex Wang
     * @since 2025-05-19 11:01:07
     ***************************************************************************/
    protected void exportFullText()
    {
        Logger.getLogger().begin("export full-text files");

        String strSQL = """
                select class_id as id,group_concat(replace(name,'"',''''),'|')||'|'||group_concat(replace(display_name,'"',''''),'|') as name
                from md_property group by class_id order by class_id""";

        List<String> listIndex = new ArrayList<>();

        PagingAction pagingFullTextAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                int iPageIndex = ((PagingEventObject) evt).getPageIndex();

                List<FullTextItem> listVO = (List<FullTextItem>) evt.getSource();

                FullText fullText = new FullText();
                fullText.setData(listVO.toArray(new FullTextItem[0]));

                String strFullTextFileName = StringHelper.leftPad(String.valueOf(iPageIndex), "0", 2);
                listIndex.add(strFullTextFileName);

                jacksonHelper.serializeThread(fullText, Path.of(strOutputRootDir, "scripts", "full-text-" + strFullTextFileName + ".json"));
            }
        };

        try
        {
            BeanListProcessor<FullTextItem> processor = new BeanListProcessor<>(FullTextItem.class);
            processor.setPagingAction(pagingFullTextAction.setPageSize(10000));

            sqlExecutor.executeQuery(strSQL, null, processor);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        strFullTextFileIndex = String.join(",", listIndex);

        Logger.getLogger().end("export full-text files");
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
        return Path.of(strOutputDictDir, classVO.getId() + ".json");
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
            if (classVO.getClassType() != ClassVO.ClassType.clazz.value())
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

            String strClassLink = strClassListHrefTemplate.formatted(classVO.getId(), strClassStyle, classVO.getDisplayName());

            // 主实体放在首位
            strClassLinks = classVO.isPrimaryClass() ? strClassLink + " / " + strClassLinks : strClassLinks + " / " + strClassLink;
        }

        strClassLinks = strClassLinks.trim().replace("/  /", "/");

        if (strClassLinks.startsWith("/"))
        {
            strClassLinks = strClassLinks.substring(1);
        }

        if (strClassLinks.endsWith("/"))
        {
            strClassLinks = strClassLinks.substring(0, strClassLinks.length() - 1);
        }

        return strClassLinks;
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

        if (propertyVO.getDataType() != null && propertyVO.getDataType().length() > 20 && propertyVO.getRefModelName() == null
                && propertyVO.getDataTypeStyle() == 300 && mapEnumString.containsKey(propertyVO.getDataType()))
        {
            strDataScope = mapEnumString.get(propertyVO.getDataType());
        }
        else
        {
            strDataScope = "[%s , %s]".formatted(StringHelper.getIfEmpty(propertyVO.getAttrMinValue(), ""),
                    StringHelper.getIfEmpty(propertyVO.getAttrMaxValue(), ""));

            if ("[ , ]".equals(strDataScope))
            {
                strDataScope = "";
            }
        }

        return strDataScope;
    }

    /***************************************************************************
     * @param classVO
     * @return ModuleVO
     * @author Rocex Wang
     * @since 2020-4-29 11:46:50
     ***************************************************************************/
    protected ModuleVO getModuleVO(ClassVO classVO)
    {
        ComponentVO componentVO = (ComponentVO) mapIdComponentVO.get(classVO.getComponentId());

        // xxx
        // if (componentVO == null)
        // {
        // return null;
        // }

        ModuleVO moduleVO = componentVO == null ? (ModuleVO) mapIdModuleVO.get(classVO.getComponentId())
                : (ModuleVO) mapIdModuleVO.get(componentVO.getOwnModule());

        return moduleVO;
    }

    /***************************************************************************
     * @author Rocex Wang
     * @since 2020-5-16 9:44:24
     ***************************************************************************/
    protected void initTargetDir()
    {
        Logger.getLogger().begin("empty target folder " + strOutputRootDir);

        try
        {
            FileHelper.deleteFolder(Path.of(strOutputRootDir));

            sqlExecutor.executeUpdate("delete from ddc_dict_json where ddc_version=?", new SQLParameter().addParam(strVersion));
        }
        catch (SQLException | IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("empty target folder " + strOutputRootDir);
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
     * @return List<? extends MetaVO>
     * @author Rocex Wang
     * @since 2020-5-9 11:20:25
     ***************************************************************************/
    protected List<? extends MetaVO> queryMetaVO(Class<? extends MetaVO> metaVOClass, String strSQL)
    {
        String strMessage = "query " + metaVOClass.getSimpleName();

        Logger.getLogger().begin(strMessage);

        List<? extends MetaVO> listMetaVO = null;

        try
        {
            BeanListProcessor<? extends MetaVO> processor = new BeanListProcessor<>(metaVOClass);

            listMetaVO = (List<? extends MetaVO>) sqlExecutor.executeQuery(strSQL, null, processor);
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
        if (!isSaveJsonToDB || listClassVO == null || listClassVO.isEmpty())
        {
            return;
        }

        Logger.getLogger().begin("save data dict json file");

        int[] iCount = {0, listClassVO.size()};
        
        ExecutorService executorService = Executors.newFixedThreadPool(ResHelper.getThreadCount());
        
        listClassVO.forEach(classVO -> executorService.execute(() ->
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
                return;
            }

            DictJsonVO dictJsonVO = new DictJsonVO();
            dictJsonVO.setId(classVO.getId());
            dictJsonVO.setDdcVersion(strVersion);
            dictJsonVO.setName(classVO.getName());
            dictJsonVO.setClassId(classVO.getId());
            dictJsonVO.setDictJson(objReadAllBytes);
            dictJsonVO.setDisplayName(classVO.getDisplayName());
            
            try
            {
                sqlExecutor.insertVO(new DictJsonVO[]{dictJsonVO});
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
            
            Logger.getLogger().log2(Logger.iLoggerLevelDebug, ++iCount[0] + "/" + iCount[1]);
        }));
        
        executorService.shutdown();
        
        while (!executorService.isTerminated())
        {
            ResHelper.sleep(10);
        }
        
        Logger.getLogger().log2(Logger.iLoggerLevelDebug, iCount[0] + "/" + iCount[1] + "  done!\n");
        Logger.getLogger().end("save data dict json file");
    }
    
    /***************************************************************************
     * @param listClassVO
     * @author Rocex Wang
     * @since 2025-07-11 12:34:46
     ***************************************************************************/
    protected void saveToDictJson2(List<ClassVO> listClassVO)
    {
        if (!isSaveJsonToDB || listClassVO == null || listClassVO.isEmpty())
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
            dictJsonVO.setId(classVO.getId());
            dictJsonVO.setDdcVersion(strVersion);
            dictJsonVO.setName(classVO.getName());
            dictJsonVO.setClassId(classVO.getId());
            dictJsonVO.setDictJson(objReadAllBytes);
            dictJsonVO.setDisplayName(classVO.getDisplayName());

            listDictJsonVO.add(dictJsonVO);

            if (listDictJsonVO.size() > 500)
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
     * @param listClassVO
     * @author Rocex Wang
     * @since 2022-08-02 13:27:39
     ***************************************************************************/
    protected void saveToDictJson3(List<ClassVO> listClassVO)
    {
        if (!isSaveJsonToDB || listClassVO == null || listClassVO.isEmpty())
        {
            return;
        }

        Logger.getLogger().begin("save data dict json file");

        int[] iCount = {0, listClassVO.size()};

        List<DictJsonVO> listDictJsonVO = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(ResHelper.getThreadCount());

        listClassVO.forEach(classVO -> executorService.execute(() ->
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
                return;
            }

            DictJsonVO dictJsonVO = new DictJsonVO();
            dictJsonVO.setId(classVO.getId());
            dictJsonVO.setDdcVersion(strVersion);
            dictJsonVO.setName(classVO.getName());
            dictJsonVO.setClassId(classVO.getId());
            dictJsonVO.setDictJson(objReadAllBytes);
            dictJsonVO.setDisplayName(classVO.getDisplayName());
            
            listDictJsonVO.add(dictJsonVO);
            
            Logger.getLogger().log2(Logger.iLoggerLevelDebug, ++iCount[0] + "/" + iCount[1]);
        }));

        executorService.shutdown();

        while (!executorService.isTerminated())
        {
            ResHelper.sleep(10);
        }

        PageProcessor<DictJsonVO> pageProcessor = new PageProcessor<>(DictJsonVO.class);
        pageProcessor.setPagingAction(new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                List<DictJsonVO> listDictJsonVO2 = (List<DictJsonVO>) evt.getSource();

                try
                {
                    sqlExecutor.insertVO(listDictJsonVO2.toArray(new DictJsonVO[0]));
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        });
        
        try
        {
            pageProcessor.doAction(listClassVO);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().log2(Logger.iLoggerLevelDebug, iCount[0] + "/" + iCount[1] + "  done!\n");
        Logger.getLogger().end("save data dict json file");
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
        List<PropertyVO> listPkPropertyVO = new ArrayList<>();      // 主键列
        List<PropertyVO> listNormalPropertyVO = new ArrayList<>();  // 普通字段列
        List<PropertyVO> listCustomPropertyVO = new ArrayList<>();  // 自定义项列
        List<PropertyVO> listPropertyFinalVO = new ArrayList<>();   // dr、ts列

        List<String> listPk = Arrays.asList(StringHelper.isEmpty(classVO.getKeyAttribute()) ? new String[]{} : classVO.getKeyAttribute().split(";"));

        List<String> listAuditTsDr = Arrays.asList("dr", "ts", "creator", "created_by", "create_user", "creatorid", "creator_id", "created", "createdate",
                "created_date", "createtime", "create_date", "create_time", "creationtime", "creation_time", "last_modified_by", "modifier", "modifierid",
                "last_modified_date", "modifiedtime", "modified_time", "modifydate", "modifytime", "modify_date", "modify_time", "update_time");

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
            else if (listAuditTsDr.contains(strPropKey.toLowerCase()))
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
        jacksonHelper.serializeThread(classVO, getClassFilePath(classVO));
    }
}
