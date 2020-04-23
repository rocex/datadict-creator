package org.rocex.datadict.vo;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 13:20:06
 ***************************************************************************/
public class ClassVO extends MetaVO
{
    private Integer iClassType;
    private String strComponentId;
    private String strDefaultTableName;
    private String strFullClassname;
    private String strKeyAttribute;
    
    /***************************************************************************
     * @return the componentId
     * @author Rocex Wang
     * @version 2020-4-22 9:47:37
     ***************************************************************************/
    public String getComponentId()
    {
        return strComponentId;
    }
    
    /***************************************************************************
     * @return the defaultTableName
     * @author Rocex Wang
     * @version 2020-4-22 10:39:06
     ***************************************************************************/
    public String getDefaultTableName()
    {
        return strDefaultTableName;
    }
    
    /***************************************************************************
     * @return the fullClassname
     * @author Rocex Wang
     * @version 2020-4-22 14:32:30
     ***************************************************************************/
    public String getFullClassname()
    {
        return strFullClassname;
    }
    
    /***************************************************************************
     * @return the keyAttribute
     * @author Rocex Wang
     * @version 2020-4-22 14:32:45
     ***************************************************************************/
    public String getKeyAttribute()
    {
        return strKeyAttribute;
    }
    
    /***************************************************************************
     * @param componentId the componentId to set
     * @author Rocex Wang
     * @version 2020-4-22 9:47:37
     ***************************************************************************/
    public void setComponentId(String componentId)
    {
        strComponentId = componentId;
    }
    
    /***************************************************************************
     * @param defaultTableName the defaultTableName to set
     * @author Rocex Wang
     * @version 2020-4-22 10:39:06
     ***************************************************************************/
    public void setDefaultTableName(String defaultTableName)
    {
        strDefaultTableName = defaultTableName;
    }
    
    /***************************************************************************
     * @param fullClassname the fullClassname to set
     * @author Rocex Wang
     * @version 2020-4-22 14:32:30
     ***************************************************************************/
    public void setFullClassname(String fullClassname)
    {
        strFullClassname = fullClassname;
    }
    
    /***************************************************************************
     * @param keyAttribute the keyAttribute to set
     * @author Rocex Wang
     * @version 2020-4-22 14:32:45
     ***************************************************************************/
    public void setKeyAttribute(String keyAttribute)
    {
        strKeyAttribute = keyAttribute;
    }

    /***************************************************************************
     * @return the classType
     * @author Rocex Wang
     * @version 2020-4-22 16:46:54
     ***************************************************************************/
    public Integer getClassType()
    {
        return iClassType;
    }

    /***************************************************************************
     * @param classType the classType to set
     * @author Rocex Wang
     * @version 2020-4-22 16:46:54
     ***************************************************************************/
    public void setClassType(Integer classType)
    {
        iClassType = classType;
    }
}
