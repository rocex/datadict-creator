package org.rocex.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/***************************************************************************
 * 计算耗时<br>
 * @author Rocex Wang
 * @version 2020-5-8 14:43:26
 ***************************************************************************/
public class TimerLogger extends Logger
{
    private static TimerLogger timerLogger;
    
    private DecimalFormat df = new DecimalFormat("#.###");// 格式化小数，不足的补 空格
    
    private Map<String, Long> mapTimer = new HashMap<>();
    
    /***************************************************************************
     * @return TimerLogger
     * @author Rocex Wang
     * @version 2020-5-8 14:54:34
     ***************************************************************************/
    public static TimerLogger getLogger()
    {
        if (timerLogger == null)
        {
            timerLogger = new TimerLogger();
        }
        
        return timerLogger;
    }
    
    /***************************************************************************
     * @param strMessage 必须和 end(String) 的参数值相同
     * @author Rocex Wang
     * @version 2020-5-8 14:54:39
     ***************************************************************************/
    public void begin(String strMessage)
    {
        mapTimer.put(strMessage, System.currentTimeMillis());
    }
    
    /***************************************************************************
     * @param strMessage 必须和 start(String) 的参数值相同
     * @author Rocex Wang
     * @version 2020-5-8 14:54:37
     ***************************************************************************/
    public void end(String strMessage)
    {
        if (!mapTimer.containsKey(strMessage))
        {
            return;
        }
        
        double lTime = System.currentTimeMillis() - mapTimer.get(strMessage);
        
        debug("[" + strMessage + "] 耗时: " + df.format(lTime) + "ms, " + df.format(lTime / 1000) + "s, " + df.format(lTime / 60000) + "m");
    }
}
