package org.rocex.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @since 2021-10-19 11:07:29
 ***************************************************************************/
@JsonFilter("JacksonFilter")
public class JacksonPropertyFilterProvider extends FilterProvider
{
    private Map<Class<?>, Set<String>> mapExclude = new HashMap<>();
    private Map<Class<?>, Set<String>> mapInclude = new HashMap<>();
    
    private ObjectMapper mapper;
    
    /***************************************************************************
     * @param mapper
     * @author Rocex Wang
     * @since 2021-10-19 01:49:38
     ***************************************************************************/
    public JacksonPropertyFilterProvider(ObjectMapper mapper)
    {
        this.mapper = mapper;
        
        this.mapper.setFilterProvider(this);
    }
    
    private void addToMap(Map<Class<?>, Set<String>> mapFilter, Class<?> clazz, String... strFields)
    {
        if (strFields == null || strFields.length == 0)
        {
            return;
        }
        
        Set<String> setField = new HashSet<>(Arrays.asList(strFields));
        
        mapFilter.put(clazz, setField);
    }
    
    public boolean apply(Class<?> clazz, String strField)
    {
        Set<String> setIncludeFields = mapInclude.get(clazz);
        Set<String> setExcludeFields = mapExclude.get(clazz);
        
        if (setIncludeFields != null && setIncludeFields.contains(strField))
        {
            return true;
        }
        else if (setExcludeFields != null && !setExcludeFields.contains(strField))
        {
            return true;
        }
        else if (setIncludeFields == null && setExcludeFields == null)
        {
            return true;
        }
        
        return false;
    }
    
    public JacksonPropertyFilterProvider exclude(Class<?> clazz, String... strFields)
    {
        addToMap(mapExclude, clazz, strFields);
        
        mapper.addMixIn(clazz, getClass());
        
        return this;
    }
    
    @Override
    public BeanPropertyFilter findFilter(Object filterId)
    {
        return null;
    }
    
    @Override
    public PropertyFilter findPropertyFilter(Object filterId, Object valueToFilter)
    {
        return new SimpleBeanPropertyFilter()
        {
            @Override
            public void serializeAsField(Object pojo, JsonGenerator jsonGen, SerializerProvider provider, PropertyWriter writer) throws Exception
            {
                if (apply(pojo.getClass(), writer.getName()))
                {
                    writer.serializeAsField(pojo, jsonGen, provider);
                }
                else if (!jsonGen.canOmitFields())
                {
                    writer.serializeAsOmittedField(pojo, jsonGen, provider);
                }
            }
        };
    }
    
    public JacksonPropertyFilterProvider include(Class<?> clazz, String... strFields)
    {
        addToMap(mapInclude, clazz, strFields);
        
        mapper.addMixIn(clazz, getClass());
        
        return this;
    }
}
