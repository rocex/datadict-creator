package org.rocex.datadict;

import java.io.File;
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
        
        settings = StringHelper.load("settings" + File.separator + "settings-" + args[0] + ".properties");
        
        CreateDataDictAction action = new CreateDataDictAction();
        
        long lStart = System.currentTimeMillis();
        
        action.doAction();
        
        Logger.getLogger().debug("耗时:" + (System.currentTimeMillis() - lStart) / 1000 + "s");
    }
}
