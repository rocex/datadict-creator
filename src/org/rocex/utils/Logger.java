package org.rocex.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2019-5-21 11:13:04
 ***************************************************************************/
public class Logger
{
    public static int iLoggerLevelDebug = 20;
    public static int iLoggerLevelError = 30;
    public static int iLoggerLevelOff = 9999;
    public static int iLoggerLevelTrace = 10;

    private static Logger logger;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int iEnableLevel = Logger.iLoggerLevelDebug;

    /***************************************************************************
     * @return Logger
     * @author Rocex Wang
     * @version 2019-6-13 10:49:25
     ***************************************************************************/
    public static Logger getLogger()
    {
        if (logger == null)
        {
            logger = new Logger();
        }

        return logger;
    }

    /***************************************************************************
     * @param strMsg
     * @author Rocex Wang
     * @version 2020-4-22 10:16:37
     ***************************************************************************/
    public void debug(String strMsg)
    {
        log(iLoggerLevelDebug, "debug", strMsg);
    }

    /***************************************************************************
     * @param strMsg
     * @author Rocex Wang
     * @version 2019-6-13 10:49:09
     ***************************************************************************/
    public void error(String strMsg)
    {
        log(iLoggerLevelError, "error", strMsg);
    }

    /***************************************************************************
     * @param strMsg
     * @param ex
     * @author Rocex Wang
     * @version 2019-6-13 10:49:11
     ***************************************************************************/
    public void error(String strMsg, Throwable ex)
    {
        log(iLoggerLevelError, "error", strMsg);

        if (iLoggerLevelError >= iEnableLevel)
        {
            ex.printStackTrace();
        }
    }

    public int getEnableLevel()
    {
        return iEnableLevel;
    }

    /***************************************************************************
     * @param iLevel
     * @param strMsg
     * @author Rocex Wang
     * @version 2020-5-28 10:36:48
     ***************************************************************************/
    protected void log(int iLevel, String strLevel, String strMsg)
    {
        if (iLevel < iEnableLevel)
        {
            return;
        }

        System.out.println(LocalDateTime.now().format(formatter) + " [" + strLevel + "] " + strMsg);
    }

    /***************************************************************************
     * @param enableLevel the enableLevel to set
     * @author Rocex Wang
     * @version 2020-5-26 19:50:34
     ***************************************************************************/
    public void setEnableLevel(int enableLevel)
    {
        iEnableLevel = enableLevel;
    }

    /***************************************************************************
     * @param enableLevel trace < debug < error
     * @author Rocex Wang
     * @version 2020-5-28 10:03:04
     ***************************************************************************/
    public void setEnableLevel(String enableLevel)
    {
        if ("trace".equalsIgnoreCase(enableLevel))
        {
            setEnableLevel(iLoggerLevelTrace);
        }
        else if ("debug".equalsIgnoreCase(enableLevel))
        {
            setEnableLevel(iLoggerLevelDebug);
        }
        else if ("error".equalsIgnoreCase(enableLevel))
        {
            setEnableLevel(iLoggerLevelError);
        }
        else
        {
            setEnableLevel(iLoggerLevelOff);
        }
    }
    
    /***************************************************************************
     * @param strMsg
     * @author Rocex Wang
     * @version 2019-6-13 10:49:13
     ***************************************************************************/
    public void trace(String strMsg)
    {
        log(iLoggerLevelTrace, "trace", strMsg);
    }
}
