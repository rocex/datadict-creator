package org.rocex.db.param;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.rocex.utils.Logger;

/***************************************************************************
 * Blob类型<br>
 * @author Rocex Wang
 * @version 2019-8-12 15:26:13
 ***************************************************************************/
public class BlobParamType implements SQLParamType
{
    private Object blob = null;
    
    private byte bytes[] = null;
    
    private transient InputStream input = null;
    
    private int length = -1;
    
    public BlobParamType(byte[] bytes)
    {
        this.bytes = bytes;
        this.length = bytes.length;
    }
    
    public BlobParamType(InputStream input, int length)
    {
        this.input = input;
        this.length = length;
    }
    
    public BlobParamType(Object blob)
    {
        this.blob = blob;
    }
    
    protected void close(OutputStream out)
    {
        if (out == null)
        {
            return;
        }
        
        try
        {
            out.close();
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    public Object getBlob()
    {
        return blob;
    }
    
    public byte[] getBytes()
    {
        if (bytes == null)
        {
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;
            
            try
            {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(blob);
                oos.flush();
                baos.flush();
                bytes = baos.toByteArray();
            }
            catch (IOException ex)
            {
                Logger.getLogger().error("BlobParamType getBytes error", ex);
            }
            finally
            {
                close(oos);
                close(baos);
            }
        }
        
        return bytes;
    }
    
    public InputStream getInputStream()
    {
        if (input == null)
        {
            input = new ByteArrayInputStream(getBytes());
        }
        
        return input;
    }
    
    public int getLength()
    {
        if (length == -1)
        {
            length = getBytes().length;
        }
        
        return length;
    }
}
