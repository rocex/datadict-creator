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

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 15:43:05
 ***************************************************************************/
public class DataDictCreator
{
    private Map<String, ? extends MetaVO> mapClassVO = new HashMap<>();
    private Map<String, ? extends MetaVO> mapComponentVO = new HashMap<>();
    private Map<String, String> mapEnumString = new HashMap<>();
    private Map<String, ? extends MetaVO> mapModuleVO = new HashMap<>();
    
    private SQLExecutor sqlExecutor = new SQLExecutor();
    
    private String strOutputDir = "C:\\datadict\\";
    
    /***************************************************************************
     * @param args
     * @author Rocex Wang
     * @version 2020-4-21 15:43:05
     ***************************************************************************/
    public static void main(String[] args)
    {
        DataDictCreator creator = new DataDictCreator();
        
        creator.doAction();
    }
    
    private String buildFilePath(boolean isDir, String... strPaths)
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
    
    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-21 15:47:35
     ***************************************************************************/
    protected void doAction()
    {
        String strModuleSQL = "select id,name,displayname from md_module order by lower(name)";
        String strComponentSQL = "select id,name,displayname,namespace,ownmodule from md_component";
        String strClassSQL = "select id,name,displayname,defaulttablename,fullclassname,keyattribute,componentid,classtype from md_class";
        String strPropertySQL = "select distinct a.id id,a.name name,a.displayname displayname,attrlength,attrminvalue,attrmaxvalue,attrsequence,datatype,datatypestyle,a.defaultvalue defaultvalue"
                + ",a.nullable nullable,a.precise precise,refmodelname,classid,b.sqldatetype sqldatetype,b.pkey"
                + " from md_property a left join md_column b on a.name=b.name where classid=? and b.tableid=? order by b.pkey desc,a.attrsequence";
        
        try
        {
            List<ModuleVO> listModuleVO = (List<ModuleVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(ModuleVO.class), strModuleSQL);
            List<ComponentVO> listComponentVO = (List<ComponentVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(ComponentVO.class), strComponentSQL);
            List<ClassVO> listClassVO = (List<ClassVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(ClassVO.class), strClassSQL);
            
            mapModuleVO = toMap(listModuleVO);
            mapComponentVO = toMap(listComponentVO);
            mapClassVO = toMap(listClassVO);
            
            int count = 0;
            
            for (ClassVO classVO : listClassVO)
            {
                if (count++ > 10)
                {
                    // break;
                }
                
                if (classVO.getClassType() != 201)
                {
                    continue;
                }
                
                // 取实体所有属性
                SQLParameter para = new SQLParameter();
                para.addParam(classVO.getId());
                para.addParam(classVO.getDefaultTableName());
                
                List<PropertyVO> listPropertyVO = (List<PropertyVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(PropertyVO.class), strPropertySQL, para);
                
                String strHtmlRow = "";
                
                for (int i = 0; i < listPropertyVO.size(); i++)
                {
                    PropertyVO propertyVO = listPropertyVO.get(i);
                    
                    ClassVO refClassVO = (ClassVO) mapClassVO.get(propertyVO.getDataType());
                    
                    // 引用实体模型
                    String strRefClassPathHref = refClassVO.getDisplayName();
                    
                    if (refClassVO.getClassType() == 201)
                    {
                        String strRefClassPath = getAbsClassFilePath(refClassVO);
                        
                        strRefClassPathHref = MessageFormat.format("<a href=\"{0}\">{1}</a>", strRefClassPath, refClassVO.getDisplayName());
                    }
                    
                    // 数据库类型
                    String strDbType = propertyVO.getSqlDateType().contains("char") ? propertyVO.getSqlDateType() + "(" + propertyVO.getAttrLength() + ")"
                            : propertyVO.getSqlDateType();
                    
                    // 枚举
                    
                    strHtmlRow += MessageFormat.format(classVO.getKeyAttribute().equals(propertyVO.getId()) ? DataDictHtml.strPkRow : DataDictHtml.strRow,
                            i + 1, propertyVO.getName(), propertyVO.getDisplayName(), propertyVO.getName(), strDbType,
                            "N".equals(propertyVO.getNullable()) ? "  √" : "", strRefClassPathHref + " (" + refClassVO.getName() + ")",
                            propertyVO.getDefaultValue() == null ? "" : propertyVO.getDefaultValue(), getEnumString(propertyVO));
                }
                
                String strHtml = MessageFormat.format(DataDictHtml.strHtml, classVO.getDisplayName() + " " + classVO.getDefaultTableName(),
                        classVO.getFullClassname(), strHtmlRow, DateFormat.getDateTimeInstance().format(new Date()));
                
                writeFile(classVO, strHtml);
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
        return buildFilePath(false, "..", getModuleId(classVO), classVO.getDefaultTableName() + "_" + classVO.getId() + ".html");
    }
    
    private String getClassDirPath(ClassVO classVO)
    {
        return buildFilePath(true, strOutputDir, getModuleId(classVO));
    }
    
    private String getClassFilePath(ClassVO classVO)
    {
        return buildFilePath(false, getClassDirPath(classVO), classVO.getDefaultTableName() + "_" + classVO.getId() + ".html");
    }
    
    protected String getEnumString(PropertyVO propertyVO)
    {
        if (mapEnumString.containsKey(propertyVO.getDataType()))
        {
            return mapEnumString.get(propertyVO.getDataType());
        }
        
        String strEnumValueSQL = "select id,name,value,enumsequence from md_enumvalue where id=? order by enumsequence";
        
        SQLParameter para = new SQLParameter();
        para.addParam(propertyVO.getDataType());
        
        List<EnumVO> listEnumValueVO = null;
        
        try
        {
            listEnumValueVO = (List<EnumVO>) sqlExecutor.executeQuery(new BeanListProcessor<>(EnumVO.class), strEnumValueSQL, para);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        if (listEnumValueVO == null || listEnumValueVO.size() == 0)
        {
            return "";
        }
        
        String strEnum = "";
        
        for (EnumVO metaVO : listEnumValueVO)
        {
            strEnum += metaVO.getName() + "=" + metaVO.getValue() + "; ";
        }
        
        mapEnumString.put(propertyVO.getDataType(), strEnum);
        
        return strEnum;
    }
    
    private String getModuleId(ClassVO classVO)
    {
        ComponentVO componentVO = (ComponentVO) mapComponentVO.get(classVO.getComponentId());
        
        ModuleVO moduleVO = (ModuleVO) mapModuleVO.get(componentVO.getOwnModule());
        
        return (moduleVO == null ? componentVO.getOwnModule() : moduleVO.getId()).toLowerCase();
    }
    
    private Map<String, ? extends MetaVO> toMap(List<? extends MetaVO> listMetaVO)
    {
        Map<String, MetaVO> mapMetaVO = new HashMap<>();
        
        for (MetaVO metaVO : listMetaVO)
        {
            mapMetaVO.put(metaVO.getId(), metaVO);
        }
        
        return mapMetaVO;
    }
    
    private void writeFile(ClassVO classVO, String strContent)
    {
        File file = new File(getClassDirPath(classVO));
        
        if (!file.exists())
        {
            file.mkdir();
        }
        
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        
        try
        {
            fileWriter = new FileWriter(getClassFilePath(classVO));
            
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
