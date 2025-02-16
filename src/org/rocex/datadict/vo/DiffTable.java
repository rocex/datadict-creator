package org.rocex.datadict.vo;

import org.rocex.vo.SuperVO;

public class DiffTable extends SuperVO
{
    private String schema2;
    private String tableName;
    private String tableShowName;
    
    public String getSchema2()
    {
        return schema2;
    }
    
    public String getTableName()
    {
        return tableName;
    }
    
    public String getTableShowName()
    {
        return tableShowName;
    }
    
    public void setSchema2(String schema2)
    {
        this.schema2 = schema2;
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
