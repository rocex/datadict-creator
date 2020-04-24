package org.rocex.datadict;

/***************************************************************************
 * <br>
 * @author Rocex Wang
 * @version 2020-4-22 14:10:00
 ***************************************************************************/
public class DataDictHtml
{
    public static String strHtml = "<html>\n" + 
            "<head>\n" + 
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" + 
            "    <title>{0} ({1})</title>\n" + 
            "    <link rel=\"stylesheet\" type=\"text/css\" href=\"../style.css\" />\n" + 
            "</head>\n" + 
            "<body>\n" + 
            "    <div class=\"title\">\n" + 
            "        <h3><a href=\"../index.html\" style=\"float: left; margin-left: 20px;\">首页</a>{0} ({1})</h3>\n" + 
            "    </div>\n" + 
            "    <div class=\"footer\">\n" + 
            "        <span>\n" + 
            "            {2}\n" +
            "        </span>\n" + 
            "    </div>\n" + 
            "    <table id=\"propTable\">\n" + 
            "        <tr>\n" + 
            "            <th width=\"40\">序号</th>\n" + 
            "            <th>属性编码</th>\n" + 
            "            <th>属性名称</th>\n" + 
            "            <th>字段编码</th>\n" + 
            "            <th>字段类型</th>\n" + 
            "            <th>是否必输</th>\n" +
            "            <th>引用模型</th>\n" + 
            "            <th>默认值</th>\n" + 
            "            <th>取值范围/枚举</th>\n" +
            "        </tr>\n" + 
            "{3}" + 
            "    </table>\n" + 
            "    <br>\n" +
            "    <div class=\"footer\">\n" + 
            "        <a href=\"http://www.yonyou.com\">&copy;用友网络科技股份有限公司</a>  " +
            "        <span>NC Cloud 产品本部 ({1})  <a href='mailto:wpz@yonyou.com'>@wpz</a>  {4}</span>\n" +
            "    </div>\n" + 
            "</body>\n" + 
            "</html>";
    
    public static String strHtmlIndex = "<html>\n" + 
            "<head>\n" + 
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" + 
            "    <title>NC Cloud 数据字典 ({0})</title>\n" + 
            "    <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />\n" + 
            "</head>\n" + 
            "<body>\n" + 
            "    <div class=\"title\">\n" + 
            "        <h3>NC Cloud 数据字典 ({0})</h3>\n" + 
            "    </div>\n<br>\n" + 
            "    <table id=\"propTable\">\n" + 
            "        <tr>\n" + 
            "            <th>实体</th>\n" + 
            "            <th>实体</th>\n" + 
            "            <th>实体</th>\n" + 
            "            <th>实体</th>\n" + 
            "            <th>实体</th>\n" + 
            "        </tr>\n" + 
            "{1}" + 
            "    </table>\n<br>\n" +
            "    <div class=\"footer\">\n" + 
            "        <a href=\"http://www.yonyou.com\">&copy;用友网络科技股份有限公司</a>  " + 
            "        <span>NC Cloud 产品本部 ({0})  <a href=mailto:wpz@yonyou.com>@wpz</a>  {2}</span>\n" + 
            "    </div>\n" + 
            "</body>\n" + 
            "</html>";
    
    public static String strPkRow = 
            "        <tr style=\"color: red;\">\n" + 
            "            <td>{0}</td>\n" + 
            "            <td>{1}</td>\n" + 
            "            <td>{2}</td>\n" + 
            "            <td>{3}</td>\n" + 
            "            <td>{4}</td>\n" + 
            "            <td>{5}</td>\n" + 
            "            <td>{6}</td>\n" + 
            "            <td>{7}</td>\n" +
            "            <td>{8}</td>\n" +
            "        </tr>\n";
    
    public static String strRow = 
            "        <tr>\n" + 
            "            <td>{0}</td>\n" + 
            "            <td>{1}</td>\n" + 
            "            <td>{2}</td>\n" + 
            "            <td>{3}</td>\n" + 
            "            <td>{4}</td>\n" + 
            "            <td>{5}</td>\n" + 
            "            <td>{6}</td>\n" + 
            "            <td>{7}</td>\n" +
            "            <td>{8}</td>\n" +
            "        </tr>\n";
}
