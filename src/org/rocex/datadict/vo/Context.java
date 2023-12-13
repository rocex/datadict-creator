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
        settings = FileHelper.load("data" + File.separator + "settings.properties");
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

    public void setSetting(String strKey, String strValue)
    {
        settings.setProperty(strKey, strValue);
    }

    public String getSetting(String strKey)
    {
        return settings.getProperty(strKey);
    }
}
