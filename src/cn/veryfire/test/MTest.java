package cn.veryfire.test;

import cn.veryfire.ctrip.order.CtripOrder;
import org.json.JSONArray;
import org.json.JSONObject;
import weaver.general.Util;
import weaver.general.browserData.BrowserData;
import weaver.interfaces.workflow.browser.BaseBrowser;
import weaver.interfaces.workflow.browser.Browser;
import weaver.interfaces.workflow.browser.BrowserBean;

import java.util.HashMap;
import java.util.Map;

public class MTest {

    public static void main(String[] args) throws Exception {

//        HrmSyn2Ctrip hrmSyn2Ctrip = new HrmSyn2Ctrip();
//
//        Authentication authencationEntity = new Authentication();
//        authencationEntity.setValid("A");//状态
//        authencationEntity.setEmployeeID("01458");//员工编号
//        authencationEntity.setName("黎春");//员工姓名
//        authencationEntity.setDept1("信息管理部");//部门
//        authencationEntity.setDept2("上海开能环保设备股份有限公司");//部门
//        authencationEntity.setRankName("C");//职级
//        authencationEntity.setEmail("lichun@canature.com");//邮箱
//
//        //authencationEntity.setConfirmPersonList(getConfirmPersonList("liuhuiming@canature.com"));
//
//        authencationEntity.setSubAccountName("KNHB_提前审批授权");//子账户
//        authencationEntity.setIsSendEMail(true);//是否发送邮件
//
//
//        hrmSyn2Ctrip.synHrmresource2Ctrip(authencationEntity);


        // CorpTicket getCorpTicket = new CorpTicket();


        // HrmSyn2Ctrip hrm = new HrmSyn2Ctrip();
        // hrm.setAppKey(appKey);
        // hrm.setTicket(getCorpTicket.getEmployeeSyncTicket(appKey,
        // appSecurity, version).getTicket());
        // hrm.setAppSecurity(appSecurity);
        // hrm.setVersion(version);
        // hrm.setCorporationID("KNHB");
        // hrm.setTicket();
        // System.err.println(hrm.getTicket());
        //
        // hrm.multipleEmployeeSync();
        // //////////////////////////////////
        // SetApprovalAction setApp = new SetApprovalAction(appKey,
        // appSecurity);
        // ArrayList<HashMap<String, String>> detailTableDataMap = new
        // ArrayList<HashMap<String, String>>();
        // HashMap<String, String> hashMap = new HashMap<>();
        // hashMap.put("ccry", "黎春");
        // hashMap.put("cfrq", "2018-05-10");
        //
        // hashMap.put("cfcs", "北京");
        // hashMap.put("ddcs", "西安");
        //
        //
        // hashMap.put("price", "100");
        // hashMap.put("ccrs", "1");
        // detailTableDataMap.add(hashMap);
        //
        // String ticket = getCorpTicket.getSetApproval(appKey,
        // appSecurity).getTicket();
        //
        // SetApprovalServiceRequest requestSetApp =
        // setApp.getRequestSetApp(null, null,detailTableDataMap,ticket);
        //
        // setApp.setApproval(requestSetApp);
        // /////////////////////////////
//        HrmSyn2Ctrip hrm = new HrmSyn2Ctrip();
//
//         Authentication authencationEntity = new Authentication();
//         authencationEntity.setEmployeeID("01189");
//         authencationEntity.setName("孙亚冬");
//         authencationEntity.setValid("A");
//
//         authencationEntity.setMobilePhone("13501879845");
//         authencationEntity.setCostCenter("信息管理部");//成本中心
//
//         authencationEntity.setCostCenter2("信息管理部");//差旅类型
//         authencationEntity.setCostCenter3("信息管理部");//项目
//
//         //authencationEntity.setConfirmPerson("w00000@ctrip.com");
//
//
//         authencationEntity.setSubAccountName("KNHB_现结提前审批授权");
//
//         hrm.synHrmresource2Ctrip(authencationEntity);


        // OrderSearchTicketResponse employeeSyncTicket =
        // GetSSOTicket.getEmployeeSyncTicket(appKey, appSecurity, version);
        // String ticket = "5ae2f8ac73fa2e401400199f";
        // System.err.println(ticket);
        //
        // AuthenticationListRequst requst = GetCorpTicket
        // .buildAuthenticationListRequst(ticket);
        // System.err.println(requst.getAuthenticationInfoList().get(0).getAuthentication().getName());
        //
        //
        // GetCorpTicket.getSSOLoginTicket(appKey, appSecurity, version, 1,
        // 314);

        // String url = "https://ct.ctrip.com/SwitchAPI/Order/Ticket";
        // String data = HttpUtil.PostData(url, "{\"appKey\":\"" + appSecurity
        // + "\",\"appSecurity\":\"" + appSecurity + "\"}");
        // JSONObject json = (JSONObject) JSON.parse(data);
        //
        // String ticket = (String) json.get("Ticket");
        // System.err.println(ticket);

        // System.err.println("=============================");
        // GetCorpTicket gerCorpRicket = new GetCorpTicket();
        // OrderSearchTicketResponse employeeSyncTicket = gerCorpRicket
        // .getEmployeeSyncTicket(appKey, appSecurity, version);
        // String ticket2 = employeeSyncTicket.getTicket();

        // OrderSearchTicketResponse orderSearchTicket =
        // getCorpTicket.getOrderSearchTicket(appKey, appSecurity, version);

    /*    CtripOrder ctripOrder = new CtripOrder("obk_KNHB", "obk_KNHB", "1.0");
        String searchOrders = ctripOrder.SearchOrders("XC201808090032",
                "", "");


        JSONObject arry = new JSONObject(searchOrders);
        JSONObject jsonval = (JSONObject) arry.get("Status");

        JSONArray ItineraryList = arry.getJSONArray("ItineraryList");
        for (int orderIndex = 0; orderIndex < ItineraryList.length(); orderIndex++) {
            JSONObject Itinerary = (JSONObject) ItineraryList.get(orderIndex);
            JSONArray flightOrderInfoList = null;
            if (!Itinerary.get("FlightOrderInfoList").toString().equals("null")) {
                flightOrderInfoList = Itinerary.getJSONArray("FlightOrderInfoList");
            }
            if (flightOrderInfoList != null && flightOrderInfoList.length() > 0) {
                for (int j = 0; j < flightOrderInfoList.length(); j++) {
                    JSONObject flightOrderInfo = flightOrderInfoList.getJSONObject(j);
                    JSONObject basicInfo = (JSONObject) flightOrderInfo.get("BasicInfo");

                }

            }
        }*/
    }

}
