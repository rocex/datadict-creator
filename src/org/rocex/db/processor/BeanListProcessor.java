package org.rocex.db.processor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.rocex.utils.Logger;
import org.rocex.vo.SuperVO;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2019-8-7 10:10:37
 ***************************************************************************/
public class BeanListProcessor<T extends SuperVO> extends ResultSetProcessor
{
    protected boolean blSupportPagenation = false;// 是否支持分页

    protected Class<T> clazz = null;

    private Predicate<? super T> filter;

    private Map<String, String> mapFieldRelationship;// 查询的数据库字段和VO中字段在不相同的情况下的对照关系，相同的可忽略

    private String strFields[]; // 只处理指定的字段，每个值对应到VO中的属性(不区分大小写)，null为都处理

    /***************************************************************************
     * @param clazz
     * @author Rocex Wang
     * @version 2019-8-7 10:30:35
     ***************************************************************************/
    public BeanListProcessor(Class<T> clazz)
    {
        this(clazz, null);
    }

    /***************************************************************************
     * @param clazz
     * @param blCloseResultSet
     * @author Rocex Wang
     * @version 2020-5-15 9:45:45
     ***************************************************************************/
    public BeanListProcessor(Class<T> clazz, boolean blCloseResultSet)
    {
        this(clazz, null);

        setAutoCloseResultSet(blCloseResultSet);
    }

    /***************************************************************************
     * @param clazz
     * @param mapFieldRelationship 查询的数据库字段和VO中字段在不相同的情况下的对照关系，相同的可忽略
     * @param strFields 只处理指定的字段，每个值对应到VO中的属性(不区分大小写)，null为都处理
     * @author Rocex Wang
     * @version 2020-1-17 10:25:29
     ***************************************************************************/
    public BeanListProcessor(Class<T> clazz, Map<String, String> mapFieldRelationship, Predicate<? super T> filter, String... strFields)
    {
        super();

        this.clazz = clazz;
        this.filter = filter;
        this.strFields = strFields;
        this.mapFieldRelationship = mapFieldRelationship;
    }

    /***************************************************************************
     * @param clazz
     * @param mapRelationship
     * @param blCloseResultSet
     * @param strFields
     * @author Rocex Wang
     * @version 2020-5-14 16:54:58
     ***************************************************************************/
    public BeanListProcessor(Class<T> clazz, Map<String, String> mapRelationship, String... strFields)
    {
        this(clazz, mapRelationship, null, strFields);
    }

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datahub.db.processor.BeanProcessor#processResultSet(java.sql.ResultSet)
     * @author Rocex Wang
     * @version 2019-8-7 10:16:06
     ****************************************************************************/
    @Override
    protected List<T> processResultSet(ResultSet resultSet) throws SQLException
    {
        List<T> listVO = new ArrayList<>();

        try
        {
            BeanProcessor<T> beanProcessor = new BeanProcessor<>(clazz, mapFieldRelationship, strFields);

            if (filter != null)
            {
                while (resultSet.next())
                {
                    T processRow = beanProcessor.processRow(resultSet);

                    if (filter.test(processRow))
                    {
                        listVO.add(processRow);
                    }
                }
            }
            else
            {
                while (resultSet.next())
                {
                    T processRow = beanProcessor.processRow(resultSet);

                    listVO.add(processRow);
                }
            }
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);

            throw new SQLException(ex.getMessage(), ex);
        }

        return listVO;
    }

    /***************************************************************************
     * @param filter the filter to set
     * @author Rocex Wang
     * @version 2020-5-15 10:15:36
     ***************************************************************************/
    public void setFilter(Predicate<? super T> filter)
    {
        this.filter = filter;
    }
}
