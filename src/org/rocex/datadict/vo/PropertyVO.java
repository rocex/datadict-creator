package org.rocex.datadict.vo;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @since 2020-4-21 13:44:18
 ***************************************************************************/
@Table(name = "md_property", indexes = { @Index(name = "i_md_property_class_id", columnList = "class_id"),
        @Index(name = "i_md_property_data_type_style", columnList = "data_type_style"),
        @Index(name = "i_md_property_class_id_sequence", columnList = "class_id,attr_sequence"),
        @Index(name = "i_md_property_ddc_version", columnList = "ddc_version") })
public class PropertyVO extends MetaVO
{
    private Boolean blAccessPower = false;
    private Boolean blCalculation = false;
    private Boolean blCustomAttr = false;
    private Boolean blDynamicAttr = false;
    private Boolean blFixedLength = false;
    private Boolean blHidden = false;
    private Boolean blKeyProp = false;
    private Boolean blNotSerialize = false;
    private Boolean blNullable = false;
    private Boolean blReadOnly = false;
    
    private Integer iAttrLength;
    private Integer iAttrSequence;
    private Integer iDataTypeStyle;     // 999 - 数据库字段
    private Integer iPrecise;
    
    private String strAccessorClassname;
    private String strAccessPowerGroup;
    private String strAttrMaxValue;
    private String strAttrMinValue;
    private String strClassId;
    private String strDataScope;
    private String strDataType;
    private String strDataTypeDisplayName;
    private String strDataTypeName;
    private String strDataTypeSql;
    private String strDefaultValue;
    private String strDynamicTable;
    private String strRefModelName;
    private String strRemarks;
    
    /***************************************************************************
     * @return the accessorClassname
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public String getAccessorClassname()
    {
        return strAccessorClassname;
    }
    
    /***************************************************************************
     * @return the accessPowerGroup
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    @Column(length = 50)
    public String getAccessPowerGroup()
    {
        return strAccessPowerGroup;
    }
    
    /***************************************************************************
     * @return the attrLength
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public Integer getAttrLength()
    {
        return iAttrLength;
    }
    
    /***************************************************************************
     * @return the attrMaxValue
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    @Column(length = 50)
    public String getAttrMaxValue()
    {
        return strAttrMaxValue;
    }
    
    /***************************************************************************
     * @return the attrMinValue
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    @Column(length = 50)
    public String getAttrMinValue()
    {
        return strAttrMinValue;
    }
    
    /***************************************************************************
     * @return the attrSequence
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public Integer getAttrSequence()
    {
        return iAttrSequence;
    }
    
    /***************************************************************************
     * @return the classId
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    @Column(length = 50)
    public String getClassId()
    {
        return strClassId;
    }
    
    /***************************************************************************
     * @return the dataScope
     * @author Rocex Wang
     * @since 2021-10-15 03:04:16
     ***************************************************************************/
    @Column(insertable = false, updatable = false)
    public String getDataScope()
    {
        return strDataScope;
    }
    
    /***************************************************************************
     * @return the dataType
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    @Column(length = 50)
    public String getDataType()
    {
        return strDataType;
    }
    
    @Column(insertable = false, updatable = false)
    public String getDataTypeDisplayName()
    {
        return strDataTypeDisplayName;
    }
    
    @Column(insertable = false, updatable = false)
    public String getDataTypeName()
    {
        return strDataTypeName;
    }
    
    /***************************************************************************
     * @return the sqlDateType
     * @author Rocex Wang
     * @since 2020-4-23 11:36:08
     ***************************************************************************/
    @Column(length = 50)
    public String getDataTypeSql()
    {
        return strDataTypeSql;
    }
    
    /***************************************************************************
     * @return the dataTypeStyle
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public Integer getDataTypeStyle()
    {
        return iDataTypeStyle;
    }
    
    /***************************************************************************
     * @return the defaultValue
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    @Column(length = 80)
    public String getDefaultValue()
    {
        return strDefaultValue;
    }
    
    /***************************************************************************
     * @return the dynamicTable
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    @Column(length = 50)
    public String getDynamicTable()
    {
        return strDynamicTable;
    }
    
    @Override
    @Column(insertable = false, updatable = false)
    public String getModelType()
    {
        return super.getModelType();
    }
    
    /***************************************************************************
     * @return the precise
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public Integer getPrecise()
    {
        return iPrecise;
    }
    
    /***************************************************************************
     * @return the refModelName
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    @Column(length = 512)
    public String getRefModelName()
    {
        return strRefModelName;
    }
    
    @Column(insertable = false, updatable = false)
    public String getRemarks()
    {
        return strRemarks;
    }
    
    /***************************************************************************
     * @return the accessPower
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public Boolean isAccessPower()
    {
        return blAccessPower;
    }
    
    /***************************************************************************
     * @return the calculation
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public Boolean isCalculation()
    {
        return blCalculation;
    }
    
    /***************************************************************************
     * @return the customAttr
     * @author Rocex Wang
     * @since 2020-11-3 18:27:38
     ***************************************************************************/
    public Boolean isCustomAttr()
    {
        return blCustomAttr;
    }
    
    /***************************************************************************
     * @return the dynamicAttr
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public Boolean isDynamicAttr()
    {
        return blDynamicAttr;
    }
    
    /***************************************************************************
     * @return the fixedLength
     * @author Rocex Wang
     * @since 2022-08-02 03:52:14
     ***************************************************************************/
    public Boolean isFixedLength()
    {
        return blFixedLength;
    }
    
    /***************************************************************************
     * @return the hidden
     * @author Rocex Wang
     * @since 2022-08-02 03:52:14
     ***************************************************************************/
    public Boolean isHidden()
    {
        return blHidden;
    }
    
    /***************************************************************************
     * @return the keyProp
     * @author Rocex Wang
     * @since 2021-10-18 02:14:09
     ***************************************************************************/
    public Boolean isKeyProp()
    {
        return blKeyProp;
    }
    
    /***************************************************************************
     * @return the notSerialize
     * @author Rocex Wang
     * @since 2022-08-02 03:52:14
     ***************************************************************************/
    public Boolean isNotSerialize()
    {
        return blNotSerialize;
    }
    
    /***************************************************************************
     * @return the nullable
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public Boolean isNullable()
    {
        return blNullable;
    }
    
    /***************************************************************************
     * @return the readOnly
     * @author Rocex Wang
     * @since 2022-08-02 03:52:14
     ***************************************************************************/
    public Boolean isReadOnly()
    {
        return blReadOnly;
    }
    
    /***************************************************************************
     * @param accessorClassname the accessorClassname to set
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public void setAccessorClassname(String accessorClassname)
    {
        strAccessorClassname = accessorClassname;
    }
    
    /***************************************************************************
     * @param accessPower the accessPower to set
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public void setAccessPower(Boolean accessPower)
    {
        blAccessPower = accessPower;
    }
    
    /***************************************************************************
     * @param accessPowerGroup the accessPowerGroup to set
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public void setAccessPowerGroup(String accessPowerGroup)
    {
        strAccessPowerGroup = accessPowerGroup;
    }
    
    /***************************************************************************
     * @param attrLength the attrLength to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setAttrLength(Integer attrLength)
    {
        iAttrLength = attrLength;
    }
    
    /***************************************************************************
     * @param attrMaxValue the attrMaxValue to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setAttrMaxValue(String attrMaxValue)
    {
        strAttrMaxValue = attrMaxValue;
    }
    
    /***************************************************************************
     * @param attrMinValue the attrMinValue to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setAttrMinValue(String attrMinValue)
    {
        strAttrMinValue = attrMinValue;
    }
    
    /***************************************************************************
     * @param attrSequence the attrSequence to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setAttrSequence(Integer attrSequence)
    {
        iAttrSequence = attrSequence;
    }
    
    /***************************************************************************
     * @param calculation the calculation to set
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public void setCalculation(Boolean calculation)
    {
        blCalculation = calculation;
    }
    
    /***************************************************************************
     * @param classId the classId to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setClassId(String classId)
    {
        strClassId = classId;
    }
    
    /***************************************************************************
     * @param customAttr the customAttr to set
     * @author Rocex Wang
     * @since 2020-11-3 18:27:38
     ***************************************************************************/
    public void setCustomAttr(Boolean customAttr)
    {
        blCustomAttr = customAttr;
    }
    
    /***************************************************************************
     * @param dataScope the dataScope to set
     * @author Rocex Wang
     * @since 2021-10-15 03:04:16
     ***************************************************************************/
    public void setDataScope(String dataScope)
    {
        strDataScope = dataScope;
    }
    
    /***************************************************************************
     * @param dataType the dataType to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setDataType(String dataType)
    {
        strDataType = dataType;
    }
    
    public void setDataTypeDisplayName(String dataTypeDisplayName)
    {
        strDataTypeDisplayName = dataTypeDisplayName;
    }
    
    public void setDataTypeName(String dataTypeName)
    {
        strDataTypeName = dataTypeName;
    }
    
    /***************************************************************************
     * @param sqlDateType the sqlDateType to set
     * @author Rocex Wang
     * @since 2020-4-23 11:36:08
     ***************************************************************************/
    public void setDataTypeSql(String sqlDateType)
    {
        strDataTypeSql = sqlDateType;
    }
    
    /***************************************************************************
     * @param dataTypeStyle the dataTypeStyle to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setDataTypeStyle(Integer dataTypeStyle)
    {
        iDataTypeStyle = dataTypeStyle;
    }
    
    /***************************************************************************
     * @param defaultValue the defaultValue to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setDefaultValue(String defaultValue)
    {
        strDefaultValue = defaultValue;
    }
    
    /***************************************************************************
     * @param dynamicAttr the dynamicAttr to set
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public void setDynamicAttr(Boolean dynamicAttr)
    {
        blDynamicAttr = dynamicAttr;
    }
    
    /***************************************************************************
     * @param dynamicTable the dynamicTable to set
     * @author Rocex Wang
     * @since 2021-11-16 04:05:17
     ***************************************************************************/
    public void setDynamicTable(String dynamicTable)
    {
        strDynamicTable = dynamicTable;
    }
    
    /***************************************************************************
     * @param fixedLength the fixedLength to set
     * @author Rocex Wang
     * @since 2022-08-02 03:52:14
     ***************************************************************************/
    public void setFixedLength(Boolean fixedLength)
    {
        blFixedLength = fixedLength;
    }
    
    /***************************************************************************
     * @param hidden the hidden to set
     * @author Rocex Wang
     * @since 2022-08-02 03:52:14
     ***************************************************************************/
    public void setHidden(Boolean hidden)
    {
        blHidden = hidden;
    }
    
    /***************************************************************************
     * @param keyProp the keyProp to set
     * @author Rocex Wang
     * @since 2021-10-18 02:14:09
     ***************************************************************************/
    public void setKeyProp(Boolean keyProp)
    {
        blKeyProp = keyProp;
    }
    
    /***************************************************************************
     * @param notSerialize the notSerialize to set
     * @author Rocex Wang
     * @since 2022-08-02 03:52:14
     ***************************************************************************/
    public void setNotSerialize(Boolean notSerialize)
    {
        blNotSerialize = notSerialize;
    }
    
    /***************************************************************************
     * @param nullable the nullable to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setNullable(Boolean nullable)
    {
        blNullable = nullable;
    }
    
    /***************************************************************************
     * @param precise the precise to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setPrecise(Integer precise)
    {
        iPrecise = precise;
    }
    
    /***************************************************************************
     * @param readOnly the readOnly to set
     * @author Rocex Wang
     * @since 2022-08-02 03:52:14
     ***************************************************************************/
    public void setReadOnly(Boolean readOnly)
    {
        blReadOnly = readOnly;
    }
    
    /***************************************************************************
     * @param refModelName the refModelName to set
     * @author Rocex Wang
     * @since 2020-4-22 15:00:08
     ***************************************************************************/
    public void setRefModelName(String refModelName)
    {
        strRefModelName = refModelName;
    }
    
    public void setRemarks(String strRemarks)
    {
        this.strRemarks = strRemarks;
    }
}
