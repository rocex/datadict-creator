package org.rocex.datadict.action;

interface ISyncDBSchemaAction
{
    default void afterSyncData() {}

    default void beforeSyncData() {}

    default void syncMetaData() {}
}
