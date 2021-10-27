package org.rocex.datadict.vo;

import java.util.List;

import javax.persistence.Index;
import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 13:20:06
 ***************************************************************************/
@Table(name = "md_class", indexes = { @Index(name = "i_md_class_component_id", columnList = "component_id"),
        @Index(name = "i_md_class_default_table_name", columnList = "default_table_name") })
public class ClassVO extends MetaVO
{
    private Integer iClassType;

    private List<PropertyVO> propertyVO;

    private String strClassListUrl;
    private String strComponentId;
    private String strDefaultTableName;
    private String strFullClassname;
    private String strIsPrimary;
    private String strKeyAttribute;
    
    /***************************************************************************
     * @return the classListUrl
     * @author Rocex Wang
     * @since 2021-10-22 03:40:16
     ***************************************************************************/
    public String getClassListUrl()
    {
        return strClassListUrl;
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
     * @return the isPrimary
     * @author Rocex Wang
     * @version 2020-4-23 19:55:37
     ***************************************************************************/
    public String getIsPrimary()
    {
        return strIsPrimary;
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
     * @return the propertyVO
     * @author Rocex Wang
     * @since 2021-10-15 03:00:29
     ***************************************************************************/
    public List<PropertyVO> getPropertyVO()
    {
        return propertyVO;
    }

    /***************************************************************************
     * @param classListUrl the classListUrl to set
     * @author Rocex Wang
     * @since 2021-10-22 03:40:16
     ***************************************************************************/
    public void setClassListUrl(String classListUrl)
    {
        strClassListUrl = classListUrl;
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
     * @param isPrimary the isPrimary to set
     * @author Rocex Wang
     * @version 2020-4-23 19:55:37
     ***************************************************************************/
    public void setIsPrimary(String isPrimary)
    {
        strIsPrimary = isPrimary;
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
     * @param propertyVO the propertyVO to set
     * @author Rocex Wang
     * @since 2021-10-15 03:00:29
     ***************************************************************************/
    public void setPropertyVO(List<PropertyVO> propertyVO)
    {
        this.propertyVO = propertyVO;
    }
}
