package org.rocex.datadict;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-22 14:10:00
 ***************************************************************************/
public class CreateDataDictAction
{
    private int iIndex = 1;
    
    private Map<String, ? extends MetaVO> mapClassVO = new HashMap<>();     // class id 和 class 的对应关系
    private Map<String, String> mapClassVOByComponent = new HashMap<>();     // component id 和 component 内所有 class 链接的对应关系
    private Map<String, ? extends MetaVO> mapComponentVO = new HashMap<>(); // component id 和 component 的对应关系
    private Map<String, String> mapEnumString = new HashMap<>();             // enum id 和 enum name and value 的对应关系
    private Map<String, String> mapId = new HashMap<>();                     // 为了减小生成的文件体积，把元数据id和序号做个对照关系
    private Map<String, ? extends MetaVO> mapModuleVO = new HashMap<>();    // module id 和 module 的对应关系
    
    private SQLExecutor sqlExecutor = null;
    
    private String strCreateTime = DateFormat.getDateTimeInstance().format(new Date());
    
    private String strOutputDdcDir;     // 输出数据字典文件目录
    private String strOutputRootDir;    // 输出文件根目录
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
        
        Properties dbProp = new Properties();
        
        dbProp.setProperty("jdbc.driver", DataDictCreator.settings.getProperty(strVersion + ".jdbc.driver"));
        dbProp.setProperty("jdbc.url", DataDictCreator.settings.getProperty(strVersion + ".jdbc.url"));
        dbProp.setProperty("jdbc.user", DataDictCreator.settings.getProperty(strVersion + ".jdbc.user"));
        dbProp.setProperty("jdbc.password", DataDictCreator.settings.getProperty(strVersion + ".jdbc.password"));
        
        sqlExecutor = new SQLExecutor(dbProp);
    }
    
    /***************************************************************************
     * component id 和 component 内所有 class 链接的对应关系，用于数据字典右上角实体列表
     * @param listClassVO
     * @author Rocex Wang
     * @version 2020-4-24 15:41:49
     ***************************************************************************/
    private void buildClassVOMapByComponentId(List<ClassVO> listClassVO)
    {
        for (ClassVO classVO : listClassVO)
        {
            if (classVO.getClassType() != 201)
            {
                continue;
            }
            
            ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());
            
            String strClassLinks = mapClassVOByComponent.get(componentVO.getId());
            
            if (strClassLinks == null)
            {
                strClassLinks = "";
            }
            
            String strClassLink = MessageFormat.format(" / <a href=\"{0}\" class=\"{1}\">{2}</a>", getClassUrl2(classVO),
                    "Y".equals(classVO.getIsPrimary()) ? "pk-row" : "", classVO.getDisplayName());
            
            // 主实体放在首位
            strClassLinks = "Y".equals(classVO.getIsPrimary()) ? strClassLink + strClassLinks : strClassLinks + strClassLink;
            
            mapClassVOByComponent.put(componentVO.getId(), strClassLinks);
        }
    }
    
    /***************************************************************************
     * enum id 和 enum name and value 的对应关系
     * @author Rocex Wang
     * @version 2020-4-26 10:18:30
     ***************************************************************************/
    private void buildEnumString()
    {
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
            
            strEnum += enumVO.getValue() + "=" + enumVO.getName() + "; <br>";
            
            mapEnumString.put(enumVO.getId(), strEnum);
        }
    }
    
    /***************************************************************************
     * 建立 metaId 和 metaVO 的关系
     * @param listMetaVO
     * @return Map
     * @author Rocex Wang
     * @version 2020-4-26 10:18:51
     ***************************************************************************/
    private Map<String, ? extends MetaVO> buildMap(List<? extends MetaVO> listMetaVO)
    {
        Map<String, MetaVO> mapMetaVO = new HashMap<>();
        
        for (MetaVO metaVO : listMetaVO)
        {
            mapMetaVO.put(metaVO.getId(), metaVO);
        }
        
        return mapMetaVO;
    }
    
    /***************************************************************************
     * 拷贝静态文件、css、js 等
     * @author Rocex Wang
     * @version 2020-4-29 10:33:18
     ***************************************************************************/
    private void copyHtmlFiles()
    {
        try
        {
            FileHelper.copyFolder(Paths.get("settings", "html"), Paths.get(strOutputRootDir), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    /***************************************************************************
     * 生成数据字典文件
     * @param classVO
     * @author Rocex Wang
     * @version 2020-4-26 10:19:45
     ***************************************************************************/
    private void createDataDictFile(ClassVO classVO)
    {
        if (classVO.getClassType() != 201)
        {
            return;
        }
        
        String strPropertySQL = "select distinct a.id id,a.name name,a.displayname displayname,attrlength,attrminvalue,attrmaxvalue,attrsequence"
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
        
        if (listPropertyVO == null || listPropertyVO.size() == 0)
        {
            return;
        }
        
        String strHtmlRows = "";
        String strHtmlDataDictRow = DataDictCreator.settings.getProperty("HtmlDataDictRow");
        
        for (int i = 0; i < listPropertyVO.size(); i++)
        {
            PropertyVO propertyVO = listPropertyVO.get(i);
            
            // 引用实体模型
            ClassVO refClassVO = (ClassVO) mapClassVO.get(propertyVO.getDataType());
            
            String strRefClassPathHref = refClassVO.getDisplayName();
            
            if (refClassVO.getClassType() == 201)
            {
                String strRefClassPath = getClassUrl2(refClassVO);
                
                strRefClassPathHref = MessageFormat.format("<a href=\"{0}\">{1}</a>", strRefClassPath, refClassVO.getDisplayName());
            }
            
            strRefClassPathHref = strRefClassPathHref + " (" + refClassVO.getName() + ")";
            
            // 数据库类型
            String strDbType = getDbType(propertyVO);
            
            // 默认值
            String strDefaultValue = propertyVO.getDefaultValue() == null ? "" : propertyVO.getDefaultValue();
            
            // 枚举
            String strEnumString = "";
            if (propertyVO.getDataType().length() > 20 && propertyVO.getRefModelName() == null && propertyVO.getDataTypeStyle() == 300
                    && mapEnumString.containsKey(propertyVO.getDataType()))
            {
                strEnumString = mapEnumString.get(propertyVO.getDataType());
            }
            
            // 每行的css，主键行字体红色，其它行正常黑色
            String strRowStyle = classVO.getKeyAttribute().equals(propertyVO.getId()) ? "pk-row" : "";
            
            // 是否必输
            String strMustInput = "N".equals(propertyVO.getNullable()) ? "√" : "";
            
            strHtmlRows += MessageFormat.format(strHtmlDataDictRow, strRowStyle, i + 1, propertyVO.getName(), propertyVO.getDisplayName(), propertyVO.getName(),
                    strDbType, strMustInput, strRefClassPathHref, strDefaultValue, strEnumString);
        }
        
        ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());
        
        // 组件内实体列表链接
        String strClassList = mapClassVOByComponent.get(componentVO.getId()).trim();
        
        if (strClassList.startsWith("/")) // 截掉最前面的斜杠
        {
            strClassList = strClassList.substring(1);
        }
        
        String strHtml = MessageFormat.format(DataDictCreator.settings.getProperty("HtmlDataDictFile"), classVO.getDisplayName(), classVO.getDefaultTableName(),
                classVO.getFullClassname(), DataDictCreator.settings.get(strVersion + ".DataDictVersion"), strClassList, strHtmlRows, getFooter());
        
        FileHelper.writeFile(getClassFilePath(classVO), strHtml);
    }
    
    /***************************************************************************
     * 生成数据字典索引文件
     * @param listClassVO
     * @author Rocex Wang
     * @version 2020-4-26 10:11:27
     ***************************************************************************/
    private void createDataDictIndexFile(List<ClassVO> listClassVO)
    {
        String strContent = "";
        String strRow = "";
        
        int iIndex = 1;
        
        for (ClassVO classVO : listClassVO)
        {
            if (classVO.getClassType() != 201)
            {
                continue;
            }
            
            String strAbsClassFilePath = getClassUrl(classVO);
            
            strRow += MessageFormat.format("            <td><a href=\"{0}\">{1}<br>{2}</a></td>\n", strAbsClassFilePath, classVO.getDisplayName(),
                    classVO.getDefaultTableName());
            
            if (iIndex % 5 == 0)
            {
                strContent += "        <tr align=\"center\">\n" + strRow + "        </tr>\n";
                strRow = "";
            }
            
            iIndex++;
        }
        
        String strHtml = MessageFormat.format(DataDictCreator.settings.getProperty("HtmlDataDictIndexFile"),
                DataDictCreator.settings.get(strVersion + ".DataDictVersion"), strContent, getFooter());
        
        FileHelper.writeFile(Paths.get(strOutputRootDir, "data-dict-table.html"), strHtml);
    }
    
    /***************************************************************************
     * 生成构造实体树的json数据
     * @param listClassVO
     * @author Rocex Wang
     * @version 2020-4-29 11:21:07
     ***************************************************************************/
    private void createDataDictTreeData(List<ModuleVO> listModuleVO, List<ClassVO> listClassVO)
    {
        String strModuleRow = "";
        String strClassRow = "";
        
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
            
            strClassRow += MessageFormat.format(strLeafTemplate, getMappedClassId(classVO), getMappedModuleId(moduleVO), classVO.getDefaultTableName(),
                    classVO.getDisplayName(), strUrl, "ddc");
            
            String strModuleId = getModuleId(classVO);
            
            if (!listUsedClassModule.contains(strModuleId))
            {
                listUsedClassModule.add(strModuleId);
            }
        }
        
        String strModuleTemplate = "'{'id:\"{0}\",name:\"{1} {2}\"'}',";
        
        for (ModuleVO moduleVO : listModuleVO)
        {
            if (!listUsedClassModule.contains(moduleVO.getId()))
            {
                continue;
            }
            
            String _format = MessageFormat.format(strModuleTemplate, getMappedModuleId(moduleVO), moduleVO.getName(), moduleVO.getDisplayName());
            
            strModuleRow += _format;
            
            listUsedClassModule.remove(moduleVO.getId());
        }
        
        strModuleRow += MessageFormat.format(strModuleTemplate, "no-meta-table", "no-meta-table", "没有元数据的表");
        
        FileHelper.writeFile(Paths.get(strOutputRootDir, "scripts", "data-dict-tree.js"), "var dataDictIndexData=[" + strModuleRow + strClassRow + "];");
    }
    
    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-21 15:47:35
     ***************************************************************************/
    public void doAction()
    {
        copyHtmlFiles();
        
        String strModuleSQL = "select lower(id) id,lower(name) name,displayname from md_module a left join dap_dapsystem b on lower(a.id)=lower(b.devmodule) order by b.moduleid";
        String strComponentSQL = "select id,name,displayname,lower(ownmodule) ownmodule from md_component";
        String strClassSQL = "select id,name,displayname,defaulttablename,fullclassname,keyattribute,componentid,classtype,isprimary from md_class order by lower(defaulttablename)";
        
        try
        {
            List<ModuleVO> listModuleVO = (List<ModuleVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(ModuleVO.class), strModuleSQL);
            List<ComponentVO> listComponentVO = (List<ComponentVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(ComponentVO.class), strComponentSQL);
            List<ClassVO> listClassVO = (List<ClassVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(ClassVO.class), strClassSQL);
            
            mapModuleVO = buildMap(listModuleVO);
            mapComponentVO = buildMap(listComponentVO);
            mapClassVO = buildMap(listClassVO);
            
            buildEnumString();
            buildClassVOMapByComponentId(listClassVO);
            
            createDataDictTreeData(listModuleVO, listClassVO);
            createDataDictIndexFile(listClassVO);
            
            for (ClassVO classVO : listClassVO)
            {
                createDataDictFile(classVO);
            }
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
     * 实体文件全路径的绝对路径
     * @param classVO
     * @return Path
     * @author Rocex Wang
     * @version 2020-4-26 10:21:59
     ***************************************************************************/
    private Path getClassFilePath(ClassVO classVO)
    {
        return Paths.get(strOutputDdcDir, getMappedClassId(classVO) + ".html");
    }
    
    /***************************************************************************
     * 实体的访问url，相对于根目录
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-5-2 14:26:12
     ***************************************************************************/
    private String getClassUrl(ClassVO classVO)
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
    private String getClassUrl2(ClassVO classVO)
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
    private String getDbType(PropertyVO propertyVO)
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
    private String getFooter()
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
    private synchronized String getMappedClassId(ClassVO classVO)
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
    private synchronized String getMappedId(String strType, String strId)
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
    private synchronized String getMappedModuleId(ModuleVO moduleVO)
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
    private String getModuleId(ClassVO classVO)
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
    private ModuleVO getModuleVO(ClassVO classVO)
    {
        ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());
        
        ModuleVO moduleVO = (ModuleVO) mapModuleVO.get(componentVO.getOwnModule());
        
        return moduleVO;
    }
}
