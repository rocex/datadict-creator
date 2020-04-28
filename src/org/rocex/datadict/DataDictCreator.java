package org.rocex.datadict;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;

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
        long lStart = System.currentTimeMillis();
        
        try
        {
            settings.setProperty("HtmlIndexFile", new String(Files.readAllBytes(new File("settings" + File.separator + "DataDictIndexFile.html").toPath())));
            settings.setProperty("HtmlDataDictFile", new String(Files.readAllBytes(new File("settings" + File.separator + "DataDictFile.html").toPath())));
            settings.setProperty("HtmlDataDictRow", new String(Files.readAllBytes(new File("settings" + File.separator + "DataDictRow.html").toPath())));
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        String strDataDictVersionList = settings.getProperty("DataDictVersionList");
        
        args = strDataDictVersionList.split(",");
        
        for (String strVersion : args)
        {
            CreateDataDictAction action = new CreateDataDictAction(strVersion);
            
            action.doAction();
        }
        
        Logger.getLogger().debug("耗时:" + (System.currentTimeMillis() - lStart) / 1000 + "s");
    }
}
