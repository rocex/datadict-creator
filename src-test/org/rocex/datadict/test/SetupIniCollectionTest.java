package org.rocex.datadict.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

/***************************************************************************
 * 根据安装盘的目录结构，从安装盘的 setup.ini 文件中读取模块信息 <br>
 * @author Rocex Wang
 * @since 2025-05-22 04:32:00
 ***************************************************************************/
public class SetupIniCollectionTest
{
    Path pathFrom = Paths.get("C:/dist/");
    
    public void testCollectSetupIni() throws IOException
    {
        if (!pathFrom.toFile().exists())
        {
            return;
        }

        Files.walkFileTree(pathFrom, new SimpleFileVisitor<>()
        {
            Path pathParent = pathFrom;
            
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException
            {
                if (pathParent.compareTo(path.getParent()) != 0)
                {
                    pathParent = path.getParent();
                }
                
                Path pathFile = Paths.get(path.toString(), "setup.ini");
                
                if (pathFile.toFile().exists())
                {
                    Properties prop = new Properties();

                    prop.load(Files.newBufferedReader(pathFile, Charset.forName("CP936")));//
                    
                    String strDir = pathFile.getParent().getFileName().toString();
                    String strCode = prop.getProperty("code");
                    String strName = prop.getProperty("name");
                    
                    System.out.printf("%-8s %-20s %-20s %s \n", strCode, strDir, pathFrom.compareTo(pathParent) == 0 ? "root" : pathParent.toFile().getName(),
                            strName);
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
