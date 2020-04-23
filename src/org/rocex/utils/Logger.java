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
    private static Logger logger;
    
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
        System.out.println(DateFormat.getDateTimeInstance().format(new Date()) + " " + strMsg);
    }
    
    /***************************************************************************
     * @param strMsg
     * @author Rocex Wang
     * @version 2019-6-13 10:49:09
     ***************************************************************************/
    public void error(String strMsg)
    {
        System.err.println(DateFormat.getDateTimeInstance().format(new Date()) + " " + strMsg);
    }
    
    /***************************************************************************
     * @param strMsg
     * @param ex
     * @author Rocex Wang
     * @version 2019-6-13 10:49:11
     ***************************************************************************/
    public void error(String strMsg, Throwable ex)
    {
        System.err.println(DateFormat.getDateTimeInstance().format(new Date()) + " " + strMsg);
        ex.printStackTrace();
    }
    
    /***************************************************************************
     * @param strMsg
     * @author Rocex Wang
     * @version 2019-6-13 10:49:13
     ***************************************************************************/
    public void trace(String strMsg)
    {
        // System.out.println(DateFormat.getDateTimeInstance().format(new Date()) + " " + strMsg);
    }
    
    /***************************************************************************
     * @param strMsg
     * @param ex
     * @author Rocex Wang
     * @version 2019-6-13 10:49:16
     ***************************************************************************/
    public void trace(String strMsg, Throwable ex)
    {
        System.out.println(DateFormat.getDateTimeInstance().format(new Date()) + " " + strMsg);
        ex.printStackTrace();
    }
}
