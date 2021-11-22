package org.rocex.datadict;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.utils.JacksonHelper;
import org.rocex.utils.Logger;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-22 14:10:00
 ***************************************************************************/
public class CreateJsonDataDictAction extends CreateDataDictAction
{
    static
    {
        try
        {
            DataDictCreator.settings.setProperty("HtmlIndexFile", new String(Files.readAllBytes(Paths.get("settings", "json", "template", "index.html"))));
        }
        catch (IOException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }

    /***************************************************************************
     * @author Rocex Wang
     * @version 2020-4-26 14:52:18
     ***************************************************************************/
    public CreateJsonDataDictAction(String strVersion)
    {
        super(strVersion);

        strTreeDataClassTemplate = "'{'id:\"{0}\",pId:\"{1}\",name:\"{2} {3}\"'}',";

        strRefClassPathHrefTemplate = "<a href=\"javascript:void(0);\" onClick=loadDataDict(\"{0}\");>{1}</a>";
        strClassListHrefTemplate = "<a href=\"javascript:void(0);\" onClick=loadDataDict(\"{0}\"); class=\"{1}\">{2}</a>";
    }

    /***************************************************************************
     * @param classVO
     * @param listPropertyVO
     * @author Rocex Wang
     * @version 2020-5-13 15:27:35
     ***************************************************************************/
    @Override
    protected void createDataDictFile(ClassVO classVO, List<PropertyVO> listPropertyVO)
    {
        if (listPropertyVO == null || listPropertyVO.isEmpty())
        {
            return;
        }

        super.createDataDictFile(classVO, listPropertyVO);

        new JacksonHelper()
                .exclude(ClassVO.class, "accessorClassname", "bizItfImpClassname", "classType", "componentId", "help", "id", "keyAttribute", "name",
                        "refModelName", "returnType")
                .exclude(PropertyVO.class, "accessorClassname", "accessorClassname", "accessPower", "accessPowerGroup", "attrLength", "attrSequence",
                        "calculation", "classId", "customAttr", "dataType", "dataTypeStyle", "dynamicAttr", "dynamicTable", "id", "originalId", "precise",
                        "refModelName")
                .serialize(classVO, getClassFilePath(classVO));
    }

    /***************************************************************************
     * 实体文件全路径的绝对路径
     * @param classVO
     * @return Path
     * @author Rocex Wang
     * @version 2020-4-26 10:21:59
     ***************************************************************************/
    @Override
    protected Path getClassFilePath(ClassVO classVO)
    {
        return Paths.get(strOutputDictDir, getMappedClassId(classVO) + ".json");
    }

    /***************************************************************************
     * 实体的访问url，相对于根目录
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-5-2 14:26:12
     ***************************************************************************/
    @Override
    protected String getClassUrl(ClassVO classVO)
    {
        return getMappedClassId(classVO);
    }

    /***************************************************************************
     * 实体的访问url，相对于当前目录
     * @param classVO
     * @return String
     * @author Rocex Wang
     * @version 2020-5-2 14:30:53
     ***************************************************************************/
    @Override
    protected String getClassUrl2(ClassVO classVO)
    {
        return getMappedClassId(classVO);
    }
}
