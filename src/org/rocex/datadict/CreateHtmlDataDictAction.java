package org.rocex.datadict;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import org.rocex.datadict.vo.ClassVO;
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

    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-26 14:52:18
     ***************************************************************************/
    public CreateHtmlDataDictAction(String strVersion)
    {
        super(strVersion);
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
        return "./ddc/" + getMappedClassId(classVO) + ".html";
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
    @Override
    protected String getFooter()
    {
        String strFooter = DataDictCreator.settings.getProperty("HtmlDataDictFooterFile");
        
        strFooter = MessageFormat.format(strFooter, strCreateTime);
        
        return strFooter;
    }
}
