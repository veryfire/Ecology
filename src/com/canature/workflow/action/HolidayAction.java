package com.canature.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 休假申请单流程节点后Action
 *
 * @author lichu
 * @since 1.6
 */
public class HolidayAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("进入请假申请节点后action:" + requestInfo.getRequestid(), HolidayAction.class);
        String requestid = requestInfo.getRequestid();
        Property[] mainProperty = requestInfo.getMainTableInfo().getProperty();
        Map<String, String> mainMap = new HashMap<String, String>();
        for (Property propertyName : mainProperty) {
            mainMap.put(propertyName.getName(), propertyName.getValue());
        }
        String managerid = mainMap.get("zzbzjl");
        String zsss = mainMap.get("zsss");
        String value = "";
        if (!"".equals(managerid) && !"".equals(zsss)) {
            if (managerid.equals(zsss)) {
                value = "0";
            } else {
                value = "1";
            }
            updatePDValue(requestid, value);
        } else {
            RequestManager requestManager = requestInfo.getRequestManager();
            requestManager.setMessageid(requestid);
            requestManager.setMessagecontent("直接上级或者休假审批人为空,请联系OA人员!");
        }
        return SUCCESS;
    }

    /**
     * 更新流程审批判断的value
     *
     * @param requestId 流程请求ID
     * @param value     要更新的值 相同 0  不同 1
     */
    public void updatePDValue(String requestId, String value) {
        RecordSet recordSet = new RecordSet();
        String sql = " update formtable_main_14 set pd1 = '" + value + "' where requestid = " + requestId;
        recordSet.execute(sql);
        writeLog("update formtable_main_14 pd1 is completed!" + sql, HolidayAction.class);
    }
}
