package org.rocex.db.processor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.rocex.utils.Logger;
import org.rocex.utils.StringHelper;
import org.rocex.vo.SuperVO;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2019-8-7 10:10:37
 ***************************************************************************/
public class BeanProcessor<T extends SuperVO> extends ResultSetProcessor
{
    protected Class<T> clazz = null;
    
    private Map<Integer, String> mapField = new HashMap<>();
    
    private Map<String, String> mapFieldRelationship;// 查询字段和VO中字段在不相同的情况下的对照关系，相同的可忽略
    
    private String strFields[];// 只处理指定的字段，每个值对应到VO中的属性(不区分大小写)，null为都处理
    
    /***************************************************************************
     * @param clazz
     * @author Rocex Wang
     * @version 2019-8-7 10:30:35
     ***************************************************************************/
    public BeanProcessor(Class<T> clazz)
    {
        this(clazz, null);
    }
    
    /***************************************************************************
     * @param clazz
     * @param mapFieldRelationship 查询字段和VO中字段在不相同的情况下的对照关系，相同的可忽略
     * @param strFields 只处理指定的字段，每个值对应到VO中的属性(不区分大小写)，null为都处理
     * @author Rocex Wang
     * @version 2019-9-24 12:48:41
     ***************************************************************************/
    public BeanProcessor(Class<T> clazz, Map<String, String> mapFieldRelationship, String... strFields)
    {
        super();
        
        this.clazz = clazz;
        this.strFields = strFields;
        this.mapFieldRelationship = mapFieldRelationship;
    }
    
    /***************************************************************************
     * 初始化查询字段索引以及vo中对应的属性
     * @param resultSet
     * @author Rocex Wang
     * @version 2020-1-17 15:33:14
     ***************************************************************************/
    protected void initFieldInfo(ResultSet resultSet)
    {
        List<String> listField = new ArrayList<>();
        
        if (strFields != null)
        {
            for (int i = 0; i < strFields.length; i++)
            {
                strFields[i] = strFields[i].toLowerCase();
            }
            
            listField = Arrays.asList(strFields);
        }
        
        try
        {
            ResultSetMetaData metaData = resultSet.getMetaData();
            
            int iColumnCount = metaData.getColumnCount();
            
            for (int i = 1; i <= iColumnCount; i++)
            {
                String strColumnName = metaData.getColumnLabel(i);
                
                if (strColumnName == null)
                {
                    strColumnName = metaData.getColumnName(i);
                }
                
                if (mapFieldRelationship != null && mapFieldRelationship.containsKey(strColumnName))
                {
                    strColumnName = mapFieldRelationship.get(strColumnName);
                }
                
                strColumnName = StringHelper.underlineToCamel(strColumnName.toLowerCase());
                
                if (!listField.isEmpty() && !listField.contains(strColumnName.toLowerCase()))
                {
                    continue;
                }
                
                mapField.put(i, strColumnName);
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datahub.db.processor.ResultSetProcessor#processResultSet(java.sql.ResultSet)
     * @author Rocex Wang
     * @version 2019-8-7 10:16:06
     ****************************************************************************/
    @Override
    protected T processResultSet(ResultSet resultSet) throws SQLException
    {
        T superVO = null;
        
        try
        {
            if (resultSet.next())
            {
                superVO = processRow(resultSet);
            }
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
            
            throw new SQLException(ex.getMessage(), ex);
        }
        
        return superVO;
    }
    
    /***************************************************************************
     * @param resultSet
     * @return T
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException
     * @author Rocex Wang
     * @version 2019-8-13 14:37:53
     ***************************************************************************/
    protected T processRow(ResultSet resultSet) throws Exception
    {
        if (mapField.isEmpty())
        {
            initFieldInfo(resultSet);
        }
        
        T superVO = clazz.newInstance();
        
        Set<Entry<Integer, String>> entrySet = mapField.entrySet();
        
        for (Entry<Integer, String> entry : entrySet)
        {
            superVO.setValue(entry.getValue(), resultSet.getObject(entry.getKey()));
        }
        
        return superVO;
    }
}
