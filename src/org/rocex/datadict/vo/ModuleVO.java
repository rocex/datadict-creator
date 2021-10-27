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
    private String strModuleId;

    /***************************************************************************
     * @return the moduleId
     * @author Rocex Wang
     * @version 2020-5-19 20:57:57
     ***************************************************************************/
    public String getModuleId()
    {
        return strModuleId;
    }

    /***************************************************************************
     * @param moduleId the moduleId to set
     * @author Rocex Wang
     * @version 2020-5-19 20:57:57
     ***************************************************************************/
    public void setModuleId(String moduleId)
    {
        strModuleId = moduleId;
    }
}
