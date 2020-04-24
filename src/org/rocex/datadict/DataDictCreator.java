package org.rocex.datadict;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.rocex.utils.Logger;
import org.rocex.utils.StringHelper;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 15:43:05
 ***************************************************************************/
public class DataDictCreator
{
    public static Properties settings = StringHelper.load("settings.properties");
    
    private Map<String, ? extends MetaVO> mapClassVO = new HashMap<>();
    private Map<String, String> mapClassVOByComponent = new HashMap<>();
    private Map<String, ? extends MetaVO> mapComponentVO = new HashMap<>();
    private Map<String, String> mapEnumString = new HashMap<>();
    private Map<String, ? extends MetaVO> mapModuleVO = new HashMap<>();
    
    private SQLExecutor sqlExecutor = new SQLExecutor();
    
    private String strOutputDir = settings.getProperty("OutputDir");
    
    /***************************************************************************
     * @param args
     * @author Rocex Wang
     * @version 2020-4-21 15:43:05
     ***************************************************************************/
    public static void main(String[] args)
    {
        DataDictCreator creator = new DataDictCreator();
        
        long lStart = System.currentTimeMillis();
        
        creator.doAction();
        
        Logger.getLogger().debug("耗时:" + (System.currentTimeMillis() - lStart) / 1000 + "s");
    }
    
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
    
    private Map<String, ? extends MetaVO> buildMap(List<? extends MetaVO> listMetaVO)
    {
        Map<String, MetaVO> mapMetaVO = new HashMap<>();
        
        for (MetaVO metaVO : listMetaVO)
        {
            mapMetaVO.put(metaVO.getId(), metaVO);
        }
        
        return mapMetaVO;
    }
    
    private void closeQuietly(Closeable... closeables)
    {
        if (closeables == null || closeables.length == 0)
        {
            return;
        }
        
        for (Closeable closeable : closeables)
        {
            if (closeable != null)
            {
                try
                {
                    closeable.close();
                }
                catch (IOException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        }
    }
    
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
            String strDbType = propertyVO.getSqlDateType().contains("char") ? propertyVO.getSqlDateType() + "(" + propertyVO.getAttrLength() + ")"
                    : propertyVO.getSqlDateType();
            
            // 默认值
            String strDefaultValue = propertyVO.getDefaultValue() == null ? "" : propertyVO.getDefaultValue();
            
            // 枚举
            String strEnumString = "";
            if (propertyVO.getDataType().length() > 20 && propertyVO.getRefModelName() == null && propertyVO.getDataTypeStyle() == 300
                    && mapEnumString.containsKey(propertyVO.getDataType()))
            {
                strEnumString = mapEnumString.get(propertyVO.getDataType());
            }
            
            String strHtmlRow = classVO.getKeyAttribute().equals(propertyVO.getId()) ? DataDictHtml.strPkRow : DataDictHtml.strRow;
            
            // 是否必输
            String strMustInput = "N".equals(propertyVO.getNullable()) ? "  √" : "";
            
            strHtmlRows += MessageFormat.format(strHtmlRow, i + 1, propertyVO.getName(), propertyVO.getDisplayName(), propertyVO.getName(), strDbType,
                    strMustInput, strRefClassPathHref, strDefaultValue, strEnumString);
        }
        
        ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());
        
        String _string = classVO.getFullClassname() + " " + mapClassVOByComponent.get(componentVO.getId());
        
        String strHtml = MessageFormat.format(DataDictHtml.strHtml, classVO.getDisplayName() + " " + classVO.getDefaultTableName(),
                settings.get("DataDictVersion"), _string, strHtmlRows, DateFormat.getDateTimeInstance().format(new Date()));
        
        writeFile(getClassFilePath(classVO), strHtml);
    }
    
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
        
        String strHtml = MessageFormat.format(DataDictHtml.strHtmlIndex, settings.get("DataDictVersion"), strContent,
                DateFormat.getDateTimeInstance().format(new Date()));
        
        writeFile(getFilePath(false, strOutputDir, "index.html"), strHtml);
    }
    
    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-21 15:47:35
     ***************************************************************************/
    protected void doAction()
    {
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
    
    private String getAbsClassFilePath(ClassVO classVO)
    {
        return getFilePath(false, "..", getModuleId(classVO), classVO.getDefaultTableName() + "_" + classVO.getId() + ".html");
    }
    
    private String getClassDirPath(ClassVO classVO)
    {
        return getFilePath(true, strOutputDir, getModuleId(classVO));
    }
    
    private String getClassFilePath(ClassVO classVO)
    {
        return getFilePath(false, getClassDirPath(classVO), classVO.getDefaultTableName() + "_" + classVO.getId() + ".html");
    }
    
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
    
    private String getModuleId(ClassVO classVO)
    {
        ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());
        
        ModuleVO moduleVO = (ModuleVO) mapModuleVO.get(componentVO.getOwnModule());
        
        return (moduleVO == null ? componentVO.getOwnModule() : moduleVO.getId()).toLowerCase();
    }
    
    private void writeFile(String strFilePath, String strContent)
    {
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        
        try
        {
            File file = new File(strFilePath);
            
            if (!file.getParentFile().exists())
            {
                file.getParentFile().mkdir();
            }
            
            fileWriter = new FileWriter(file);
            
            bufferedWriter = new BufferedWriter(fileWriter);
            
            bufferedWriter.write(strContent);
            bufferedWriter.flush();
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        finally
        {
            closeQuietly(bufferedWriter, fileWriter);
        }
    }
}
