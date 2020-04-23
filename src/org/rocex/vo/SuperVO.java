package org.rocex.vo;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.rocex.utils.Logger;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2019-5-29 21:36:12
 ***************************************************************************/
public class SuperVO implements Serializable
{
    private static transient Map<String, Method> mapGetter = new HashMap<>();
    private static transient Map<String, Method> mapSetter = new HashMap<>();
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see java.lang.Object#clone()
     * @author Rocex Wang
     * @version 2019-6-27 10:53:55
     ****************************************************************************/
    @Override
    public SuperVO clone()
    {
        SuperVO newVO = null;
        
        try
        {
            newVO = getClass().newInstance();
            
            newVO.fromMap(toMap());
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        return newVO;
    }
    
    /***************************************************************************
     * 从sourceVO复制属性数据
     * @param sourceVO
     * @return targetVO
     * @author Rocex Wang
     * @version 2020-1-17 19:38:47
     ***************************************************************************/
    public void cloneFrom(SuperVO sourceVO)
    {
        fromMap(sourceVO.toMap());
    }
    
    /***************************************************************************
     * @param mapKeyValue
     * @author Rocex Wang
     * @version 2019-5-29 0:30:24
     ***************************************************************************/
    public void fromMap(Map<String, Object> mapKeyValue)
    {
        Set<Entry<String, Object>> entrySet = mapKeyValue.entrySet();
        
        for (Entry<String, Object> entry : entrySet)
        {
            setValue(entry.getKey(), entry.getValue());
        }
    }
    
    /***************************************************************************
     * @param prop
     * @author Rocex Wang
     * @version 2020-1-18 13:04:27
     ***************************************************************************/
    public void fromMap(Properties prop)
    {
        Set<Entry<Object, Object>> entrySet = prop.entrySet();
        
        for (Entry<Object, Object> entry : entrySet)
        {
            setValue((String) entry.getKey(), entry.getValue());
        }
    }
    
    /***************************************************************************
     * @param data
     * @return Object
     * @author Rocex Wang
     * @version 2019-6-11 11:05:23
     ***************************************************************************/
    public Object getValue(String strFieldName)
    {
        if (strFieldName == null || strFieldName.trim().length() == 0)
        {
            return null;
        }
        
        String strKey = getClass().getName() + "." + strFieldName.toLowerCase();
        
        Method method = mapGetter.get(strKey);
        
        if (method == null)
        {
            Method[] methods = getClass().getMethods();
            
            for (Method method2 : methods)
            {
                if (method2.getParameterCount() != 0)
                {
                    continue;
                }
                
                String strName = method2.getName();
                
                if (strName.equalsIgnoreCase("get" + strFieldName) || strName.equalsIgnoreCase("is" + strFieldName) && method2.getReturnType() == Boolean.class)
                {
                    method = method2;
                    mapGetter.put(strKey, method2);
                    
                    break;
                }
            }
        }
        
        if (method != null)
        {
            try
            {
                return method.invoke(this, (Object[]) null);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        }
        
        return null;
    }
    
    /***************************************************************************
     * 直接给属性赋值
     * @param strFieldName
     * @param objValue
     * @author Rocex Wang
     * @version 2020-1-18 13:45:45
     ***************************************************************************/
    protected void setFieldValue(String strFieldName, Object objValue)
    {
        try
        {
            Field field = getClass().getDeclaredField(strFieldName);
            
            field.setAccessible(true);
            field.set(this, objValue);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    /***************************************************************************
     * @param strFieldName
     * @param objValue
     * @author Rocex Wang
     * @version 2019-6-11 11:18:34
     ***************************************************************************/
    public void setValue(String strFieldName, Object objValue)
    {
        if (strFieldName == null || strFieldName.trim().length() == 0)
        {
            return;
        }
        
        String strKey = getClass().getName() + "." + strFieldName.toLowerCase();
        
        Method method = mapSetter.get(strKey);
        
        if (method == null)
        {
            Method[] methods = getClass().getMethods();
            
            for (Method method2 : methods)
            {
                String strName = method2.getName();
                
                if (strName.equalsIgnoreCase("set" + strFieldName) && method2.getParameterCount() == 1)
                {
                    method = method2;
                    mapSetter.put(strKey, method2);
                    
                    break;
                }
            }
        }
        
        if (method != null)
        {
            try
            {
                Logger.getLogger().trace(strFieldName + ": " + objValue);
                
                method.invoke(this, new Object[] { objValue });
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
                
                if (objValue instanceof BigDecimal)
                {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    
                    if (parameterTypes != null && parameterTypes[0] == Integer.class)
                    {
                        setValue(strFieldName, ((BigDecimal) objValue).intValue());
                    }
                }
            }
        }
    }
    
    protected void setValueToBoolean(String strFieldName, Object objValue)
    {
        Boolean blValue = null;
        
        if (objValue == null)
        {
            blValue = false;
        }
        else if (objValue instanceof Boolean)
        {
            blValue = (Boolean) objValue;
        }
        else if (objValue instanceof BigDecimal)
        {
            blValue = ((BigDecimal) objValue).intValue() == 1;
        }
        else
        {
            blValue = Boolean.valueOf(objValue.toString());
        }
        
        setFieldValue(strFieldName, blValue);
    }
    
    /***************************************************************************
     * @param strFieldName
     * @param objValue
     * @author Rocex Wang
     * @version 2020-1-6 13:23:56
     ***************************************************************************/
    protected void setValueToInt(String strFieldName, Object objValue)
    {
        Integer intValue = null;
        
        if (objValue instanceof BigDecimal)
        {
            intValue = ((BigDecimal) objValue).intValue();
        }
        else if (objValue instanceof Integer)
        {
            intValue = (Integer) objValue;
        }
        else if (objValue != null)
        {
            intValue = Integer.parseInt(objValue.toString());
        }
        
        setFieldValue(strFieldName, intValue);
    }
    
    /***************************************************************************
     * @return Map<String, Object>
     * @author Rocex Wang
     * @version 2019-5-29 22:02:38
     ***************************************************************************/
    public Map<String, Object> toMap()
    {
        Map<String, Object> mapKeyValue = new LinkedHashMap<>();
        
        Method[] methods = getClass().getMethods();
        
        for (Method method : methods)
        {
            if (method.getParameterCount() != 0)
            {
                continue;
            }
            
            String strName = method.getName();
            
            try
            {
                if (strName.startsWith("get") && !"getClass".equals(strName))
                {
                    Object objValue = method.invoke(this, (Object[]) null);
                    
                    if (objValue == null)
                    {
                        continue;
                    }
                    
                    mapKeyValue.put(strName.substring(3), objValue);
                }
                else if (strName.startsWith("is") && method.getReturnType() == Boolean.class)
                {
                    Object objValue = method.invoke(this, (Object[]) null);
                    
                    if (objValue == null)
                    {
                        continue;
                    }
                    
                    mapKeyValue.put(strName.substring(2), objValue);
                }
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        }
        
        return mapKeyValue;
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see java.lang.Object#toString()
     * @author Rocex Wang
     * @version 2019-5-24 18:23:54
     ****************************************************************************/
    @Override
    public String toString()
    {
        return toMap().toString();
    }
}
