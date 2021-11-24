package org.rocex.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;

/***************************************************************************
 * jackson工具类<br>
 * @author Rocex Wang
 * @since 2021-10-19 02:03:45
 ***************************************************************************/
public class JacksonHelper
{
    private ObjectMapper mapper;

    private JacksonPropertyFilterProvider filterProvider;

    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-24 02:25:01
     ***************************************************************************/
    public JacksonHelper()
    {
        super();

        mapper = new ObjectMapper();
        filterProvider = new JacksonPropertyFilterProvider(mapper);
    }

    /***************************************************************************
     * @param clazz 要序列化的实体类
     * @param strFields 要排除序列化的属性名
     * @return JacksonHelper
     * @author Rocex Wang
     * @since 2021-11-24 14:25:04
     ***************************************************************************/
    public JacksonHelper exclude(Class<?> clazz, String... strFields)
    {
        filterProvider.exclude(clazz, strFields);

        return this;
    }

    /***************************************************************************
     * @param clazz 要序列化的实体类
     * @param strFields 要排除序列化的属性名
     * @return JacksonHelper
     * @author Rocex Wang
     * @since 2021-11-24 14:25:06
     ***************************************************************************/
    public JacksonHelper include(Class<?> clazz, String... strFields)
    {
        filterProvider.include(clazz, strFields);

        return this;
    }

    /***************************************************************************
     * 序列化
     * @param obj
     * @param pathFile
     * @author Rocex Wang
     * @since 2021-11-24 14:25:09
     ***************************************************************************/
    public void serialize(Object obj, Path pathFile)
    {
        mapper.setDateFormat(StdDateFormat.getDateTimeInstance());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try
        {
            if (Files.notExists(pathFile.getParent()))
            {
                Files.createDirectories(pathFile.getParent());
            }

            mapper.writeValue(pathFile.toFile(), obj);
            // mapper.writerWithDefaultPrettyPrinter().writeValue(pathFile.toFile(), obj);
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }

    /***************************************************************************
     * 在新线程下序列化
     * @param obj
     * @param pathFile
     * @author Rocex Wang
     * @since 2021-11-24 14:25:12
     ***************************************************************************/
    public void serializeThread(Object obj, Path pathFile)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                serialize(obj, pathFile);
            }
        }.start();
    }
}
