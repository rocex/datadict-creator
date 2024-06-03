package org.rocex.datadict.action;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.ComponentVO;
import org.rocex.datadict.vo.Context;
import org.rocex.datadict.vo.DictJsonVO;
import org.rocex.datadict.vo.EnumValueVO;
import org.rocex.datadict.vo.MetaVO;
import org.rocex.datadict.vo.MetaVO.ModelType;
import org.rocex.datadict.vo.ModuleVO;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.db.SQLExecutor;
import org.rocex.db.SQLExecutor.DBType;
import org.rocex.db.param.SQLParameter;
import org.rocex.db.processor.BeanListProcessor;
import org.rocex.db.processor.PagingAction;
import org.rocex.db.processor.ResultSetProcessor;
import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;
import org.rocex.utils.ResHelper;
import org.rocex.utils.StringHelper;
import org.rocex.vo.IAction;

/***************************************************************************
 * 同步其它NC数据库的元数据和表结构到sqlite数据库<br>
 * @author Rocex Wang
 * @since 2021-10-28 02:17:34
 ***************************************************************************/
public class SyncDBSchemaAction implements IAction, Closeable
{
    protected Boolean isBIP;
    protected Boolean isCreateDbDdc;

    protected List<String> listNeedSyncTableName = new Vector<>();                  // 要同步的表名

    protected Map<String, String> mapId = new HashMap<>();                          // 为了减小生成的文件体积，把元数据长id和新生成的短id做个对照关系
    protected Map<String, String> mapPrimaryKeyByTableName = new HashMap<>();       // 表名和表主键列表的对应关系，多个主键用；分隔，表名用全小写
    protected Map<String, PropertyVO> mapPropertyVOByTableName = new HashMap<>();   // 表名+属性名 - PropertyVO

    protected Pattern patternTableFilter;

    protected Properties propCodeName;
    protected Properties propDBSource;

    protected SQLExecutor sqlExecutorTarget;

    // 排除的一些表名前缀
    protected String[] strTableFilters = {"aqua_explain_", "gltmp_verdtlbal", "gl_tmp_table", "hr_temptable", "ic_temp_", "iufo_measpub_", "iufo_measure_data_",
        "iufo_tmp_pub_", "ntb_tmp_formual_", "obsclass", "pitemid", "pkjkbx", "sm_securitylog_", "sm_securitylog_", "ssccm_adjustlog_b_", "ssccm_adjust_log_",
        "szxmid", "tb_cell_wbk", "tb_fd_sht", "tb_fd_sht", "tb_tmp_", "tb_tmp_tcheck", "tb_tmp_tcheck_", "tb_tt_", "tb_tt_gh_budgetmodle", "temp000",
        "temppkts", "temptable_", "temp_", "temp_bd_", "temp_fa_", "temp_ia_", "temp_ic_", "temp_pam_", "temp_scacosts_", "temp_scas", "temq_", "temq_",
        "tmpbd_", "tmpin", "tmpina", "tmpinb", "tmpinpk_", "tmpins", "tmpinsrc_", "tmpintop_", "tmpub_calog_temp", "tmp_", "tmp_arap_", "tmp_gl_", "tmp_po_",
        "tmp_scmf", "tmp_so_", "tm_mqsend_success_", "transf2pcm", "t_ationid", "t_emplate", "t_laterow", "t_laterow", "uidbcache_temp_", "uidbcache_temp_",
        "wa_temp_", "zdp_"};

    protected String strTs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(new Date());
    protected String strVersion;              // 数据字典版本

    /***************************************************************************
     * @param strVersion
     * @author Rocex Wang
     * @since 2021-10-28 02:18:20
     ***************************************************************************/
    public SyncDBSchemaAction(String strVersion)
    {
        super();

        this.strVersion = strVersion;

        isBIP = Boolean.valueOf(Context.getInstance().getVersionSetting(strVersion, "isBIP", "true"));
        isCreateDbDdc = Boolean.parseBoolean(Context.getInstance().getVersionSetting(strVersion, "createDbDdc", "true"));

        propCodeName = FileHelper.load("data" + File.separator + "code-name.properties");

        // source
        propDBSource = new Properties();
        propDBSource.setProperty("jdbc.url", Context.getInstance().getSetting(strVersion + ".jdbc.url"));
        propDBSource.setProperty("jdbc.user", Context.getInstance().getSetting(strVersion + ".jdbc.user"));
        propDBSource.setProperty("jdbc.driver", Context.getInstance().getSetting(strVersion + ".jdbc.driver"));
        propDBSource.setProperty("jdbc.password", Context.getInstance().getSetting(strVersion + ".jdbc.password"));

        // target
        Properties dbPropTarget = new Properties();

        dbPropTarget.setProperty("jdbc.url", Context.getInstance().getVersionSetting(strVersion, "target.jdbc.url"));
        dbPropTarget.setProperty("jdbc.user", Context.getInstance().getVersionSetting(strVersion, "target.jdbc.user"));
        dbPropTarget.setProperty("jdbc.driver", Context.getInstance().getVersionSetting(strVersion, "target.jdbc.driver"));
        dbPropTarget.setProperty("jdbc.password", Context.getInstance().getVersionSetting(strVersion, "target.jdbc.password"));

        sqlExecutorTarget = new SQLExecutor(dbPropTarget);

        String strTableFilterPattern = Context.getInstance().getVersionSetting(strVersion, "exclude.tablePattern");
        patternTableFilter = Pattern.compile(strTableFilterPattern, Pattern.CASE_INSENSITIVE);
    }

    protected void afterSyncDataBip()
    {
        Logger.getLogger().begin("after sync bip data");

        String[] strSQLs = {//"update md_class set table_name='' where (table_name is null or table_name in ('null','NULL'))",
            "update md_module set parent_module_id='md__am' where model_type='md' and id in('adc','aim','ambd','ampub','ams','aom','apm','asp','aum','eiot','iass','lim','lom','mim','omm','pam','pvm','rmm','saa','sem','sim','som','spp');",
            "update md_module set parent_module_id='md__bztrc' where model_type='md' and id in('btis','commom','common','tenant');",
            "update md_module set parent_module_id='md__cgy' where model_type='md' and id in('auction','bcres','bcsourcing','bctask','bid','check','colmsu','conformance','contractchange','cooperation','cpu','cpu-base','cpu-basedoc','cpu-bid-biddoc','cpu-bid-bidsub','cpu-bid-project','cpu-bid-score','cpu-buyoffer','cpu-contract','cpu-lawbid','cpu-order','cpu-price','cpu-pricedecision','cpu-privilege','cpu-pubapp','cpu-supplymgr','cpubase','ewallet','lawbid','mall','osmpu','osmsu','pes','privacy','qmpu','qmsu','saleauction','sourcing','suppliermgr','synergy','tpl','vmi-board','vmi-config','yc','ycyuncaimall','yonbip-cpu-bcsourcing','yonbip-cpu-lawbid','yuncai-bid-biddoc','yuncai-mall-bussiness');",
            "update md_module set parent_module_id='md__ec' where model_type='md' and id in('clm','ec','ecc','ecca','ece','eces','ecn','xtdyh','yonbip-ec-announce','yonbip-ec-base','yonbip-ec-conference','yonbip-ec-contacts','yonbip-ec-feed-ranking','yonbip-ec-file','yonbip-ec-group','yonbip-ec-iform','yonbip-ec-link','yonbip-ec-logger','yonbip-ec-meeting','yonbip-ec-official','yonbip-ec-project','yonbip-ec-resource','yonbip-ec-schedule','yonbip-ec-ucenter','yonbip-ec-upesnpub','yonbip-ec-vote');",
            "update md_module set parent_module_id='md__epm' where model_type='md' and id in('ystzd');",
            "update md_module set parent_module_id='md__fi' where model_type='md' and id in('aai','ctjl','eaai','eac','eap','ear','earap','eeac','efa','egl','eia','eis','epca','epcm','epub','esc','espa','evarc','evnt','fa','fi','fiap','fiapbill','fiar','fiarbill','fieaai','fiecc','fieces','fieia','fieml','fiepcm','fiesc','fireport','gl','pma','prjl','rcl','rvn','yonbip-fi-ctmbam','yonbip-fi-ctmfa','yonbip-fi-ctmrsm','yonbip-fi-ctmtmsp','yonbip-fi-eafcapture','yonbip-fi-eafdocfile','yonbip-fi-eafmid','yonbip-fi-eafrecord','yonbip-fi-eafsys','yonbip-fi-eia','yonbip-fi-epmbcs','yonbip-fi-epmer','yonbip-fi-epmpbf','yonbip-fi-epmplatform','yonbip-fi-sepmbcdc','yonbip-fi-sepmdcp');",
            "update md_module set parent_module_id='md__fkgxcz' where model_type='md' and id in('apct','bam','cam','cm','cmp','cspl','drft','fdtr','grm','ia','knlg','lcm','lgm','prdm','qim','sc','sla','ssc','sscimg','sscpejxpj','sscsfm','stct','stwb','tlm','tmsp','ygxygl','znbzbx','znsd');",
            "update md_module set parent_module_id='md__hr' where model_type='md' and id in('attend','bina','cs','demo','hr','hrbd','hrbudget','hrcb','hrcm','hrcpt','hrdimp','hred','hrem','hresp','hris','hrjx','hrod','hrotd','hrpa','hrpx','hrsm','hrtm','hrtset','hrxc','hrxzhs_mdd','hr_official','os','pq','re','si','ss','yonbip-hr-border','yonbip-hr-demappraisal','yonbip-hr-payext-retirementplan','yonbip-hr-payext-trustee');",
            "update md_module set parent_module_id='md__iuap' where model_type='md' and id in('archive','areaformat','asc','base','baseapp','basedata','basedoc','bd','bf','bill','billcode','billmake','bs','businesslog','coredoc','dzhtfw','eventcenter','fp','gztact','gztbdm','gztsys','gzysys','international','iuap','iuap-aip-console','iuap-aip-ipa','iuap-aip-kg','iuap-aip-ps','iuap-aip-service','iuap-aip-vpa','iuap-apcom-auditlog','iuap-apcom-coderule','iuap-apcom-contract','iuap-apcom-file','iuap-apcom-i18n','iuap-apcom-messagecenter','iuap-apcom-messageplatform','iuap-apcom-migration','iuap-apcom-prewarning','iuap-apcom-print','iuap-apcom-ruleengine','iuap-apcom-scheduler','iuap-apcom-workbench','iuap-apcom-workflow','iuap-apdoc-basedoc','iuap-hc-manager','iuap-im-core','iuap-ipaas-designer','iuap-metadata-extend','iuap-metadata-import','iuap-test','iuap-ymdp-portal','iuap-yonbuilder-businesssflow','iuap-yonbuilder-designer','iuap-yonbuilder-mobile','iuap-yonbuilder-transdata','iuap-yyc-yontest','iuap_aip_rpa','iuap_aip_search','kg-catalog-dept-auth','kg-discovery','kg-excel-table-data','kg-guide','kg-interface-push-data','kg-job','ma','md','mddcommon','mdf','nameformat','oldbd','org','pb','pc','pub','restapi','sn','social','socialmdm','sys','ucfbase','ud','ui','uimg','usermanager','xport','xt-wxxcx-you','xtwjfw','yht','ynf');",
            "update md_module set parent_module_id='md__mkt' where model_type='md' and id in('act','ap','b2broute','bbsmk','crmc','ct','cust','dc','dcas','dsfa','goods','marketing','marketingpublic','mka','om','order','pmc','prm','retail','rm','sa','sfa','udh','udm','udm-sys','udmbase','uhybase','um','uorder','usmp');",
            "update md_module set parent_module_id='md__mm' where model_type='md' and id in('aem','dcrp','dfm','dfm-ems','dfm-tam','ed','ems','hse','les','mfg','mr','mso','osm','pcc','po','qam','qmsdfm','qmsqit','qmsqts','sfc','sop','tam','tbs','vc','wjm');",
            "update md_module set parent_module_id='md__ndi' where model_type='md' and id in('yonbip-ndi-jgmj');",
            "update md_module set parent_module_id='md__pm' where model_type='md' and id in('bgdm','bgtm','ckam','pgrm','prdc','prfl','pric','pris','prjb','prjc','prjr','prtp','rscm','rskm','wkhr');",
            "update md_module set parent_module_id='md__scm' where model_type='md' and id in('aa','b2bpricing','channeloperatecenter','invp','pe','portalmg','pu','quote','sact','sccs','scct','sce','scm','scmbd','scpcommon','scpda','scpdp','scppe','scpsnpio','sh','snp','st','stock','uit','uscmf','usp','ustock','voucher');",
            "update md_module set parent_module_id='md__szyx' where model_type='md' and id in('ausa','dsd','fsm','mkp');",
            "update md_module set parent_module_id='md__tax' where model_type='md' and id in('mdd','taxbd','taxpubdoc','taxspec','yonbip-fi-taxability','yonbip-fi-taxbd','yonbip-fi-taxbuilding','yonbip-fi-taxgateway','yonbip-fi-taxincome','yonbip-fi-taxinfra','yonbip-fi-taxit','yonbip-fi-taxot','yonbip-fi-taxoth','yonbip-fi-taxotypd','yonbip-fi-taxreturn');",

            "update md_class set display_name=name where (display_name is null or display_name in ('','null','NULL'));",
            "update md_class set primary_class='1' where id in(select main_class_id from md_class where main_class_id is not null);", // 设置同一个业务对象下的主实体
            "update md_class set component_id=biz_object_id where biz_object_id is not null;",

            "update md_property set data_type='1976686225086391910' where data_type='attachment' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391911' where data_type='bigText' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391912' where data_type='byte' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391913' where data_type='contact' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391914' where data_type='correlation' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391915' where data_type='date' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391916' where data_type='date_MDD' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391917' where data_type='dateTime' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391918' where data_type='dateTime_Timestamp' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391919' where data_type='decimalRange' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391920' where data_type='DefaultCT' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391921' where data_type='FreeCT' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391922' where data_type='image' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391923' where data_type='int' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391924' where data_type='intDate' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391925' where data_type='intDateTime' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391926' where data_type='link' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391927' where data_type='long' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391928' where data_type='MaterialPropCT' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391929' where data_type='multiLanguage' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391930' where data_type='multiLineText' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391931' where data_type='multipleOption' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391932' where data_type='multipleOption_String' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391933' where data_type='number' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391934' where data_type='OptionCT' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391935' where data_type='quote' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391936' where data_type='quoteList' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391937' where data_type='short' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391938' where data_type='singleOption' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391939' where data_type='singleOption_int' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391940' where data_type='singleOption_Short' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391941' where data_type='singleOption_String' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391942' where data_type='SkuPropCT' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391943' where data_type='switch' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391944' where data_type='text' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391945' where data_type='text_MDD' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391946' where data_type='time' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391947' where data_type='time_MDD' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391948' where data_type='timestamp' and ddc_version='2312-bip';",
            "update md_property set data_type='1976686225086391949' where data_type='UserDefine' and ddc_version='2312-bip';",};

        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("after sync bip data");
    }

    /***************************************************************************
     * 修复一些数据库数据问题
     * @author Rocex Wang
     * @since 2020-5-9 14:05:43
     ***************************************************************************/
    protected void afterSyncDataNcc()
    {
        Logger.getLogger().begin("after sync ncc data");

        String[] strSQLs = {"update md_class set name='Memo' where id='BS000010000100001030' and name='MEMO'",
            "update md_class set name='MultiLangText' where id='BS000010000100001058' and name='MULTILANGTEXT'",
            "update md_class set name='Custom' where id in('BS000010000100001056','BS000010000100001059') and name='CUSTOM'",
            "update md_module set display_name='财务' where id='gl'",
            // "delete from md_module where id='FBM'",
            "update md_component set own_module='fbm' where own_module='FBM'", "update md_component set own_module='mmpac' where own_module='NC_MM_PAC6.31'",
            "update md_component set own_module='hryf' where own_module='HRYF'", "update md_component set own_module='ufob' where own_module='UFOB'",
            "update md_component set own_module='hrp' where own_module='hr_hrp'", "update md_component set own_module='uapbd' where own_module='UAP_BD'",
            "update md_component set own_module='uap' where own_module='ncwebpub'", "update md_component set own_module='nresa' where own_module='resa'",
            "insert into md_module(id, display_name, name, parent_module_id,ddc_version) values ('pca', 'pca', 'pca', 'mm','" + strVersion + "')"};

        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("after sync ncc data");
    }

    /****************************************************************************
     * {@inheritDoc}<br>
     * @see IAction#doAction(EventObject)
     * @author Rocex Wang
     * @since 2021-10-28 14:17:51
     ****************************************************************************/
    @Override
    public void doAction(EventObject evt)
    {
        Logger.getLogger().begin("sync db schema and meta data");

        if (!isNeedSyncData())
        {
            return;
        }

        sqlExecutorTarget.initDBSchema(ModuleVO.class, ComponentVO.class, ClassVO.class, PropertyVO.class, EnumValueVO.class, DictJsonVO.class);

        beforeSyncDataBip();

        if (isCreateDbDdc)
        {
            syncDBMeta();
        }

        if (isBIP)
        {
            syncMetaDataBip();

            afterSyncDataBip();
        }
        else
        {
            syncMetaDataNcc();

            afterSyncDataNcc();
        }

        Logger.getLogger().end("sync db schema and meta data");
    }

    protected String getDataTypeSqlBip(PropertyVO propertyVO)
    {
        if (!isBIP)
        {
            return propertyVO.getDataTypeSql();
        }

        PropertyVO tableNamePropertyVO = mapPropertyVOByTableName.get(propertyVO.getTableName() + "." + propertyVO.getName());
        if (tableNamePropertyVO == null)
        {
            tableNamePropertyVO = mapPropertyVOByTableName.get(propertyVO.getTableName() + "." + StringHelper.camelToUnderline(propertyVO.getName()));
            if (tableNamePropertyVO == null)
            {
                tableNamePropertyVO = mapPropertyVOByTableName.get(propertyVO.getTableName() + "." + StringHelper.underlineToCamel(propertyVO.getName()));
            }
        }

        if (tableNamePropertyVO != null)
        {
            String strDataTypeSql = tableNamePropertyVO.getDataTypeSql();

            propertyVO.setColumnCode(tableNamePropertyVO.getName());

            if (StringHelper.isNotBlank(strDataTypeSql))
            {
                propertyVO.setDataTypeSql(strDataTypeSql);

                return strDataTypeSql;
            }
        }

        String strDataType = propertyVO.getDataType() == null ? "varchar" : propertyVO.getDataType();
        if (strDataType.contains("text"))
        {
            propertyVO.setDataTypeSql("varchar");
        }
        else
        {
            propertyVO.setDataTypeSql(strDataType);
        }

        // 参照-305,枚举-203
        if (strDataType.length() == 19 &&
            ((propertyVO.getDataTypeStyle() == 305 && propertyVO.getRefModelName() != null) || propertyVO.getDataTypeStyle() == 203))
        {
            propertyVO.setDataTypeSql("varchar");

            if (propertyVO.getAttrLength() == null)
            {
                propertyVO.setAttrLength(22);
            }
        }

        return getDataTypeSql(propertyVO);
    }

    /***************************************************************************
     * 返回数据库类型定义
     * @param propertyVO
     * @return String
     * @author Rocex Wang
     * @since 2020-4-28 10:14:34
     ***************************************************************************/
    protected String getDataTypeSql(PropertyVO propertyVO)
    {
        if (propertyVO.getDataTypeSql() == null && (propertyVO.getDataType().length() == 20 || propertyVO.getDataType().length() == 36))
        {
            propertyVO.setDataTypeSql("char");
        }

        String strDbType = propertyVO.getDataTypeSql().toLowerCase();

        if ((strDbType.contains("char") || strDbType.contains("text")) && propertyVO.getAttrLength() != null)
        {
            strDbType = strDbType + "(" + propertyVO.getAttrLength() + ")";
        }
        else if ((strDbType.contains("decimal") || strDbType.contains("number")) && propertyVO.getAttrLength() != null)
        {
            strDbType = strDbType + "(" + propertyVO.getAttrLength() + ", " + propertyVO.getPrecise() + ")";
        }

        propertyVO.setDataTypeSql(strDbType);

        return strDbType;
    }

    /***************************************************************************
     * @param classVO
     * @return mappedId
     * @author Rocex Wang
     * @since 2020-4-30 13:47:11
     ***************************************************************************/
    protected synchronized String getMappedClassId(ClassVO classVO)
    {
        return getMappedId("class", classVO.getId());
    }

    /***************************************************************************
     * @param strType
     * @param strId
     * @return mappedId
     * @author Rocex Wang
     * @since 2020-4-30 13:47:06
     ***************************************************************************/
    protected synchronized String getMappedId(String strType, String strId)
    {
        if (strId != null && strId.length() < 24)
        {
            return strId;
        }

        String strKey = strType + "_" + strId;

        String strValue = mapId.get(strKey);

        if (strValue != null)
        {
            return strValue;
        }

        String strMapId = StringHelper.getId();

        mapId.put(strKey, strMapId);

        return strMapId;
    }

    /***************************************************************************
     * @param moduleVO
     * @return mappedId
     * @author Rocex Wang
     * @since 2020-4-30 13:46:54
     ***************************************************************************/
    protected synchronized String getMappedModuleId(ModuleVO moduleVO)
    {
        return getMappedId("module", moduleVO.getId());
    }

    /***************************************************************************
     * 根据数据库特性一次性读取所有表的主键，如果不能确定数据库，还是按照jdbc的api每次只取一个表的主键
     * @param strTableName
     * @param mapPrimaryKey
     * @return String
     * @throws Exception
     * @author Rocex Wang
     * @since 2020-5-22 14:03:59
     ***************************************************************************/
    protected String getPrimaryKeys(SQLExecutor sqlExecutorSource, String strDBCatalog, String strDBSchema, String strTableName,
        Map<String, String> mapPrimaryKey) throws Exception
    {
        List<String> listPk = new ArrayList<>();

        // 找到表的主键
        try (Connection connection = sqlExecutorSource.getConnection();
             ResultSet rsPkColumns = connection.getMetaData().getPrimaryKeys(strDBCatalog, strDBSchema, strTableName))
        {
            List<PropertyVO> listPkPropertyVO = (List<PropertyVO>) new BeanListProcessor<>(PropertyVO.class, mapPrimaryKey, "name").doAction(rsPkColumns);

            for (PropertyVO propertyVO : listPkPropertyVO)
            {
                if (propertyVO.getName() != null)
                {
                    listPk.add(propertyVO.getName().toLowerCase());
                }
            }
        }

        return String.join(";", listPk);
    }

    void beforeSyncDataBip()
    {
        Logger.getLogger().begin("before sync bip data");

        if (!isBIP)
            return;

        String[] strDBModuleSQLs = {
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__iuap', '2312-bip', '技术应用平台', 'md', 'iuap', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__fi', '2312-bip', '智能会计', 'md', 'fi', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__scm', '2312-bip', '供应链云', 'md', 'scm', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__hr', '2312-bip', '人力云', 'md', 'hr', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__mkt', '2312-bip', '营销云', 'md', 'mkt', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__am', '2312-bip', '资产云', 'md', 'am', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__cgy', '2312-bip', '采购云', 'md', 'cgy', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__tax', '2312-bip', '税务云', 'md', 'tax', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__mm', '2312-bip', '制造云', 'md', 'mm', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__epm', '2312-bip', '企业绩效', 'md', 'epm', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__pm', '2312-bip', '项目云', 'md', 'pm', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__trst', '2312-bip', '云可信', 'md', 'trst', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__bztrc', '2312-bip', '商旅云', 'md', 'bztrc', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__ctrm', '2312-bip', '贸易云', 'md', 'ctrm', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__base', '2312-bip', '领域基础', 'md', 'base', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__ec', '2312-bip', '协同云', 'md', 'ec', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__szyx', '2312-bip', '数字营销', 'md', 'szyx', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__fkgxcz', '2312-bip', '费控共享财资云', 'md', 'fkgxcz', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__ndi', '2312-bip', '国防工业云', 'md', 'ndi', 'md_clazz');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__other', '2312-bip', '其它', 'md', 'other', 'md_clazz');",

            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__iuap', '2312-bip', '技术应用平台', 'db', 'iuap', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__fi', '2312-bip', '智能会计', 'db', 'fi', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__scm', '2312-bip', '供应链云', 'db', 'scm', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__hr', '2312-bip', '人力云', 'db', 'hr', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__mkt', '2312-bip', '营销云', 'db', 'mkt', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__am', '2312-bip', '资产云', 'db', 'am', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__cgy', '2312-bip', '采购云', 'db', 'cgy', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__tax', '2312-bip', '税务云', 'db', 'tax', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__mm', '2312-bip', '制造云', 'db', 'mm', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__epm', '2312-bip', '企业绩效', 'db', 'epm', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__pm', '2312-bip', '项目云', 'db', 'pm', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__trst', '2312-bip', '云可信', 'db', 'trst', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__bztrc', '2312-bip', '商旅云', 'db', 'bztrc', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__ctrm', '2312-bip', '贸易云', 'db', 'ctrm', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__base', '2312-bip', '领域基础', 'db', 'base', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__ec', '2312-bip', '协同云', 'db', 'ec', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__szyx', '2312-bip', '数字营销', 'db', 'szyx', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__fkgxcz', '2312-bip', '费控共享财资云', 'db', 'fkgxcz', 'db_table');",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__ndi', '2312-bip', '国防工业云', 'db', 'ndi', 'db_table');"};

        String[] strDBComponentSQLs = {
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_aim', '2312-bip', 'db', 'amc_aim', 'db__am');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_ambd', '2312-bip', 'db', 'amc_ambd', 'db__am');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_aom', '2312-bip', 'db', 'amc_aom', 'db__am');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_aum', '2312-bip', 'db', 'amc_aum', 'db__am');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_mro', '2312-bip', 'db', 'amc_mro', 'db__am');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__btis', '2312-bip', 'db', 'btis', 'db__bztrc');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_adjust', '2312-bip', 'db', 'cpu_adjust', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_baseservice', '2312-bip', 'db', 'cpu_baseservice', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_base_doc', '2312-bip', 'db', 'cpu_base_doc', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_base_portal', '2312-bip', 'db', 'cpu_base_portal', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_bi', '2312-bip', 'db', 'cpu_bi', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_bid', '2312-bip', 'db', 'cpu_bid', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_cooperation', '2312-bip', 'db', 'cpu_cooperation', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_lawbid', '2312-bip', 'db', 'cpu_lawbid', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_mall', '2312-bip', 'db', 'cpu_mall', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_pubbiz_sourcing', '2312-bip', 'db', 'cpu_pubbiz_sourcing', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_pubservice', '2312-bip', 'db', 'cpu_pubservice', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_sourcing', '2312-bip', 'db', 'cpu_sourcing', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_suppliermgr', '2312-bip', 'db', 'cpu_suppliermgr', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_synergy', '2312-bip', 'db', 'cpu_synergy', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pub_dop', '2312-bip', 'db', 'pub_dop', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yuncai_yop', '2312-bip', 'db', 'yuncai_yop', 'db__cgy');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctrm_ct', '2312-bip', 'db', 'ctrm_ct', 'db__ctrm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctrm_share', '2312-bip', 'db', 'ctrm_share', 'db__ctrm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__db_project', '2312-bip', 'db', 'db_project', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_0', '2312-bip', 'db', 'encrypt_0', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_1', '2312-bip', 'db', 'encrypt_1', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_10', '2312-bip', 'db', 'encrypt_10', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_11', '2312-bip', 'db', 'encrypt_11', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_12', '2312-bip', 'db', 'encrypt_12', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_13', '2312-bip', 'db', 'encrypt_13', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_14', '2312-bip', 'db', 'encrypt_14', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_15', '2312-bip', 'db', 'encrypt_15', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_2', '2312-bip', 'db', 'encrypt_2', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_3', '2312-bip', 'db', 'encrypt_3', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_4', '2312-bip', 'db', 'encrypt_4', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_5', '2312-bip', 'db', 'encrypt_5', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_6', '2312-bip', 'db', 'encrypt_6', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_7', '2312-bip', 'db', 'encrypt_7', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_8', '2312-bip', 'db', 'encrypt_8', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_9', '2312-bip', 'db', 'encrypt_9', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_announce', '2312-bip', 'db', 'esn_announce', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_api', '2312-bip', 'db', 'esn_api', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_base', '2312-bip', 'db', 'esn_base', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_email', '2312-bip', 'db', 'esn_email', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_file', '2312-bip', 'db', 'esn_file', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_file_docs', '2312-bip', 'db', 'esn_file_docs', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_im', '2312-bip', 'db', 'esn_im', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_link', '2312-bip', 'db', 'esn_link', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_message', '2312-bip', 'db', 'esn_message', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_plugins', '2312-bip', 'db', 'esn_plugins', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_schedule', '2312-bip', 'db', 'esn_schedule', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__form_relation', '2312-bip', 'db', 'form_relation', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iform', '2312-bip', 'db', 'iform', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__key_db', '2312-bip', 'db', 'key_db', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__kfpt_openapi', '2312-bip', 'db', 'kfpt_openapi', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__meeting', '2312-bip', 'db', 'meeting', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__opsys', '2312-bip', 'db', 'opsys', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__supervision', '2312-bip', 'db', 'supervision', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ucenter', '2312-bip', 'db', 'ucenter', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_clm_contract', '2312-bip', 'db', 'yonbip_clm_contract', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ec_logger', '2312-bip', 'db', 'yonbip_ec_logger', 'db__ec');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_base', '2312-bip', 'db', 'epmp_base', 'db__epm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_bcdc', '2312-bip', 'db', 'epmp_bcdc', 'db__epm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_bcs', '2312-bip', 'db', 'epmp_bcs', 'db__epm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_bill', '2312-bip', 'db', 'epmp_bill', 'db__epm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_bp', '2312-bip', 'db', 'epmp_bp', 'db__epm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_br', '2312-bip', 'db', 'epmp_br', 'db__epm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_dcp', '2312-bip', 'db', 'epmp_dcp', 'db__epm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieaai', '2312-bip', 'db', 'fieaai', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiearap', '2312-bip', 'db', 'fiearap', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiearapbill', '2312-bip', 'db', 'fiearapbill', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiecc', '2312-bip', 'db', 'fiecc', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieeac', '2312-bip', 'db', 'fieeac', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiefa', '2312-bip', 'db', 'fiefa', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieia', '2312-bip', 'db', 'fieia', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieis', '2312-bip', 'db', 'fieis', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiepca', '2312-bip', 'db', 'fiepca', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieprjl', '2312-bip', 'db', 'fieprjl', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiepub', '2312-bip', 'db', 'fiepub', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiercl', '2312-bip', 'db', 'fiercl', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiervn', '2312-bip', 'db', 'fiervn', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fievarc', '2312-bip', 'db', 'fievarc', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__figl', '2312-bip', 'db', 'figl', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__figlbase', '2312-bip', 'db', 'figlbase', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__saas', '2312-bip', 'db', 'saas', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ejls', '2312-bip', 'db', 'yonbip_fi_ejls', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__apct', '2312-bip', 'db', 'apct', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmbam', '2312-bip', 'db', 'ctmbam', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmcam', '2312-bip', 'db', 'ctmcam', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmcmp', '2312-bip', 'db', 'ctmcmp', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmcspl', '2312-bip', 'db', 'ctmcspl', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmdrft', '2312-bip', 'db', 'ctmdrft', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmfa', '2312-bip', 'db', 'ctmfa', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmfdtr', '2312-bip', 'db', 'ctmfdtr', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmgrm', '2312-bip', 'db', 'ctmgrm', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmlcm', '2312-bip', 'db', 'ctmlcm', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmlgm', '2312-bip', 'db', 'ctmlgm', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmrsm', '2312-bip', 'db', 'ctmrsm', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmstct', '2312-bip', 'db', 'ctmstct', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmstwb', '2312-bip', 'db', 'ctmstwb', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmthre', '2312-bip', 'db', 'ctmthre', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmtlm', '2312-bip', 'db', 'ctmtlm', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmtmsp', '2312-bip', 'db', 'ctmtmsp', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__intelligent_audit', '2312-bip', 'db', 'intelligent_audit', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__intelligent_robot', '2312-bip', 'db', 'intelligent_robot', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__shared_service', '2312-bip', 'db', 'shared_service', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_img', '2312-bip', 'db', 'ssc_img', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_km', '2312-bip', 'db', 'ssc_km', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_pdm', '2312-bip', 'db', 'ssc_pdm', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_pe', '2312-bip', 'db', 'ssc_pe', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_sfm', '2312-bip', 'db', 'ssc_sfm', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_sla', '2312-bip', 'db', 'ssc_sla', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_staff_credit', '2312-bip', 'db', 'ssc_staff_credit', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmpub', '2312-bip', 'db', 'yonbip_fi_ctmpub', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__znbz', '2312-bip', 'db', 'znbz', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__znbz_fileservice', '2312-bip', 'db', 'znbz_fileservice', 'db__fkgxcz');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cloud_attachment', '2312-bip', 'db', 'cloud_attachment', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__corehr', '2312-bip', 'db', 'corehr', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_cpt', '2312-bip', 'db', 'diwork_cpt', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_esp', '2312-bip', 'db', 'diwork_esp', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_hr_bonus', '2312-bip', 'db', 'diwork_hr_bonus', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_hr_budget', '2312-bip', 'db', 'diwork_hr_budget', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_pub', '2312-bip', 'db', 'diwork_pub', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_talent', '2312-bip', 'db', 'diwork_talent', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_train', '2312-bip', 'db', 'diwork_train', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_wa', '2312-bip', 'db', 'diwork_wa', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_wa_mdd', '2312-bip', 'db', 'diwork_wa_mdd', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hrattenddb', '2312-bip', 'db', 'hrattenddb', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_cpt', '2312-bip', 'db', 'hr_cpt', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_dataanalysis', '2312-bip', 'db', 'hr_dataanalysis', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_intelligent', '2312-bip', 'db', 'hr_intelligent', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_official', '2312-bip', 'db', 'hr_official', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_orgsystem', '2312-bip', 'db', 'hr_orgsystem', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_otd', '2312-bip', 'db', 'hr_otd', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_qualify', '2312-bip', 'db', 'hr_qualify', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_sinsurance', '2312-bip', 'db', 'hr_sinsurance', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_socialization', '2312-bip', 'db', 'hr_socialization', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_staffing', '2312-bip', 'db', 'hr_staffing', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__performance', '2312-bip', 'db', 'performance', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc', '2312-bip', 'db', 'ssc', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_hr_tm', '2312-bip', 'db', 'yonbip_hr_tm', 'db__hr');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__approvecenter', '2312-bip', 'db', 'approvecenter', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__auth_0', '2312-bip', 'db', 'auth_0', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__bits', '2312-bip', 'db', 'bits', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__bussiness_expansion_third_pub', '2312-bip', 'db', 'bussiness_expansion_third_pub', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmfdma', '2312-bip', 'db', 'ctmfdma', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_portal', '2312-bip', 'db', 'esn_portal', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiproduct', '2312-bip', 'db', 'fiproduct', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__helpcenter', '2312-bip', 'db', 'helpcenter', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__high-yxy', '2312-bip', 'db', 'high-yxy', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ht_staffing', '2312-bip', 'db', 'ht_staffing', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__idm', '2312-bip', 'db', 'idm', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_data', '2312-bip', 'db', 'iuap_aip_data', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_ipa', '2312-bip', 'db', 'iuap_aip_ipa', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_kg', '2312-bip', 'db', 'iuap_aip_kg', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_ps', '2312-bip', 'db', 'iuap_aip_ps', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_search', '2312-bip', 'db', 'iuap_aip_search', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_vpa', '2312-bip', 'db', 'iuap_aip_vpa', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_aprvcntr', '2312-bip', 'db', 'iuap_apcom_aprvcntr', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_auth', '2312-bip', 'db', 'iuap_apcom_auth', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_benchservice', '2312-bip', 'db', 'iuap_apcom_benchservice', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_businesssflow', '2312-bip', 'db', 'iuap_apcom_businesssflow', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_contract', '2312-bip', 'db', 'iuap_apcom_contract', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_file', '2312-bip', 'db', 'iuap_apcom_file', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_messagecenter', '2312-bip', 'db', 'iuap_apcom_messagecenter', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_messageservice', '2312-bip', 'db', 'iuap_apcom_messageservice', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_migration', '2312-bip', 'db', 'iuap_apcom_migration', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_print', '2312-bip', 'db', 'iuap_apcom_print', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_ruleengine', '2312-bip', 'db', 'iuap_apcom_ruleengine', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_supportcenter', '2312-bip', 'db', 'iuap_apcom_supportcenter', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_workflow', '2312-bip', 'db', 'iuap_apcom_workflow', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apdoc_basedoc', '2312-bip', 'db', 'iuap_apdoc_basedoc', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apdoc_coredoc', '2312-bip', 'db', 'iuap_apdoc_coredoc', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apdoc_finbd', '2312-bip', 'db', 'iuap_apdoc_finbd', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apdoc_social', '2312-bip', 'db', 'iuap_apdoc_social', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_cloud_basedoc', '2312-bip', 'db', 'iuap_cloud_basedoc', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_data_datafusion', '2312-bip', 'db', 'iuap_data_datafusion', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_devops_data', '2312-bip', 'db', 'iuap_devops_data', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_dp_data', '2312-bip', 'db', 'iuap_dp_data', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_hubble_data', '2312-bip', 'db', 'iuap_hubble_data', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_im_core', '2312-bip', 'db', 'iuap_im_core', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ipaas', '2312-bip', 'db', 'iuap_ipaas', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ipaas_mdm', '2312-bip', 'db', 'iuap_ipaas_mdm', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_metadata_base', '2312-bip', 'db', 'iuap_metadata_base', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_metadata_extendservice', '2312-bip', 'db', 'iuap_metadata_extendservice', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_metadata_import', '2312-bip', 'db', 'iuap_metadata_import', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_metadata_service', '2312-bip', 'db', 'iuap_metadata_service', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_uuas_usercenter', '2312-bip', 'db', 'iuap_uuas_usercenter', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ymc_data', '2312-bip', 'db', 'iuap_ymc_data', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ymdp_portal', '2312-bip', 'db', 'iuap_ymdp_portal', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yms_console', '2312-bip', 'db', 'iuap_yms_console', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yms_data', '2312-bip', 'db', 'iuap_yms_data', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yonbuilder_dynamic_ds', '2312-bip', 'db', 'iuap_yonbuilder_dynamic_ds', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yonbuilder_service', '2312-bip', 'db', 'iuap_yonbuilder_service', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ypr_data', '2312-bip', 'db', 'iuap_ypr_data', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yyc_data', '2312-bip', 'db', 'iuap_yyc_data', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__mtd_0331', '2312-bip', 'db', 'mtd_0331', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__mysql', '2312-bip', 'db', 'mysql', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__todocenter', '2312-bip', 'db', 'todocenter', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__uba', '2312-bip', 'db', 'uba', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__udhcloud', '2312-bip', 'db', 'udhcloud', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ugoods', '2312-bip', 'db', 'ugoods', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ycreport', '2312-bip', 'db', 'ycreport', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yms_pre', '2312-bip', 'db', 'yms_pre', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip-indmkt', '2312-bip', 'db', 'yonbip-indmkt', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_cpu_dw', '2312-bip', 'db', 'yonbip_cpu_dw', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__engineering_data', '2312-bip', 'db', 'engineering_data', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ils_les', '2312-bip', 'db', 'ils_les', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp', '2312-bip', 'db', 'imp', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_dfm', '2312-bip', 'db', 'imp_dfm', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_hse', '2312-bip', 'db', 'imp_hse', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_ib', '2312-bip', 'db', 'imp_ib', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_plm', '2312-bip', 'db', 'imp_plm', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_sfc', '2312-bip', 'db', 'imp_sfc', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__mf_ecn', '2312-bip', 'db', 'mf_ecn', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__production_order', '2312-bip', 'db', 'production_order', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__qms_dfm', '2312-bip', 'db', 'qms_dfm', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__qms_qit', '2312-bip', 'db', 'qms_qit', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__requirements_planning', '2312-bip', 'db', 'requirements_planning', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_mm_sop', '2312-bip', 'db', 'yonbip_mm_sop', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ndi_hrfp', '2312-bip', 'db', 'yonbip_ndi_hrfp', 'db__ndi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ndi_jgmj', '2312-bip', 'db', 'yonbip_ndi_jgmj', 'db__ndi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ndi_smxt', '2312-bip', 'db', 'yonbip_ndi_smxt', 'db__ndi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__quotation', '2312-bip', 'db', 'quotation', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__sact', '2312-bip', 'db', 'sact', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__scct', '2312-bip', 'db', 'scct', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__scmbd', '2312-bip', 'db', 'scmbd', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__scmmp', '2312-bip', 'db', 'scmmp', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__scp', '2312-bip', 'db', 'scp', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__upurchase', '2312-bip', 'db', 'upurchase', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ustock', '2312-bip', 'db', 'ustock', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_scm_dw', '2312-bip', 'db', 'yonbip_scm_dw', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__indagts', '2312-bip', 'db', 'indagts', 'db__szyx');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__inddsd', '2312-bip', 'db', 'inddsd', 'db__szyx');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__indfsm', '2312-bip', 'db', 'indfsm', 'db__szyx');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__indrp', '2312-bip', 'db', 'indrp', 'db__szyx');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_billcenter', '2312-bip', 'db', 'diwork_billcenter', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_building', '2312-bip', 'db', 'diwork_building', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_einvoice_auth', '2312-bip', 'db', 'diwork_einvoice_auth', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_income', '2312-bip', 'db', 'diwork_income', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_infra', '2312-bip', 'db', 'diwork_infra', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_input_tax', '2312-bip', 'db', 'diwork_input_tax', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_invoice_bd', '2312-bip', 'db', 'diwork_invoice_bd', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_other_tax', '2312-bip', 'db', 'diwork_other_tax', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_piaoeda_client', '2312-bip', 'db', 'diwork_piaoeda_client', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_report', '2312-bip', 'db', 'diwork_report', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_taxability', '2312-bip', 'db', 'diwork_taxability', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_taxpubdoc', '2312-bip', 'db', 'diwork_taxpubdoc', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_taxspec', '2312-bip', 'db', 'diwork_taxspec', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__idoc', '2312-bip', 'db', 'idoc', 'db__tax');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__trustedsign_v5', '2312-bip', 'db', 'trustedsign_v5', 'db__trst');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_hy_doccrbd', '2312-bip', 'db', 'yonbip_hy_doccrbd', 'db__trst');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__crm', '2312-bip', 'db', 'crm', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__omsdata', '2312-bip', 'db', 'omsdata', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pricecenter', '2312-bip', 'db', 'pricecenter', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__quartz', '2312-bip', 'db', 'quartz', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__sddcdata', '2312-bip', 'db', 'sddcdata', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__udm', '2312-bip', 'db', 'udm', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__uorders', '2312-bip', 'db', 'uorders', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__upmalls', '2312-bip', 'db', 'upmalls', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__uretaildata', '2312-bip', 'db', 'uretaildata', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__usmp', '2312-bip', 'db', 'usmp', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yilian', '2312-bip', 'db', 'yilian', 'db__mkt');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pmcloud', '2312-bip', 'db', 'pmcloud', 'db__pm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pmcloud_ods', '2312-bip', 'db', 'pmcloud_ods', 'db__pm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pca', '2312-bip', 'db', 'pca', 'db__mm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__uinttrade', '2312-bip', 'db', 'uinttrade', 'db__scm');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmfm', '2312-bip', 'db', 'yonbip_fi_ctmfm', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmfm_init', '2312-bip', 'db', 'yonbip_fi_ctmfm_init', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmlc', '2312-bip', 'db', 'yonbip_fi_ctmlc', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmlc_init', '2312-bip', 'db', 'yonbip_fi_ctmlc_init', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmpub_init', '2312-bip', 'db', 'yonbip_fi_ctmpub_init', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_merge_schema', '2312-bip', 'db', 'yonbip_fi_merge_schema', 'db__fi');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_installer', '2312-bip', 'db', 'iuap_installer', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__logger', '2312-bip', 'db', 'logger', 'db__iuap');",
            "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__sys', '2312-bip', 'db', 'sys', 'db__iuap');"};

        String[] strMDComponentSQLs = {
            "insert into md_component (id, biz_model, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391905', '0', '2312-bip', 'BIP基础数据类型', 'md', 'BipBasicDataType', 'iuap');"};

        String[] strMDClassSQLs = {
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391910', 1, '1976686225086391905', '2312-bip', '附件', 'md', 'Attachment', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391911', 1, '1976686225086391905', '2312-bip', '大文本', 'md', 'BigText', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391912', 1, '1976686225086391905', '2312-bip', '字节', 'md', 'Byte', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391913', 1, '1976686225086391905', '2312-bip', '联系人', 'md', 'Contact', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391914', 1, '1976686225086391905', '2312-bip', '相关性', 'md', 'Correlation', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391915', 1, '1976686225086391905', '2312-bip', '日期', 'md', 'Date', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391916', 1, '1976686225086391905', '2312-bip', '日期', 'md', 'Date_MDD', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391917', 1, '1976686225086391905', '2312-bip', '日期时间', 'md', 'DateTime', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391918', 1, '1976686225086391905', '2312-bip', '日期时间戳', 'md', 'DateTime_Timestamp', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391919', 1, '1976686225086391905', '2312-bip', '小数范围', 'md', 'DecimalRange', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391920', 1, '1976686225086391905', '2312-bip', 'DefaultCT', 'md', 'DefaultCT', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391921', 1, '1976686225086391905', '2312-bip', 'FreeCT', 'md', 'FreeCT', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391922', 1, '1976686225086391905', '2312-bip', '图片', 'md', 'Image', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391923', 1, '1976686225086391905', '2312-bip', '整数', 'md', 'Int', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391924', 1, '1976686225086391905', '2312-bip', '整数日期', 'md', 'IntDate', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391925', 1, '1976686225086391905', '2312-bip', '整数日期时间', 'md', 'IntDateTime', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391926', 1, '1976686225086391905', '2312-bip', '链接', 'md', 'Link', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391927', 1, '1976686225086391905', '2312-bip', '长整数', 'md', 'Long', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391928', 1, '1976686225086391905', '2312-bip', 'MaterialPropCT', 'md', 'MaterialPropCT', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391929', 1, '1976686225086391905', '2312-bip', '多语言', 'md', 'MultiLanguage', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391930', 1, '1976686225086391905', '2312-bip', '多行文本', 'md', 'MultiLineText', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391931', 1, '1976686225086391905', '2312-bip', '多选项', 'md', 'MultipleOption', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391932', 1, '1976686225086391905', '2312-bip', '多选项-字符串', 'md', 'MultipleOption_String', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391933', 1, '1976686225086391905', '2312-bip', '数字', 'md', 'Number', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391934', 1, '1976686225086391905', '2312-bip', 'OptionCT', 'md', 'OptionCT', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391935', 1, '1976686225086391905', '2312-bip', '引用', 'md', 'Quote', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391936', 1, '1976686225086391905', '2312-bip', '引用-列表', 'md', 'QuoteList', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391937', 1, '1976686225086391905', '2312-bip', '短整数', 'md', 'Short', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391938', 1, '1976686225086391905', '2312-bip', '单选项', 'md', 'SingleOption', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391939', 1, '1976686225086391905', '2312-bip', '单选项-整数', 'md', 'SingleOption_int', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391940', 1, '1976686225086391905', '2312-bip', '单选项-短整数', 'md', 'SingleOption_Short', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391941', 1, '1976686225086391905', '2312-bip', '单选项-字符串', 'md', 'SingleOption_String', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391942', 1, '1976686225086391905', '2312-bip', 'SkuPropCT', 'md', 'SkuPropCT', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391943', 1, '1976686225086391905', '2312-bip', '开关', 'md', 'Switch', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391944', 1, '1976686225086391905', '2312-bip', '文本', 'md', 'Text', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391945', 1, '1976686225086391905', '2312-bip', '文本', 'md', 'Text_MDD', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391946', 1, '1976686225086391905', '2312-bip', '时间', 'md', 'Time', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391947', 1, '1976686225086391905', '2312-bip', '时间', 'md', 'Time_MDD', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391948', 1, '1976686225086391905', '2312-bip', '时间戳', 'md', 'Timestamp', null);",
            "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391949', 1, '1976686225086391905', '2312-bip', '用户定义', 'md', 'UserDefine', null);"};

        try
        {
            sqlExecutorTarget.executeUpdate(strDBModuleSQLs);
            sqlExecutorTarget.executeUpdate(strDBComponentSQLs);

            sqlExecutorTarget.executeUpdate(strMDComponentSQLs);
            sqlExecutorTarget.executeUpdate(strMDClassSQLs);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }

        Logger.getLogger().end("before sync bip data");
    }

    /***************************************************************************
     * 获取数据库临时表并加到strTableFilters里
     * @throws Exception
     * @author Rocex Wang
     * @since 2020-5-22 14:03:59
     ***************************************************************************/
    protected void initTableFiltersWithTempTableName(SQLExecutor sqlExecutorSource) throws SQLException
    {
        if (sqlExecutorSource.getDBType() == DBType.Oracle)
        {
            Logger.getLogger().begin("oracle query all temporary tables");

            String strSQL = "select lower(table_name) from user_tables where temporary='Y' order by table_name";

            final List<String> listTempTableName = new ArrayList<>();

            sqlExecutorSource.executeQuery(strSQL, new ResultSetProcessor()
            {
                @Override
                protected Object processResultSet(ResultSet resultSet) throws SQLException
                {
                    while (resultSet.next())
                    {
                        listTempTableName.add(resultSet.getString(1));
                    }

                    return null;
                }
            });

            listTempTableName.addAll(Arrays.asList(strTableFilters));

            strTableFilters = listTempTableName.toArray(new String[0]);

            Logger.getLogger().end("oracle query all temporary tables");
        }
    }

    /***************************************************************************
     * @return boolean
     * @author Rocex Wang
     * @since 2021-11-11 16:26:03
     ***************************************************************************/
    public boolean isNeedSyncData()
    {
        if (!sqlExecutorTarget.isTableExist("md_module"))
        {
            return true;
        }

        String strSQL = "select count(1) from md_module where ddc_version='" + strVersion + "'";

        try
        {
            int iRecordCount = (Integer) this.sqlExecutorTarget.executeQuery(strSQL, new ResultSetProcessor()
            {
                @Override
                protected Object processResultSet(ResultSet resultSet) throws SQLException
                {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            });

            return iRecordCount == 0;
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        return true;
    }

    /***************************************************************************
     * @param strTableName
     * @return true：表名合法；false：不合法
     * @author Rocex Wang
     * @since 2021-11-17 19:07:50
     ***************************************************************************/
    protected boolean isTableNameValid(String strTableName)
    {
        for (String strTableFilter : strTableFilters)
        {
            if (strTableName.startsWith(strTableFilter))
            {
                return false;
            }
        }

        boolean blValid = patternTableFilter.matcher(strTableName).matches();

        return !blValid;
    }

    /***************************************************************************
     * @param metaVOClass
     * @param strSQL
     * @param param
     * @param pagingAction
     * @return List<? extends MetaVO>
     * @author Rocex Wang
     * @since 2020-5-9 11:20:25
     ***************************************************************************/
    protected List<? extends MetaVO> queryMetaVO(SQLExecutor sqlExecutorSource, Class<? extends MetaVO> metaVOClass, String strSQL, SQLParameter param,
        PagingAction pagingAction)
    {
        Logger.getLogger().begin("query " + metaVOClass.getSimpleName());

        List<? extends MetaVO> listMetaVO = null;

        try
        {
            BeanListProcessor<? extends MetaVO> processor = new BeanListProcessor<>(metaVOClass);
            processor.setPagingAction(pagingAction);

            listMetaVO = (List<? extends MetaVO>) sqlExecutorSource.executeQuery(strSQL, param, processor);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("query " + metaVOClass.getSimpleName());

        return listMetaVO;
    }

    /***************************************************************************
     * @throws SQLException
     * @author Rocex Wang
     * @since 2021-11-15 13:59:54
     ***************************************************************************/
    protected void syncDBField(SQLExecutor sqlExecutorSource, String strDBCatalog, String strDBSchema, ClassVO classVO) throws SQLException
    {
        PagingAction pagingFieldAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                int iSequence = 0;
                List<PropertyVO> listPropertyVO = (List<PropertyVO>) evt.getSource();

                for (PropertyVO propertyVO : listPropertyVO)
                {
                    propertyVO.setClassId(classVO.getId());

                    String strPropLowerName = propertyVO.getName().toLowerCase();

                    propertyVO.setDataTypeStyle(ClassVO.ClassType.db.value());
                    propertyVO.setName(strPropLowerName);
                    propertyVO.setDdcVersion(strVersion);
                    propertyVO.setId(StringHelper.getId());
                    propertyVO.setAttrSequence(++iSequence);
                    propertyVO.setDataTypeSql(getDataTypeSql(propertyVO));
                    propertyVO.setDefaultValue(StringHelper.isEmpty(propertyVO.getDefaultValue()) ? null : propertyVO.getDefaultValue().toLowerCase());
                    propertyVO.setDisplayName(
                        StringHelper.isEmpty(propertyVO.getRemarks()) ? strPropLowerName : StringHelper.removeCRLF(propertyVO.getRemarks()));
                }

                Map<String, PropertyVO> mapTableNamePropertyVO2 = listPropertyVO.stream()
                    .collect(Collectors.toMap(propertyVO -> propertyVO.getTableName() + "." + propertyVO.getName(), propertyVO -> propertyVO));

                mapPropertyVOByTableName.putAll(mapTableNamePropertyVO2);

                try
                {
                    sqlExecutorTarget.insertVO(listPropertyVO.toArray(new PropertyVO[0]));

                    listPropertyVO.clear();
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        };

        Map<String, String> mapColumn = new HashMap<>();
        mapColumn.put("COLUMN_NAME", "Name");
        mapColumn.put("COLUMN_SIZE", "AttrLength");
        mapColumn.put("COLUMN_DEF", "DefaultValue");
        mapColumn.put("TABLE_NAME", "TableName");
        mapColumn.put("TYPE_NAME", "DataTypeSql");
        mapColumn.put("DECIMAL_DIGITS", "Precise");
        mapColumn.put("NULLABLE", "Nullable");

        BeanListProcessor<? extends MetaVO> processor = new BeanListProcessor<>(PropertyVO.class, mapColumn, propertyVO ->
        {
            String strTableName = propertyVO.getTableName().toLowerCase();

            return listNeedSyncTableName.contains(strTableName) && isTableNameValid(strTableName);
        });

        try (Connection connection = sqlExecutorSource.getConnection();
             ResultSet rsColumns = connection.getMetaData().getColumns(strDBCatalog, strDBSchema, classVO.getTableName(), null))
        {
            processor.setPagingAction(pagingFieldAction);

            processor.doAction(rsColumns);
        }
    }

    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-11 14:00:41
     ***************************************************************************/
    protected void syncDBMeta()
    {
        Logger.getLogger().begin("sync database meta");

        String strSourceUrl = Context.getInstance().getSetting(strVersion + ".jdbc.url");

        String strSrcDBSchemaList = Context.getInstance().getVersionSetting(strVersion, "jdbc.schemas");

        String[] strSrcDBSchemas = StringHelper.isEmpty(strSrcDBSchemaList) ? new String[]{""} : strSrcDBSchemaList.split(",");

        for (String strSrcDBSchema : strSrcDBSchemas)
        {
            Properties dbPropSource2 = (Properties) propDBSource.clone();
            dbPropSource2.setProperty("jdbc.url", strSourceUrl.replace("${schema}", strSrcDBSchema));

            try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2);
                 Connection connection = sqlExecutorSource.getConnection())
            {
                String strSrcDBCatalog = connection.getCatalog();

                initTableFiltersWithTempTableName(sqlExecutorSource);

                syncDBTable(sqlExecutorSource, strSrcDBCatalog, strSrcDBSchema, "db__" + strSrcDBSchema);
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        }

        Logger.getLogger().end("sync database meta");
    }

    /***************************************************************************
     * 查询所有表
     * @author Rocex Wang
     * @since 2020-5-11 11:19:19
     * @throws Exception
     ***************************************************************************/
    protected void syncDBTable(SQLExecutor sqlExecutorSource, String strDBCatalog, String strDBSchema, String strComponentId) throws SQLException
    {
        String strMsg = "sync all tables and fields from schema [%s]".formatted(strDBSchema);

        Logger.getLogger().begin(strMsg);

        Map<String, String> mapTable = new HashMap<>();
        mapTable.put("TABLE_NAME", "TableName");
        mapTable.put("TABLE_TYPE", "ClassListUrl"); // 用 ClassListUrl 代替了，反正 ClassListUrl 也不保存

        Map<String, String> mapPrimaryKey = new HashMap<>();
        mapPrimaryKey.put("COLUMN_NAME", "name");
        mapPrimaryKey.put("KEY_SEQ", "AttrSequence");

        PagingAction pagingAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                List<ClassVO> listClassVO = (List<ClassVO>) evt.getSource();

                try
                {
                    sqlExecutorTarget.insertVO(listClassVO.toArray(new ClassVO[0]));
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }

                ExecutorService executorService = Executors.newFixedThreadPool(ResHelper.getThreadCount());

                listClassVO.forEach(classVO -> executorService.execute(() ->
                {
                    try
                    {
                        syncDBField(sqlExecutorSource, strDBCatalog, strDBSchema, classVO);
                    }
                    catch (SQLException ex)
                    {
                        Logger.getLogger().error(ex.getMessage(), ex);
                    }
                }));

                executorService.shutdown();

                while (!executorService.isTerminated())
                {
                    ResHelper.sleep(100);
                }

                listClassVO.clear();
            }
        };

        BeanListProcessor<ClassVO> processor = new BeanListProcessor<>(ClassVO.class, mapTable, classVO ->
        {
            String strTableName = Objects.toString(classVO.getTableName(), "").toLowerCase();

            if (!"table".equalsIgnoreCase(classVO.getClassListUrl()) || !isTableNameValid(strTableName) || listNeedSyncTableName.contains(strTableName))
            {
                return false;
            }

            classVO.setDdcVersion(strVersion);
            classVO.setId(StringHelper.getId());
            classVO.setName(strTableName);
            classVO.setComponentId(strComponentId);
            classVO.setModelType(ModelType.db.name());
            classVO.setTableName(strTableName);
            classVO.setClassType(ClassVO.ClassType.db.value());
            classVO.setDisplayName(
                propCodeName.getProperty(strTableName, StringHelper.isEmpty(classVO.getRemarks()) ? "" : StringHelper.removeCRLF(classVO.getRemarks())));

            try
            {
                String strPrimaryKeys = getPrimaryKeys(sqlExecutorSource, strDBCatalog, strDBSchema, strTableName, mapPrimaryKey);

                classVO.setKeyAttribute(strPrimaryKeys);

                mapPrimaryKeyByTableName.put(strTableName, strPrimaryKeys);
            }
            catch (Exception ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }

            listNeedSyncTableName.add(strTableName);

            return true;
        }, "TableName", "ClassListUrl", "Remarks");

        try (Connection connection = sqlExecutorSource.getConnection();
             ResultSet rsTable = connection.getMetaData().getTables(strDBCatalog, strDBSchema, "%", new String[]{"TABLE"}))
        {
            processor.setPagingAction(pagingAction);
            processor.doAction(rsTable);
        }

        Logger.getLogger().end(strMsg);
    }

    protected void syncMetaData(SQLExecutor sqlExecutorSource, String strModuleSQL, String strComponentSQL, String strBizObjAsComponentSQL, String strClassSQL,
        String strPropertySQL, String strEnumAsClass, String strEnumValueSQL)
    {
        Logger.getLogger().begin("sync bip metadata");

        PagingAction pagingAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                List<MetaVO> listVO = (List<MetaVO>) evt.getSource();

                for (MetaVO metaVO : listVO)
                {
                    if (metaVO.getId() == null)
                    {
                        metaVO.setId(StringHelper.getId());
                    }

                    metaVO.setDisplayName(StringHelper.removeCRLF(metaVO.getDisplayName()));

                    if (metaVO instanceof ClassVO classVO && StringHelper.isBlank(classVO.getKeyAttribute()))
                    {
                        classVO.setKeyAttribute(mapPrimaryKeyByTableName.get(Objects.toString(classVO.getTableName(), "").toLowerCase()));
                    }
                }

                try
                {
                    sqlExecutorTarget.insertVO(listVO.toArray(new MetaVO[0]));
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        };

        PagingAction pagingPropertyAction = new PagingAction()
        {
            @Override
            public void doAction(EventObject evt)
            {
                List<PropertyVO> listVO = (List<PropertyVO>) evt.getSource();

                for (PropertyVO propertyVO : listVO)
                {
                    propertyVO.setId(StringHelper.getId());
                    propertyVO.setDisplayName(StringHelper.removeCRLF(propertyVO.getDisplayName()));
                    propertyVO.setDataTypeSql(isBIP ? getDataTypeSqlBip(propertyVO) : getDataTypeSql(propertyVO));
                }

                try
                {
                    sqlExecutorTarget.insertVO(listVO.toArray(new MetaVO[0]));
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            }
        };

        queryMetaVO(sqlExecutorSource, ModuleVO.class, strModuleSQL, null, pagingAction);
        queryMetaVO(sqlExecutorSource, ComponentVO.class, strComponentSQL, null, pagingAction);
        queryMetaVO(sqlExecutorSource, ClassVO.class, strClassSQL, null, pagingAction);
        queryMetaVO(sqlExecutorSource, PropertyVO.class, strPropertySQL, null, pagingPropertyAction);

        if (StringHelper.isNotBlank(strBizObjAsComponentSQL))
        {
            queryMetaVO(sqlExecutorSource, ComponentVO.class, strBizObjAsComponentSQL, null, pagingAction);
        }

        if (StringHelper.isNotEmpty(strEnumAsClass))
        {
            queryMetaVO(sqlExecutorSource, ClassVO.class, strEnumAsClass, null, pagingAction);
        }

        queryMetaVO(sqlExecutorSource, EnumValueVO.class, strEnumValueSQL, null, pagingAction);

        Logger.getLogger().end("sync bip metadata");
    }

    protected void syncMetaDataBip()
    {
        String strOtherSQL1 = ",'" + strVersion + "' as ddc_version,'" + strTs + "' as ts";
        String strOtherSQL2 = ",'" + ModelType.md.name() + "' as model_type" + strOtherSQL1;

        String strModuleSQL =
            "select distinct lower(own_module) as id,own_module as display_name,null as help,own_module as name,'md__other' as parent_module_id,null as version_type" +
                strOtherSQL2 +
                " from md_meta_component where ytenant_id='0' and own_module is not null and own_module not in('','null','NULL') order by own_module";

        String strComponentSQL =
            "select id,null as biz_model,display_name,null as help,name,null as namespace,lower(own_module) as own_module,null as version,null as version_type" +
                strOtherSQL2 +
                " from md_meta_component where ytenant_id='0' and own_module is not null and own_module not in('','null','NULL') order by own_module";

        String strBizObjAsComponentSQL =
            "select a.id,null as biz_model,a.display_name,null as help,a.name,null as namespace,lower(b.own_module) as own_module,null as version,null as version_type" +
                strOtherSQL2 + " from md_biz_obj a left join md_meta_component b on b.ytenant_id='0' and a.meta_component_uri=b.uri" +
                " where a.ytenant_id='0' and b.own_module is not null and a.code in(select biz_object_code from md_meta_class where ytenant_id='0')";

        String strClassSQL = "select a.id,null as accessor_classname,null as authen,null as biz_itf_imp_classname," + ClassVO.ClassType.clazz.value() +
            " as class_type,b.id as component_id" +
            ",a.table_name as table_name,a.display_name,'' as full_classname,null as help,null as key_attribute,a.name,null as own_module,null as primary_class" +
            ",null as ref_model_name,null as return_type,null as version_type,c.id as biz_object_id,c.main_entity,d.id as main_class_id" + strOtherSQL2 +
            " from md_meta_class a left join md_meta_component b on b.ytenant_id='0' and a.meta_component_uri=b.uri" +
            " left join md_biz_obj c on c.ytenant_id='0' and a.biz_object_code=c.code left join md_meta_class d on d.ytenant_id='0' and c.main_entity=d.uri" +
            " where a.ytenant_id='0' and a.meta_component_uri is not null" +
            " and b.own_module is not null and b.own_module not in('','null','NULL') order by a.meta_component_uri";

        String strEnumAsClass = "select a.id,null as accessor_classname,null as authen,null as biz_itf_imp_classname," + ClassVO.ClassType.enumeration.value() +
            " as class_type,b.id as component_id,null as table_name" +
            ",a.display_name,null as full_classname,null as help,null as key_attribute,null as model_type,a.name,null as own_module,null as primary_class" +
            ",null as ref_model_name,null as return_type,null as version_type" + strOtherSQL2 +
            " from md_enumeration a left join md_meta_component b on b.ytenant_id='0' and a.meta_component_uri=b.uri" +
            " where a.ytenant_id='0' and a.uri in (select enumeration_uri from md_enumeration_literal where ytenant_id='0')";

        String strPropertySQL =
            "select a.id,null as accessor_classname,0 as access_power,0 as access_power_group,length as attr_length,max_value as attr_max_value,min_value as attr_min_value" +
                ",0 as attr_sequence,0 as calculation,b.id as class_id,0 as custom_attr" +
                ",(case when ref_meta_class_uri is not null then (select id from md_meta_class where ytenant_id='0' and ref_meta_class_uri=uri)" +
                " when ref_enum_uri is not null then (select id from md_enumeration where ytenant_id='0' and ref_enum_uri=uri) else biz_type end) as data_type" +
                ",'' as data_type_sql,(case when ref_meta_class_uri is not null then 305 when ref_enum_uri is not null then 203 else 300 end) as data_type_style" +
                ",default_value,a.display_name,0 as dynamic_attr,null as dynamic_table,0 as fixed_length,null as help,0 as hidden,0 as key_prop,a.name,0 as not_serialize" +
                ",(case when a.is_nullable is null then '1' else a.is_nullable end) as nullable,precise,0 as read_only,ref_meta_class_uri as ref_model_name,null as version_type" +
                ",b.table_name as table_name" + strOtherSQL1 + " from md_attribute a left join md_meta_class b on a.object_uri=b.uri and b.ytenant_id='0'" +
                " where a.ytenant_id='0' and a.biz_type is not null and object_uri in(select uri from md_meta_class where ytenant_id='0')";

        String strEnumValueSQL = "select a.id,b.id as class_id,0 as enum_sequence,code as enum_value,a.name as name,0 as version_type" + strOtherSQL1 +
            " from md_enumeration_literal a left join md_enumeration b on a.enumeration_uri=b.uri and b.ytenant_id='0'" +
            " where a.ytenant_id='0' order by enumeration_uri,code";

        Properties dbPropSourceMd = new Properties();

        String strUrl = Context.getInstance().getSetting(strVersion + ".md.jdbc.url", Context.getInstance().getSetting(strVersion + ".jdbc.url"));
        dbPropSourceMd.setProperty("jdbc.url", strUrl.replace("${schema}", "iuap_metadata_base"));
        dbPropSourceMd.setProperty("jdbc.user",
            Context.getInstance().getSetting(strVersion + ".md.jdbc.user", Context.getInstance().getSetting(strVersion + ".jdbc.user")));
        dbPropSourceMd.setProperty("jdbc.driver",
            Context.getInstance().getSetting(strVersion + ".md.jdbc.driver", Context.getInstance().getSetting(strVersion + ".jdbc.driver")));
        dbPropSourceMd.setProperty("jdbc.password",
            Context.getInstance().getSetting(strVersion + ".md.jdbc.password", Context.getInstance().getSetting(strVersion + ".jdbc.password")));

        try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSourceMd))
        {
            syncMetaData(sqlExecutorSource, strModuleSQL, strComponentSQL, strBizObjAsComponentSQL, strClassSQL, strPropertySQL, strEnumAsClass,
                strEnumValueSQL);
        }
    }

    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-11 14:00:20
     ***************************************************************************/
    protected void syncMetaDataNcc()
    {
        String strOtherSQL = "'" + strVersion + "' as ddc_version,'" + strTs + "' as ts";

        String strModuleSQL = "select distinct id,name,displayname,parentmoduleid,help,versiontype," + strOtherSQL + " from md_module order by name";

        String strComponentSQL =
            "select id as original_id,name,displayname,ownmodule,namespace,help,isbizmodel as biz_model,version,versiontype," + strOtherSQL +
                " from md_component";

        String strClassSQL = "select id,name,displayname,defaulttablename as table_name,fullclassname,keyattribute,componentid,classtype,isprimary,help" +
            ",accessorclassname,bizitfimpclassname,refmodelname,returntype,isauthen,versiontype," + strOtherSQL + " from md_class order by defaulttablename";

        String strPropertySQL = "select a.id original_id,a.name as name,a.displayname as displayname,attrlength,attrminvalue" +
            ",attrmaxvalue,attrsequence,customattr,datatype,datatypestyle,a.defaultvalue as defaultvalue" +
            ",a.nullable as nullable,a.precise as precise,refmodelname,classid,accesspowergroup,accessorclassname,dynamictable" +
            ",a.help,accesspower,calculation,dynamicattr,a.fixedlength,a.hided as hidden,a.notserialize,a.readonly,b.sqldatetype data_type_sql" +
            ",b.pkey key_prop,a.versiontype," + strOtherSQL +
            " from md_property a left join md_column b on a.name=b.name and b.tableid=? where classid=? order by b.pkey desc,a.attrsequence";

        String strEnumValueSQL = "select id as class_id,enumsequence as enum_sequence,name,value enum_value,versiontype," + strOtherSQL +
            " from md_enumvalue order by id,enumsequence";

        Properties dbPropSource2 = (Properties) propDBSource.clone();

        try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2))
        {
            syncMetaData(sqlExecutorSource, strModuleSQL, strComponentSQL, null, strClassSQL, strPropertySQL, null, strEnumValueSQL);
        }
    }

    @Override
    public void close()
    {
        mapId.clear();
        propDBSource.clear();
        propCodeName.clear();
        listNeedSyncTableName.clear();
        mapPrimaryKeyByTableName.clear();
        mapPropertyVOByTableName.clear();

        sqlExecutorTarget.close();
    }
}
