package org.rocex.datadict.vo;

public class DiffColumn extends DiffTable
{
    private String columnName;
    private String columnShowName;
    private String dataTypeSql;
    private String defaultValue;
    private String merge1;
    private String merge2;
    private Boolean nullable;
    
    public String getColumnName()
    {
        return columnName;
    }
    
    public String getColumnShowName()
    {
        return columnShowName;
    }
    
    public String getDataTypeSql()
    {
        return dataTypeSql;
    }
    
    public String getDefaultValue()
    {
        return defaultValue;
    }
    
    public String getMerge1()
    {
        return merge1;
    }
    
    public String getMerge2()
    {
        return merge2;
    }
    
    public Boolean isNullable()
    {
        return nullable;
    }
    
    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }
    
    public void setColumnShowName(String columnShowName)
    {
        this.columnShowName = columnShowName;
    }
    
    public void setDataTypeSql(String dataTypeSql)
    {
        this.dataTypeSql = dataTypeSql;
    }
    
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    
    public void setMerge1(String merge1)
    {
        this.merge1 = merge1;
    }
    
    public void setMerge2(String merge2)
    {
        this.merge2 = merge2;
    }
    
    public void setNullable(Boolean nullable)
    {
        this.nullable = nullable;
    }
}
