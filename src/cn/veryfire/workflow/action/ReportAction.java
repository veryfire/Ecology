package cn.veryfire.workflow.action;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

import java.util.HashMap;

/**
 * 返工记录表Action
 */
public class ReportAction extends Util implements Action {
    private String tableName;
    private String requestid;
    private String isavailable = "0";

    private HashMap<String, String> mainMap = new HashMap<String, String>();

    @Override
    public String execute(RequestInfo requestInfo) {
        this.tableName = requestInfo.getRequestManager().getBillTableName();
        this.requestid = requestInfo.getRequestid();
        Property[] property = requestInfo.getMainTableInfo().getProperty();
        for (Property propertyName : property) {
            String fieldName = propertyName.getName();
            String fieldValue = propertyName.getValue();
            this.mainMap.put(fieldName, fieldValue);
        }

        System.out.println("tableName=" + tableName);
        //返工记录表
        if ("formtable_main_286".equals(tableName)) {
            RecordSetDataSource rs = new RecordSetDataSource("BigData");

            String djbh = null2String(this.mainMap.get("djbh"));
            String userid = null2String(this.mainMap.get("zdr"));
            String username = getHrmlastname(userid);
            String deptid = null2String(this.mainMap.get("fxbm"));
            String deptname = getDeptName(deptid);
            String fgcj1 = null2String(this.mainMap.get("fgcj1"));
            String fgcj1name = getSelectName("22166", fgcj1);
            String fgcj2 = null2String(this.mainMap.get("fgcj2"));
            String fgcj2name = getSelectName("22167", fgcj2);
            String fgcj3 = null2String(this.mainMap.get("fgcj3"));
            String fgcj3name = getSelectName("22168", fgcj3);
            String gzbm1 = null2String(this.mainMap.get("gzbm1"));
            String gzbm1name = getSelectName("30301", gzbm1);
            String gzbm2 = null2String(this.mainMap.get("gzbm2"));
            String gzbm2name = getSelectName("30302", gzbm2);
            String gzbm3 = null2String(this.mainMap.get("gzbm3"));
            String gzbm3name = getSelectName("30303", gzbm3);
            String rq = null2String(this.mainMap.get("zdrq"));
            String fggs = null2String(this.mainMap.get("fggs"));
            String insertSql = "INSERT INTO KN_FGJLB(requestid, djbh, userid, username, deptid, deptname," +
                    " fgcj1, fgcj1name, fgcj2, fgcj2name, fgcj3, fgcj3name, " +
                    " gzbm1, gzbm1name, gzbm2, gzbm2name, gzbm3, gzbm3name, " +
                    " fggs, isavailable,rq)" +
                    " VALUES ('" + requestid + "', '" + djbh + "', '" + userid + "', '" + username + "', '" + deptid + "', '" + deptname + "', '" +
                    fgcj1 + "', '" +
                    fgcj1name + "', '" +
                    fgcj2 + "', '" +
                    fgcj2name + "', '" +
                    fgcj3 + "', '" +
                    fgcj3name + "', '" +
                    gzbm1 + "', '" +
                    gzbm1name + "', '" +
                    gzbm2 + "', '" +
                    gzbm2name + "', '" +
                    gzbm3 + "', '" +
                    gzbm3name + "', '" +
                    fggs + "', '" +
                    isavailable +
                    "', '" +
                    rq + "')";

            rs.execute(insertSql);

        }
        //一次交检合格率
        if ("formtable_main_358".equals(tableName)) {
            RecordSetDataSource rs = new RecordSetDataSource("BigData");

            String userid = null2String(this.mainMap.get("zdr"));
            String username = getHrmlastname(userid);
            String rq = null2String(this.mainMap.get("rq"));
            String deptid = null2String(this.mainMap.get("zdbm"));
            String deptname = getDeptName(deptid);
            String djbh = null2String(this.mainMap.get("djbh"));
            String cj = null2String(this.mainMap.get("cj"));
            String cjname = getSelectName("27565", cj);

            String scsl = null2String(this.mainMap.get("scsl"));
            String bhgsl = null2String(this.mainMap.get("bhgsl"));
            String hgsl = null2String(this.mainMap.get("hgsl"));
            String blppm = null2String(this.mainMap.get("blppm"));
            String mbz = null2String(this.mainMap.get("mbz"));

            String insertSql = "INSERT INTO KN_YCJJHGL(requestid, userid, username, rq, deptid, deptname, djbh, cj, cjname, scsl, bhgsl, hgsl, blppm, mbz, isavailable) VALUES" +
                    " ('" + requestid + "', '" + userid + "', '" + username + "', '" + rq + "', '" + deptid + "', '" + deptname + "', '" + djbh + "', '" + cj + "', '" + cjname + "', '" + scsl + "', '" + bhgsl + "', '" + hgsl + "', '" + blppm + "', '" + mbz + "', '" + isavailable + "')";
            rs.execute(insertSql);
        }
        //安全金字塔流程
        if ("formtable_main_424".equals(tableName)) {
            RecordSetDataSource rs = new RecordSetDataSource("BigData");

            String userid = null2String(this.mainMap.get("sqr"));
            String username = getHrmlastname(userid);
            String zdrq = null2String(this.mainMap.get("zdrq"));
            String deptid = null2String(this.mainMap.get("zdbm"));
            String deptname = getDeptName(deptid);
            String fssgrq = null2String(this.mainMap.get("fssgrq"));
            String fsdeptid = null2String(this.mainMap.get("fssgbm"));
            String fsdeptname = getDeptName(fsdeptid);
            String sglx = null2String(this.mainMap.get("sglx"));
            String sglxname = getSelectName("32606", sglx);
            String sgyy = null2String(this.mainMap.get("sgyy"));
            String djbh = null2String(this.mainMap.get("djbh"));

            String insertSql = "INSERT INTO KN_AQJZT(requestid, userid, username," +
                    " zdrq, deptid, deptname, fssgrq, fsdeptid, fsdeptname, sglx, sglxname, sgyy, djbh, isavailable) VALUES" +
                    " ('" + requestid + "', '" + userid + "', '" + username + "', '" + zdrq + "', '" + deptid + "', '" + deptname + "', '" + fssgrq + "', '" + fsdeptid + "', '" + fsdeptname + "', '" + sglx + "', '" + sglxname + "', '" + sgyy + "', '" + djbh + "', '" + isavailable + "')";

            rs.execute(insertSql);

        }
        //库存周转率流程
        if ("formtable_main_425".equals(tableName)) {
            RecordSetDataSource rs = new RecordSetDataSource("BigData");
            String djbh = null2String(this.mainMap.get("djbh"));
            String userid = null2String(this.mainMap.get("zdr"));
            String username = getHrmlastname(userid);
            String zdrq = null2String(this.mainMap.get("zdrq"));
            String ssgs = getSubcompanyName(this.mainMap.get("ssgs"));
            String year = null2String(this.mainMap.get("nf"));
            String month = getSelectName("32614", this.mainMap.get("yf"));
            String ycl = null2String(this.mainMap.get("ycl"));
            String sp = null2String(this.mainMap.get("sp"));
            String yclzgl = null2String(this.mainMap.get("yclzgl"));
            String spzgl = null2String(this.mainMap.get("spzgl"));
            String insertSql = "INSERT INTO KN_KCZZL(requestid, djbh, userid, username, " +
                    "zdrq, ssgs, year, month, ycl, sp, yclzgl, spzgl, isavailable) VALUES " +
                    "('" + requestid + "', '" + djbh + "', '" + userid + "', '" + username + "', '" + zdrq + "', '" + ssgs + "', '" + year + "', " +
                    "'" + month + "', '" + ycl + "', '" + sp + "', '" + yclzgl + "', '" + spzgl + "', '" + isavailable + "')";
            rs.execute(insertSql);
        }
        return SUCCESS;
    }

    /**
     * 获取分公司名称
     *
     * @param subCompanyId
     * @return
     */
    public String getSubcompanyName(String subCompanyId) {
        RecordSet rs = new RecordSet();
        rs.execute("select subcompanyname from hrmsubcompany where id =" + subCompanyId);
        return rs.next() ? null2String(rs.getString(1)) : null;
    }

    /**
     * 获取人员lastname
     *
     * @param userid
     * @return
     */
    public String getHrmlastname(String userid) {
        RecordSet rs = new RecordSet();
        rs.execute("select lastname from hrmresource where id = " + userid);
        return rs.next() ? null2String(rs.getString(1)) : "";
    }

    /**
     * 获取部门名称
     *
     * @param deptid
     * @return
     */
    public String getDeptName(String deptid) {
        RecordSet rs = new RecordSet();
        rs.execute("select departmentname from hrmdepartment where id = " + deptid);
        return rs.next() ? null2String(rs.getString(1)) : "";
    }

    /**
     * 获取下拉框的值
     *
     * @param fieldId     字段id
     * @param selectValue 下拉框的index
     * @return
     */
    public String getSelectName(String fieldId, String selectValue) {
        RecordSet rs = new RecordSet();
        rs.execute("select selectname from workflow_selectitem where fieldid = " + fieldId + " and selectvalue = " + selectValue);
        return rs.next() ? null2String(rs.getString(1)) : "";
    }
}
