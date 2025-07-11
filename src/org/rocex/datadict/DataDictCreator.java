package org.rocex.datadict;

import java.io.IOException;
import java.nio.file.Path;

import org.rocex.datadict.action.CreateDBDiffAction;
import org.rocex.datadict.action.CreateDataDictAction;
import org.rocex.datadict.action.MergeDBAction;
import org.rocex.datadict.action.SyncDBMDAction;
import org.rocex.datadict.action.SyncDBMDBipAction;
import org.rocex.datadict.action.SyncDBMDNccAction;
import org.rocex.datadict.vo.Context;
import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;

/***************************************************************************
 * 根据数据库中元数据生成NC数据字典<br>
 * @author Rocex Wang
 * @since 2020-4-21 15:43:05
 ***************************************************************************/
public class DataDictCreator
{
    /***************************************************************************
     * @param args String[]
     * @author Rocex Wang
     * @since 2020-4-21 15:43:05
     ***************************************************************************/
    public static void main(String[] args)
    {
        Logger.getLogger().begin("create all data dictionary");
        
        String strDataDictVersionList = Context.getInstance().getSetting("DataDictVersionList");
        
        String[] strDataDictVersions = strDataDictVersionList.split(",");
        
        for (String strVersion : strDataDictVersions)
        {
            Context.getInstance().resetVersion(strVersion);
            
            try
            {
                FileHelper.checkAndCreatePath(Path.of(Context.getInstance().getSetting("WorkDir"), "datadict-" + strVersion));
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
            
            boolean isBIP = Boolean.parseBoolean(Context.getInstance().getSetting("isBIP", "true"));
            
            try (SyncDBMDAction syncDBMDAction = isBIP ? new SyncDBMDBipAction(strVersion) : new SyncDBMDNccAction(strVersion);
                    CreateDataDictAction createDataDictAction = new CreateDataDictAction(strVersion))
            {
                syncDBMDAction.doAction(null);
                
                System.gc();
                
                createDataDictAction.doAction(null);
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                System.gc();
            }
            
            Logger.getLogger().debug("\n");
        }
        
        Logger.getLogger().end("create all data dictionary");
        
        boolean blDiffEnable = Boolean.parseBoolean(Context.getInstance().getSetting("diff.enable", "false"));
        boolean blMergeEnable = Boolean.parseBoolean(Context.getInstance().getSetting("merge.enable", "false"));
        
        if (blDiffEnable)
        {
            new CreateDBDiffAction().doAction(null);
        }
        
        if (blMergeEnable)
        {
            new MergeDBAction().doAction(null);
        }
    }
}
