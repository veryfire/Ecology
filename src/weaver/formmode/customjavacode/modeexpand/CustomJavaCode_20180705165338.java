package weaver.formmode.customjavacode.modeexpand;

import cn.veryfire.ctrip.hrm.HrmSyn2Ctrip;
import weaver.conn.RecordSet;
import weaver.formmode.customjavacode.AbstractModeExpandJavaCode;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class CustomJavaCode_20180705165338 extends AbstractModeExpandJavaCode {
    BaseBean baseBean = new BaseBean();

    /**
     * 执行模块扩展动作
     *
     * @param param param包含(但不限于)以下数据
     *              user 当前用户
     */
    public void doModeExpand(Map<String, Object> param) throws Exception {
        User user = (User) param.get("user");
        String billid = "";//数据id
        int modeid = -1;//模块id
        RequestInfo requestInfo = (RequestInfo) param.get("RequestInfo");
        if (requestInfo != null) {
            billid = Util.null2String(requestInfo.getRequestid());
            modeid = Util.getIntValue(requestInfo.getWorkflowid());
            if (!"".equals(billid) && modeid > 0) {
                RecordSet rs = new RecordSet();
                //------请在下面编写业务逻辑代码------
                HrmSyn2Ctrip hrmSyn2Ctrip = new HrmSyn2Ctrip();
                String sql = "select id from hrmresource where workcode = '" + billid + "'";
                rs.execute(sql);
                if (rs.next()) {
                    hrmSyn2Ctrip.synHrm2CrtipByWorkCodeIds(rs.getString(1));
                }
            }
        }
    }

}

