package org.rocex.db.param;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2019-8-12 11:07:04
 ***************************************************************************/
public class SQLParameter implements Serializable
{
    private List<Object> listParam = new ArrayList<>();
    
    public static void setStatementParameter(PreparedStatement statement, SQLParameter param) throws SQLException
    {
        if (statement == null || param == null)
        {
            throw new IllegalArgumentException("SQLParameter cannot be null!!!");
        }
        
        for (int i = 1, iParamCount = param.getCountParams(); i <= iParamCount; i++)
        {
            Object objParam = param.get(i - 1);
            
            if (objParam == null)
            {
                throw new IllegalArgumentException("SQLParameter cannot be null!!!");
            }
            
            if (objParam instanceof NullParamType)
            {
                statement.setNull(i, ((NullParamType) objParam).getType());
            }
            else if (objParam instanceof Integer)
            {
                statement.setInt(i, ((Integer) objParam).intValue());
            }
            else if (objParam instanceof Short)
            {
                statement.setShort(i, ((Short) objParam).shortValue());
            }
            else if (objParam instanceof Timestamp)
            {
                statement.setTimestamp(i, (Timestamp) objParam);
            }
            else if (objParam instanceof Time)
            {
                statement.setTime(i, (Time) objParam);
            }
            else if (objParam instanceof String)
            {
                statement.setString(i, (String) objParam);
            }
            else if (objParam instanceof Boolean)
            {
                statement.setBoolean(i, (Boolean) objParam);
            }
            else if (objParam instanceof Date)
            {
                statement.setDate(i, (Date) objParam);
            }
            else if (objParam instanceof Double)
            {
                statement.setDouble(i, ((Double) objParam).doubleValue());
            }
            else if (objParam instanceof Float)
            {
                statement.setFloat(i, ((Float) objParam).floatValue());
            }
            else if (objParam instanceof Long)
            {
                statement.setLong(i, ((Long) objParam).longValue());
            }
            else if (objParam instanceof Boolean)
            {
                statement.setBoolean(i, ((Boolean) objParam).booleanValue());
            }
            else if (objParam instanceof java.sql.Date)
            {
                statement.setDate(i, (java.sql.Date) objParam);
            }
            else if (objParam instanceof BlobParamType)// 如果是BLOB
            {
                statement.setBytes(i, ((BlobParamType) objParam).getBytes());
            }
            else if (objParam instanceof ClobParamType)// 如果是CLOB
            {
                ClobParamType clob = (ClobParamType) objParam;
                statement.setCharacterStream(i, clob.getReader(), clob.getLength());
            }
            else
            {
                statement.setObject(i, objParam);
            }
        }
    }
    
    /***************************************************************************
     * 加入一个Blob参数类型
     * @param bytes 字节数组参数
     * @author Rocex Wang
     * @version 2019-8-12 11:13:55
     ***************************************************************************/
    public void addBlobParam(byte[] bytes)
    {
        if (bytes == null)
        {
            addNullParam(Types.BLOB);
        }
        else
        {
            listParam.add(new BlobParamType(bytes));
        }
    }
    
    /***************************************************************************
     * 加入一个Blob参数类型
     * @param stream stream 字节流
     * @param length length 长度
     * @author Rocex Wang
     * @version 2019-8-12 11:20:02
     ***************************************************************************/
    public void addBlobParam(InputStream stream, int length)
    {
        if (stream == null)
        {
            addNullParam(Types.BLOB);
        }
        else
        {
            listParam.add(new BlobParamType(stream, length));
        }
    }
    
    /***************************************************************************
     * @param blob 加入一个Blob参数类型
     * @author Rocex Wang
     * @version 2019-8-12 11:20:30
     ***************************************************************************/
    public void addBlobParam(Object blob)
    {
        if (blob == null)
        {
            addNullParam(Types.BLOB);
        }
        else
        {
            listParam.add(new BlobParamType(blob));
        }
    }
    
    /***************************************************************************
     * 加入一个Clob参数类型
     * @param reader 字符流
     * @param length 长度
     * @author Rocex Wang
     * @version 2019-8-12 11:21:06
     ***************************************************************************/
    public void addClobParam(Reader reader, int length)
    {
        if (reader == null)
        {
            addNullParam(Types.CLOB);
        }
        else
        {
            listParam.add(new ClobParamType(reader, length));
        }
    }
    
    /***************************************************************************
     * @param clob 加入一个Clob参数类型
     * @author Rocex Wang
     * @version 2019-8-12 11:21:33
     ***************************************************************************/
    public void addClobParam(String clob)
    {
        if (clob == null)
        {
            addNullParam(Types.CLOB);
        }
        else
        {
            listParam.add(new ClobParamType(clob));
        }
    }
    
    /***************************************************************************
     * 加入一个Null参数类型
     * @param type 参数的类型
     * @see java.sql.Types
     * @author Rocex Wang
     * @version 2019-8-12 11:21:48
     ***************************************************************************/
    public void addNullParam(int type)
    {
        listParam.add(new NullParamType(type));
    }
    
    /***************************************************************************
     * @param param 加入一个布尔类型参数
     * @author Rocex Wang
     * @version 2019-8-12 11:22:49
     ***************************************************************************/
    public void addParam(boolean param)
    {
        listParam.add(Boolean.valueOf(param));
    }
    
    /***************************************************************************
     * @param param 加入一个双精度类型参数
     * @author Rocex Wang
     * @version 2019-8-12 11:23:07
     ***************************************************************************/
    public void addParam(double param)
    {
        listParam.add(Double.valueOf(param));
    }
    
    /***************************************************************************
     * @param param 加入一个浮点类型参数
     * @author Rocex Wang
     * @version 2019-8-12 11:23:23
     ***************************************************************************/
    public void addParam(float param)
    {
        listParam.add(Float.valueOf(param));
    }
    
    /***************************************************************************
     * @param param 加入一个整数类型参数
     * @author Rocex Wang
     * @version 2019-8-12 13:44:39
     ***************************************************************************/
    public void addParam(int param)
    {
        listParam.add(Integer.valueOf(param));
    }
    
    /***************************************************************************
     * @param param 加入一个整型参数
     * @author Rocex Wang
     * @version 2019-8-12 14:35:53
     ***************************************************************************/
    public void addParam(Integer param)
    {
        if (param == null)
        {
            addNullParam(Types.INTEGER);
        }
        else
        {
            listParam.add(param);
        }
    }
    
    /***************************************************************************
     * @param param 加入一个长整数类型参数
     * @author Rocex Wang
     * @version 2019-8-12 14:36:07
     ***************************************************************************/
    public void addParam(long param)
    {
        listParam.add(Long.valueOf(param));
    }
    
    /***************************************************************************
     * @param param 加入一个任意对象参数，注意该参数不能为空
     * @author Rocex Wang
     * @version 2019-8-12 14:36:29
     ***************************************************************************/
    public void addParam(Object param)
    {
        if (param == null)
        {
            throw new IllegalArgumentException("SQL Parameter object cannot be null, which can be replaced by NullType Object!!");
        }
        
        listParam.add(param);
    }
    
    /***************************************************************************
     * @param param 加入一个短整数类型参数
     * @author Rocex Wang
     * @version 2019-8-12 14:36:45
     ***************************************************************************/
    public void addParam(short param)
    {
        listParam.add(Short.valueOf(param));
    }
    
    /***************************************************************************
     * @param param 加入一个字符串类型参数
     * @author Rocex Wang
     * @version 2019-8-12 14:36:59
     ***************************************************************************/
    public void addParam(String param)
    {
        if (param == null)
        {
            addNullParam(Types.VARCHAR);
        }
        else if ("".equals(param))
        {
            addNullParam(Types.VARCHAR);
        }
        else
        {
            listParam.add(param);
        }
    }
    
    /***************************************************************************
     * 清除所有参数
     * @author Rocex Wang
     * @version 2019-8-12 14:37:21
     ***************************************************************************/
    public void clearParams()
    {
        listParam.clear();
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see java.lang.Object#clone()
     * @author Rocex Wang
     * @version 2019-8-12 14:37:50
     ****************************************************************************/
    @Override
    public SQLParameter clone()
    {
        SQLParameter param = new SQLParameter();
        
        param.listParam.addAll(this.listParam);
        
        return param;
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see java.lang.Object#equals(java.lang.Object)
     * @author Rocex Wang
     * @version 2019-8-12 14:39:19
     ****************************************************************************/
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        
        final SQLParameter that = (SQLParameter) o;
        
        return !(listParam != null ? !listParam.equals(that.listParam) : that.listParam != null);
    }
    
    /***************************************************************************
     * @param index 参数的顺序索引
     * @return 根据索引得到参数对象
     * @author Rocex Wang
     * @version 2019-8-12 14:39:27
     ***************************************************************************/
    public Object get(int index)
    {
        return listParam.get(index);
    }
    
    /***************************************************************************
     * @return 得到参数的个数
     * @author Rocex Wang
     * @version 2019-8-12 14:39:44
     ***************************************************************************/
    public int getCountParams()
    {
        return listParam.size();
    }
    
    /***************************************************************************
     * @return 得到所有参数集合
     * @author Rocex Wang
     * @version 2019-8-12 14:40:05
     ***************************************************************************/
    public List<?> getParameters()
    {
        return listParam;
    }
    
    /***************************************************************************
     * 参数的替换，用来保持参数对象的原始信息，而不用重新构造参数对象.
     * @param index 要替换对象的索引从0开始记数
     * @param param
     * @author Rocex Wang
     * @version 2019-8-12 14:40:24
     ***************************************************************************/
    public void replace(int index, Object param)
    {
        if (param == null)
        {
            throw new IllegalArgumentException("SQL Parameter object cannot be null, which can be replaced by NullType Object!!");
        }
        
        listParam.remove(index);
        listParam.add(index, param);
    }
    
    /***************************************************************************
     * 参数的替换，用来保持参数对象的原始信息，而不用重新构造参数对象
     * @param index 要替换对象的索引从0开始记数
     * @param param 字符串对象
     * @author Rocex Wang
     * @version 2019-8-12 14:40:52
     ***************************************************************************/
    public void replace(int index, String param)
    {
        listParam.remove(index);
        
        if (param == null)
        {
            listParam.add(new NullParamType(Types.VARCHAR));
        }
        else
        {
            listParam.add(index, param);
        }
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see java.lang.Object#toString()
     * @author Rocex Wang
     * @version 2019-8-12 14:41:39
     ****************************************************************************/
    @Override
    public String toString()
    {
        String strValue = "";
        
        for (int i = 0; i < listParam.size(); i++)
        {
            strValue += i + 1 + "=" + listParam.get(i) + ", ";
        }
        
        return "parameter:[" + (strValue.length() > 0 ? strValue.substring(0, strValue.length() - 2) : strValue) + "]";
    }
}
