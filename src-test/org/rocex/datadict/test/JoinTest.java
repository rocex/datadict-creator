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
        Logger.getLogger().start("for StringBuffer");
        StringBuilder strbuff = new StringBuilder();
        for (String s : strTest)
        {
            strbuff.append(",").append(s);
        }

        str = strbuff.substring(1);

        Logger.getLogger().stop("for StringBuffer");

        //
        Logger.getLogger().start("for StringBuilder");
        StringBuilder strbuilder = new StringBuilder();
        for (String s : strTest)
        {
            strbuilder.append(",").append(s);
        }

        str = strbuilder.substring(1);

        Logger.getLogger().stop("for StringBuilder");

        //
        Logger.getLogger().start("String.join");

        str = String.join(",", strTest);

        Logger.getLogger().stop("String.join");

        //
        Logger.getLogger().start("stream");

        String collect = Arrays.asList(strTest).stream().collect(Collectors.joining(","));

        Logger.getLogger().stop("stream");

        //
        Logger.getLogger().start("StringJoiner");
        StringJoiner strJoiner = new StringJoiner(",");
        for (String s : strTest)
        {
            strJoiner.add(s);
        }

        str = strJoiner.toString();
        Logger.getLogger().stop("StringJoiner");
    }
}
