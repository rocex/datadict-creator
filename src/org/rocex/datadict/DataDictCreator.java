package org.rocex.datadict;

import java.io.IOException;
import java.nio.file.Path;

import org.rocex.datadict.action.CreateDBDiffAction;
import org.rocex.datadict.action.CreateDataDictAction;
import org.rocex.datadict.action.MergeDBAction;
import org.rocex.datadict.action.SyncDBSchemaAction;
import org.rocex.datadict.action.SyncDBSchemaBipAction;
import org.rocex.datadict.action.SyncDBSchemaNccAction;
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
        boolean blDiffEnable = false;// Boolean.parseBoolean(Context.getInstance().getSetting("diff.enable", "false"));
        boolean blMergeEnable = true;
        boolean blSkipEnable = true;
        
        if (blDiffEnable)
        {
            new CreateDBDiffAction().doAction(null);
        }
        
        if (blMergeEnable)
        {
            new MergeDBAction().doAction(null);
        }
        
        if (blSkipEnable)
        {
            return;
        }
        
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
            
            try (SyncDBSchemaAction syncDBSchemaAction = isBIP ? new SyncDBSchemaBipAction(strVersion) : new SyncDBSchemaNccAction(strVersion))
            {
                syncDBSchemaAction.doAction(null);
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                System.gc();
            }
            
            try (CreateDataDictAction createDataDictAction = new CreateDataDictAction(strVersion))
            {
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
    }
}
