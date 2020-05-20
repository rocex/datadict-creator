package org.rocex.utils;

import java.text.DateFormat;
import java.util.Date;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2019-5-21 11:13:04
 ***************************************************************************/
public class Logger
{
    public static int iLoggerLevelDebug = 20;
    public static int iLoggerLevelError = 30;
    public static int iLoggerLevelTrace = 10;
    
    private static Logger logger;
    
    private DateFormat dateFormat = DateFormat.getDateTimeInstance();
    
    private int iEnableLevel = iLoggerLevelDebug;
    
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
        log(iLoggerLevelDebug, strMsg);
    }
    
    /***************************************************************************
     * @param strMsg
     * @author Rocex Wang
     * @version 2019-6-13 10:49:09
     ***************************************************************************/
    public void error(String strMsg)
    {
        log(iLoggerLevelError, strMsg);
    }
    
    /***************************************************************************
     * @param strMsg
     * @param ex
     * @author Rocex Wang
     * @version 2019-6-13 10:49:11
     ***************************************************************************/
    public void error(String strMsg, Throwable ex)
    {
        log(iLoggerLevelError, strMsg);
        
        if (iLoggerLevelError > iEnableLevel)
        {
            ex.printStackTrace();
        }
    }
    
    public void log(int iLevel, String strMsg)
    {
        if (iLevel < iEnableLevel)
        {
            return;
        }
        
        System.out.println(dateFormat.format(new Date()) + " " + strMsg);
    }
    
    /***************************************************************************
     * @param strMsg
     * @author Rocex Wang
     * @version 2019-6-13 10:49:13
     ***************************************************************************/
    public void trace(String strMsg)
    {
        log(iLoggerLevelTrace, strMsg);
    }
}
