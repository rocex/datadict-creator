package org.rocex.datadict.vo;

import org.rocex.vo.SuperVO;

public class DiffTable extends SuperVO
{
    private String schema;
    private String tableName;
    private String tableShowName;

    public String getSchema()
    {
        return schema;
    }

    public String getTableName()
    {
        return tableName;
    }

    public String getTableShowName()
    {
        return tableShowName;
    }

    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public void setTableShowName(String tableShowName)
    {
        this.tableShowName = tableShowName;
    }
}
