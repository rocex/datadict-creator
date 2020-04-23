package org.rocex.db.param;

import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.rocex.utils.Logger;

/***************************************************************************
 * Clob类型<br>
 * @author Rocex Wang
 * @version 2019-8-12 15:25:40
 ***************************************************************************/
public class ClobParamType implements SQLParamType
{
    private int length = 0;
    
    private transient Reader reader = null;
    
    private String s = null;
    
    public ClobParamType(Reader read, int length)
    {
        this.reader = read;
        this.length = length;
    }
    
    public ClobParamType(String s)
    {
        try
        {
            this.s = s;
            length = s.getBytes("iso8859-1").length;
        }
        catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    public int getLength()
    {
        return length;
    }
    
    public Reader getReader()
    {
        if (reader == null)
        {
            reader = new StringReader(s);
        }
        
        return reader;
    }
}
