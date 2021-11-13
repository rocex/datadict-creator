package org.rocex.datadict.vo;

import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 16:22:12
 ***************************************************************************/
@Table(name = "md_module")
public class ModuleVO extends MetaVO
{
    private String strParentModuleId;

    /***************************************************************************
     * @return the parentModuleId
     * @author Rocex Wang
     * @since 2021-11-12 02:26:17
     ***************************************************************************/
    public String getParentModuleId()
    {
        return strParentModuleId;
    }

    /***************************************************************************
     * @param parentModuleId the parentModuleId to set
     * @author Rocex Wang
     * @since 2021-11-12 02:26:17
     ***************************************************************************/
    public void setParentModuleId(String parentModuleId)
    {
        strParentModuleId = parentModuleId;
    }
}
