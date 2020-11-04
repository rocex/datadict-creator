package org.rocex.utils;

import java.util.Comparator;

/***************************************************************************
 * 按照自然顺序排序，就像windows资源管理器里面的文件名排序<br>
 * 形如： a1,a2,a10,a11，而不是：a1,a10,a11,a2
 * @param <T>
 * @author Rocex Wang
 * @version 2020-10-23 21:57:45
 ***************************************************************************/
public class NaturalComparator<T> implements Comparator<T>
{
    char charAt(String s, int i)
    {
        return i >= s.length() ? 0 : s.charAt(i);
    }
    
    @Override
    public int compare(T obj1, T obj2)
    {
        String strKey1 = getCompareKey(obj1);
        String strKey2 = getCompareKey(obj2);

        int ia = 0, ib = 0;
        int nza = 0, nzb = 0;
        char ca, cb;

        while (true)
        {
            // Only count the number of zeroes leading the last number compared
            nza = nzb = 0;

            ca = charAt(strKey1, ia);
            cb = charAt(strKey2, ib);

            // skip over leading spaces or zeros
            while (Character.isSpaceChar(ca) || ca == '0')
            {
                if (ca == '0')
                {
                    nza++;
                }
                else
                {
                    // Only count consecutive zeroes
                    nza = 0;
                }

                // if the next character isn't a digit, then we've had a run of only zeros
                // we still need to treat this as a 0 for comparison purposes
                if (!Character.isDigit(charAt(strKey1, ia + 1)))
                {
                    break;
                }

                ca = charAt(strKey1, ++ia);
            }

            while (Character.isSpaceChar(cb) || cb == '0')
            {
                if (cb == '0')
                {
                    nzb++;
                }
                else
                {
                    // Only count consecutive zeroes
                    nzb = 0;
                }

                // if the next character isn't a digit, then we've had a run of only zeros
                // we still need to treat this as a 0 for comparison purposes
                if (!Character.isDigit(charAt(strKey2, ib + 1)))
                {
                    break;
                }

                cb = charAt(strKey2, ++ib);
            }

            // Process run of digits
            if (Character.isDigit(ca) && Character.isDigit(cb))
            {
                int bias = compareRight(strKey1.substring(ia), strKey2.substring(ib));
                if (bias != 0)
                {
                    return bias;
                }
            }

            if (ca == 0 && cb == 0)
            {
                // The strings compare the same. Perhaps the caller
                // will want to call strcmp to break the tie.
                return compareEqual(strKey1, strKey2, nza, nzb);
            }
            if (ca < cb)
            {
                return -1;
            }
            if (ca > cb)
            {
                return +1;
            }

            ++ia;
            ++ib;
        }
    }

    int compareEqual(String a, String b, int nza, int nzb)
    {
        if (nza - nzb != 0)
        {
            return nza - nzb;
        }

        if (a.length() == b.length())
        {
            return a.compareTo(b);
        }

        return a.length() - b.length();
    }

    int compareRight(String a, String b)
    {
        int bias = 0, ia = 0, ib = 0;

        // The longest run of digits wins. That aside, the greatest
        // value wins, but we can't know that it will until we've scanned
        // both numbers to know that they have the same magnitude, so we
        // remember it in BIAS.
        for (;; ia++, ib++)
        {
            char ca = charAt(a, ia);
            char cb = charAt(b, ib);

            if (!isDigit(ca) && !isDigit(cb))
            {
                return bias;
            }
            if (!isDigit(ca))
            {
                return -1;
            }
            if (!isDigit(cb))
            {
                return +1;
            }
            if (ca == 0 && cb == 0)
            {
                return bias;
            }

            if (bias == 0)
            {
                if (ca < cb)
                {
                    bias = -1;
                }
                else if (ca > cb)
                {
                    bias = +1;
                }
            }
        }
    }

    String getCompareKey(T obj)
    {
        return String.valueOf(obj);
    }

    boolean isDigit(char c)
    {
        return Character.isDigit(c) || c == '.' || c == ',';
    }
}