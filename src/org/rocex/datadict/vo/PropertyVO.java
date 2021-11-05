package org.rocex.datadict.vo;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 13:44:18
 ***************************************************************************/
@Table(name = "md_property")
public class PropertyVO extends MetaVO
{
    private boolean blKeyProp = false;

    private Integer iAttrLength;
    private Integer iAttrSequence;
    private Integer iDataTypeStyle;     // 999 - 数据库字段
    private Integer iPrecise;

    private String strAttrMaxValue;
    private String strAttrMinValue;
    private String strClassId;
    private String strCustomAttr;
    private String strDataScope;
    private String strDataType;
    private String strDefaultValue;
    private String strNullable;
    private String strOriginalId;
    private String strRefClassPathHref;
    private String strRefModelName;
    private String strDataTypeSql;

    /***************************************************************************
     * @return the attrLength
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public Integer getAttrLength()
    {
        return iAttrLength;
    }

    /***************************************************************************
     * @return the attrMaxValue
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public String getAttrMaxValue()
    {
        return strAttrMaxValue;
    }

    /***************************************************************************
     * @return the attrMinValue
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public String getAttrMinValue()
    {
        return strAttrMinValue;
    }

    /***************************************************************************
     * @return the attrSequence
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public Integer getAttrSequence()
    {
        return iAttrSequence;
    }

    /***************************************************************************
     * @return the classId
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public String getClassId()
    {
        return strClassId;
    }

    /***************************************************************************
     * @return the customAttr
     * @author Rocex Wang
     * @version 2020-11-3 18:27:38
     ***************************************************************************/
    public String getCustomAttr()
    {
        return strCustomAttr;
    }

    /***************************************************************************
     * @return the dataScope
     * @author Rocex Wang
     * @since 2021-10-15 03:04:16
     ***************************************************************************/
    public String getDataScope()
    {
        return strDataScope;
    }

    /***************************************************************************
     * @return the dataType
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public String getDataType()
    {
        return strDataType;
    }

    /***************************************************************************
     * @return the dataTypeStyle
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public Integer getDataTypeStyle()
    {
        return iDataTypeStyle;
    }

    /***************************************************************************
     * @return the defaultValue
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public String getDefaultValue()
    {
        return strDefaultValue;
    }

    @Id
    @Override
    @Column(nullable = false, length = 128)
    public String getId()
    {
        return super.getId();
    }

    /***************************************************************************
     * @return the nullable
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public String getNullable()
    {
        return strNullable;
    }

    /***************************************************************************
     * @return the originalId
     * @author Rocex Wang
     * @since 2021-11-11 11:09:34
     ***************************************************************************/
    public String getOriginalId()
    {
        return strOriginalId;
    }

    /***************************************************************************
     * @return the precise
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public Integer getPrecise()
    {
        return iPrecise;
    }

    /***************************************************************************
     * @return the refClassPathHref
     * @author Rocex Wang
     * @since 2021-10-18 05:16:45
     ***************************************************************************/
    public String getRefClassPathHref()
    {
        return strRefClassPathHref;
    }

    /***************************************************************************
     * @return the refModelName
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public String getRefModelName()
    {
        return strRefModelName;
    }

    /***************************************************************************
     * @return the sqlDateType
     * @author Rocex Wang
     * @version 2020-4-23 11:36:08
     ***************************************************************************/
    public String getDataTypeSql()
    {
        return strDataTypeSql;
    }

    /***************************************************************************
     * @return the keyProp
     * @author Rocex Wang
     * @since 2021-10-18 02:14:09
     ***************************************************************************/
    public boolean isKeyProp()
    {
        return blKeyProp;
    }

    /***************************************************************************
     * @param attrLength the attrLength to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setAttrLength(Integer attrLength)
    {
        iAttrLength = attrLength;
    }

    /***************************************************************************
     * @param attrMaxValue the attrMaxValue to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setAttrMaxValue(String attrMaxValue)
    {
        strAttrMaxValue = attrMaxValue;
    }

    /***************************************************************************
     * @param attrMinValue the attrMinValue to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setAttrMinValue(String attrMinValue)
    {
        strAttrMinValue = attrMinValue;
    }

    /***************************************************************************
     * @param attrSequence the attrSequence to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setAttrSequence(Integer attrSequence)
    {
        iAttrSequence = attrSequence;
    }

    /***************************************************************************
     * @param classId the classId to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setClassId(String classId)
    {
        strClassId = classId;
    }

    /***************************************************************************
     * @param customAttr the customAttr to set
     * @author Rocex Wang
     * @version 2020-11-3 18:27:38
     ***************************************************************************/
    public void setCustomAttr(String customAttr)
    {
        strCustomAttr = customAttr;
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
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setDataType(String dataType)
    {
        strDataType = dataType;
    }

    /***************************************************************************
     * @param dataTypeStyle the dataTypeStyle to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setDataTypeStyle(Integer dataTypeStyle)
    {
        iDataTypeStyle = dataTypeStyle;
    }

    /***************************************************************************
     * @param defaultValue the defaultValue to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setDefaultValue(String defaultValue)
    {
        strDefaultValue = defaultValue;
    }

    /***************************************************************************
     * @param keyProp the keyProp to set
     * @author Rocex Wang
     * @since 2021-10-18 02:14:09
     ***************************************************************************/
    public void setKeyProp(boolean keyProp)
    {
        blKeyProp = keyProp;
    }

    /***************************************************************************
     * @param nullable the nullable to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setNullable(String nullable)
    {
        strNullable = nullable;
    }

    /***************************************************************************
     * @param originalId the originalId to set
     * @author Rocex Wang
     * @since 2021-11-11 11:09:34
     ***************************************************************************/
    public void setOriginalId(String originalId)
    {
        strOriginalId = originalId;
    }

    /***************************************************************************
     * @param precise the precise to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setPrecise(Integer precise)
    {
        iPrecise = precise;
    }

    /***************************************************************************
     * @param refClassPathHref the refClassPathHref to set
     * @author Rocex Wang
     * @since 2021-10-18 05:16:45
     ***************************************************************************/
    public void setRefClassPathHref(String refClassPathHref)
    {
        strRefClassPathHref = refClassPathHref;
    }

    /***************************************************************************
     * @param refModelName the refModelName to set
     * @author Rocex Wang
     * @version 2020-4-22 15:00:08
     ***************************************************************************/
    public void setRefModelName(String refModelName)
    {
        strRefModelName = refModelName;
    }

    /***************************************************************************
     * @param sqlDateType the sqlDateType to set
     * @author Rocex Wang
     * @version 2020-4-23 11:36:08
     ***************************************************************************/
    public void setDataTypeSql(String sqlDateType)
    {
        strDataTypeSql = sqlDateType;
    }
}
