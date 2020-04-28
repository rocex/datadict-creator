package org.rocex.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-28 20:19:15
 ***************************************************************************/
public class FileHelper
{
    /***************************************************************************
     * @param pathFrom
     * @param pathTo
     * @param options
     * @throws IOException
     * @author Rocex Wang
     * @version 2020-4-28 20:36:51
     ***************************************************************************/
    public static void copyFolder(Path pathFrom, Path pathTo, CopyOption... options) throws IOException
    {
        if (Files.notExists(pathFrom))
        {
            throw new IOException("源文件夹不存在");
        }
        
        if (Files.notExists(pathTo))
        {
            Files.createDirectories(pathTo);
        }
        
        Files.walkFileTree(pathFrom, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Path pathCurrentTo = pathTo.resolve(pathFrom.relativize(file));
                
                if (Files.notExists(pathCurrentTo.getParent()))  // 如果说父路径不存在，则创建
                {
                    Files.createDirectories(pathCurrentTo.getParent());
                }
                
                Files.copy(file, pathCurrentTo, options);
                
                return FileVisitResult.CONTINUE;  // 递归遍历文件，空文件无法复制
            }
        });
    }
    
    /***************************************************************************
     * @param strFilePath
     * @author Rocex Wang
     * @version 2019-5-21 11:09:32
     ***************************************************************************/
    public static Properties load(String strFilePath)
    {
        Reader reader = null;
        Properties properties = new Properties();
        
        try
        {
            reader = new BufferedReader(new FileReader(strFilePath));
            
            properties.load(reader);
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        }
        
        return properties;
    }
    
    public static void main(String[] args) throws IOException
    {
        
        Path FromPath = Paths.get("C:\\Users\\10505\\Desktop" + File.separator + "bbq");
        
        Path toPath = Paths.get("C:\\Users\\10505\\Desktop" + File.separator + "bbq1");
        
        copyFolder(FromPath, toPath);
    }
}
