package org.rocex.datadict.vo;

import java.math.BigDecimal;

import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-23 13:35:11
 ***************************************************************************/
@Table(name = "md_enumvalue")
public class EnumVO extends MetaVO
{
    private BigDecimal iEnumSequence;

    private String strClassId;
    private String strEnumValue;

    /***************************************************************************
     * @return the classId
     * @author Rocex Wang
     * @since 2021-11-09 03:20:46
     ***************************************************************************/
    public String getClassId()
    {
        return strClassId;
    }

    /***************************************************************************
     * @return the enumSequence
     * @author Rocex Wang
     * @since 2021-11-09 03:09:45
     ***************************************************************************/
    public BigDecimal getEnumSequence()
    {
        return iEnumSequence;
    }

    /***************************************************************************
     * @return the value
     * @author Rocex Wang
     * @version 2020-4-23 13:36:10
     ***************************************************************************/
    public String getEnumValue()
    {
        return strEnumValue;
    }

    /***************************************************************************
     * @param classId the classId to set
     * @author Rocex Wang
     * @since 2021-11-09 03:20:46
     ***************************************************************************/
    public void setClassId(String classId)
    {
        strClassId = classId;
    }

    /***************************************************************************
     * @param enumSequence the enumSequence to set
     * @author Rocex Wang
     * @since 2021-11-09 03:09:45
     ***************************************************************************/
    public void setEnumSequence(BigDecimal enumSequence)
    {
        iEnumSequence = enumSequence;
    }

    /***************************************************************************
     * @param value the value to set
     * @author Rocex Wang
     * @version 2020-4-23 13:36:10
     ***************************************************************************/
    public void setEnumValue(String value)
    {
        strEnumValue = value;
    }
}
