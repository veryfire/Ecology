package cn.veryfire.ctrip.hrm;

import cn.veryfire.ctrip.ticket.CorpTicket;
import cn.veryfire.ctrip.util.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import corp.openapicalls.contract.employee.*;
import corp.openapicalls.service.employee.MultipleEmployeeSyncService;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 人事信息批量跟新 从OA到携程
 *
 * @author lichu
 */
public class HrmSyn2Ctrip extends BaseBean {
    private String ticket;
    private CorpTicket getCorpTicket = new CorpTicket();
    private String appKey;
    private String appSecurity;
    private String version;
    private String corporationID;
    private String subAccountName = "KNHB_提前审批授权";

    public HrmSyn2Ctrip() {
        this.appKey = getPropValue("ctripInfo", "appKey");
        this.appSecurity = getPropValue("ctripInfo", "appSecurity");
        this.version = getPropValue("ctripInfo", "version");
        this.corporationID = getPropValue("ctripInfo", "corporationID");
        this.ticket = this.getCorpTicket.getEmployeeSyncTicket(this.appKey, this.appSecurity, this.version).getTicket();
        //this.subAccountName = getPropValue("ctripInfo", "SubAccountName");
        writeLog("crtip params----->:appKey:" + appKey + ",appSecurity:" + appSecurity + ",version:" + version + ",corporationID:" + corporationID + ",subAccountName:" + this.subAccountName);
    }

    // 判断员工是否在携程中开卡
    public boolean isOpenCard(String Ticket, String EmployeeID) {
        boolean flag = false;
        String param = "{\"Ticket\":\"" + Ticket + "\",\"CorpID\":\"" + this.corporationID
                + "\",\"EmployeeID\":\"" + EmployeeID + "\"}";
        String url = "https://ct.ctrip.com/corpservice/OpenCard/IsOpenedCard?type=json";
        String postData = HttpUtil.PostData(url, param);
        JSONObject json = (JSONObject) JSON.parse(postData);
        flag = (Boolean) json.get("IsOpened");
        return flag;
    }

    // 获取OA系统中的人员信息,构造批量更新的实体类
    // 构造批量更新实体
    protected AuthenticationListRequst buildAuthenticationListRequst(String ticket, boolean isSendConfirm) {
        AuthenticationListRequst authenticationListRequst = new AuthenticationListRequst();
        authenticationListRequst.setTicket(ticket);
        authenticationListRequst.setCorporationID(this.corporationID);

        String ctripSql = "select lastname,workcode,email,status,levels,dept,dept2,subaccountname,manageremail from ctriphrm";
        RecordSet rs = new RecordSet();
        writeLog("synHrm sql:" + ctripSql, HrmSyn2Ctrip.class);
        rs.execute(ctripSql);
        List<AuthenticationInfoList> authenticationInfoLists = new ArrayList<AuthenticationInfoList>();
        String lastname = "";
        String workcode = "";
        String email = "";
        String status = "";
        String levels = "";
        String dept2 = "";
        String subaccountname = "";
        String manageremail = "";
        while (rs.next()) {
            AuthenticationInfoList authenticationInfoList = new AuthenticationInfoList();
            Authentication authencationEntity = new Authentication();

            lastname = rs.getString("lastname");
            workcode = rs.getString("workcode");
            email = rs.getString("email");
            status = rs.getString("status");
            levels = rs.getString("levels");
            dept2 = rs.getString("dept2");
            subaccountname = rs.getString("subaccountname");
            manageremail = rs.getString("manageremail");

            authencationEntity.setValid(status);//状态
            authencationEntity.setEmployeeID(workcode);//员工编号
            authencationEntity.setName(lastname);//员工姓名
            authencationEntity.setDept1(dept2);//部门
            authencationEntity.setDept2(subaccountname);//部门
            authencationEntity.setRankName(levels);//职级
            authencationEntity.setEmail(email);//邮箱
            if (isSendConfirm && !"".equals(manageremail)) {
                authencationEntity.setConfirmPersonList(getConfirmPersonList(manageremail));
            }
            authencationEntity.setSubAccountName(this.subAccountName);//子账户
            authencationEntity.setIsSendEMail(true);//是否发送邮件
            authenticationInfoList.setAuthentication(authencationEntity);
            authenticationInfoList.setSequence(workcode);
            authenticationInfoLists.add(authenticationInfoList);
        }
        authenticationListRequst.setAppkey(this.appKey);
        authenticationListRequst.setAuthenticationInfoList(authenticationInfoLists);
        writeLog("同步人员实体信息,isSendConfirm:" + isSendConfirm + "    " + JSON.toJSONString(authenticationListRequst), HrmSyn2Ctrip.class);
        return authenticationListRequst;
    }

    /**
     * 批量更新人事信息到携程
     * 需要传入两次
     * 第一次传入基本信息
     * 第二次传入授权人相关信息
     *
     * @return void 返回类型
     * @throws
     * @Title: multipleEmployeeSync
     * @Description: 批量人事更新
     */
    public void multipleEmployeeSync() {
        MultipleEmployeeSyncService empoyeeSyncService = new MultipleEmployeeSyncService();
        /**
         * 第一次先构造构造没有授权人的实体，传入过去，第一次构造有授权人的信息用来更新数据
         */
        AuthenticationListRequst authenticationListRequstNoConfirm = buildAuthenticationListRequst(this.ticket, false);//没有授权人
        AuthenticationListRequst authenticationListRequstAndConfirm = buildAuthenticationListRequst(this.ticket, true);//有授权人

        AuthenticationInfoListResponse authenticationInfoListResponseNoConfirm = empoyeeSyncService.MultipleEmployeeSync(authenticationListRequstNoConfirm);
        if (authenticationInfoListResponseNoConfirm != null) {
            String result = authenticationInfoListResponseNoConfirm.getResult();
            if (result.equals("Failed")) {
                writeLog("synHrmResource no base result :" + JSON.toJSONString(authenticationInfoListResponseNoConfirm), HrmSyn2Ctrip.class);
                return;//如果传入基本信息出错，直接返回，不在继续传入授权相关信息
            }
        }
        AuthenticationInfoListResponse authenticationInfoListResponseAndConfirm = empoyeeSyncService.MultipleEmployeeSync(authenticationListRequstAndConfirm);
        if (authenticationInfoListResponseAndConfirm != null) {
            String result = authenticationInfoListResponseAndConfirm.getResult();
            if (result.equals("Failed")) {
                writeLog("synHrmResource and confirm result :" + JSON.toJSONString(authenticationInfoListResponseAndConfirm), HrmSyn2Ctrip.class);
            }
        }

    }

    /**
     * 测试类
     * 构造实体，更新单个人员信息
     *
     * @param authencationEntity
     */
    public void synHrmresource2Ctrip(Authentication authencationEntity) {
        AuthenticationListRequst authenticationListRequst = new AuthenticationListRequst();
        authenticationListRequst.setAppkey(this.appKey);//this.appKey
        authenticationListRequst.setTicket(this.ticket);//
        authenticationListRequst.setCorporationID(this.corporationID);//

        List<AuthenticationInfoList> authenticationInfoLists = new ArrayList<AuthenticationInfoList>();

        AuthenticationInfoList authenticationInfo = new AuthenticationInfoList();
        authenticationInfo.setAuthentication(authencationEntity);
        authenticationInfo.setSequence(authencationEntity.getEmployeeID());
        authenticationInfoLists.add(authenticationInfo);
        authenticationListRequst
                .setAuthenticationInfoList(authenticationInfoLists);
        MultipleEmployeeSyncService empoyeeSyncService = new MultipleEmployeeSyncService();
        System.out.println(JSON.toJSONString(authenticationListRequst));
        AuthenticationInfoListResponse authenticationInfoListResponse = empoyeeSyncService.MultipleEmployeeSync(authenticationListRequst);
        if (authenticationInfoListResponse != null) {
            String result = authenticationInfoListResponse.getResult();

            if (result.equals("Failed")) {
                List<ErrorMessage> errorMessages = authenticationInfoListResponse.getErrorMessageList();
                if (errorMessages != null && errorMessages.size() > 0) {
                    for (ErrorMessage errorMessage : errorMessages) {
                        System.out.println(JSONObject.toJSONString(errorMessage));
                    }
                }
            }
        }
    }


    /**
     * 根据工号更新人员到携程
     *
     * @param workcodes
     * @return
     */
    public ArrayList<HashMap<String, String>> synHrm2CrtipByWorkCodeIds(String workcodes, boolean isSendConfirm) {
        writeLog("workcodes===========" + workcodes);
        if ("".equals(workcodes)) {
            return null;
        }
        String[] workcodeArr = null;
        StringBuffer sb = new StringBuffer();
        if (workcodes.contains(",")) {
            workcodeArr = workcodes.split(",");
            for (int i = 0; i < workcodeArr.length; i++) {
                if (i < workcodeArr.length - 1) {
                    sb.append("'").append(workcodeArr[i]).append("',");
                } else {
                    sb.append("'").append(workcodeArr[i]).append("'");
                }
            }
        } else {
            sb = new StringBuffer(workcodes);
        }
        if ("".equals(sb.toString())) {
            return null;
        }

        ArrayList<HashMap<String, String>> synList = new ArrayList<HashMap<String, String>>();
        AuthenticationListRequst authenticationListRequst = new AuthenticationListRequst();
        authenticationListRequst.setAppkey(this.appKey);
        authenticationListRequst.setTicket(this.ticket);
        authenticationListRequst.setCorporationID(this.corporationID);
        List<AuthenticationInfoList> authenticationInfoLists = new ArrayList<AuthenticationInfoList>();
        String ctripSql = "select lastname,workcode,email,status,levels,dept,dept2,subaccountname,manageremail from ctriphrm where workcode in (select workcode from hrmresource where id in(" + sb.toString() + "))";
        writeLog("ctripSql===========" + ctripSql);
        RecordSet rs = new RecordSet();
        rs.execute(ctripSql);
        String lastname = "";
        String workcode = "";
        String email = "";
        String status = "";
        String levels = "";
        String dept2 = "";
        String subaccountname = "";
        String manageremail = "";
        while (rs.next()) {
            lastname = rs.getString("lastname");
            workcode = rs.getString("workcode");
            email = rs.getString("email");
            status = rs.getString("status");
            levels = rs.getString("levels");
            dept2 = rs.getString("dept2");
            subaccountname = rs.getString("subaccountname");
            manageremail = rs.getString("manageremail");

            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("lastname", lastname);
            hashMap.put("workcode", workcode);
            hashMap.put("email", email);
            hashMap.put("status", status);
            hashMap.put("levels", levels);
            hashMap.put("dept2", dept2);
            hashMap.put("subaccountname", subaccountname);
            hashMap.put("manageremail", manageremail);
            synList.add(hashMap);
            AuthenticationInfoList authenticationInfo = new AuthenticationInfoList();
            Authentication authencationEntity = new Authentication();
            authencationEntity.setValid(status);//状态
            authencationEntity.setEmployeeID(workcode);//员工编号
            authencationEntity.setName(lastname);//员工姓名
            authencationEntity.setDept1(dept2);//部门
            authencationEntity.setDept2(subaccountname);//开票公司
            authencationEntity.setRankName(levels);//职级
            authencationEntity.setEmail(email);//邮箱

            authencationEntity.setSubAccountName(this.subAccountName);//子账户
            authencationEntity.setIsSendEMail(true);//是否发送邮件
            authenticationInfo.setAuthentication(authencationEntity);
            authenticationInfo.setSequence(workcode);
            authenticationInfoLists.add(authenticationInfo);
        }
        authenticationListRequst.setAuthenticationInfoList(authenticationInfoLists);
        writeLog("authenticationInfoListResponse:" + JSON.toJSONString(authenticationListRequst), HrmSyn2Ctrip.class);
        MultipleEmployeeSyncService empoyeeSyncService = new MultipleEmployeeSyncService();

        AuthenticationInfoListResponse authenticationInfoListResponse = empoyeeSyncService.MultipleEmployeeSync(authenticationListRequst);
        if (authenticationInfoListResponse != null) {
            writeLog("authenticationInfoListResponse:" + JSON.toJSONString(authenticationInfoListResponse), HrmSyn2Ctrip.class);
            String result = authenticationInfoListResponse.getResult();
            if (result.equals("Failed")) {
                List<ErrorMessage> errorMessages = authenticationInfoListResponse.getErrorMessageList();
                if (errorMessages != null && errorMessages.size() > 0) {
                    for (ErrorMessage errorMessage : errorMessages) {
                        writeLog("syn result===============:" + JSONObject.toJSONString(errorMessage), HrmSyn2Ctrip.class);
                    }
                }
                return null;
            }
        }
        return synList;
    }

    public List<ConfirmPersonEntity> getConfirmPersonList(String email) {
        List<ConfirmPersonEntity> confirmPersonEntities = new ArrayList<ConfirmPersonEntity>();
        String[] confirmProductType = new String[]{"N", "I", "H", "T"};
        for (int i = 0; i < 2; i++) {
            for (String name : confirmProductType) {
                ConfirmPersonEntity confirmPersonEntity = new ConfirmPersonEntity();
                confirmPersonEntity.setProductType(name);
                confirmPersonEntity.setAuthorizedTime(i + 1);
                confirmPersonEntity.setConfirmPerson(email);
                confirmPersonEntity.setConfirmPersoncc(email);
                confirmPersonEntities.add(confirmPersonEntity);
            }
        }
        return confirmPersonEntities;
    }
}
