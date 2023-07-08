package org.rocex.db.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.rocex.db.param.SQLParameter;
import org.rocex.utils.Logger;
import org.rocex.utils.StringHelper;
import org.rocex.vo.SuperVO;

/***************************************************************************
 * <br>
 * Created on 2017-2-10 15:40:26<br>
 * @author Rocex Wang
 ***************************************************************************/
public class SQLExecutor
{
    public static final int iBatchSize = 1000;//
    
    protected Properties dbProp = null;
    
    private Connection connection = null;
    
    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-27 10:05:45
     ***************************************************************************/
    public SQLExecutor(Properties dbProp)
    {
        super();
        
        this.dbProp = dbProp;
    }
    
    /***************************************************************************
     * @param resultSet
     * @author Rocex Wang
     * @version 2019-8-15 15:19:57
     ***************************************************************************/
    public void close(ResultSet resultSet)
    {
        if (resultSet == null)
        {
            return;
        }
        
        try
        {
            resultSet.close();
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    /***************************************************************************
     * @param statement
     * @author Rocex Wang
     * @version 2019-8-15 15:26:42
     ***************************************************************************/
    public void close(Statement statement)
    {
        if (statement == null)
        {
            return;
        }
        
        try
        {
            statement.close();
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    /***************************************************************************
     * <br>
     * Created on 2017-2-10 16:15:04<br>
     * @param connection
     * @author Rocex Wang
     ***************************************************************************/
    public void closeConnection()
    {
        if (connection == null)
        {
            return;
        }
        
        try
        {
            connection.close();
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        Logger.getLogger().trace("connection closed.");
    }
    
    /***************************************************************************
     * @param strSQL
     * @return PreparedStatement
     * @throws Exception
     * @author Rocex Wang
     * @version 2019-8-8 10:32:00
     ***************************************************************************/
    PreparedStatement createPreparedStatement(String strSQL) throws SQLException
    {
        Connection connection = getConnection();
        
        PreparedStatement statement = null;
        
        try
        {
            // statement = connection.prepareStatement(strSQL, ResultSet.TYPE_SCROLL_INSENSITIVE,
            // ResultSet.CONCUR_READ_ONLY);
            statement = connection.prepareStatement(strSQL);
        }
        catch (Exception ex)
        {
            statement = connection.prepareStatement(strSQL);
            
            Logger.getLogger().error(ex.getMessage());
        }
        
        statement.closeOnCompletion();
        statement.setQueryTimeout(30);
        
        return statement;
    }
    
    /***************************************************************************
     * @return Statement
     * @throws Exception
     * @author Rocex Wang
     * @version 2019-8-8 10:29:44
     ***************************************************************************/
    Statement createStatement() throws SQLException
    {
        Connection connection = getConnection();
        
        Statement statement = null;
        
        try
        {
            // statement = connection.createStatement(getResultSetType(), ResultSet.CONCUR_READ_ONLY);
            statement = connection.createStatement();
        }
        catch (Exception ex)
        {
            statement = connection.createStatement();
            
            Logger.getLogger().error(ex.getMessage());
        }
        
        statement.closeOnCompletion();
        statement.setQueryTimeout(30);
        
        return statement;
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datahub.db.ISQLExecutor#executeQuery(java.lang.Class, java.lang.String,
     *      org.rocex.db.param.SQLParameter, org.rocex.vo.PageInfo)
     * @author Rocex Wang
     * @version 2019-8-14 10:24:49
     ****************************************************************************/
    public List<? extends SuperVO> executeQuery(Class<? extends SuperVO> clazz, String strSQL, SQLParameter param) throws SQLException
    {
        ResultSetProcessor processor = new BeanListProcessor<>(clazz);
        
        return (List<? extends SuperVO>) executeQuery(strSQL, param, processor);
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datahub.db.ISQLExecutor#executeQuery(java.lang.String, ResultSetProcessor)
     * @author Rocex Wang
     * @version 2019-8-1 16:34:30
     ****************************************************************************/
    public Object executeQuery(String strSQL, ResultSetProcessor processor) throws SQLException
    {
        if (strSQL == null || (strSQL = strSQL.trim()).length() == 0)
        {
            return null;
        }
        
        ResultSet resultSet = null;
        Statement statement = null;
        
        try
        {
            statement = createStatement();
            
            Logger.getLogger().trace(strSQL);
            
            resultSet = statement.executeQuery(strSQL);
            
            return processor.doAction(resultSet);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
            
            throw ex;
        }
        finally
        {
            close(resultSet);
            close(statement);
        }
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datahub.db.ISQLExecutor#executeQuery(java.lang.String,
     *      org.rocex.db.param.SQLParameter,
     *      org.rocex.datahub.db.processor.ResultSetProcessor)
     * @author Rocex Wang
     * @version 2019-8-13 10:54:33
     ****************************************************************************/
    public Object executeQuery(String strSQL, SQLParameter param, ResultSetProcessor processor) throws SQLException
    {
        if (strSQL == null || (strSQL = strSQL.trim()).length() == 0)
        {
            return null;
        }
        
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        
        try
        {
            statement = createPreparedStatement(strSQL);
            
            Logger.getLogger().trace(strSQL);
            
            if (param != null)
            {
                Logger.getLogger().trace(param.toString());
                
                SQLParameter.setStatementParameter(statement, param);
            }
            
            resultSet = statement.executeQuery();
            
            return processor.doAction(resultSet);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
            
            throw ex;
        }
        finally
        {
            close(resultSet);
            close(statement);
        }
    }
    
    /***************************************************************************
     * @param strSQL
     * @return int
     * @throws Exception
     * @author Rocex Wang
     * @version 2020-5-14 11:38:12
     ***************************************************************************/
    public int executeUpdate(String strSQL) throws SQLException
    {
        Statement statement = null;
        
        try
        {
            statement = createStatement();
            
            Logger.getLogger().trace(strSQL);
            
            int iSuccessCount = statement.executeUpdate(strSQL);
            
            return iSuccessCount;
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        finally
        {
            close(statement);
        }
        
        return -1;
    }
    
    /***************************************************************************
     * @param strSQLs
     * @return int
     * @throws Exception
     * @author Rocex Wang
     * @version 2020-5-14 11:38:12
     ***************************************************************************/
    public int[] executeUpdate(String... strSQLs) throws SQLException
    {
        Statement statement = null;
        
        boolean blAutoCommit = getConnection().getAutoCommit();
        
        try
        {
            getConnection().setAutoCommit(false);
            
            statement = createStatement();
            
            for (String strSQL : strSQLs)
            {
                statement.addBatch(strSQL);
                
                Logger.getLogger().trace(strSQL);
            }
            
            int[] iSuccessCount = statement.executeBatch();
            
            getConnection().commit();
            
            statement.clearBatch();
            
            return iSuccessCount;
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        finally
        {
            close(statement);
            
            getConnection().setAutoCommit(blAutoCommit);
        }
        
        return new int[] {};
    }
    
    /***************************************************************************
     * @param strSQL
     * @param params
     * @return int[]
     * @throws SQLException
     * @author Rocex Wang
     * @since 2021-10-28 20:04:49
     ***************************************************************************/
    public int[] executeUpdate(String strSQL, SQLParameter... params) throws SQLException
    {
        int[] iResult = new int[params.length];
        
        PreparedStatement statement = null;
        
        boolean blAutoCommit = getConnection().getAutoCommit();
        
        try
        {
            getConnection().setAutoCommit(false);
            
            statement = createPreparedStatement(strSQL);
            
            Logger.getLogger().trace(strSQL);
            
            for (int i = 0, j = 1, k = 0; i < params.length; i++, j++)
            {
                SQLParameter.setStatementParameter(statement, params[i]);
                
                statement.addBatch();
                
                if (j % iBatchSize == 0 || j == params.length)
                {
                    int[] iBatchResult = statement.executeBatch();
                    
                    getConnection().commit();
                    
                    statement.clearBatch();
                    
                    System.arraycopy(iBatchResult, 0, iResult, k, iBatchResult.length);
                    
                    k += iBatchResult.length;
                }
            }
            
            return iResult;
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        finally
        {
            close(statement);
            
            getConnection().setAutoCommit(blAutoCommit);
        }
        
        return new int[] {};
    }
    
    /***************************************************************************
     * <br>
     * Created on 2017-2-10 16:10:05<br>
     * @return Connection
     * @throws Exception
     * @author Rocex Wang
     ***************************************************************************/
    public Connection getConnection() throws SQLException
    {
        if (connection == null || connection.isClosed())
        {
            Logger.getLogger().trace("create connection...");
            
            String strDriver = dbProp.getProperty("jdbc.driver");
            String strConnUrl = dbProp.getProperty("jdbc.url");
            String strUserName = dbProp.getProperty("jdbc.user");
            String strPassword = dbProp.getProperty("jdbc.password");
            
            try
            {
                Class.forName(strDriver);
            }
            catch (ClassNotFoundException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
            
            connection = DriverManager.getConnection(strConnUrl, strUserName, strPassword);
        }
        
        return connection;
    }
    
    /***************************************************************************
     * @param method
     * @return field name
     * @author Rocex Wang
     * @since 2021-10-28 10:14:41
     ***************************************************************************/
    public String getFieldName(Method method)
    {
        Column annoColumn = method.getAnnotation(Column.class);
        
        if (annoColumn != null && StringHelper.isNotEmpty(annoColumn.name()))
        {
            return annoColumn.name();
        }
        
        String strField = method.getName();
        if (strField.startsWith("get") || strField.startsWith("set"))
        {
            strField = strField.substring(3);
        }
        else if (strField.startsWith("is"))
        {
            strField = strField.substring(2);
        }
        
        return StringHelper.camelToUnderline(strField);
    }
    
    /***************************************************************************
     * @param clazz
     * @return id field name
     * @author Rocex Wang
     * @since 2021-10-28 10:20:47
     ***************************************************************************/
    public String getIdFieldName(Class<?> clazz)
    {
        Method[] methods = clazz.getMethods();
        
        for (Method method : methods)
        {
            Id annoId = method.getAnnotation(Id.class);
            
            if (annoId != null)
            {
                String strField = getFieldName(method);
                
                return strField;
            }
        }
        
        throw new RuntimeException("实体类[" + clazz.getName() + "]必须有一个包含@Id的方法");
    }
    
    /***************************************************************************
     * @param <T>
     * @param strFields
     * @param superVOs
     * @return SQLParameter[]
     * @author Rocex Wang
     * @since 2021-10-28 20:04:35
     ***************************************************************************/
    public <T extends SuperVO> SQLParameter[] getParamInsert(String[] strFields, T... superVOs)
    {
        Method[] methods = SuperVO.getGetter(superVOs[0].getClass());
        
        SQLParameter[] params = new SQLParameter[superVOs.length];
        
        for (int i = 0; i < superVOs.length; i++)
        {
            params[i] = new SQLParameter();
            
            for (Method method : methods)
            {
                Column annoColumn = method.getAnnotation(Column.class);
                
                if (annoColumn != null && !annoColumn.insertable())
                {
                    continue;
                }
                
                try
                {
                    params[i].addParam(method.invoke(superVOs[i], (Object[]) null));
                }
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        }
        
        return params;
    }
    
    /***************************************************************************
     * @return resultSetType
     * @author Rocex Wang
     * @version 2020-4-17 17:57:01
     ***************************************************************************/
    int getResultSetType()
    {
        return ResultSet.TYPE_SCROLL_SENSITIVE;
    }
    
    /***************************************************************************
     * @param clazz
     * @param strFields
     * @return create table sql
     * @author Rocex Wang
     * @since 2021-10-28 20:04:21
     ***************************************************************************/
    public String getSQLCreate(Class<? extends SuperVO> clazz, String... strFields)
    {
        String strTableName = getTableNameFromClass(clazz);
        
        StringBuilder strSQL = new StringBuilder("create table ").append(strTableName).append("(");
        
        Method[] methods = SuperVO.getGetter(clazz);
        
        for (Method method : methods)
        {
            String strFieldName = getFieldName(method);
            
            if (strFields != null && strFields.length > 0 && Arrays.binarySearch(strFields, strFieldName) < 0)
            {
                continue;
            }
            
            Column annoColumn = method.getAnnotation(Column.class);
            
            int iLength = 255;
            int iPrecision = 0;
            boolean blNullable = true;
            
            if (annoColumn != null)
            {
                if (!annoColumn.insertable() && !annoColumn.updatable())
                {
                    continue;
                }
                
                iLength = annoColumn.length();
                iPrecision = annoColumn.precision();
                blNullable = annoColumn.nullable();
                
                if (StringHelper.isNotEmpty(annoColumn.columnDefinition()))
                {
                    strSQL.append(strFieldName).append(" ").append(annoColumn.columnDefinition()).append(blNullable ? " null" : " not null").append(",");
                    
                    continue;
                }
            }
            
            // formula varchar2(400) default '~' null
            StringBuilder strField = new StringBuilder(strFieldName);
            
            Class<?> returnType = method.getReturnType();
            
            if (returnType == Boolean.class)
            {
                strField.append(" char(1)");
            }
            else if (returnType == Integer.class)
            {
                strField.append(" int");
            }
            else if (returnType == BigDecimal.class || returnType == double.class || returnType == Double.class)
            {
                strField.append(" decimal(").append(iLength).append(",").append(iPrecision).append(")");
            }
            else
            {
                strField.append(" varchar(").append(iLength).append(")");
            }
            
            strSQL.append(strField).append(blNullable ? " null" : " not null").append(",");
        }
        
        String strIdFieldName = getIdFieldName(clazz);
        
        strSQL.append(" constraint ").append(strTableName).append("_").append(strIdFieldName).append(" primary key (").append(strIdFieldName).append("))");
        
        return strSQL.toString();
    }
    
    /***************************************************************************
     * @param clazz
     * @return String
     * @author Rocex Wang
     * @since 2021-11-12 14:00:56
     ***************************************************************************/
    public String getSQLDelete(Class<? extends SuperVO> clazz)
    {
        String strSQL = "delete from " + getTableNameFromClass(clazz);
        
        return strSQL;
    }
    
    /***************************************************************************
     * @param clazz
     * @param strFields
     * @return insert sql
     * @author Rocex Wang
     * @since 2021-10-28 20:04:11
     ***************************************************************************/
    public String getSQLInsert(Class<? extends SuperVO> clazz, String... strFields)
    {
        String strTableName = getTableNameFromClass(clazz);
        
        StringBuilder strSQL = new StringBuilder("insert into ").append(strTableName).append("(");
        
        Method[] methods = SuperVO.getGetter(clazz);
        
        StringBuilder strFieldSQL = new StringBuilder();
        StringBuilder strMarkSQL = new StringBuilder();
        
        for (Method method : methods)
        {
            String strFieldName = getFieldName(method);
            
            if (strFields != null && strFields.length > 0 && Arrays.binarySearch(strFields, strFieldName) < 0)
            {
                continue;
            }
            
            Column annoColumn = method.getAnnotation(Column.class);
            
            if (annoColumn != null && !annoColumn.insertable())
            {
                continue;
            }
            
            strFieldSQL.append(",").append(strFieldName);
            strMarkSQL.append(",?");
        }
        
        strSQL.append(strFieldSQL.substring(1)).append(") values (").append(strMarkSQL.substring(1)).append(")");
        
        return strSQL.toString();
    }
    
    /***************************************************************************
     * @param clazz
     * @param strFields
     * @return insert sql
     * @author Rocex Wang
     * @since 2021-10-28 20:04:11
     ***************************************************************************/
    public String getSQLSelect(Class<? extends SuperVO> clazz, String... strFields)
    {
        String strTableName = getTableNameFromClass(clazz);
        
        Method[] methods = SuperVO.getGetter(clazz);
        
        StringBuilder strFieldSQL = new StringBuilder();
        
        for (Method method : methods)
        {
            String strFieldName = getFieldName(method);
            
            if (strFields != null && strFields.length > 0 && Arrays.binarySearch(strFields, strFieldName) < 0)
            {
                continue;
            }
            
            Column annoColumn = method.getAnnotation(Column.class);
            
            if (annoColumn != null && !annoColumn.insertable() && !annoColumn.updatable())
            {
                continue;
            }
            
            strFieldSQL.append(",").append(strFieldName);
        }
        
        StringBuilder strSQL = new StringBuilder("select ").append(strFieldSQL.substring(1)).append(" from ").append(strTableName);
        
        return strSQL.toString();
    }
    
    /***************************************************************************
     * @param clazz
     * @param strFields
     * @return update sql
     * @author Rocex Wang
     * @since 2021-10-28 20:03:58
     ***************************************************************************/
    public String getSQLUpdate(Class<? extends SuperVO> clazz, String... strFields)
    {
        String strTableName = getTableNameFromClass(clazz);
        
        StringBuilder strSQL = new StringBuilder("update ").append(strTableName).append(" set ");
        
        Method[] methods = SuperVO.getGetter(clazz);
        
        StringBuilder strFieldSQL = new StringBuilder();
        
        for (Method method : methods)
        {
            String strFieldName = getFieldName(method);
            
            if (strFields != null && strFields.length > 0 && Arrays.binarySearch(strFields, strFieldName) < 0)
            {
                continue;
            }
            
            Column annoColumn = method.getAnnotation(Column.class);
            
            if (annoColumn != null && !annoColumn.updatable())
            {
                continue;
            }
            
            strFieldSQL.append(",").append(strFieldName).append("=?");
        }
        
        strSQL.append(strFieldSQL.substring(1));
        
        return strSQL.toString();
    }
    
    /***************************************************************************
     * 获取指定实体类对应的表名
     * @param clazz
     * @return 若指定的类中含有{@code javax.persistence.Table}注解，则返回注解的name字段的值，否则返回由类名驼峰转下划线的表名
     * @author Rocex Wang
     * @since 2021-10-28 09:29:30
     ***************************************************************************/
    public String getTableNameFromClass(Class<? extends SuperVO> clazz)
    {
        final String strClassName = clazz.getSimpleName();
        final Table table = clazz.getAnnotation(Table.class);
        
        String strTableName = table != null && StringHelper.isNotEmpty(table.name()) ? table.name() : StringHelper.camelToUnderline(strClassName);
        
        return strTableName;
    }
    
    /***************************************************************************
     * @param classes
     * @author Rocex Wang
     * @since 2021-10-28 13:33:10
     ***************************************************************************/
    public void initDBSchema(Class<? extends SuperVO>... classes)
    {
        if (classes == null || classes.length == 0)
        {
            return;
        }
        
        for (Class<? extends SuperVO> clazz : classes)
        {
            if (isTableExist(getTableNameFromClass(clazz)))
            {
                continue;
            }
            
            String strSQL = getSQLCreate(clazz);
            
            try
            {
                executeUpdate(strSQL);
                
                initTableIndexes(clazz);
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        }
    }
    
    /***************************************************************************
     * @param clazz
     * @author Rocex Wang
     * @since 2021-10-28 20:03:10
     ***************************************************************************/
    public void initTableIndexes(Class<? extends SuperVO> clazz)
    {
        final Table table = clazz.getAnnotation(Table.class);
        
        Index[] indexes = null;
        
        if (table == null || (indexes = table.indexes()) == null || indexes.length == 0)
        {
            return;
        }
        
        String strTableName = getTableNameFromClass(clazz);
        
        for (Index index : indexes)
        {
            index.columnList();
            
            StringBuilder strSQL = new StringBuilder("create index ").append(index.name()).append(" on ").append(strTableName).append(" (")
                    .append(index.columnList()).append(")");
            
            try
            {
                executeUpdate(strSQL.toString());
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        }
    }
    
    /***************************************************************************
     * @param <T>
     * @param superVOs
     * @return
     * @throws SQLException
     * @author Rocex Wang
     * @since 2021-10-28 20:03:16
     ***************************************************************************/
    public <T extends SuperVO> String[] insertVO(T... superVOs) throws SQLException
    {
        String strSQL = getSQLInsert(superVOs[0].getClass());
        
        SQLParameter[] params = getParamInsert(null, superVOs);
        
        executeUpdate(strSQL, params);
        
        return null;
    }
    
    /***************************************************************************
     * @param strTableName
     * @return boolean
     * @author Rocex Wang
     * @since 2021-10-28 20:03:21
     ***************************************************************************/
    public boolean isTableExist(String strTableName)
    {
        ResultSet resultSet = null;
        
        try
        {
            return (boolean) executeQuery("select 1 from " + strTableName, new ResultSetProcessor()
            {
                @Override
                protected Object processResultSet(ResultSet resultSet) throws SQLException
                {
                    return resultSet.next();
                }
            });
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
            
            return false;
        }
        finally
        {
            close(resultSet);
        }
    }
}
