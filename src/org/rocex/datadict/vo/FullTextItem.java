package org.rocex.datadict.vo;

import org.rocex.vo.SuperVO;

public class FullTextItem extends SuperVO
{
    private String strId, strName;
    
    @Override
    public String getId()
    {
        return strId;
    }
    
    public String getName()
    {
        return strName;
    }
    
    @Override
    public void setId(String id)
    {
        strId = id;
    }
    
    public void setName(String name)
    {
        strName = name;
    }
}
