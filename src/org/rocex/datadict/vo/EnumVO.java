package org.rocex.datadict.vo;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-23 13:35:11
 ***************************************************************************/
public class EnumVO extends MetaVO
{
    private String strValue;
    
    /***************************************************************************
     * @return the value
     * @author Rocex Wang
     * @version 2020-4-23 13:36:10
     ***************************************************************************/
    public String getValue()
    {
        return strValue;
    }
    
    /***************************************************************************
     * @param value the value to set
     * @author Rocex Wang
     * @version 2020-4-23 13:36:10
     ***************************************************************************/
    public void setValue(String value)
    {
        strValue = value;
    }
}
