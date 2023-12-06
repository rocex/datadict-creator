package org.rocex.datadict;

import org.rocex.datadict.action.CreateDataDictAction;
import org.rocex.datadict.action.CreateHtmlDataDictAction;
import org.rocex.datadict.action.SyncDBSchemaAction;
import org.rocex.datadict.vo.Context;
import org.rocex.utils.Logger;
import org.rocex.vo.IAction;

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

        String strCreateType = Context.getInstance().getSetting("createType", "json");

        String strDataDictVersionList = Context.getInstance().getSetting("DataDictVersionList");

        String[] strDataDictVersions = strDataDictVersionList.split(",");

        for (String strVersion : strDataDictVersions)
        {
            String strDataDictVersion = Context.getInstance().getSetting(strVersion + ".DataDictVersion");

            new SyncDBSchemaAction(strVersion).doAction(null);

            Logger.getLogger().begin("create data dictionary " + strDataDictVersion);

            IAction action = "html".equalsIgnoreCase(strCreateType) ? new CreateHtmlDataDictAction(strVersion) : new CreateDataDictAction(strVersion);

            action.doAction(null);

            Logger.getLogger().end("create data dictionary " + strDataDictVersion);

            Logger.getLogger().debug("\n");
        }

        Logger.getLogger().end("create all data dictionary");
    }
}
