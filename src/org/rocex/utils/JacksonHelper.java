package org.rocex.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @since 2021-10-19 02:03:45
 ***************************************************************************/
public class JacksonHelper
{
    private ObjectMapper mapper;
    
    private JacksonPropertyFilterProvider filterProvider;

    public JacksonHelper()
    {
        super();
        
        mapper = new ObjectMapper();
        filterProvider = new JacksonPropertyFilterProvider(mapper);
    }

    public JacksonHelper exclude(Class<?> clazz, String... strFields)
    {
        filterProvider.exclude(clazz, strFields);
        
        return this;
    }
    
    public JacksonHelper include(Class<?> clazz, String... strFields)
    {
        filterProvider.include(clazz, strFields);

        return this;
    }
    
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

            mapper.writerWithDefaultPrettyPrinter().writeValue(pathFile.toFile(), obj);
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
}
