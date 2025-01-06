package org.rocex.datadict.vo;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.rocex.vo.SuperVO;

@Table(name = "md_index", indexes = {@Index(name = "i_md_index_ddc_version", columnList = "ddc_version"),
    @Index(name = "i_md_index_class_id", columnList = "class_id")})
public class IndexVO extends SuperVO
{
    private Boolean blNonUnique;

    private Integer iOrdinalPosition;
    private Integer iType;

    private Long lCardinality;
    private Long lPages;

    private String strAscOrDesc;
    private String strClassId;
    private String strColumnName;
    private String strDdcVersion;
    private String strFilterCondition;
    private String strIndexName;
    private String strIndexQualifier;
    private String strIndexSql;
    private String strSchema;
    private String strTableName;

    public String getAscOrDesc()
    {
        return strAscOrDesc;
    }

    public Long getCardinality()
    {
        return lCardinality;
    }

    @Column(length = 50)
    public String getClassId()
    {
        return strClassId;
    }

    @Column(insertable = false, updatable = false)
    public String getColumnName()
    {
        return strColumnName;
    }

    @Id(order = 2)
    @Column(nullable = false, length = 4)
    public String getDdcVersion()
    {
        return strDdcVersion;
    }

    public String getFilterCondition()
    {
        return strFilterCondition;
    }

    @Column(length = 64)
    public String getIndexName()
    {
        return strIndexName;
    }

    public String getIndexQualifier()
    {
        return strIndexQualifier;
    }

    @Column(length = 512)
    public String getIndexSql()
    {
        return strIndexSql;
    }

    public Integer getOrdinalPosition()
    {
        return iOrdinalPosition;
    }

    public Long getPages()
    {
        return lPages;
    }

    public String getSchema()
    {
        return strSchema;
    }

    public String getTableName()
    {
        return strTableName;
    }

    public Integer getType()
    {
        return iType;
    }

    public Boolean isNonUnique()
    {
        return blNonUnique;
    }

    public void setAscOrDesc(String ascOrDesc)
    {
        strAscOrDesc = ascOrDesc;
    }

    public void setCardinality(Long lCardinality)
    {
        this.lCardinality = lCardinality;
    }

    public void setClassId(String classId)
    {
        strClassId = classId;
    }

    public void setColumnName(String columnName)
    {
        strColumnName = columnName;
    }

    public void setDdcVersion(String ddcVersion)
    {
        strDdcVersion = ddcVersion;
    }

    public void setFilterCondition(String filterCondition)
    {
        strFilterCondition = filterCondition;
    }

    public void setIndexName(String indexName)
    {
        strIndexName = indexName;
    }

    public void setIndexQualifier(String indexQualifier)
    {
        strIndexQualifier = indexQualifier;
    }

    public void setIndexSql(String indexSql)
    {
        strIndexSql = indexSql;
    }

    public void setNonUnique(Boolean blNonUnique)
    {
        this.blNonUnique = blNonUnique;
    }

    public void setOrdinalPosition(Integer iOrdinalPosition)
    {
        this.iOrdinalPosition = iOrdinalPosition;
    }

    public void setPages(Long lPages)
    {
        this.lPages = lPages;
    }

    public void setSchema(String schema)
    {
        strSchema = schema;
    }

    public void setTableName(String tableName)
    {
        strTableName = tableName;
    }

    public void setType(Integer iType)
    {
        this.iType = iType;
    }
}
