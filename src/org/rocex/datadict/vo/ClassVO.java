package org.rocex.datadict.vo;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.List;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @since 2020-4-21 13:20:06
 ***************************************************************************/
@Table(name = "md_class", indexes = {@Index(name = "i_md_class_component_id", columnList = "component_id"),
    @Index(name = "i_md_class_default_table_name", columnList = "default_table_name"), @Index(name = "i_md_class_class_type", columnList = "class_type"),
    @Index(name = "i_md_class_ddc_version", columnList = "ddc_version")})
public class ClassVO extends MetaVO
{
    private Boolean blIsAuthen;
    private Boolean blIsPrimary = false;

    private Integer iClassType;     // 999 - 数据库表

    private List<PropertyVO> propertyVO;
    private String strAccessorClassname;
    private String strBizItfImpClassname;
    private String strClassListUrl;
    private String strComponentId;
    private String strDefaultTableName;
    private String strFullClassname;
    private String strKeyAttribute;
    private String strRefModelName;

    private String strReturnType;

    /***************************************************************************
     * @return the accessorClassname
     * @author Rocex Wang
     * @since 2021-11-16 02:14:23
     ***************************************************************************/
    public String getAccessorClassname()
    {
        return strAccessorClassname;
    }

    /***************************************************************************
     * @param accessorClassname the accessorClassname to set
     * @author Rocex Wang
     * @since 2021-11-16 02:14:23
     ***************************************************************************/
    public void setAccessorClassname(String accessorClassname)
    {
        strAccessorClassname = accessorClassname;
    }

    /***************************************************************************
     * @return the bizItfImpClassname
     * @author Rocex Wang
     * @since 2021-11-16 02:14:23
     ***************************************************************************/
    public String getBizItfImpClassname()
    {
        return strBizItfImpClassname;
    }

    /***************************************************************************
     * @param bizItfImpClassname the bizItfImpClassname to set
     * @author Rocex Wang
     * @since 2021-11-16 02:14:23
     ***************************************************************************/
    public void setBizItfImpClassname(String bizItfImpClassname)
    {
        strBizItfImpClassname = bizItfImpClassname;
    }

    /***************************************************************************
     * @return the classListUrl
     * @author Rocex Wang
     * @since 2021-10-22 03:40:16
     ***************************************************************************/
    @Column(insertable = false, updatable = false)
    public String getClassListUrl()
    {
        return strClassListUrl;
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
     * @return the classType
     * @author Rocex Wang
     * @since 2020-4-22 16:46:54
     ***************************************************************************/
    public Integer getClassType()
    {
        return iClassType;
    }

    /***************************************************************************
     * @param classType the classType to set
     * @author Rocex Wang
     * @since 2020-4-22 16:46:54
     ***************************************************************************/
    public void setClassType(Integer classType)
    {
        iClassType = classType;
    }

    /***************************************************************************
     * @return the componentId
     * @author Rocex Wang
     * @since 2020-4-22 9:47:37
     ***************************************************************************/
    @Column(length = 50)
    public String getComponentId()
    {
        return strComponentId;
    }

    /***************************************************************************
     * @param componentId the componentId to set
     * @author Rocex Wang
     * @since 2020-4-22 9:47:37
     ***************************************************************************/
    public void setComponentId(String componentId)
    {
        strComponentId = componentId;
    }

    /***************************************************************************
     * @return the defaultTableName
     * @author Rocex Wang
     * @since 2020-4-22 10:39:06
     ***************************************************************************/
    @Column(length = 50)
    public String getDefaultTableName()
    {
        return strDefaultTableName;
    }

    /***************************************************************************
     * @param defaultTableName the defaultTableName to set
     * @author Rocex Wang
     * @since 2020-4-22 10:39:06
     ***************************************************************************/
    public void setDefaultTableName(String defaultTableName)
    {
        strDefaultTableName = defaultTableName;
    }

    /***************************************************************************
     * @return the fullClassname
     * @author Rocex Wang
     * @since 2020-4-22 14:32:30
     ***************************************************************************/
    public String getFullClassname()
    {
        return strFullClassname;
    }

    /***************************************************************************
     * @param fullClassname the fullClassname to set
     * @author Rocex Wang
     * @since 2020-4-22 14:32:30
     ***************************************************************************/
    public void setFullClassname(String fullClassname)
    {
        strFullClassname = fullClassname;
    }

    /***************************************************************************
     * @return the isAuthen
     * @author Rocex Wang
     * @since 2022-08-02 02:34:03
     ***************************************************************************/
    public Boolean getIsAuthen()
    {
        return blIsAuthen;
    }

    /***************************************************************************
     * @param isAuthen the isAuthen to set
     * @author Rocex Wang
     * @since 2022-08-02 02:34:03
     ***************************************************************************/
    public void setIsAuthen(Boolean isAuthen)
    {
        blIsAuthen = isAuthen;
    }

    /***************************************************************************
     * @return the isPrimary
     * @author Rocex Wang
     * @since 2020-4-23 19:55:37
     ***************************************************************************/
    public Boolean getIsPrimary()
    {
        return blIsPrimary;
    }

    /***************************************************************************
     * @param isPrimary the isPrimary to set
     * @author Rocex Wang
     * @since 2020-4-23 19:55:37
     ***************************************************************************/
    public void setIsPrimary(Boolean isPrimary)
    {
        blIsPrimary = isPrimary;
    }

    /***************************************************************************
     * @return the keyAttribute
     * @author Rocex Wang
     * @since 2020-4-22 14:32:45
     ***************************************************************************/
    @Column(length = 50)
    public String getKeyAttribute()
    {
        return strKeyAttribute;
    }

    /***************************************************************************
     * @param keyAttribute the keyAttribute to set
     * @author Rocex Wang
     * @since 2020-4-22 14:32:45
     ***************************************************************************/
    public void setKeyAttribute(String keyAttribute)
    {
        strKeyAttribute = keyAttribute;
    }

    /***************************************************************************
     * @return the propertyVO
     * @author Rocex Wang
     * @since 2021-10-15 03:00:29
     ***************************************************************************/
    @Column(insertable = false, updatable = false)
    public List<PropertyVO> getPropertyVO()
    {
        return propertyVO;
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

    /***************************************************************************
     * @return the refModelName
     * @author Rocex Wang
     * @since 2021-11-16 02:14:23
     ***************************************************************************/
    @Column(length = 512)
    public String getRefModelName()
    {
        return strRefModelName;
    }

    /***************************************************************************
     * @param refModelName the refModelName to set
     * @author Rocex Wang
     * @since 2021-11-16 02:14:23
     ***************************************************************************/
    public void setRefModelName(String refModelName)
    {
        strRefModelName = refModelName;
    }

    /***************************************************************************
     * @return the returnType
     * @author Rocex Wang
     * @since 2021-11-16 02:30:29
     ***************************************************************************/
    @Column(length = 50)
    public String getReturnType()
    {
        return strReturnType;
    }

    /***************************************************************************
     * @param returnType the returnType to set
     * @author Rocex Wang
     * @since 2021-11-16 02:30:29
     ***************************************************************************/
    public void setReturnType(String returnType)
    {
        strReturnType = returnType;
    }
}
