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
    private Integer iVersionType;

    private String strDdcVersion;
    private String strDisplayName;
    private String strHelp;
    private String strId;
    private String strName;
    private String strTs;

    /***************************************************************************
     * @return the dDCVersion
     * @author Rocex Wang
     * @since 2022-08-01 01:42:04
     ***************************************************************************/
    @Column(length = 4)
    public String getDdcVersion()
    {
        return strDdcVersion;
    }

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
     * @return the help
     * @author Rocex Wang
     * @since 2022-08-02 02:08:30
     ***************************************************************************/
    @Column(length = 80)
    public String getHelp()
    {
        return strHelp;
    }

    /***************************************************************************
     * @return the id
     * @author Rocex Wang
     * @version 2020-4-21 13:45:33
     ***************************************************************************/
    @Id
    @Column(nullable = false, length = 50)
    public String getId()
    {
        return strId;
    }

    /***************************************************************************
     * @return the name
     * @author Rocex Wang
     * @version 2020-4-21 13:45:33
     ***************************************************************************/
    @Column(nullable = false)
    public String getName()
    {
        return strName;
    }

    /***************************************************************************
     * @return the ts
     * @author Rocex Wang
     * @since 2022-08-02 02:08:30
     ***************************************************************************/
    @Column(length = 19)
    public String getTs()
    {
        return strTs;
    }

    /***************************************************************************
     * @return the versionType
     * @author Rocex Wang
     * @since 2022-08-02 02:16:32
     ***************************************************************************/
    public Integer getVersionType()
    {
        return iVersionType;
    }

    /***************************************************************************
     * @param dDCVersion the dDCVersion to set
     * @author Rocex Wang
     * @since 2022-08-01 01:42:04
     ***************************************************************************/
    public void setDdcVersion(String dDCVersion)
    {
        strDdcVersion = dDCVersion;
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
     * @param help the help to set
     * @author Rocex Wang
     * @since 2022-08-02 02:08:30
     ***************************************************************************/
    public void setHelp(String help)
    {
        strHelp = help;
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

    /***************************************************************************
     * @param ts the ts to set
     * @author Rocex Wang
     * @since 2022-08-02 02:08:30
     ***************************************************************************/
    public void setTs(String ts)
    {
        strTs = ts;
    }

    /***************************************************************************
     * @param versionType the versionType to set
     * @author Rocex Wang
     * @since 2022-08-02 02:16:32
     ***************************************************************************/
    public void setVersionType(Integer versionType)
    {
        iVersionType = versionType;
    }
}
