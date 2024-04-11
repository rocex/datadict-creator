package org.rocex.datadict.vo;

import org.rocex.utils.FileHelper;

import java.io.File;
import java.util.Properties;

public class Context
{
    private static Context context;
    
    public static Properties settings;
    
    public static Context getInstance()
    {
        if (context == null)
            context = new Context();
        
        return context;
    }
    
    private Context()
    {
        settings = FileHelper.load("data" + File.separator + "settings.properties");
    }
    
    public String getSetting(String strKey)
    {
        return settings.getProperty(strKey);
    }
    
    public String getSetting(String strKey, String strDefaultValue)
    {
        return settings.getProperty(strKey, strDefaultValue);
    }
    
    public void setSetting(String strKey, String strValue)
    {
        settings.setProperty(strKey, strValue);
    }
}
