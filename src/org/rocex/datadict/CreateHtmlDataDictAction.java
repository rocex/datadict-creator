package org.rocex.datadict;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-22 14:10:00
 ***************************************************************************/
public class CreateHtmlDataDictAction extends CreateDataDictAction
{
    static
    {
        try
        {
            DataDictCreator.settings.setProperty("HtmlIndexFile", new String(Files.readAllBytes(Paths.get("settings", "html", "template", "index.html"))));
            DataDictCreator.settings.setProperty("HtmlDataDictFile",
                    new String(Files.readAllBytes(Paths.get("settings", "html", "template", "DataDictFile.html"))));
            DataDictCreator.settings.setProperty("HtmlDataDictRow",
                    new String(Files.readAllBytes(Paths.get("settings", "html", "template", "DataDictRow.html"))));
            DataDictCreator.settings.setProperty("HtmlDataDictFooterFile",
                    new String(Files.readAllBytes(Paths.get("settings", "html", "template", "DataDictFooterFile.html"))));
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }

    protected String strHtmlDataDictRow = DataDictCreator.settings.getProperty("HtmlDataDictRow");
    
    protected StringBuilder strHtmlRows = new StringBuilder();
    
    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-26 14:52:18
     ***************************************************************************/
    public CreateHtmlDataDictAction(String strVersion)
    {
        super(strVersion);
    }
    
    @Override
    protected void createDataDictFile(ClassVO classVO, List<PropertyVO> listPropertyVO)
    {
        if (listPropertyVO == null || listPropertyVO.isEmpty())
        {
            return;
        }
        
        strHtmlRows.setLength(0);
        
        super.createDataDictFile(classVO, listPropertyVO);

        // 组件内实体列表链接
        String strClassList = classVO.getClassListUrl();

        if (strClassList.startsWith("/")) // 截掉最前面的斜杠
        {
            strClassList = strClassList.substring(1);
        }

        String strFullClassname = classVO.getFullClassname() == null ? "" : " / " + classVO.getFullClassname();

        String strHtml = MessageFormat.format(DataDictCreator.settings.getProperty("HtmlDataDictFile"), classVO.getDisplayName(), classVO.getDefaultTableName(),
                strFullClassname, DataDictCreator.settings.get(strVersion + ".DataDictVersion"), strClassList, strHtmlRows, getFooter());

        FileHelper.writeFileThread(getClassFilePath(classVO), strHtml);
    }
    
    @Override
    protected void createDataDictFileRow(ClassVO classVO, PropertyVO propertyVO, int iRowIndex)
    {
        super.createDataDictFileRow(classVO, propertyVO, iRowIndex);
        
        // 数据库类型
        String strDbType = propertyVO.getDataTypeSql();
        
        // 是否必输
        String strMustInput = propertyVO.isNullable() ? "" : "√";
        
        // 每行的css，主键行字体红色，其它行正常黑色
        List<String> listPk = Arrays.asList(classVO.getKeyAttribute().split(";"));
        String strRowStyle = listPk.contains(propertyVO.getId()) ? "pk-row" : "";

        String strHtmlRow = MessageFormat.format(strHtmlDataDictRow, strRowStyle, iRowIndex + 1, propertyVO.getName(), propertyVO.getDisplayName(),
                propertyVO.getName(), strDbType, strMustInput, propertyVO.getRefClassPathHref(), propertyVO.getDefaultValue(), propertyVO.getDataScope());

        strHtmlRows.append(strHtmlRow);
    }
    
    /***************************************************************************
     * 实体文件全路径的绝对路径
     * @param classVO
     * @return Path
     * @author Rocex Wang
     * @version 2020-4-26 10:21:59
     ***************************************************************************/
    @Override
    protected Path getClassFilePath(ClassVO classVO)
    {
        return Paths.get(strOutputDictDir, getMappedClassId(classVO) + ".html");
    }

    /***************************************************************************
     * 实体的访问url，相对于根目录
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-5-2 14:26:12
     ***************************************************************************/
    @Override
    protected String getClassUrl(ClassVO classVO)
    {
        return "./dict/" + getMappedClassId(classVO) + ".html";
    }

    /***************************************************************************
     * 实体的访问url，相对于当前目录
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-5-2 14:30:53
     ***************************************************************************/
    @Override
    protected String getClassUrl2(ClassVO classVO)
    {
        return "./" + getMappedClassId(classVO) + ".html";
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
}
