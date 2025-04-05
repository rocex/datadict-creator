package org.rocex.datadict.vo;

import org.rocex.vo.SuperVO;

public class FullText
{
    private FullTextItem[] data;

    public FullTextItem[] getData()
    {
        return data;
    }

    public void setData(FullTextItem[] data)
    {
        this.data = data;
    }

    public class FullTextItem extends SuperVO
    {
        private String strName;

        public String getName()
        {
            return strName;
        }

        public void setName(String name)
        {
            strName = name;
        }
    }
}