package org.rocex.datadict.action;

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
import org.rocex.db.processor.ResultSetProcessor;
import org.rocex.utils.FileHelper;
import org.rocex.utils.Logger;
import org.rocex.utils.ResHelper;
import org.rocex.utils.StringHelper;
import org.rocex.vo.IAction;

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
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/***************************************************************************
 * 同步其它NC数据库的元数据和表结构到sqlite数据库<br>
 * @author Rocex Wang
 * @since 2021-10-28 02:17:34
 ***************************************************************************/
public class SyncDBSchemaAction implements IAction
{
    protected Properties dbPropSource;

    protected Boolean isBIP = true;

    protected List<String> listNeedSyncTableName = new Vector<>();              // 要同步的表名

    protected Map<String, String> mapTableNamePrimaryKeys = new HashMap<>();    // 表名和表主键列表的对应关系，多个主键用；分隔，表名用全小写

    protected Pattern patternTableFilter;
    protected Properties propCodeName;

    protected SQLExecutor sqlExecutorTarget;

    // 排除的一些表名前缀
    protected String[] strTableFilters = {"aqua_explain_", "gltmp_verdtlbal", "gl_tmp_table", "hr_temptable", "ic_temp_", "iufo_measpub_", "iufo_measure_data_", "iufo_tmp_pub_",
        "ntb_tmp_formual_", "obsclass", "pitemid", "pkjkbx", "sm_securitylog_", "sm_securitylog_", "ssccm_adjustlog_b_", "ssccm_adjust_log_", "szxmid", "tb_cell_wbk", "tb_fd_sht",
        "tb_fd_sht", "tb_tmp_", "tb_tmp_tcheck", "tb_tmp_tcheck_", "tb_tt_", "tb_tt_gh_budgetmodle", "temp000", "temppkts", "temptable_", "temp_", "temp_bd_", "temp_fa_",
        "temp_ia_", "temp_ic_", "temp_pam_", "temp_scacosts_", "temp_scas", "temq_", "temq_", "tmpbd_", "tmpin", "tmpina", "tmpinb", "tmpinpk_", "tmpins", "tmpinsrc_", "tmpintop_",
        "tmpub_calog_temp", "tmp_", "tmp_arap_", "tmp_gl_", "tmp_po_", "tmp_scmf", "tmp_so_", "tm_mqsend_success_", "transf2pcm", "t_ationid", "t_emplate", "t_laterow",
        "t_laterow", "uidbcache_temp_", "uidbcache_temp_", "wa_temp_", "zdp_"};

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

        isBIP = Boolean.valueOf(Context.getInstance().getVersionSetting(strVersion, "isBIP"));

        propCodeName = FileHelper.load("data" + File.separator + "code-name.properties");

        // source
        dbPropSource = new Properties();
        dbPropSource.setProperty("jdbc.url", Context.getInstance().getSetting(strVersion + ".jdbc.url"));
        dbPropSource.setProperty("jdbc.user", Context.getInstance().getSetting(strVersion + ".jdbc.user"));
        dbPropSource.setProperty("jdbc.driver", Context.getInstance().getSetting(strVersion + ".jdbc.driver"));
        dbPropSource.setProperty("jdbc.password", Context.getInstance().getSetting(strVersion + ".jdbc.password"));

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

    protected void adjustDataBip()
    {
        String[] strSQLs = {"update md_class set default_table_name='' where (default_table_name is null or default_table_name in ('null','NULL'))",
            "update md_class set display_name='' where (display_name is null or display_name in ('null','NULL'))"};

        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }

    /***************************************************************************
     * 修复一些数据库数据问题
     * @author Rocex Wang
     * @since 2020-5-9 14:05:43
     ***************************************************************************/
    protected void adjustDataNcc()
    {
        Logger.getLogger().begin("fix ncc data");

        // @formatter:off
        String[] strSQLs = { "update md_class set name='Memo' where id='BS000010000100001030' and name='MEMO'",
                "update md_class set name='MultiLangText' where id='BS000010000100001058' and name='MULTILANGTEXT'",
                "update md_class set name='Custom' where id in('BS000010000100001056','BS000010000100001059') and name='CUSTOM'",
                "update md_module set display_name='财务' where id='gl'",
                // "delete from md_module where id='FBM'",
                "update md_component set own_module='fbm' where own_module='FBM'",
                "update md_component set own_module='mmpac' where own_module='NC_MM_PAC6.31'",
                "update md_component set own_module='hryf' where own_module='HRYF'", "update md_component set own_module='ufob' where own_module='UFOB'",
                "update md_component set own_module='hrp' where own_module='hr_hrp'", "update md_component set own_module='uapbd' where own_module='UAP_BD'",
                "update md_component set own_module='uap' where own_module='ncwebpub'", "update md_component set own_module='nresa' where own_module='resa'",
                "insert into md_module(id, display_name, name, parent_module_id,ddc_version) values ('pca', 'pca', 'pca', 'mm','" + strVersion + "')" };
        // @formatter:on

        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }

        Logger.getLogger().end("fix ncc data");
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

        Boolean blCreateDbDdc = Boolean.parseBoolean(Context.getInstance().getSetting("createDbDdc", "false"));

        if (blCreateDbDdc)
        {
            syncDBMeta();
        }

        if (isBIP)
        {
            syncMetaDataBip();

            adjustDataBip();
        }
        else
        {
            syncMetaDataNcc();

            adjustDataNcc();
        }

        Logger.getLogger().end("sync db schema and meta data");
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

        if (strDbType.contains("char") || strDbType.contains("text"))
        {
            strDbType = strDbType + "(" + propertyVO.getAttrLength() + ")";
        }
        else if (strDbType.contains("decimal") || strDbType.contains("number"))
        {
            strDbType = strDbType + "(" + propertyVO.getAttrLength() + ", " + propertyVO.getPrecise() + ")";
        }

        propertyVO.setDataTypeSql(strDbType);

        return strDbType;
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
    protected String getPrimaryKeys(SQLExecutor sqlExecutorSource, String strDBCatalog, String strDBSchema, String strTableName, Map<String, String> mapPrimaryKey) throws Exception
    {
        String strPks = "";

        // 找到表的主键
        try (Connection connection = sqlExecutorSource.getConnection();
             ResultSet rsPkColumns = connection.getMetaData().getPrimaryKeys(strDBCatalog, strDBSchema, strTableName))
        {
            List<PropertyVO> listPkPropertyVO = (List<PropertyVO>) new BeanListProcessor<>(PropertyVO.class, mapPrimaryKey, "name").doAction(rsPkColumns);

            for (PropertyVO propertyVO : listPkPropertyVO)
            {
                if (propertyVO.getName() != null)
                {
                    strPks += propertyVO.getName().toLowerCase() + ";";
                }
            }

            if (strPks.endsWith(";"))
            {
                strPks = strPks.substring(0, strPks.length() - 1);
            }
        }

        return strPks;
    }

    void initBipModules()
    {
        String[] strModuleSQLs = {
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('iuap', '2312-bip', 'iuap', 'db', 'iuap', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('fi', '2312-bip', '智能会计', 'db', 'fi', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('scm', '2312-bip', '供应链云', 'db', 'scm', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('hr_cloud', '2312-bip', '人力云', 'db', 'hr_cloud', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('yonbip-mkt', '2312-bip', '营销云', 'db', 'yonbip-mkt', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('am', '2312-bip', '资产云', 'db', 'am', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('cgy', '2312-bip', '采购云', 'db', 'cgy', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('tax', '2312-bip', '税务云', 'db', 'tax', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('mm', '2312-bip', '制造云', 'db', 'mm', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('epm', '2312-bip', '企业绩效', 'db', 'epm', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('yonbip-pm', '2312-bip', '项目云', 'db', 'yonbip-pm', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('trst', '2312-bip', '云可信', 'db', 'trst', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('bztrc', '2312-bip', '商旅云', 'db', 'bztrc', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('ctrm', '2312-bip', '贸易云', 'db', 'ctrm', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('yonbip-base', '2312-bip', '领域基础', 'db', 'yonbip-base', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('ec', '2312-bip', '协同云', 'db', 'ec', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('szyx', '2312-bip', '数字营销', 'db', 'szyx', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('fkgxcz', '2312-bip', '费控共享财资云', 'db', 'fkgxcz', 'db_tables', null);",
            "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id, version_type) values ('ndi', '2312-bip', '国防工业云', 'db', 'ndi', 'db_tables', null);"};

        String[] strComponentSQLs = {"insert into md_component (id,ddc_version,model_type,name,own_module) values ('amc_aim','2312-bip','db','amc_aim','am');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('amc_ambd','2312-bip','db','amc_ambd','am');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('amc_aom','2312-bip','db','amc_aom','am');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('amc_aum','2312-bip','db','amc_aum','am');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('amc_mro','2312-bip','db','amc_mro','am');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('btis','2312-bip','db','btis','bztrc');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_adjust','2312-bip','db','cpu_adjust','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_baseservice','2312-bip','db','cpu_baseservice','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_base_doc','2312-bip','db','cpu_base_doc','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_base_portal','2312-bip','db','cpu_base_portal','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_bi','2312-bip','db','cpu_bi','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_bid','2312-bip','db','cpu_bid','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_cooperation','2312-bip','db','cpu_cooperation','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_lawbid','2312-bip','db','cpu_lawbid','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_mall','2312-bip','db','cpu_mall','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_pubbiz_sourcing','2312-bip','db','cpu_pubbiz_sourcing','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_pubservice','2312-bip','db','cpu_pubservice','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_sourcing','2312-bip','db','cpu_sourcing','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_suppliermgr','2312-bip','db','cpu_suppliermgr','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cpu_synergy','2312-bip','db','cpu_synergy','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('pub_dop','2312-bip','db','pub_dop','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yuncai_yop','2312-bip','db','yuncai_yop','cgy');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctrm_ct','2312-bip','db','ctrm_ct','ctrm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctrm_share','2312-bip','db','ctrm_share','ctrm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('db_project','2312-bip','db','db_project','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_0','2312-bip','db','encrypt_0','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_1','2312-bip','db','encrypt_1','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_10','2312-bip','db','encrypt_10','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_11','2312-bip','db','encrypt_11','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_12','2312-bip','db','encrypt_12','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_13','2312-bip','db','encrypt_13','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_14','2312-bip','db','encrypt_14','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_15','2312-bip','db','encrypt_15','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_2','2312-bip','db','encrypt_2','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_3','2312-bip','db','encrypt_3','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_4','2312-bip','db','encrypt_4','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_5','2312-bip','db','encrypt_5','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_6','2312-bip','db','encrypt_6','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_7','2312-bip','db','encrypt_7','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_8','2312-bip','db','encrypt_8','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('encrypt_9','2312-bip','db','encrypt_9','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_announce','2312-bip','db','esn_announce','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_api','2312-bip','db','esn_api','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_base','2312-bip','db','esn_base','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_email','2312-bip','db','esn_email','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_file','2312-bip','db','esn_file','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_file_docs','2312-bip','db','esn_file_docs','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_im','2312-bip','db','esn_im','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_link','2312-bip','db','esn_link','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_message','2312-bip','db','esn_message','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_plugins','2312-bip','db','esn_plugins','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_schedule','2312-bip','db','esn_schedule','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('form_relation','2312-bip','db','form_relation','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iform','2312-bip','db','iform','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('key_db','2312-bip','db','key_db','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('kfpt_openapi','2312-bip','db','kfpt_openapi','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('meeting','2312-bip','db','meeting','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('opsys','2312-bip','db','opsys','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('supervision','2312-bip','db','supervision','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ucenter','2312-bip','db','ucenter','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_clm_contract','2312-bip','db','yonbip_clm_contract','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_ec_logger','2312-bip','db','yonbip_ec_logger','ec');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('epmp_base','2312-bip','db','epmp_base','epm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('epmp_bcdc','2312-bip','db','epmp_bcdc','epm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('epmp_bcs','2312-bip','db','epmp_bcs','epm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('epmp_bill','2312-bip','db','epmp_bill','epm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('epmp_bp','2312-bip','db','epmp_bp','epm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('epmp_br','2312-bip','db','epmp_br','epm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('epmp_dcp','2312-bip','db','epmp_dcp','epm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fieaai','2312-bip','db','fieaai','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fiearap','2312-bip','db','fiearap','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fiearapbill','2312-bip','db','fiearapbill','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fiecc','2312-bip','db','fiecc','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fieeac','2312-bip','db','fieeac','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fiefa','2312-bip','db','fiefa','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fieia','2312-bip','db','fieia','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fieis','2312-bip','db','fieis','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fiepca','2312-bip','db','fiepca','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fieprjl','2312-bip','db','fieprjl','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fiepub','2312-bip','db','fiepub','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fiercl','2312-bip','db','fiercl','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fiervn','2312-bip','db','fiervn','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fievarc','2312-bip','db','fievarc','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('figl','2312-bip','db','figl','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('figlbase','2312-bip','db','figlbase','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('saas','2312-bip','db','saas','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_fi_ejls','2312-bip','db','yonbip_fi_ejls','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('apct','2312-bip','db','apct','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmbam','2312-bip','db','ctmbam','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmcam','2312-bip','db','ctmcam','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmcmp','2312-bip','db','ctmcmp','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmcspl','2312-bip','db','ctmcspl','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmdrft','2312-bip','db','ctmdrft','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmfa','2312-bip','db','ctmfa','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmfdtr','2312-bip','db','ctmfdtr','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmgrm','2312-bip','db','ctmgrm','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmlcm','2312-bip','db','ctmlcm','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmlgm','2312-bip','db','ctmlgm','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmrsm','2312-bip','db','ctmrsm','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmstct','2312-bip','db','ctmstct','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmstwb','2312-bip','db','ctmstwb','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmthre','2312-bip','db','ctmthre','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmtlm','2312-bip','db','ctmtlm','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmtmsp','2312-bip','db','ctmtmsp','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('intelligent_audit','2312-bip','db','intelligent_audit','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('intelligent_robot','2312-bip','db','intelligent_robot','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('shared_service','2312-bip','db','shared_service','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ssc_img','2312-bip','db','ssc_img','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ssc_km','2312-bip','db','ssc_km','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ssc_pdm','2312-bip','db','ssc_pdm','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ssc_pe','2312-bip','db','ssc_pe','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ssc_sfm','2312-bip','db','ssc_sfm','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ssc_sla','2312-bip','db','ssc_sla','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ssc_staff_credit','2312-bip','db','ssc_staff_credit','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_fi_ctmpub','2312-bip','db','yonbip_fi_ctmpub','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('znbz','2312-bip','db','znbz','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('znbz_fileservice','2312-bip','db','znbz_fileservice','fkgxcz');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('cloud_attachment','2312-bip','db','cloud_attachment','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('corehr','2312-bip','db','corehr','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_cpt','2312-bip','db','diwork_cpt','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_esp','2312-bip','db','diwork_esp','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_hr_bonus','2312-bip','db','diwork_hr_bonus','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_hr_budget','2312-bip','db','diwork_hr_budget','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_pub','2312-bip','db','diwork_pub','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_talent','2312-bip','db','diwork_talent','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_train','2312-bip','db','diwork_train','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_wa','2312-bip','db','diwork_wa','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_wa_mdd','2312-bip','db','diwork_wa_mdd','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hrattenddb','2312-bip','db','hrattenddb','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_cpt','2312-bip','db','hr_cpt','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_dataanalysis','2312-bip','db','hr_dataanalysis','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_intelligent','2312-bip','db','hr_intelligent','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_official','2312-bip','db','hr_official','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_orgsystem','2312-bip','db','hr_orgsystem','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_otd','2312-bip','db','hr_otd','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_qualify','2312-bip','db','hr_qualify','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_sinsurance','2312-bip','db','hr_sinsurance','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_socialization','2312-bip','db','hr_socialization','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('hr_staffing','2312-bip','db','hr_staffing','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('performance','2312-bip','db','performance','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ssc','2312-bip','db','ssc','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_hr_tm','2312-bip','db','yonbip_hr_tm','hr_cloud');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('approvecenter','2312-bip','db','approvecenter','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('auth_0','2312-bip','db','auth_0','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('bits','2312-bip','db','bits','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('bussiness_expansion_third_pub','2312-bip','db','bussiness_expansion_third_pub','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ctmfdma','2312-bip','db','ctmfdma','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('esn_portal','2312-bip','db','esn_portal','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('fiproduct','2312-bip','db','fiproduct','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('helpcenter','2312-bip','db','helpcenter','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('high-yxy','2312-bip','db','high-yxy','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ht_staffing','2312-bip','db','ht_staffing','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('idm','2312-bip','db','idm','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_aip_data','2312-bip','db','iuap_aip_data','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_aip_ipa','2312-bip','db','iuap_aip_ipa','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_aip_kg','2312-bip','db','iuap_aip_kg','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_aip_ps','2312-bip','db','iuap_aip_ps','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_aip_search','2312-bip','db','iuap_aip_search','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_aip_vpa','2312-bip','db','iuap_aip_vpa','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_aprvcntr','2312-bip','db','iuap_apcom_aprvcntr','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_auth','2312-bip','db','iuap_apcom_auth','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_benchservice','2312-bip','db','iuap_apcom_benchservice','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_businesssflow','2312-bip','db','iuap_apcom_businesssflow','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_contract','2312-bip','db','iuap_apcom_contract','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_file','2312-bip','db','iuap_apcom_file','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_messagecenter','2312-bip','db','iuap_apcom_messagecenter','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_messageservice','2312-bip','db','iuap_apcom_messageservice','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_migration','2312-bip','db','iuap_apcom_migration','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_print','2312-bip','db','iuap_apcom_print','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_ruleengine','2312-bip','db','iuap_apcom_ruleengine','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_supportcenter','2312-bip','db','iuap_apcom_supportcenter','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apcom_workflow','2312-bip','db','iuap_apcom_workflow','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apdoc_basedoc','2312-bip','db','iuap_apdoc_basedoc','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apdoc_coredoc','2312-bip','db','iuap_apdoc_coredoc','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apdoc_finbd','2312-bip','db','iuap_apdoc_finbd','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_apdoc_social','2312-bip','db','iuap_apdoc_social','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_cloud_basedoc','2312-bip','db','iuap_cloud_basedoc','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_data_datafusion','2312-bip','db','iuap_data_datafusion','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_devops_data','2312-bip','db','iuap_devops_data','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_dp_data','2312-bip','db','iuap_dp_data','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_hubble_data','2312-bip','db','iuap_hubble_data','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_im_core','2312-bip','db','iuap_im_core','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_ipaas','2312-bip','db','iuap_ipaas','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_ipaas_mdm','2312-bip','db','iuap_ipaas_mdm','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_metadata_base','2312-bip','db','iuap_metadata_base','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_metadata_extendservice','2312-bip','db','iuap_metadata_extendservice','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_metadata_import','2312-bip','db','iuap_metadata_import','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_metadata_service','2312-bip','db','iuap_metadata_service','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_uuas_usercenter','2312-bip','db','iuap_uuas_usercenter','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_ymc_data','2312-bip','db','iuap_ymc_data','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_ymdp_portal','2312-bip','db','iuap_ymdp_portal','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_yms_console','2312-bip','db','iuap_yms_console','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_yms_data','2312-bip','db','iuap_yms_data','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_yonbuilder_dynamic_ds','2312-bip','db','iuap_yonbuilder_dynamic_ds','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_yonbuilder_service','2312-bip','db','iuap_yonbuilder_service','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_ypr_data','2312-bip','db','iuap_ypr_data','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_yyc_data','2312-bip','db','iuap_yyc_data','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('mtd_0331','2312-bip','db','mtd_0331','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('mysql','2312-bip','db','mysql','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('todocenter','2312-bip','db','todocenter','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('uba','2312-bip','db','uba','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('udhcloud','2312-bip','db','udhcloud','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ugoods','2312-bip','db','ugoods','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ycreport','2312-bip','db','ycreport','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yms_pre','2312-bip','db','yms_pre','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip-indmkt','2312-bip','db','yonbip-indmkt','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_cpu_dw','2312-bip','db','yonbip_cpu_dw','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('engineering_data','2312-bip','db','engineering_data','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ils_les','2312-bip','db','ils_les','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('imp','2312-bip','db','imp','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('imp_dfm','2312-bip','db','imp_dfm','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('imp_hse','2312-bip','db','imp_hse','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('imp_ib','2312-bip','db','imp_ib','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('imp_plm','2312-bip','db','imp_plm','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('imp_sfc','2312-bip','db','imp_sfc','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('mf_ecn','2312-bip','db','mf_ecn','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('production_order','2312-bip','db','production_order','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('qms_dfm','2312-bip','db','qms_dfm','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('qms_qit','2312-bip','db','qms_qit','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('requirements_planning','2312-bip','db','requirements_planning','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_mm_sop','2312-bip','db','yonbip_mm_sop','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_ndi_hrfp','2312-bip','db','yonbip_ndi_hrfp','ndi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_ndi_jgmj','2312-bip','db','yonbip_ndi_jgmj','ndi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_ndi_smxt','2312-bip','db','yonbip_ndi_smxt','ndi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('quotation','2312-bip','db','quotation','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('sact','2312-bip','db','sact','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('scct','2312-bip','db','scct','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('scmbd','2312-bip','db','scmbd','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('scmmp','2312-bip','db','scmmp','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('scp','2312-bip','db','scp','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('upurchase','2312-bip','db','upurchase','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('ustock','2312-bip','db','ustock','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_scm_dw','2312-bip','db','yonbip_scm_dw','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('indagts','2312-bip','db','indagts','szyx');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('inddsd','2312-bip','db','inddsd','szyx');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('indfsm','2312-bip','db','indfsm','szyx');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('indrp','2312-bip','db','indrp','szyx');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_billcenter','2312-bip','db','diwork_billcenter','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_building','2312-bip','db','diwork_building','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_einvoice_auth','2312-bip','db','diwork_einvoice_auth','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_income','2312-bip','db','diwork_income','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_infra','2312-bip','db','diwork_infra','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_input_tax','2312-bip','db','diwork_input_tax','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_invoice_bd','2312-bip','db','diwork_invoice_bd','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_other_tax','2312-bip','db','diwork_other_tax','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_piaoeda_client','2312-bip','db','diwork_piaoeda_client','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_report','2312-bip','db','diwork_report','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_taxability','2312-bip','db','diwork_taxability','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_taxpubdoc','2312-bip','db','diwork_taxpubdoc','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('diwork_taxspec','2312-bip','db','diwork_taxspec','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('idoc','2312-bip','db','idoc','tax');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('trustedsign_v5','2312-bip','db','trustedsign_v5','trst');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_hy_doccrbd','2312-bip','db','yonbip_hy_doccrbd','trst');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('crm','2312-bip','db','crm','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('omsdata','2312-bip','db','omsdata','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('pricecenter','2312-bip','db','pricecenter','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('quartz','2312-bip','db','quartz','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('sddcdata','2312-bip','db','sddcdata','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('udm','2312-bip','db','udm','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('uorders','2312-bip','db','uorders','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('upmalls','2312-bip','db','upmalls','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('uretaildata','2312-bip','db','uretaildata','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('usmp','2312-bip','db','usmp','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yilian','2312-bip','db','yilian','yonbip-mkt');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('pmcloud','2312-bip','db','pmcloud','yonbip-pm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('pmcloud_ods','2312-bip','db','pmcloud_ods','yonbip-pm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('pca','2312-bip','db','pca','mm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('uinttrade','2312-bip','db','uinttrade','scm');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_fi_ctmfm','2312-bip','db','yonbip_fi_ctmfm','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_fi_ctmfm_init','2312-bip','db','yonbip_fi_ctmfm_init','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_fi_ctmlc','2312-bip','db','yonbip_fi_ctmlc','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_fi_ctmlc_init','2312-bip','db','yonbip_fi_ctmlc_init','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_fi_ctmpub_init','2312-bip','db','yonbip_fi_ctmpub_init','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('yonbip_fi_merge_schema','2312-bip','db','yonbip_fi_merge_schema','fi');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('iuap_installer','2312-bip','db','iuap_installer','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('logger','2312-bip','db','logger','iuap');",
            "insert into md_component (id,ddc_version,model_type,name,own_module) values ('sys','2312-bip','db','sys','iuap');"};

        try
        {
            sqlExecutorTarget.executeUpdate(strModuleSQLs);
            sqlExecutorTarget.executeUpdate(strComponentSQLs);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
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
     * @param strDefaultTableName
     * @return true：表名合法；false：不合法
     * @author Rocex Wang
     * @since 2021-11-17 19:07:50
     ***************************************************************************/
    protected boolean isTableNameValid(String strDefaultTableName)
    {
        // 表名长度小于6、不包含下划线、要排除的 都认为不是合法要生成数据字典的表
        // boolean blValid = strDefaultTableName.length() > 6 && strDefaultTableName.contains("_");
        //
        // if (!blValid)
        // {
        // return false;
        // }

        for (String strTableFilter : strTableFilters)
        {
            if (strDefaultTableName.startsWith(strTableFilter))
            {
                return false;
            }
        }

        boolean blValid = patternTableFilter.matcher(strDefaultTableName).matches();

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
    protected List<? extends MetaVO> queryMetaVO(SQLExecutor sqlExecutorSource, Class<? extends MetaVO> metaVOClass, String strSQL, SQLParameter param, IAction pagingAction)
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
    protected void syncDBField(SQLExecutor sqlExecutorSource, String strDBCatalog, String strDBSchema, Map<String, String> mapTableNameAndId) throws SQLException
    {
        String strMsg = "sync all field from schema [%s]".formatted(strDBSchema);
        Logger.getLogger().begin(strMsg);

        // 表名和属性最大顺序号
        Map<String, Integer> mapSequence = new HashMap<>();

        IAction pagingFieldAction = evt ->
        {
            List<PropertyVO> listVO = (List<PropertyVO>) evt.getSource();

            for (PropertyVO propertyVO : listVO)
            {
                propertyVO.setClassId(mapTableNameAndId.get(propertyVO.getClassId().toLowerCase()));

                String strPropLowerName = propertyVO.getName().toLowerCase();

                propertyVO.setDataTypeStyle(ClassVO.ClassType.db.value());
                propertyVO.setName(strPropLowerName);
                propertyVO.setDdcVersion(strVersion);
                propertyVO.setId(StringHelper.getId());
                propertyVO.setDataTypeSql(getDataTypeSql(propertyVO));
                propertyVO.setDefaultValue(StringHelper.isEmpty(propertyVO.getDefaultValue()) ? null : propertyVO.getDefaultValue().toLowerCase());
                propertyVO.setDisplayName(StringHelper.isEmpty(propertyVO.getRemarks()) ? strPropLowerName : propertyVO.getRemarks().replace("\r\n", "").replace("\n", ""));

                Integer iSequence = mapSequence.get(propertyVO.getClassId());

                if (iSequence == null)
                {
                    iSequence = 0;
                }

                propertyVO.setAttrSequence(++iSequence);

                mapSequence.put(propertyVO.getClassId(), iSequence);
            }

            try
            {
                sqlExecutorTarget.insertVO(listVO.toArray(new PropertyVO[0]));
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        };

        Map<String, String> mapColumn = new HashMap<>();
        mapColumn.put("COLUMN_NAME", "Name");
        mapColumn.put("COLUMN_SIZE", "AttrLength");
        mapColumn.put("COLUMN_DEF", "DefaultValue");
        mapColumn.put("TABLE_NAME", "ClassId");
        mapColumn.put("TYPE_NAME", "DataTypeSql");
        mapColumn.put("DECIMAL_DIGITS", "Precise");
        mapColumn.put("NULLABLE", "Nullable");

        BeanListProcessor<? extends MetaVO> processor = new BeanListProcessor<>(PropertyVO.class, mapColumn, propertyVO ->
        {
            String strDefaultTableName = propertyVO.getClassId().toLowerCase();

            return listNeedSyncTableName.contains(strDefaultTableName) && isTableNameValid(strDefaultTableName);
        });

        // 一次性查出所有表的字段
        try (Connection connection = sqlExecutorSource.getConnection();
             ResultSet rsColumns = connection.getMetaData().getColumns(strDBCatalog, strDBSchema, null, null);)
        {
            processor.setPagingAction(pagingFieldAction);

            processor.doAction(rsColumns);
        }

        Logger.getLogger().end(strMsg);
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

        ExecutorService executorService = Executors.newFixedThreadPool(ResHelper.getThreadCount());

        for (String strSrcDBSchema : strSrcDBSchemas)
        {
            executorService.execute(() ->
            {
                Properties dbPropSource2 = (Properties) dbPropSource.clone();
                dbPropSource2.setProperty("jdbc.url", strSourceUrl.replace("${schema}", strSrcDBSchema));

                try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2);
                     Connection connection = sqlExecutorSource.getConnection();)
                {
                    String strSrcDBCatalog = connection.getCatalog();

                    initTableFiltersWithTempTableName(sqlExecutorSource);

                    Map<String, String> mapTableNameId = syncDBTable(sqlExecutorSource, strSrcDBCatalog, strSrcDBSchema, strSrcDBSchema);

                    syncDBField(sqlExecutorSource, strSrcDBCatalog, strSrcDBSchema, mapTableNameId);
                }
                catch (SQLException ex)
                {
                    Logger.getLogger().error(ex.getMessage(), ex);
                }
            });
        }

        executorService.shutdown();

        while (!executorService.isTerminated())
        {
            ResHelper.sleep(100);
        }

        initBipModules();

        Logger.getLogger().end("sync database meta");
    }

    /***************************************************************************
     * 查询所有表
     * @author Rocex Wang
     * @since 2020-5-11 11:19:19
     * @throws Exception
     ***************************************************************************/
    protected Map<String, String> syncDBTable(SQLExecutor sqlExecutorSource, String strDBCatalog, String strDBSchema, String strComponentId) throws SQLException
    {
        String strMsg = "sync all table from schema [%s]".formatted(strDBSchema);

        Logger.getLogger().begin(strMsg);

        Map<String, String> mapTable = new HashMap<>();
        mapTable.put("TABLE_NAME", "DefaultTableName");
        mapTable.put("TABLE_TYPE", "ClassListUrl"); // 用 ClassListUrl 代替了，反正 ClassListUrl 也不保存

        Map<String, String> mapPrimaryKey = new HashMap<>();
        mapPrimaryKey.put("COLUMN_NAME", "name");
        mapPrimaryKey.put("KEY_SEQ", "AttrSequence");

        IAction pagingAction = evt ->
        {
            List<MetaVO> listVO = (List<MetaVO>) evt.getSource();

            try
            {
                sqlExecutorTarget.insertVO(listVO.toArray(new MetaVO[0]));
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        };

        Map<String, String> mapTableNameAndId = new HashMap<>();

        BeanListProcessor<ClassVO> processor = new BeanListProcessor<ClassVO>(ClassVO.class, mapTable, classVO ->
        {
            String strDefaultTableName = classVO.getDefaultTableName().toLowerCase();

            if (!"table".equalsIgnoreCase(classVO.getClassListUrl()) || !isTableNameValid(strDefaultTableName) || listNeedSyncTableName.contains(strDefaultTableName))
            {
                return false;
            }

            listNeedSyncTableName.add(strDefaultTableName);

            classVO.setClassType(ClassVO.ClassType.db.value());
            classVO.setDdcVersion(strVersion);
            classVO.setId(StringHelper.getId());
            classVO.setComponentId(strComponentId);
            classVO.setName(strDefaultTableName);
            classVO.setDefaultTableName(strDefaultTableName);
            classVO.setModelType(ModelType.db.name());
            classVO.setDisplayName(
                propCodeName.getProperty(strDefaultTableName, StringHelper.isEmpty(classVO.getRemarks()) ? "" : classVO.getRemarks().replace("\r\n", "").replace("\n", "")));

            mapTableNameAndId.put(classVO.getDefaultTableName(), classVO.getId());

            try
            {
                String strPrimaryKeys = getPrimaryKeys(sqlExecutorSource, strDBCatalog, strDBSchema, strDefaultTableName, mapPrimaryKey);

                classVO.setKeyAttribute(strPrimaryKeys);
            }
            catch (Exception ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }

            return true;
        }, "DefaultTableName", "ClassListUrl", "Remarks");

        try (Connection connection = sqlExecutorSource.getConnection();
             ResultSet rsTable = connection.getMetaData().getTables(strDBCatalog, strDBSchema, "%", new String[]{"TABLE"});)
        {
            processor.setPagingAction(pagingAction);
            processor.doAction(rsTable);
        }

        Logger.getLogger().end(strMsg);

        return mapTableNameAndId;
    }

    protected void syncMetaData(SQLExecutor sqlExecutorSource, String strModuleSQL, String strComponentSQL, String strClassSQL, String strPropertySQL, String strEnumAsClass,
        String strEnumValueSQL)
    {
        Logger.getLogger().begin("sync bip metadata");

        IAction pagingAction = evt ->
        {
            List<MetaVO> listVO = (List<MetaVO>) evt.getSource();

            for (MetaVO metaVO : listVO)
            {
                if (metaVO.getId() == null)
                {
                    metaVO.setId(StringHelper.getId());
                }

                if (metaVO.getDisplayName() != null)
                {
                    metaVO.setDisplayName(metaVO.getDisplayName().replace("\r\n", "").replace("\n", ""));
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
        };

        IAction pagingPropertyAction = evt ->
        {
            List<PropertyVO> listVO = (List<PropertyVO>) evt.getSource();

            for (PropertyVO propertyVO : listVO)
            {
                propertyVO.setId(StringHelper.getId());
                propertyVO.setDataTypeSql(getDataTypeSql(propertyVO));
            }

            try
            {
                sqlExecutorTarget.insertVO(listVO.toArray(new MetaVO[0]));
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        };

        queryMetaVO(sqlExecutorSource, ModuleVO.class, strModuleSQL, null, pagingAction);
        queryMetaVO(sqlExecutorSource, ComponentVO.class, strComponentSQL, null, pagingAction);
        queryMetaVO(sqlExecutorSource, ClassVO.class, strClassSQL, null, pagingAction);
        queryMetaVO(sqlExecutorSource, PropertyVO.class, strPropertySQL, null, pagingPropertyAction);

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
            "select distinct own_module as id,own_module as display_name,null as help,own_module as name,null as parent_module_id,null as version_type" + strOtherSQL2 +
                " from md_meta_component where ytenant_id='0' and own_module is not null and own_module<>'' order by own_module";

        String strComponentSQL = "select id,null as biz_model,display_name,null as help,name,null as namespace,own_module,null as version,null as version_type" + strOtherSQL2 +
            " from md_meta_component where ytenant_id='0' and own_module is not null and own_module<>'' order by own_module";

        String strClassSQL =
            "select a.id,null as accessor_classname,null as authen,null as biz_itf_imp_classname," + ClassVO.ClassType.clazz.value() + " as class_type,b.id as component_id" +
                ",table_name as default_table_name,a.display_name,'' as full_classname,null as help,null as key_attribute,a.name,null as own_module,null as primary_class" +
                ",null as ref_model_name,null as return_type,null as version_type" + strOtherSQL2 +
                " from md_meta_class a left join md_meta_component b on b.ytenant_id='0' and a.meta_component_uri=b.uri" +
                " where a.ytenant_id='0' and a.meta_component_uri is not null and a.uri in (select object_uri from md_attribute where ytenant_id='0') order by meta_component_uri";

        String strEnumAsClass = "select a.id,null as accessor_classname,null as authen,null as biz_itf_imp_classname," + ClassVO.ClassType.enumeration.value() +
            " as class_type,b.id as component_id,null as default_table_name" +
            ",a.display_name,null as full_classname,null as help,null as key_attribute,null as model_type,a.name,null as own_module,null as primary_class" +
            ",null as ref_model_name,null as return_type,null as version_type" + strOtherSQL2 +
            " from md_enumeration a left join md_meta_component b on b.ytenant_id='0' and a.meta_component_uri=b.uri" +
            " where a.ytenant_id='0' and a.uri in (select enumeration_uri from md_enumeration_literal where ytenant_id='0')";

        String strPropertySQL =
            "select a.id,null as accessor_classname,0 as access_power,0 as access_power_group,length as attr_length,max_value as attr_max_value,min_value as attr_min_value" +
                ",0 as attr_sequence,0 as calculation,b.id as class_id,0 as custom_attr,biz_type as data_type,'' as data_type_sql,0 as data_type_style" +
                ",default_value,a.display_name,0 as dynamic_attr,null as dynamic_table,0 as fixed_length,null as help,0 as hidden,0 as key_prop,a.name,0 as not_serialize" +
                ",is_nullable as nullable,precise,0 as read_only,ref_meta_class_uri as ref_model_name,null as version_type" + strOtherSQL1 +
                " from md_attribute a left join md_meta_class b on a.object_uri=b.uri and b.ytenant_id='0'" +
                " where a.ytenant_id='0' and a.biz_type is not null and object_uri in(select uri from md_meta_class where ytenant_id='0')";

        String strEnumValueSQL = "select a.id,b.id as class_id,0 as enum_sequence,code as enum_value,a.name as name,0 as version_type" + strOtherSQL1 +
            " from md_enumeration_literal a left join md_enumeration b on a.enumeration_uri=b.uri and b.ytenant_id='0'" + " where a.ytenant_id='0' order by enumeration_uri,code";

        // todo
        Properties dbPropSource2 = (Properties) dbPropSource.clone();
        dbPropSource2.setProperty("jdbc.url", "jdbc:mysql://172.20.36.73:3306/iuap_metadata_base");
        dbPropSource2.setProperty("jdbc.user", "ro_all_db");
        dbPropSource2.setProperty("jdbc.password", "RMgfkzz48R!t");

        // String strSourceUrl = Context.getInstance().getSetting(strVersion + ".jdbc.url");
        // dbPropSource2.setProperty("jdbc.url", strSourceUrl.replace("${schema}", "iuap_metadata_base"));

        try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2);)
        {
            syncMetaData(sqlExecutorSource, strModuleSQL, strComponentSQL, strClassSQL, strPropertySQL, strEnumAsClass, strEnumValueSQL);
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
            "select id as original_id,name,displayname,ownmodule,namespace,help,isbizmodel as biz_model,version,versiontype," + strOtherSQL + " from md_component";

        String strClassSQL = "select id,name,displayname,defaulttablename,fullclassname,keyattribute,componentid,classtype,isprimary,help" +
            ",accessorclassname,bizitfimpclassname,refmodelname,returntype,isauthen,versiontype," + strOtherSQL + " from md_class order by defaulttablename";

        String strPropertySQL = "select a.id original_id,a.name as name,a.displayname as displayname,attrlength,attrminvalue" +
            ",attrmaxvalue,attrsequence,customattr,datatype,datatypestyle,a.defaultvalue as defaultvalue" +
            ",a.nullable as nullable,a.precise as precise,refmodelname,classid,accesspowergroup,accessorclassname,dynamictable" +
            ",a.help,accesspower,calculation,dynamicattr,a.fixedlength,a.hided as hidden,a.notserialize,a.readonly,b.sqldatetype data_type_sql" +
            ",b.pkey key_prop,a.versiontype," + strOtherSQL +
            " from md_property a left join md_column b on a.name=b.name and b.tableid=? where classid=? order by b.pkey desc,a.attrsequence";

        String strEnumValueSQL =
            "select id as class_id,enumsequence as enum_sequence,name,value enum_value,versiontype," + strOtherSQL + " from md_enumvalue order by id,enumsequence";

        Properties dbPropSource2 = (Properties) dbPropSource.clone();

        try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2);)
        {
            syncMetaData(sqlExecutorSource, strModuleSQL, strComponentSQL, strClassSQL, strPropertySQL, null, strEnumValueSQL);
        }
    }
}
