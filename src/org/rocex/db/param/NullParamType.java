package org.rocex.db.param;

/***************************************************************************
 * 空类型 <br>
 * @author Rocex Wang
 * @version 2019-8-12 16:30:02
 ***************************************************************************/
public class NullParamType implements SQLParamType
{
    private int type;
    
    public NullParamType(int type)
    {
        this.type = type;
    }
    
    public int getType()
    {
        return type;
    }
    
    public void setType(int type)
    {
        this.type = type;
    }
}
