package org.rocex.db.processor;

import java.sql.ResultSet;
import java.sql.SQLException;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2019-8-7 10:11:13
 ***************************************************************************/
public abstract class ResultSetProcessor
{
    private boolean blAutoCloseResultSet = true;
    
    /***************************************************************************
     * @param resultSet
     * @return Object
     * @throws Exception
     * @author Rocex Wang
     * @version 2019-8-13 14:42:50
     ***************************************************************************/
    public Object doAction(ResultSet resultSet) throws Exception
    {
        if (resultSet == null)
        {
            throw new IllegalArgumentException("resultset parameter can't be null");
        }
        
        try
        {
            return processResultSet(resultSet);
        }
        catch (SQLException ex)
        {
            throw new SQLException("the resultsetProcessor error!" + ex.getMessage(), ex.getSQLState());
        }
        finally
        {
            if (blAutoCloseResultSet)
            {
                resultSet.close();
            }
        }
    }
    
    /***************************************************************************
     * @param resultSet
     * @return Object
     * @author Rocex Wang
     * @version 2019-8-7 10:14:42
     * @throws Exception
     ***************************************************************************/
    protected abstract Object processResultSet(ResultSet resultSet) throws Exception;
    
    /***************************************************************************
     * @param closeResultSet the closeResultSet to set
     * @author Rocex Wang
     * @version 2020-5-14 16:48:22
     ***************************************************************************/
    public void setAutoCloseResultSet(boolean closeResultSet)
    {
        blAutoCloseResultSet = closeResultSet;
    }
}
