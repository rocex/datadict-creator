package org.rocex.datadict.vo;

import org.rocex.vo.SuperVO;

public class FullTextItem extends SuperVO
{
    private String strName;
    
    public FullTextItem()
    {
        super();
    }
    
    public String getName()
    {
        return strName;
    }
    
    public void setName(String name)
    {
        strName = name;
    }
}