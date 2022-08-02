package org.rocex.datadict.vo;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-23 13:35:11
 ***************************************************************************/
@Table(name = "md_enumvalue", indexes = { @Index(name = "i_md_enumvalue_class_id", columnList = "class_id,enum_sequence"),
        @Index(name = "i_md_enumvalue_ddc_version", columnList = "ddc_version") })
public class EnumValueVO extends MetaVO
{
    private Integer iEnumSequence;

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
    public Integer getEnumSequence()
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

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datadict.vo.MetaVO#getHelp()
     * @author Rocex Wang
     * @since 2022-08-02 16:36:42
     ****************************************************************************/
    @Override
    @Column(insertable = false, updatable = false)
    public String getHelp()
    {
        return super.getHelp();
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
    public void setEnumSequence(Integer enumSequence)
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
