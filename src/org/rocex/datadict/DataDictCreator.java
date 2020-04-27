package org.rocex.datadict;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.rocex.utils.Logger;
import org.rocex.utils.StringHelper;

/***************************************************************************
 * 根据数据库中元数据生成nc数据字典<br>
 * @author Rocex Wang
 * @version 2020-4-21 15:43:05
 ***************************************************************************/
public class DataDictCreator
{
    public static Properties settings = StringHelper.load("settings" + File.separator + "settings.properties");
    public static Properties settingsHtml = StringHelper.load("settings" + File.separator + "settings-html.properties");
    
    /***************************************************************************
     * @param args
     * @author Rocex Wang
     * @version 2020-4-21 15:43:05
     ***************************************************************************/
    public static void main(String[] args)
    {
        if (args == null || args.length == 0)
        {
            Logger.getLogger().debug("缺少版本参数！");
            return;
        }
        
        long lStart = System.currentTimeMillis();
        
        try
        {
            settingsHtml.setProperty("HtmlIndexFile",
                    new String(Files.readAllBytes(new File("settings" + File.separator + "DataDictIndexFile.html").toPath())));
            settingsHtml.setProperty("HtmlDataDictFile", new String(Files.readAllBytes(new File("settings" + File.separator + "DataDictFile.html").toPath())));
            settingsHtml.setProperty("HtmlDataDictRow", new String(Files.readAllBytes(new File("settings" + File.separator + "DataDictRow.html").toPath())));
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        String strDataDictVersionList = settings.getProperty("DataDictVersionList");
        
        args = strDataDictVersionList.split(",");
        
        for (String strVersion : args)
        {
            // settings = StringHelper.load("settings" + File.separator + "settings-" + strVersion + ".properties");
            
            CreateDataDictAction action = new CreateDataDictAction(strVersion);
            
            action.doAction();
        }
        
        Logger.getLogger().debug("耗时:" + (System.currentTimeMillis() - lStart) / 1000 + "s");
    }
}
