package org.rocex.db.processor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.rocex.db.param.SQLParameter;
import org.rocex.utils.Logger;
import org.rocex.vo.SuperVO;

/***************************************************************************
 * <br>
 * Created on 2017-2-10 15:40:26<br>
 * @author Rocex Wang
 ***************************************************************************/
public class SQLExecutor
{
    private Connection connection = null;
    protected Properties dbProp = null;
    
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
    PreparedStatement createPreparedStatement(String strSQL) throws Exception
    {
        Connection connection = getConnection();
        
        PreparedStatement statement = null;
        
        try
        {
            statement = connection.prepareStatement(strSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        }
        catch (Exception ex)
        {
            statement = connection.prepareStatement(strSQL);
            
            Logger.getLogger().error(ex.getMessage(), ex);
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
    Statement createStatement() throws Exception
    {
        Connection connection = getConnection();
        
        Statement statement = null;
        
        try
        {
            statement = connection.createStatement(getResultSetType(), ResultSet.CONCUR_READ_ONLY);
        }
        catch (Exception ex)
        {
            statement = connection.createStatement();
            
            Logger.getLogger().error(ex.getMessage(), ex);
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
    public List<? extends SuperVO> executeQuery(Class<? extends SuperVO> clazz, String strSQL, SQLParameter param) throws Exception
    {
        ResultSetProcessor processor = new BeanListProcessor<>(clazz);
        
        return (List<? extends SuperVO>) executeQuery(processor, strSQL, param);
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see org.rocex.datahub.db.ISQLExecutor#executeQuery(ResultSetProcessor, java.lang.String)
     * @author Rocex Wang
     * @version 2019-8-1 16:34:30
     ****************************************************************************/
    public Object executeQuery(ResultSetProcessor processor, String strSQL) throws Exception
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
     * @see org.rocex.datahub.db.ISQLExecutor#executeQuery(org.rocex.datahub.db.processor.ResultSetProcessor,
     *      java.lang.String,
     *      org.rocex.db.param.SQLParameter)
     * @author Rocex Wang
     * @version 2019-8-13 10:54:33
     ****************************************************************************/
    public Object executeQuery(ResultSetProcessor processor, String strSQL, SQLParameter param) throws Exception
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
    public int executeUpdate(String strSQL) throws Exception
    {
        Statement statement = null;
        
        try
        {
            statement = createStatement();
            
            Logger.getLogger().trace(strSQL);
            
            int iSuccessCount = statement.executeUpdate(strSQL);
            
            return iSuccessCount;
        }
        catch (Exception ex)
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
    public int[] executeUpdate(String... strSQLs) throws Exception
    {
        Statement statement = null;
        
        try
        {
            statement = createStatement();
            
            for (String strSQL : strSQLs)
            {
                statement.addBatch(strSQL);
                Logger.getLogger().trace(strSQL);
            }
            
            int[] iSuccessCount = statement.executeBatch();
            
            return iSuccessCount;
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        finally
        {
            close(statement);
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
    public Connection getConnection() throws Exception
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
     * @return resultSetType
     * @author Rocex Wang
     * @version 2020-4-17 17:57:01
     ***************************************************************************/
    int getResultSetType()
    {
        return ResultSet.TYPE_SCROLL_SENSITIVE;
    }
}
