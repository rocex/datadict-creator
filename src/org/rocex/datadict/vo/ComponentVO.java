package org.rocex.datadict.vo;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-21 13:20:26
 ***************************************************************************/
@Table(name = "md_component")
public class ComponentVO extends MetaVO
{
    private List<ClassVO> classVO;

    private String strOriginalId;
    private String strOwnModule;

    /***************************************************************************
     * @return the classVO
     * @author Rocex Wang
     * @since 2021-10-27 06:48:49
     ***************************************************************************/
    @Column(insertable = false, updatable = false)
    public List<ClassVO> getClassVO()
    {
        return classVO;
    }

    /***************************************************************************
     * @return the originalId
     * @author Rocex Wang
     * @since 2021-11-12 07:07:59
     ***************************************************************************/
    public String getOriginalId()
    {
        return strOriginalId;
    }

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
     * @param classVO the classVO to set
     * @author Rocex Wang
     * @since 2021-10-27 06:48:49
     ***************************************************************************/
    public void setClassVO(List<ClassVO> classVO)
    {
        this.classVO = classVO;
    }

    /***************************************************************************
     * @param originalId the originalId to set
     * @author Rocex Wang
     * @since 2021-11-12 07:07:59
     ***************************************************************************/
    public void setOriginalId(String originalId)
    {
        strOriginalId = originalId;
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
