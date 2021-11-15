package org.rocex.datadict;

import java.io.File;
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
    protected static Properties settings = FileHelper.load("settings" + File.separator + "settings.properties");
    
    /***************************************************************************
     * @param args
     * @author Rocex Wang
     * @version 2020-4-21 15:43:05
     ***************************************************************************/
    public static void main(String[] args)
    {
        TimerLogger.getLogger().begin("create all data dictionary");
        
        String strCreateType = settings.getProperty("createType", "json");
        
        String strDataDictVersionList = settings.getProperty("DataDictVersionList");
        
        String[] strDataDictVersions = strDataDictVersionList.split(",");
        
        for (String strVersion : strDataDictVersions)
        {
            String strDataDictVersion = settings.getProperty(strVersion + ".DataDictVersion");
            
            new SyncDBSchemaAction(strVersion).doAction(null);
            
            TimerLogger.getLogger().begin("create data dictionary " + strDataDictVersion);
            
            IAction action = "html".equalsIgnoreCase(strCreateType) ? new CreateHtmlDataDictAction(strVersion) : new CreateJsonDataDictAction(strVersion);
            
            action.doAction(null);
            
            TimerLogger.getLogger().end("create data dictionary " + strDataDictVersion);
            
            Logger.getLogger().debug("\n");
        }
        
        TimerLogger.getLogger().end("create all data dictionary");
    }
}
