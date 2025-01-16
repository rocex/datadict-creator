package org.rocex.datadict.test;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.rocex.utils.Logger;

public class JoinTest
{
    static String[] strTest = new String[9999999];

    public static void test()
    {
        for (int i = 0; i < strTest.length; i++)
        {
            strTest[i] = "i=" + i;
        }

        //
        String str = "";
        /* Logger.getLogger().begin("for +");
        for (int i = 0; i < strTest.length; i++)
        {
            str += "," + strTest[i];
        }
        
        str = str.substring(1);
        
        Logger.getLogger().end("for +"); */

        //
        Logger.getLogger().begin("for StringBuffer");
        StringBuilder strbuff = new StringBuilder();
        for (String s : strTest)
        {
            strbuff.append(",").append(s);
        }

        str = strbuff.substring(1);

        Logger.getLogger().end("for StringBuffer");

        //
        Logger.getLogger().begin("for StringBuilder");
        StringBuilder strbuilder = new StringBuilder();
        for (String s : strTest)
        {
            strbuilder.append(",").append(s);
        }

        str = strbuilder.substring(1);

        Logger.getLogger().end("for StringBuilder");

        //
        Logger.getLogger().begin("String.join");

        str = String.join(",", strTest);

        Logger.getLogger().end("String.join");

        //
        Logger.getLogger().begin("stream");

        String collect = Arrays.asList(strTest).stream().collect(Collectors.joining(","));

        Logger.getLogger().end("stream");

        //
        Logger.getLogger().begin("StringJoiner");
        StringJoiner strJoiner = new StringJoiner(",");
        for (String s : strTest)
        {
            strJoiner.add(s);
        }

        str = strJoiner.toString();
        Logger.getLogger().end("StringJoiner");
    }
}
