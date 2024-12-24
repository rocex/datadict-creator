package org.rocex.datadict.vo;

import java.io.File;
import java.util.Properties;

import org.rocex.utils.FileHelper;
import org.rocex.utils.StringHelper;

public class Context
{
    private static Context context;

    private static Properties settings;

    private Context()
    {
        resetVersion(null);
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

    public void resetVersion(String strVersion)
    {
        settings = FileHelper.load("data" + File.separator + "settings.properties");

        if (StringHelper.isNotBlank(strVersion))
        {
            settings = FileHelper.load("data" + File.separator + "settings-" + strVersion + ".properties", settings);
        }
    }

    public void setSetting(String strKey, String strValue)
    {
        settings.setProperty(strKey, strValue);
    }
}
