package org.rocex.datadict.vo;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 13:20:06
 ***************************************************************************/
@Table(name = "md_class", indexes = { @Index(name = "i_md_class_component_id", columnList = "component_id"),
        @Index(name = "i_md_class_default_table_name", columnList = "default_table_name"), @Index(name = "i_md_class_class_type", columnList = "class_type"),
        @Index(name = "i_md_class_ddc_version", columnList = "ddc_version") })
public class ClassVO extends MetaVO
{
    private Boolean blIsPrimary = false;
    private Boolean blIsAuthen;
    
    private Integer iClassType;     // 999 - 数据库表
    
    private String strAccessorClassname;
    private String strBizItfImpClassname;
    private String strClassListUrl;
    private String strComponentId;
    private String strDefaultTableName;
    private String strFullClassname;
    private String strHelp;
    private String strKeyAttribute;
    private String strRefModelName;
    private String strReturnType;
    
    private List<PropertyVO> propertyVO;
    
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
     * @return the bizItfImpClassname
     * @author Rocex Wang
     * @since 2021-11-16 02:14:23
     ***************************************************************************/
    public String getBizItfImpClassname()
    {
        return strBizItfImpClassname;
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
     * @return the help
     * @author Rocex Wang
     * @since 2021-11-12 03:29:20
     ***************************************************************************/
    @Override
    public String getHelp()
    {
        return strHelp;
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
     * @return the isPrimary
     * @author Rocex Wang
     * @version 2020-4-23 19:55:37
     ***************************************************************************/
    public Boolean getIsPrimary()
    {
        return blIsPrimary;
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
    @Column(insertable = false, updatable = false)
    public List<PropertyVO> getPropertyVO()
    {
        return propertyVO;
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
     * @return the returnType
     * @author Rocex Wang
     * @since 2021-11-16 02:30:29
     ***************************************************************************/
    public String getReturnType()
    {
        return strReturnType;
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
     * @param bizItfImpClassname the bizItfImpClassname to set
     * @author Rocex Wang
     * @since 2021-11-16 02:14:23
     ***************************************************************************/
    public void setBizItfImpClassname(String bizItfImpClassname)
    {
        strBizItfImpClassname = bizItfImpClassname;
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
     * @param help the help to set
     * @author Rocex Wang
     * @since 2021-11-12 03:29:20
     ***************************************************************************/
    @Override
    public void setHelp(String help)
    {
        strHelp = help;
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
     * @param isPrimary the isPrimary to set
     * @author Rocex Wang
     * @version 2020-4-23 19:55:37
     ***************************************************************************/
    public void setIsPrimary(Boolean isPrimary)
    {
        blIsPrimary = isPrimary;
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
     * @param returnType the returnType to set
     * @author Rocex Wang
     * @since 2021-11-16 02:30:29
     ***************************************************************************/
    public void setReturnType(String returnType)
    {
        strReturnType = returnType;
    }
}
