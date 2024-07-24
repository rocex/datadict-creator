package org.rocex.datadict.vo;

import java.io.File;
import java.util.Properties;

import org.rocex.utils.FileHelper;

public class Context
{
    private static Context context;

    private static Properties settings;

    private Context()
    {
        settings = FileHelper.load("data" + File.separator + "settings.properties");
    }

    public static Context getInstance()
    {
        if (context == null)
        {
            context = new Context();
        }

        return context;
    }

    public String getSetting(String strKey)
    {
        return settings.getProperty(strKey);
    }

    public String getSetting(String strKey, String strDefaultValue)
    {
        return settings.getProperty(strKey, strDefaultValue);
    }

    public String getVersionSetting(String strVersion, String strKey)
    {
        return getSetting(strVersion + "." + strKey, getSetting(strKey));
    }

    public String getVersionSetting(String strVersion, String strKey, String strDefaultValue)
    {
        return getSetting(strVersion + "." + strKey, getSetting(strKey, strDefaultValue));
    }

    public void setSetting(String strKey, String strValue)
    {
        settings.setProperty(strKey, strValue);
    }
}
