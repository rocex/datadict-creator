package org.rocex.datadict.action;

import java.sql.SQLException;
import java.util.EventObject;
import java.util.List;
import java.util.Properties;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.ComponentVO;
import org.rocex.datadict.vo.Context;
import org.rocex.datadict.vo.DictJsonVO;
import org.rocex.datadict.vo.EnumValueVO;
import org.rocex.datadict.vo.IndexVO;
import org.rocex.datadict.vo.ModuleVO;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.db.SQLExecutor;
import org.rocex.db.param.SQLParameter;
import org.rocex.db.processor.BeanListProcessor;
import org.rocex.db.processor.PagingAction;
import org.rocex.utils.Logger;
import org.rocex.vo.IAction;
import org.rocex.vo.SuperVO;

public class MergeDBAction implements IAction
{
    protected SQLExecutor sqlExecutorTarget;

    public MergeDBAction()
    {
        Properties dbPropTarget = new Properties();

        dbPropTarget.setProperty("jdbc.url", Context.getInstance().getSetting("mergeto.jdbc.url"));
        dbPropTarget.setProperty("jdbc.user", Context.getInstance().getSetting("mergeto.jdbc.user"));
        dbPropTarget.setProperty("jdbc.driver", Context.getInstance().getSetting("mergeto.jdbc.driver"));
        dbPropTarget.setProperty("jdbc.password", Context.getInstance().getSetting("mergeto.jdbc.password"));

        sqlExecutorTarget = new SQLExecutor(dbPropTarget);
        sqlExecutorTarget.initDBSchema(ModuleVO.class, ComponentVO.class, ClassVO.class, PropertyVO.class, EnumValueVO.class, IndexVO.class, DictJsonVO.class);
    }

    void clearData(String strVersion, Class clazz) throws SQLException
    {
        String strMessage = "delete data from " + sqlExecutorTarget.getTableNameFromClass(clazz);
        Logger.getLogger().start(strMessage);

        String strDeleteSQL = sqlExecutorTarget.getSQLDelete(clazz) + " where ddc_version=?";

        sqlExecutorTarget.executeUpdate(strDeleteSQL, new SQLParameter(strVersion));

        Logger.getLogger().stop(strMessage);
    }

    <T extends SuperVO> void copyData(Class<T> clazz, SQLExecutor sqlExecutorSource) throws SQLException
    {
        String strMessage = "copy data from " + sqlExecutorTarget.getTableNameFromClass(clazz);
        Logger.getLogger().start(strMessage);

        int[] iCount = {0, 0};

        PagingAction pagingAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                Logger.getLogger().log2(Logger.iLoggerLevelDebug, ++iCount[0] + "");

                List<T> listVO = (List<T>) evt.getSource();

                try
                {
                    sqlExecutorTarget.insertVO(listVO.toArray(new SuperVO[0]));
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }

                iCount[1] += listVO.size();
            }
        }.setPageSize(100);

        BeanListProcessor<T> processor = new BeanListProcessor<>(clazz);
        processor.setPagingAction(pagingAction);

        sqlExecutorSource.executeQuery(sqlExecutorSource.getSQLSelect(clazz), processor);

        Logger.getLogger().log2(Logger.iLoggerLevelDebug, iCount[0] + " batch, " + iCount[1] + " rows, done!\n");
        Logger.getLogger().stop(strMessage);
    }

    @Override
    public void doAction(EventObject evt)
    {
        String strMessage = "merge data";
        Logger.getLogger().start(strMessage);

        String strMergeFromUrl = Context.getInstance().getSetting("target.jdbc.url");
        String strMergeFromUser = Context.getInstance().getSetting("target.jdbc.user");
        String strMergeFromDriver = Context.getInstance().getSetting("target.jdbc.driver");
        String strMergeFromPassword = Context.getInstance().getSetting("target.jdbc.password");

        Properties dbProp = new Properties();
        dbProp.setProperty("jdbc.user", strMergeFromUser);
        dbProp.setProperty("jdbc.driver", strMergeFromDriver);
        dbProp.setProperty("jdbc.password", strMergeFromPassword);

        Class[] classes = {ModuleVO.class, ComponentVO.class, ClassVO.class, PropertyVO.class, EnumValueVO.class, IndexVO.class, DictJsonVO.class};

        String strDataDictVersionList = Context.getInstance().getSetting("merge.from.versions");
        String[] strDataDictVersions = strDataDictVersionList.split(",");

        for (String strVersion : strDataDictVersions)
        {
            Logger.getLogger().debug("");
            Logger.getLogger().start("merge data from " + strVersion);

            dbProp.setProperty("jdbc.url", strMergeFromUrl.replace("${version}", strVersion));

            try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbProp))
            {
                sqlExecutorSource.getConnection().getMetaData().getDatabaseProductName();

                for (Class clazz : classes)
                {
                    clearData(strVersion, clazz);

                    copyData(clazz, sqlExecutorSource);
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }

            Logger.getLogger().stop("merge data from " + strVersion);
        }

        Logger.getLogger().stop(strMessage);
    }
}
