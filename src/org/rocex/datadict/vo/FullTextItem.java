package org.rocex.datadict.vo;

import org.rocex.vo.SuperVO;

public class FullTextItem extends SuperVO
{
    private String strId, strName;

    public String getId()
    {
        return strId;
    }

    public String getName()
    {
        return strName;
    }

    public void setId(String id)
    {
        strId = id;
    }

    public void setName(String name)
    {
        strName = name;
    }
}
