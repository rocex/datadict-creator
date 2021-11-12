package org.rocex.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2019-6-4 21:24:28
 ***************************************************************************/
public class StringHelper
{
    private static Set<String> setExistId = new HashSet<>();

    public static final String WHITESPACE = " \n\r\f\t";

    /***************************************************************************
     * 驼峰转下划线，例如：userName -> user_name
     * @param strValue
     * @return 驼峰转下划线，例如：userName/UserName -> user_name
     * @author Rocex Wang
     * @version 2019-10-25 16:59:29
     ***************************************************************************/
    public static String camelToUnderline(String strValue)
    {
        if (strValue == null || strValue.trim().length() == 0)
        {
            return strValue;
        }

        String strResult = "";

        char[] chars = strValue.toCharArray();

        for (char ch : chars)
        {
            strResult += ch >= 'A' && ch <= 'Z' ? "_" + (char) (ch + ('a' - 'A')) : ch;
        }

        return strResult.startsWith("_") ? strResult.substring(1) : strResult;
    }

    /***************************************************************************
     * @param objValue
     * @return String
     * @author Rocex Wang
     * @version 2019-6-25 11:10:26
     ***************************************************************************/
    public static String defaultString(Object objValue)
    {
        return defaultString(objValue == null ? null : objValue.toString());
    }

    /***************************************************************************
     * @param strValue
     * @return String
     * @author Rocex Wang
     * @version 2019-6-4 21:37:12
     ***************************************************************************/
    public static String defaultString(String strValue)
    {
        return defaultString(strValue, "");
    }

    /***************************************************************************
     * @param strValue
     * @param strDefault
     * @return String
     * @author Rocex Wang
     * @version 2019-6-4 21:37:09
     ***************************************************************************/
    public static String defaultString(String strValue, String strDefault)
    {
        return strValue == null ? strDefault : strValue;
    }

    /***************************************************************************
     * @param object1
     * @param object2
     * @return boolean
     * @author Rocex Wang
     * @version 2019-5-13 12:10:22
     ***************************************************************************/
    public static boolean equals(Object object1, Object object2)
    {
        if (object1 == object2)
        {
            return true;
        }

        if (object1 == null || object2 == null)
        {
            return false;
        }

        return object1.equals(object2);
    }

    /***************************************************************************
     * @param strValue
     * @return 首字母变小写
     * @author Rocex Wang
     * @version 2019-6-11 11:59:38
     * @see com.jfinal.kit.StrKit
     ***************************************************************************/
    public static String firstCharToLowerCase(String strValue)
    {
        char firstChar = strValue.charAt(0);

        if (firstChar >= 'A' && firstChar <= 'Z')
        {
            char[] chars = strValue.toCharArray();
            chars[0] += 'a' - 'A';

            return new String(chars);
        }

        return strValue;
    }

    /***************************************************************************
     * @param strValue
     * @return 首字母变大写
     * @author Rocex Wang
     * @version 2019-6-11 11:59:51
     * @see com.jfinal.kit.StrKit
     ***************************************************************************/
    public static String firstCharToUpperCase(String strValue)
    {
        char firstChar = strValue.charAt(0);

        if (firstChar >= 'a' && firstChar <= 'z')
        {
            char[] chars = strValue.toCharArray();
            chars[0] -= 'a' - 'A';

            return new String(chars);
        }

        return strValue;
    }

    /***************************************************************************
     * @return String
     * @author Rocex Wang
     * @since 2021-11-11 16:58:55
     ***************************************************************************/
    public static String getId()
    {
        return getRandomNoRepeat(10);
    }

    /*********************************************************************************************************
     * Created on 2004-7-7 11:21:57 <br>
     * @param strSource
     * @return int 返回strSource的长度，以一个英文字符的长度为单位
     ********************************************************************************************************/
    public static int getLength(String strSource)
    {
        return getLength(strSource, false);
    }

    /*********************************************************************************************************
     * Created on 2004-7-7 11:21:57 <br>
     * @param strSource
     * @param blTrim
     * @return int 返回strSource的长度，以一个英文字符的长度为单位
     ********************************************************************************************************/
    public static int getLength(String strSource, boolean blTrim)
    {
        if (strSource == null)
        {
            return 0;
        }

        if (blTrim)
        {
            strSource = strSource.trim();
        }

        int iLength = 0;

        for (int i = 0; i < strSource.length(); i++)
        {
            char strTemp = strSource.charAt(i);

            iLength = iLength + (strTemp >= 0 && strTemp <= 255 ? 1 : 2);
        }

        return iLength;
    }

    /***************************************************************************
     * @param iLength
     * @return 返回定长随机字符串
     * @author Rocex Wang
     * @since 2021-10-19 15:51:46
     ***************************************************************************/
    public static String getRandom(int iLength)
    {
        String strSource = "0123456789abcdefghijklmnopqrstuvwxyz";

        StringBuffer strResult = new StringBuffer();

        // new SecureRandom()
        ThreadLocalRandom.current().ints(iLength, 0, 36).forEach(t ->
        {
            strResult.append(strSource.charAt(t));
        });

        return strResult.toString();
    }

    /***************************************************************************
     * @param iLength
     * @return 返回定长随机字符串，并保证和之前返回的不重复
     * @author Rocex Wang
     * @since 2021-10-19 16:55:07
     ***************************************************************************/
    public static String getRandomNoRepeat(int iLength)
    {
        while (true)
        {
            String strResult = getRandom(iLength);

            if (!setExistId.contains(strResult))
            {
                setExistId.add(strResult);

                return strResult;
            }

            Logger.getLogger().error("生成了重复id，容量: " + setExistId.size());
        }
    }

    /***************************************************************************
     * @return UUID.toString()
     * @author Rocex Wang
     * @version 2019-8-6 13:19:55
     ***************************************************************************/
    public static String getUUID()
    {
        return UUID.randomUUID().toString();
    }

    /***************************************************************************
     * @param strSource
     * @return boolean
     * @author Rocex Wang
     * @version 2019-7-13 16:50:00
     ***************************************************************************/
    public static boolean isEmpty(String strSource)
    {
        return strSource == null || strSource.length() == 0;
    }

    /***************************************************************************
     * @param strSource
     * @return boolean
     * @author Rocex Wang
     * @since 2021-10-28 10:12:17
     ***************************************************************************/
    public static boolean isNotEmpty(String strSource)
    {
        return strSource != null && strSource.length() > 0;
    }

    /***************************************************************************
     * 下划线转驼峰，例如：user_name -> userName
     * @param strValue
     * @return 下划线转驼峰，例如：user_name -> userName
     * @author Rocex Wang
     * @version 2019-10-25 16:44:16
     ***************************************************************************/
    public static String underlineToCamel(String strValue)
    {
        if (strValue == null || strValue.trim().length() == 0 || !strValue.contains("_"))
        {
            return strValue;
        }

        StringBuilder strResult = new StringBuilder();

        String[] strSplits = strValue.split("_");

        for (String strSplit : strSplits)
        {
            strResult.append(firstCharToUpperCase(strSplit));
        }

        return strResult.toString();
    }
}
