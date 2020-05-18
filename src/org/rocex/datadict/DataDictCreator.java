package org.rocex.datadict;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;
import org.rocex.utils.TimerLogger;

/***************************************************************************
 * 根据数据库中元数据生成nc数据字典<br>
 * @author Rocex Wang
 * @version 2020-4-21 15:43:05
 ***************************************************************************/
public class DataDictCreator
{
    public static Properties settings = FileHelper.load("settings" + File.separator + "settings.properties");
    
    /***************************************************************************
     * @param args
     * @author Rocex Wang
     * @version 2020-4-21 15:43:05
     ***************************************************************************/
    public static void main(String[] args)
    {
        TimerLogger.getLogger().begin("create data dictionary");
        
        try
        {
            settings.setProperty("HtmlIndexFile", new String(Files.readAllBytes(Paths.get("settings", "template", "index.html"))));
            settings.setProperty("HtmlDataDictFile", new String(Files.readAllBytes(Paths.get("settings", "template", "DataDictFile.html"))));
            settings.setProperty("HtmlDataDictRow", new String(Files.readAllBytes(Paths.get("settings", "template", "DataDictRow.html"))));
            settings.setProperty("HtmlDataDictFooterFile", new String(Files.readAllBytes(Paths.get("settings", "template", "DataDictFooterFile.html"))));
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        String strDataDictVersionList = settings.getProperty("DataDictVersionList");
        
        String[] strDataDictVersions = strDataDictVersionList.split(",");
        
        for (String strVersion : strDataDictVersions)
        {
            String strDataDictVersion = settings.getProperty(strVersion + ".DataDictVersion");
            
            TimerLogger.getLogger().begin("create data dictionary(" + strDataDictVersion + ")");
            
            CreateDataDictAction action = new CreateDataDictAction(strVersion);
            
            action.doAction();
            
            TimerLogger.getLogger().end("create data dictionary(" + strDataDictVersion + ")");
        }
        
        TimerLogger.getLogger().end("create data dictionary");
    }
}
