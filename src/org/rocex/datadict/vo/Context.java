package org.rocex.datadict.vo;

import org.rocex.utils.FileHelper;

import java.io.File;
import java.util.Properties;

public class Context
{
    public static Properties settings;

    private static Context context;

    private Context()
    {
        settings = FileHelper.load("settings" + File.separator + "settings.properties");
    }

    public static Context getInstance()
    {
        if (context == null)
            context = new Context();

        return context;
    }

    public String getSetting(String strKey, String strDefaultValue)
    {
        return settings.getProperty(strKey, strDefaultValue);
    }

    public String setSetting(String strKey, String strValue)
    {
        return settings.getProperty(strKey, strValue);
    }

    public String getSetting(String strKey)
    {
        return settings.getProperty(strKey);
    }
}
