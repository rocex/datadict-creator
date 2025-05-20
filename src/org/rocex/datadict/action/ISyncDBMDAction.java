package org.rocex.datadict.action;

import org.rocex.db.SQLExecutor;

interface ISyncDBMDAction
{
    default void afterSyncDBMetaData(SQLExecutor sqlExecutorSource)
    {}
    
    void afterSyncMetaData();
    
    default void beforeSyncDBMetaData(SQLExecutor sqlExecutorSource)
    {}
    
    void beforeSyncMetaData();
    
    void syncDBMetaData();
    
    void syncMDMetaData();
}
