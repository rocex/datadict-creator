package org.rocex.datadict.vo;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-23 13:35:11
 ***************************************************************************/
@Table(name = "ddc_dict_json", indexes = { @Index(name = "i_ddc_dict_json_ddc_version", columnList = "ddc_version") })
public class DictJsonVO extends MetaVO
{
    private String strClassId;
    private Object strDictJson;

    /***************************************************************************
     * @return the classId
     * @author Rocex Wang
     * @since 2022-08-02 01:30:37
     ***************************************************************************/
    public String getClassId()
    {
        return strClassId;
    }

    /***************************************************************************
     * @return the dictJson
     * @author Rocex Wang
     * @since 2022-08-01 05:37:02
     ***************************************************************************/
    @Column(columnDefinition = "longtext")
    public Object getDictJson()
    {
        return strDictJson;
    }

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datadict.vo.MetaVO#getHelp()
     * @author Rocex Wang
     * @since 2022-08-02 16:38:22
     ****************************************************************************/
    @Override
    @Column(insertable = false, updatable = false)
    public String getHelp()
    {
        return super.getHelp();
    }

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datadict.vo.MetaVO#getVersionType()
     * @author Rocex Wang
     * @since 2022-08-02 16:38:22
     ****************************************************************************/
    @Override
    @Column(insertable = false, updatable = false)
    public Integer getVersionType()
    {
        return super.getVersionType();
    }

    /***************************************************************************
     * @param classId the classId to set
     * @author Rocex Wang
     * @since 2022-08-02 01:30:37
     ***************************************************************************/
    public void setClassId(String classId)
    {
        strClassId = classId;
    }

    /***************************************************************************
     * @param dictJson the dictJson to set
     * @author Rocex Wang
     * @since 2022-08-01 05:37:02
     ***************************************************************************/
    public void setDictJson(Object dictJson)
    {
        strDictJson = dictJson;
    }
}
