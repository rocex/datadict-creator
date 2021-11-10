package org.rocex.datadict.vo;

import javax.persistence.Column;
import javax.persistence.Id;

import org.rocex.vo.SuperVO;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 13:43:18
 ***************************************************************************/
public class MetaVO extends SuperVO
{
    private String strDisplayName;
    private String strId;
    private String strName;
    
    /***************************************************************************
     * @return the displayName
     * @author Rocex Wang
     * @version 2020-4-21 13:45:48
     ***************************************************************************/
    public String getDisplayName()
    {
        return strDisplayName;
    }
    
    /***************************************************************************
     * @return the id
     * @author Rocex Wang
     * @version 2020-4-21 13:45:33
     ***************************************************************************/
    @Id
    @Column(nullable = false, length = 128)
    public String getId()
    {
        return strId;
    }
    
    /***************************************************************************
     * @return the name
     * @author Rocex Wang
     * @version 2020-4-21 13:45:33
     ***************************************************************************/
    public String getName()
    {
        return strName;
    }
    
    /***************************************************************************
     * @param displayName the displayName to set
     * @author Rocex Wang
     * @version 2020-4-21 13:45:48
     ***************************************************************************/
    public void setDisplayName(String displayName)
    {
        strDisplayName = displayName;
    }
    
    /***************************************************************************
     * @param id the id to set
     * @author Rocex Wang
     * @version 2020-4-21 13:45:33
     ***************************************************************************/
    public void setId(String id)
    {
        strId = id;
    }
    
    /***************************************************************************
     * @param name the name to set
     * @author Rocex Wang
     * @version 2020-4-21 13:45:34
     ***************************************************************************/
    public void setName(String name)
    {
        strName = name;
    }
}
