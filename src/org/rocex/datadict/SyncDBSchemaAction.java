package org.rocex.datadict;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.ComponentVO;
import org.rocex.datadict.vo.ModuleVO;
import org.rocex.db.processor.SQLExecutor;
import org.rocex.utils.Logger;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @since 2021-10-28 02:17:34
 ***************************************************************************/
public class SyncDBSchemaAction extends CreateDataDictAction
{
    protected SQLExecutor sqlExecutorTarget = null;

    /***************************************************************************
     * @param strVersion
     * @author Rocex Wang
     * @since 2021-10-28 02:18:20
     ***************************************************************************/
    public SyncDBSchemaAction(String strVersion)
    {
        super(strVersion);
        
        Properties dbPropTarget = new Properties();
        
        // dbPropTarget.setProperty("jdbc.url", "jdbc:sqlite:" + strOutputRootDir + File.separator + "dict.sqlite");
        dbPropTarget.setProperty("jdbc.url", "jdbc:sqlite:C:/datadict/datadict.sqlite");
        dbPropTarget.setProperty("jdbc.driver", "org.sqlite.JDBC");
        
        sqlExecutorTarget = new SQLExecutor(dbPropTarget);
    }

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datadict.IAction#doAction()
     * @author Rocex Wang
     * @since 2021-10-28 14:17:51
     ****************************************************************************/
    @Override
    public void doAction()
    {
        sqlExecutorTarget.initDBSchema(ModuleVO.class, ComponentVO.class, ClassVO.class);
        
        String strModuleSQL = "select distinct lower(id) id,lower(name) name,displayname,b.moduleid moduleid from md_module a left join dap_dapsystem b on lower(a.id)=lower(b.devmodule) order by b.moduleid";
        String strComponentSQL = "select distinct id,name,displayname,lower(ownmodule) ownmodule from md_component where versiontype=0";
        String strClassSQL = "select id,name,displayname,defaulttablename,fullclassname,keyattribute,componentid,classtype,isprimary from md_class order by lower(defaulttablename)";
        String strEnumValueSQL = "select id,name,value from md_enumvalue order by id,enumsequence";
        
        // List<ModuleVO> listModuleVO = (List<ModuleVO>) queryMetaVO(ModuleVO.class, strModuleSQL);
        List<ComponentVO> listComponentVO = (List<ComponentVO>) queryMetaVO(ComponentVO.class, strComponentSQL);
        List<ClassVO> listClassVO = (List<ClassVO>) queryMetaVO(ClassVO.class, strClassSQL);
        
        try
        {
            // sqlExecutorTarget.insertVO(listModuleVO.toArray(new ModuleVO[0]));
            // sqlExecutorTarget.insertVO(listComponentVO.toArray(new ComponentVO[0]));
            sqlExecutorTarget.insertVO(listClassVO.toArray(new ClassVO[0]));
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datadict.CreateDataDictAction#getClassFilePath(org.rocex.datadict.vo.ClassVO)
     * @author Rocex Wang
     * @since 2021-10-28 15:05:15
     ****************************************************************************/
    @Override
    protected Path getClassFilePath(ClassVO classVO)
    {
        return null;
    }
}
