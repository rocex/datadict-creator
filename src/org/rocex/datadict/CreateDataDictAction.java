package org.rocex.datadict;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.MessageFormat;
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
    private Map<String, ? extends MetaVO> mapClassVO = new HashMap<>(); // class id 和 class 的对应关系
    private Map<String, String> mapClassVOByComponent = new HashMap<>(); // component id 和 component 内所有 class 链接的对应关系
    private Map<String, ? extends MetaVO> mapComponentVO = new HashMap<>(); // component id 和 component 的对应关系
    private Map<String, String> mapEnumString = new HashMap<>(); // enum id 和 enum name and value 的对应关系
    private Map<String, ? extends MetaVO> mapModuleVO = new HashMap<>(); // module id 和 module 的对应关系
    
    private SQLExecutor sqlExecutor = null;
    
    private String strCreateTime = DateFormat.getDateTimeInstance().format(new Date());
    
    private String strOutputDir;
    
    private String strVersion;
    
    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-26 14:52:18
     ***************************************************************************/
    public CreateDataDictAction(String strVersion)
    {
        super();
        
        this.strVersion = strVersion;
        
        strOutputDir = DataDictCreator.settings.getProperty(strVersion + ".OutputDir");
        
        Properties dbProp = new Properties();
        
        dbProp.setProperty("jdbc.driver", DataDictCreator.settings.getProperty(strVersion + ".jdbc.driver"));
        dbProp.setProperty("jdbc.url", DataDictCreator.settings.getProperty(strVersion + ".jdbc.url"));
        dbProp.setProperty("jdbc.user", DataDictCreator.settings.getProperty(strVersion + ".jdbc.user"));
        dbProp.setProperty("jdbc.password", DataDictCreator.settings.getProperty(strVersion + ".jdbc.password"));
        
        sqlExecutor = new SQLExecutor(dbProp);
    }
    
    /***************************************************************************
     * component id 和 component 内所有 class 链接的对应关系
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
            
            String strClassLink = MessageFormat.format(" / <a href=\"{0}\">{1}</a>", getAbsClassFilePath(classVO), classVO.getDisplayName(),
                    classVO.getFullClassname());
            
            // 主实体加粗
            strClassLinks = "Y".equals(classVO.getIsPrimary()) ? "<b>" + strClassLink + "</b>" + strClassLinks : strClassLinks + strClassLink;
            
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
        String strEnumValueSQL = "select id,name,value,enumsequence from md_enumvalue order by id,enumsequence";
        
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
            FileHelper.copyFolder(Paths.get("settings", "html"), Paths.get(strOutputDir), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
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
                String strRefClassPath = getAbsClassFilePath(refClassVO);
                
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
            String strMustInput = "N".equals(propertyVO.getNullable()) ? "  √" : "";
            
            strHtmlRows += MessageFormat.format(strHtmlDataDictRow, strRowStyle, i + 1, propertyVO.getName(), propertyVO.getDisplayName(), propertyVO.getName(),
                    strDbType, strMustInput, strRefClassPathHref, strDefaultValue, strEnumString);
        }
        
        ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());
        
        // 组件内实体列表链接
        String strClassList = classVO.getFullClassname() + " " + mapClassVOByComponent.get(componentVO.getId());
        
        String strHtml = MessageFormat.format(DataDictCreator.settings.getProperty("HtmlDataDictFile"),
                classVO.getDisplayName() + " " + classVO.getDefaultTableName(), DataDictCreator.settings.get(strVersion + ".DataDictVersion"), strClassList,
                strHtmlRows, strCreateTime);
        
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
            
            strRow += MessageFormat.format("            <td><a href=\"{0}\">{1}<br>{2}</a></td>\n",
                    getFilePath(false, ".", getModuleId(classVO), classVO.getDefaultTableName() + "_" + classVO.getId() + ".html"), classVO.getDisplayName(),
                    classVO.getDefaultTableName());
            
            if (iIndex % 5 == 0)
            {
                strContent += "        <tr align=\"center\">\n" + strRow + "        </tr>\n";
                strRow = "";
            }
            
            iIndex++;
        }
        
        String strHtml = MessageFormat.format(DataDictCreator.settings.getProperty("HtmlIndexFile"),
                DataDictCreator.settings.get(strVersion + ".DataDictVersion"), strContent, strCreateTime);
        
        FileHelper.writeFile(getFilePath(false, strOutputDir, "index.html"), strHtml);
    }
    
    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-21 15:47:35
     ***************************************************************************/
    public void doAction()
    {
        copyHtmlFiles();
        
        String strModuleSQL = "select id,name,displayname from md_module order by lower(name)";
        String strComponentSQL = "select id,name,displayname,namespace,ownmodule from md_component";
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
     * 实体文件全路径的相对路径
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-4-26 10:20:51
     ***************************************************************************/
    private String getAbsClassFilePath(ClassVO classVO)
    {
        return getFilePath(false, "..", getModuleId(classVO), classVO.getDefaultTableName() + "_" + classVO.getId() + ".html");
    }
    
    /***************************************************************************
     * 实体文件的路径
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-4-26 10:21:32
     ***************************************************************************/
    private String getClassDirPath(ClassVO classVO)
    {
        return getFilePath(true, strOutputDir, getModuleId(classVO));
    }
    
    /***************************************************************************
     * 实体文件全路径的绝对路径
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-4-26 10:21:59
     ***************************************************************************/
    private String getClassFilePath(ClassVO classVO)
    {
        return getFilePath(false, getClassDirPath(classVO), classVO.getDefaultTableName() + "_" + classVO.getId() + ".html");
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
     * 连接 strPaths 中指定的路径名为字符串路径
     * @param isDir
     * @param strPaths
     * @return String
     * @author Rocex Wang
     * @version 2020-4-26 10:22:44
     ***************************************************************************/
    private String getFilePath(boolean isDir, String... strPaths)
    {
        if (strPaths == null || strPaths.length == 0)
        {
            return ".";
        }
        
        StringBuffer strbuffPath = new StringBuffer();
        
        for (int i = 0; i < strPaths.length; i++)
        {
            strbuffPath.append(strPaths[i]).append(File.separator);
        }
        
        if (!isDir && strbuffPath.charAt(strbuffPath.length() - 1) == File.separatorChar)
        {
            strbuffPath.deleteCharAt(strbuffPath.length() - 1);
        }
        
        return strbuffPath.toString();
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
        
        return (moduleVO == null ? componentVO.getOwnModule() : moduleVO.getId()).toLowerCase();
    }
}
