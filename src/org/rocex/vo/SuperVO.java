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
import java.util.TreeMap;

import org.rocex.utils.Logger;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2019-5-29 21:36:12
 ***************************************************************************/
public class SuperVO implements Serializable
{
    private static transient Map<String, Map<String, Method>> mapAllGetter = new HashMap<>();
    private static transient Map<String, Map<String, Method>> mapAllSetter = new HashMap<>();
    
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
            
            newVO.cloneFrom(toMap());
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        return newVO;
    }
    
    /***************************************************************************
     * @param mapKeyValue
     * @author Rocex Wang
     * @version 2019-5-29 0:30:24
     ***************************************************************************/
    public void cloneFrom(Map<String, Object> mapKeyValue)
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
    public void cloneFrom(Properties prop)
    {
        Set<Entry<Object, Object>> entrySet = prop.entrySet();
        
        for (Entry<Object, Object> entry : entrySet)
        {
            setValue((String) entry.getKey(), entry.getValue());
        }
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
        cloneFrom(sourceVO.toMap());
    }
    
    /***************************************************************************
     * 得到 strFieldName 的 getter 方法
     * @param strFieldName 不区分大小写
     * @return Method
     * @author Rocex Wang
     * @version 2020-5-26 14:30:56
     ***************************************************************************/
    protected Method getGetter(String strFieldName)
    {
        String strKey = getClass().getName();
        
        Map<String, Method> mapGetter = mapAllGetter.get(strKey);
        
        if (mapGetter == null || mapGetter.isEmpty())
        {
            initGetter();
        }
        
        Method method = mapAllGetter.get(strKey).get(strFieldName.toLowerCase());
        
        return method;
    }
    
    /***************************************************************************
     * 得到 strFieldName 的 setter 方法
     * @param strFieldName 不区分大小写
     * @return Method
     * @author Rocex Wang
     * @version 2020-5-18 14:15:26
     ***************************************************************************/
    protected Method getSetter(String strFieldName)
    {
        String strKey = getClass().getName();
        
        Map<String, Method> mapSetter = mapAllSetter.get(strKey);
        
        if (mapSetter == null || mapSetter.isEmpty())
        {
            initSetter();
        }
        
        Method method = mapAllSetter.get(strKey).get(strFieldName.toLowerCase());
        
        return method;
    }
    
    /***************************************************************************
     * 取得 strFieldName 的值
     * @param strFieldName 不区分大小写
     * @return Object
     * @author Rocex Wang
     * @version 2019-6-11 11:05:23
     ***************************************************************************/
    public Object getValue(String strFieldName)
    {
        if (strFieldName == null || strFieldName.trim().length() == 0)
        {
            Logger.getLogger().trace("field is null or empty for getValue(): " + strFieldName);
            return null;
        }
        
        Method method = getGetter(strFieldName);
        
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
     * 初始化收集 VO 的所有 getter
     * @author Rocex Wang
     * @version 2020-5-26 13:51:22
     ***************************************************************************/
    protected void initGetter()
    {
        Map<String, Method> mapGetter = new LinkedHashMap<>();
        
        mapAllGetter.put(getClass().getName(), mapGetter);
        
        Method[] methods = getClass().getMethods();
        
        for (Method method : methods)
        {
            if (method.getParameterCount() != 0)
            {
                continue;
            }
            
            String strName = method.getName();
            
            if (strName.startsWith("get") && !"getClass".equals(strName))
            {
                String strKey = strName.substring(3).toLowerCase();
                
                mapGetter.put(strKey, method);
            }
            else if (strName.startsWith("is") && method.getReturnType() == Boolean.class)
            {
                String strKey = strName.substring(2).toLowerCase();
                
                mapGetter.put(strKey, method);
            }
        }
    }
    
    /***************************************************************************
     * 初始化收集 VO 的所有 setter
     * @author Rocex Wang
     * @version 2020-5-26 14:21:40
     ***************************************************************************/
    protected void initSetter()
    {
        Map<String, Method> mapSetter = new LinkedHashMap<>();
        
        mapAllSetter.put(getClass().getName(), mapSetter);
        
        Method[] methods = getClass().getMethods();
        
        for (Method method : methods)
        {
            String strName = method.getName();
            
            if (strName.startsWith("set") && method.getParameterCount() == 1)
            {
                String strKey = strName.substring(3).toLowerCase();
                
                mapSetter.put(strKey, method);
            }
        }
    }
    
    /***************************************************************************
     * 直接给属性赋值
     * @param strFieldName 不区分大小写
     * @param objValue
     * @author Rocex Wang
     * @version 2020-1-18 13:45:45
     ***************************************************************************/
    protected void setFieldValue(String strFieldName, Object objValue)
    {
        Field field = null;
        boolean blAccessible = false;
        
        try
        {
            field = getClass().getDeclaredField(strFieldName);
            
            blAccessible = field.isAccessible();
            
            if (!blAccessible)
            {
                field.setAccessible(true);
            }
            
            field.set(this, objValue);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        finally
        {
            if (!blAccessible && field != null)
            {
                field.setAccessible(false);
            }
        }
    }
    
    /***************************************************************************
     * 设置 strFieldName 的值
     * @param strFieldName 不区分大小写
     * @param objValue
     * @author Rocex Wang
     * @version 2019-6-11 11:18:34
     ***************************************************************************/
    public void setValue(String strFieldName, Object objValue)
    {
        if (strFieldName == null || strFieldName.trim().length() == 0)
        {
            Logger.getLogger().trace("field is null or empty for setValue(): " + strFieldName);
            return;
        }
        
        Method method = getSetter(strFieldName);
        
        if (method == null)
        {
            Logger.getLogger().trace("do not find field method for setValue(): " + strFieldName);
            return;
        }
        
        Logger.getLogger().trace(strFieldName + ": " + objValue);
        
        Class<?>[] parameterTypes = method.getParameterTypes();
        
        try
        {
            if (parameterTypes != null && parameterTypes.length > 0)
            {
                Class<?> classParamType = parameterTypes[0];
                
                if (classParamType == BigDecimal.class)
                {
                    setValueToBigDecimal(method, objValue);
                }
                else if (classParamType == Boolean.class)
                {
                    setValueToBoolean(method, objValue);
                }
                else if (classParamType == Integer.class)
                {
                    setValueToInt(method, objValue);
                }
                else if (classParamType == String.class)
                {
                    setValueToString(method, objValue);
                }
                else
                {
                    method.invoke(this, new Object[] { objValue });
                }
            }
            else
            {
                method.invoke(this, new Object[] { objValue });
            }
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    /***************************************************************************
     * @param method
     * @param objValue
     * @throws Exception
     * @author Rocex Wang
     * @version 2020-5-18 14:22:43
     ***************************************************************************/
    protected void setValueToBigDecimal(Method method, Object objValue) throws Exception
    {
        BigDecimal decimal = null;
        
        if (objValue == null || objValue instanceof BigDecimal)
        {
            decimal = (BigDecimal) objValue;
        }
        else if (objValue instanceof Integer)
        {
            decimal = new BigDecimal(objValue.toString());
        }
        
        method.invoke(this, new Object[] { decimal });
    }
    
    /***************************************************************************
     * @param method
     * @param objValue
     * @throws Exception
     * @author Rocex Wang
     * @version 2020-5-18 14:19:26
     ***************************************************************************/
    protected void setValueToBoolean(Method method, Object objValue) throws Exception
    {
        Boolean blValue = null;
        
        if (objValue == null || objValue instanceof Boolean)
        {
            blValue = (Boolean) objValue;
        }
        else if (objValue instanceof BigDecimal)
        {
            blValue = ((BigDecimal) objValue).intValue() == 1;
        }
        else if (objValue instanceof Integer)
        {
            blValue = ((Integer) objValue).intValue() == 1;
        }
        else
        {
            blValue = Boolean.valueOf(objValue.toString());
        }
        
        method.invoke(this, new Object[] { blValue });
    }
    
    /***************************************************************************
     * @param method
     * @param objValue
     * @author Rocex Wang
     * @version 2020-1-6 13:23:56
     * @throws Exception
     ***************************************************************************/
    protected void setValueToInt(Method method, Object objValue) throws Exception
    {
        Integer intValue = null;
        
        if (objValue == null || objValue instanceof Integer)
        {
            intValue = (Integer) objValue;
        }
        else if (objValue instanceof BigDecimal)
        {
            intValue = ((BigDecimal) objValue).intValue();
        }
        else
        {
            intValue = Integer.parseInt(objValue.toString());
        }
        
        method.invoke(this, new Object[] { intValue });
    }
    
    /***************************************************************************
     * @param method
     * @param objValue
     * @throws Exception
     * @author Rocex Wang
     * @version 2020-5-28 11:44:25
     ***************************************************************************/
    protected void setValueToString(Method method, Object objValue) throws Exception
    {
        String strValue = null;
        
        if (objValue == null || objValue instanceof String)
        {
            strValue = (String) objValue;
        }
        else
        {
            strValue = objValue.toString();
        }
        
        method.invoke(this, new Object[] { strValue });
    }
    
    /***************************************************************************
     * @return Map<String, Object>
     * @author Rocex Wang
     * @version 2019-5-29 22:02:38
     ***************************************************************************/
    public Map<String, Object> toMap()
    {
        String strKey = getClass().getName();
        
        Map<String, Method> mapGetter = mapAllGetter.get(strKey);
        
        if (mapGetter == null || mapGetter.isEmpty())
        {
            initGetter();
            
            mapGetter = mapAllGetter.get(strKey);
        }
        
        Map<String, Object> mapKeyValue = new TreeMap<>();
        
        Set<Entry<String, Method>> entrySet = mapGetter.entrySet();
        
        for (Entry<String, Method> entry : entrySet)
        {
            Object objValue = null;
            
            try
            {
                objValue = entry.getValue().invoke(this, (Object[]) null);
                
                if (objValue == null)
                {
                    continue;
                }
                
                mapKeyValue.put(entry.getKey(), objValue);
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
