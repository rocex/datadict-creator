package org.rocex.datadict;

import java.io.IOException;
import java.nio.file.Path;

import org.rocex.datadict.action.CreateDataDictAction;
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
        Logger.getLogger().start("create all data dictionary");

        String strDataDictVersionList = Context.getInstance().getSetting("DataDictVersionList");

        String[] strDataDictVersions = strDataDictVersionList.split(",");

        for (String strVersion : strDataDictVersions)
        {
            String strOutputRootDir = Context.getInstance().getVersionSetting(strVersion, "OutputDir");

            try
            {
                FileHelper.checkAndCreatePath(Path.of(strOutputRootDir));
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }

            boolean isBIP = Boolean.parseBoolean(Context.getInstance().getVersionSetting(strVersion, "isBIP", "true"));

            SyncDBSchemaAction syncDBSchemaAction = isBIP ? new SyncDBSchemaBipAction(strVersion) : new SyncDBSchemaNccAction(strVersion);
            syncDBSchemaAction.doAction(null);

            syncDBSchemaAction.close();

            System.gc();

            CreateDataDictAction createDataDictAction = new CreateDataDictAction(strVersion);
            createDataDictAction.doAction(null);

            System.gc();

            Logger.getLogger().debug("\n");
        }

        Logger.getLogger().stop("create all data dictionary");
    }
}
