package org.rocex.datadict.vo;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.List;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @since 2020-4-21 13:20:26
 ***************************************************************************/
@Table(name = "md_component", indexes = {@Index(name = "i_md_component_ddc_version", columnList = "ddc_version")})
public class ComponentVO extends MetaVO
{
    private Boolean blBizModel;

    private List<ClassVO> classVO;

    private Integer iVersion;

    private String strNamespace;
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
     * @param classVO the classVO to set
     * @author Rocex Wang
     * @since 2021-10-27 06:48:49
     ***************************************************************************/
    public void setClassVO(List<ClassVO> classVO)
    {
        this.classVO = classVO;
    }

    /***************************************************************************
     * @return the namespace
     * @author Rocex Wang
     * @since 2021-11-16 01:57:12
     ***************************************************************************/
    public String getNamespace()
    {
        return strNamespace;
    }

    /***************************************************************************
     * @param namespace the namespace to set
     * @author Rocex Wang
     * @since 2021-11-16 01:57:12
     ***************************************************************************/
    public void setNamespace(String namespace)
    {
        strNamespace = namespace;
    }

    /***************************************************************************
     * @return the originalId
     * @author Rocex Wang
     * @since 2021-11-12 07:07:59
     ***************************************************************************/
    @Column(length = 50)
    public String getOriginalId()
    {
        return strOriginalId;
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
     * @return the ownModule
     * @author Rocex Wang
     * @since 2020-4-22 10:27:36
     ***************************************************************************/
    public String getOwnModule()
    {
        return strOwnModule;
    }

    /***************************************************************************
     * @param ownModule the ownModule to set
     * @author Rocex Wang
     * @since 2020-4-22 10:27:36
     ***************************************************************************/
    public void setOwnModule(String ownModule)
    {
        strOwnModule = ownModule;
    }

    /***************************************************************************
     * @return the version
     * @author Rocex Wang
     * @since 2022-08-02 02:18:50
     ***************************************************************************/
    public Integer getVersion()
    {
        return iVersion;
    }

    /***************************************************************************
     * @param version the version to set
     * @author Rocex Wang
     * @since 2022-08-02 02:18:50
     ***************************************************************************/
    public void setVersion(Integer version)
    {
        iVersion = version;
    }

    /***************************************************************************
     * @return the bizModel
     * @author Rocex Wang
     * @since 2021-11-19 01:22:46
     ***************************************************************************/
    public Boolean isBizModel()
    {
        return blBizModel;
    }

    /***************************************************************************
     * @param bizModel the bizModel to set
     * @author Rocex Wang
     * @since 2021-11-19 01:22:46
     ***************************************************************************/
    public void setBizModel(Boolean bizModel)
    {
        blBizModel = bizModel;
    }
}
