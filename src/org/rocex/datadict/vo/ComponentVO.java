package org.rocex.datadict.vo;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 13:20:26
 ***************************************************************************/
public class ComponentVO extends MetaVO
{
    private String strOwnModule;

    /***************************************************************************
     * @return the ownModule
     * @author Rocex Wang
     * @version 2020-4-22 10:27:36
     ***************************************************************************/
    public String getOwnModule()
    {
        return strOwnModule;
    }

    /***************************************************************************
     * @param ownModule the ownModule to set
     * @author Rocex Wang
     * @version 2020-4-22 10:27:36
     ***************************************************************************/
    public void setOwnModule(String ownModule)
    {
        strOwnModule = ownModule;
    }
}
