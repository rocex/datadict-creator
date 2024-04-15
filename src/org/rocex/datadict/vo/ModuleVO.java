package org.rocex.datadict.vo;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @since 2020-4-21 16:22:12
 ***************************************************************************/
@Table(name = "md_module", indexes = {@Index(name = "i_md_module_ddc_version", columnList = "ddc_version")})
public class ModuleVO extends MetaVO
{
    public static final String strDBTablesRootId = "db_tables";

    private String strParentModuleId;

    /***************************************************************************
     * @return the parentModuleId
     * @author Rocex Wang
     * @since 2021-11-12 02:26:17
     ***************************************************************************/
    @Column(length = 50)
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

    @Id
    @Column(length = 2)
    public String getModelType()
    {
        return super.getModelType();
    }
}
