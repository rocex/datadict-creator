package org.rocex.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * 拷贝文件夹下所有的文件夹和文件
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
                
                if (Files.notExists(pathCurrentTo.getParent()))  // 如果父路径不存在，则创建
                {
                    Files.createDirectories(pathCurrentTo.getParent());
                }
                
                Files.copy(file, pathCurrentTo, options);
                
                return FileVisitResult.CONTINUE;  // 递归遍历文件，空文件无法复制
            }
        });
    }
    
    /***************************************************************************
     * 在新线程下拷贝文件夹下所有的文件夹和文件
     * @param pathFrom
     * @param pathTo
     * @param options
     * @throws IOException
     * @author Rocex Wang
     * @version 2020-5-11 14:36:23
     ***************************************************************************/
    public static void copyFolderThread(Path pathFrom, Path pathTo, CopyOption... options) throws IOException
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    copyFolder(pathFrom, pathTo, options);
                }
                catch (IOException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        }.start();
    }
    
    /***************************************************************************
     * 删除文件夹下所有的文件夹和文件
     * @param path
     * @param pathTo
     * @param options
     * @throws IOException
     * @author Rocex Wang
     * @version 2020-4-28 20:36:51
     ***************************************************************************/
    public static void deleteFolder(Path path) throws IOException
    {
        if (Files.notExists(path))
        {
            return;
        }
        
        Files.walkFileTree(path, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException
            {
                if (ex != null)
                {
                    throw ex;
                }
                
                Files.delete(dir);
                
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Files.deleteIfExists(file);
                
                return FileVisitResult.CONTINUE;  // 递归遍历文件，空文件无法复制
            }
        });
    }

    /***************************************************************************
     * 在新线程下删除文件夹下所有的文件夹和文件
     * @param path
     * @throws IOException
     * @author Rocex Wang
     * @since 2021-11-24 14:14:10
     ***************************************************************************/
    public static void deleteFolderThread(Path path) throws IOException
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    deleteFolderThread(path);
                }
                catch (IOException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        }.start();
    }
    
    /***************************************************************************
     * 加载 properties 文件
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
    
    /***************************************************************************
     * 写文件
     * @param pathFile
     * @param strContent
     * @author Rocex Wang
     * @version 2020-4-26 10:24:35
     ***************************************************************************/
    public static void writeFile(Path pathFile, String strContent)
    {
        try
        {
            if (!pathFile.getParent().toFile().exists())
            {
                Files.createDirectories(pathFile.getParent());
            }
            
            Files.write(pathFile, strContent.getBytes());
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    /***************************************************************************
     * 在新线程下写文件
     * @param pathFile
     * @param strContent
     * @author Rocex Wang
     * @version 2020-5-11 14:36:58
     ***************************************************************************/
    public static void writeFileThread(Path pathFile, String strContent)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                writeFile(pathFile, strContent);
            }
        }.start();
    }
}
