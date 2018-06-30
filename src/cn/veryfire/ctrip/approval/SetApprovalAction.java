package cn.veryfire.ctrip.approval;

import cn.veryfire.ctrip.ticket.CorpTicket;
import com.alibaba.fastjson.JSON;
import corp.openapicalls.contract.Authentification;
import corp.openapicalls.contract.setapproval.request.*;
import corp.openapicalls.contract.setapproval.response.SetApprovalResponse;
import corp.openapicalls.service.setapproval.SetApprovalService;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 提前审批审批单落地
 *
 * @author lichu
 */
public class SetApprovalAction extends BaseBean implements Action {

    private String appKey;
    private String appSecurity;
    private String approvalNumber;
    private Map<String, String> tableInfo;
    private ArrayList<HashMap<String, String>> detailTableDataMap1;
    private ArrayList<HashMap<String, String>> detailTableDataMap2;
    private ArrayList<HashMap<String, String>> detailTableDataMap3;
    private String fromDate;
    private String toDate;


    public SetApprovalAction() {
        this.appKey = getPropValue("ctripInfo", "appKey");
        this.appSecurity = getPropValue("ctripInfo", "appSecurity");
    }

    public String execute(RequestInfo requestInfo) {
        writeLog("==================================进入提前审批类==================================");
        CorpTicket getCorpTicket = new CorpTicket();
        String requestid = requestInfo.getRequestid();// 审批单行程号

        String workflowid = requestInfo.getWorkflowid();
        String tableName = getTableNameByWorkflowId(workflowid);
        String mainidSql = "(select id from " + tableName + " where requestid="
                + requestid + ")";
        Property[] mainProperty = requestInfo.getMainTableInfo().getProperty();

        this.tableInfo = getMainTableInfo(mainProperty);// 主表信息

        this.approvalNumber = tableInfo.get("djbh");// 设置行程号

        this.fromDate = this.tableInfo.get("ccksrq");
        this.toDate = this.tableInfo.get("ccjsrq");


        /**
         * 明细1 航班信息
         *
         * ccry 出差人员 ccrs 出差人数 ydlx 预定类型 cfrq 出发日期 fcrq 返程日期 cfcs 出发城市 ddcs 到达城市
         * cw 舱位 price 价格 hblx 航班类型 bz 备注
         *
         */
        String sqlDetail1 = "select ccry,ccrs,ydlx,cfrq,fcrq,cfcs,ddcs,cw,price,hblx,bz from "
                + tableName + "_dt1 where mainid =" + mainidSql;
        this.detailTableDataMap1 = getDetailTableDataMap(sqlDetail1);

        /**
         * 明细2 酒店 rzr 入住人 rzrs 入住人数 ydlx 预定类型 rzrq 入住日期 ldrq 离店日期 rzcs 入住城市
         * price 价格 fjsl 房间数量 jdxj 酒店星级 bz 备注
         */
        String sqlDetail2 = "select rzr,rzrs,ydlx,rzrq,ldrq,rzcs,price,fjsl,jdxj,bz from "
                + tableName + "_dt2 where mainid = " + mainidSql;
        this.detailTableDataMap2 = getDetailTableDataMap(sqlDetail2);

        /**
         * 明细3火车 ccry 出差人员 ccrs 出差人数 ydlx 预定类型 cfrq 出发日期 cfcs 出发城市 ddcs 到达城市
         * zxlx 坐席类型 price 价格 xclx 行程类型 bz 备注
         */
        String sqlDetail3 = "select * from " + tableName
                + "_dt3 where mainid = " + mainidSql;
        this.detailTableDataMap3 = getDetailTableDataMap(sqlDetail3);

        String ticket = getCorpTicket.getSetApproval(this.appKey,
                this.appSecurity).getTicket();
        SetApprovalServiceRequest requestSetApp = getRequestSetApp(
                this.detailTableDataMap1, this.detailTableDataMap2,
                this.detailTableDataMap3, ticket);
        setApproval(requestSetApp);

        writeLog("==================================结束提前审批类==================================");
        return Action.SUCCESS;
    }

    // 航班,酒店,火车提前审批提交接口
    public void setApproval(SetApprovalServiceRequest setApprovalServiceRequest) {
        SetApprovalService setapprovalService = new SetApprovalService();
        SetApprovalResponse setApprovalResponse = setapprovalService
                .SetApproval(setApprovalServiceRequest);
        if (setApprovalResponse != null
                && setApprovalResponse.getStatus() != null) {
            writeLog("service result:" + JSON.toJSONString(setApprovalResponse),
                    SetApprovalAction.class);
        }

    }

    /**
     * 根据部门id获取部门名称
     *
     * @param id
     * @return
     */
    public String getDepartmentName(String id) {
        String sql = "select departmentname from hrmdepartment where id = " + id;
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        if (rs.next()) {
            return rs.getString("departmentname");
        }
        return null;
    }

    /**
     * 取得请求参数信息
     *
     * @param detailTableDataMap1
     * @param detailTableDataMap2
     * @param detailTableDataMap3
     * @param ticket
     * @return
     */
    public SetApprovalServiceRequest getRequestSetApp(ArrayList<HashMap<String, String>> detailTableDataMap1, ArrayList<HashMap<String, String>> detailTableDataMap2, ArrayList<HashMap<String, String>> detailTableDataMap3,
                                                      String ticket) {

        SetApprovalServiceRequest setApprovalServiceRequest = new SetApprovalServiceRequest();
        SetApprovalRequest setApprovalRequest = new SetApprovalRequest();
        Authentification Authentification = new Authentification(this.appKey, ticket);

        setApprovalRequest.setAuth(Authentification);// 认证信息
        setApprovalRequest.setStatus(1);// 设置提前审批单状态
        setApprovalRequest.setApprovalNumber(this.approvalNumber);// approvalNumber
        // 目前先按照主表的申请人设置员工id
        setApprovalRequest.setEmployeeID(this.tableInfo.get("gh"));// 员工编号
        setApprovalRequest.setExpiredTime("");// 过期时间
        /***扩展字段  成本中心  start****/
        ArrayList<ExtendField> xtendFieldList = new ArrayList<ExtendField>();
        ExtendField extendField = new ExtendField();

        extendField.setFieldName("CostCenter1");//成本中心1
        String sqbm = this.tableInfo.get("sqbm");
        String departmentName = getDepartmentName(sqbm);

        extendField.setFieldValue(departmentName);//费用结算部门
        xtendFieldList.add(extendField);
        setApprovalRequest.setExtendFieldList(xtendFieldList);
        /***扩展字段  成本中心  end****/
        // 设置航班start
        /**
         * 明细1 航班信息 ccry 出差人员 ccrs 出差人数 ydlx 预定类型 cfrq 出发日期 fcrq 返程日期 cfcs 出发城市
         * ddcs 到达城市 cw 舱位 price 价格 hblx 航班类型 bz 备注
         */
        if (detailTableDataMap1 != null && detailTableDataMap1.size() > 0) {
            ArrayList<FlightEndorsementDetail> flightEndorsementDetails = new ArrayList<FlightEndorsementDetail>();

            //根据航班类型设置人员集合
            ArrayList<String> passengerInternational = new ArrayList<String>();
            ArrayList<String> passengerDomestic = new ArrayList<String>();
            //城市
            ArrayList<String> fromCityInternational = new ArrayList<String>();
            ArrayList<String> toCityInternational = new ArrayList<String>();
            ArrayList<String> fromCityDomestic = new ArrayList<String>();
            ArrayList<String> toCityDomestic = new ArrayList<String>();

            Integer ccrsDomestic = 0;
            Integer ccrsInternational = 0;

            for (int i = 0; i < detailTableDataMap1.size(); i++) {
                String ccry = detailTableDataMap1.get(i).get("ccry");
                String ccrs = detailTableDataMap1.get(i).get("ccrs");
                String ydlx = detailTableDataMap1.get(i).get("ydlx");
                String cfcs = detailTableDataMap1.get(i).get("cfcs");
                String ddcs = detailTableDataMap1.get(i).get("ddcs");
                if ("1".equals(ydlx)) {
                    passengerDomestic.add(ccry);
                    ccrsDomestic += Integer.parseInt(ccrs);
                    fromCityDomestic.add(cfcs);
                    toCityDomestic.add(ddcs);
                }
                if ("2".equals(ydlx)) {
                    passengerInternational.add(ccry);
                    ccrsInternational += Integer.parseInt(ccrs);
                    fromCityInternational.add(cfcs);
                    toCityInternational.add(ddcs);
                }
            }

            //国内
            if (passengerDomestic != null && !passengerDomestic.isEmpty()) {
                FlightEndorsementDetail fightEndorsementDetailDomestic = new FlightEndorsementDetail();
                fightEndorsementDetailDomestic.setFlightWay(FlightWayType.RoundTrip);
                fightEndorsementDetailDomestic.setProductType(ProductType.DomesticFlight);
                fightEndorsementDetailDomestic.setPassengerList(getPassengerDetails(passengerDomestic));
                fightEndorsementDetailDomestic.setFromCities((ArrayList<String>) getFlightCityList(fromCityDomestic));//需要转换中文城市列表
                fightEndorsementDetailDomestic.setToCities((ArrayList<String>) getFlightCityList(toCityDomestic));//需要转换中文城市列表
                fightEndorsementDetailDomestic.setDepartDateBegin(this.fromDate);
                fightEndorsementDetailDomestic.setDepartDateEnd(this.toDate);
                fightEndorsementDetailDomestic.setReturnDateBegin(this.fromDate);
                fightEndorsementDetailDomestic.setReturnDateEnd(this.toDate);
                fightEndorsementDetailDomestic.setTravelerCount(ccrsDomestic);

                flightEndorsementDetails.add(fightEndorsementDetailDomestic);
            }
            //国际
            if (passengerInternational != null && !passengerInternational.isEmpty()) {
                FlightEndorsementDetail fightEndorsementDetailInternational = new FlightEndorsementDetail();
                fightEndorsementDetailInternational.setFlightWay(FlightWayType.RoundTrip);
                fightEndorsementDetailInternational.setProductType(ProductType.InternationalFlight);
                fightEndorsementDetailInternational.setPassengerList(getPassengerDetails(passengerInternational));
                fightEndorsementDetailInternational.setFromCities((ArrayList<String>) getFlightCityList(fromCityInternational));//需要转换中文城市列表
                fightEndorsementDetailInternational.setToCities((ArrayList<String>) getFlightCityList(toCityInternational));//需要转换中文城市列表
                fightEndorsementDetailInternational.setDepartDateBegin(this.fromDate);
                fightEndorsementDetailInternational.setDepartDateEnd(this.toDate);
                fightEndorsementDetailInternational.setReturnDateBegin(this.fromDate);
                fightEndorsementDetailInternational.setReturnDateEnd(this.toDate);
                fightEndorsementDetailInternational.setTravelerCount(ccrsInternational);

                flightEndorsementDetails.add(fightEndorsementDetailInternational);
            }
            setApprovalRequest.setFlightEndorsementDetails(flightEndorsementDetails);
        }
        // 设置航班end

        // 设置酒店参数信息start
        if (detailTableDataMap2 != null && detailTableDataMap2.size() > 0) {
            ArrayList<HotelEndorsementDetail> hotelEndorsementDetails = new ArrayList<HotelEndorsementDetail>();

            //根据航班类型设置人员集合
            ArrayList<String> passengerInternational = new ArrayList<String>();
            ArrayList<String> passengerDomestic = new ArrayList<String>();

            //城市
            ArrayList<String> toCityInternational = new ArrayList<String>();
            ArrayList<String> toCityDomestic = new ArrayList<String>();

            //房间数量
            Integer fjslDomestic = 0;
            Integer fjslInternational = 0;
            //房间数量

            for (int i = 0; i < detailTableDataMap2.size(); i++) {
                String ccry = detailTableDataMap2.get(i).get("rzr");
                String ydlx = detailTableDataMap2.get(i).get("ydlx");
                String rzcs = detailTableDataMap2.get(i).get("rzcs");
                String fjsl = detailTableDataMap2.get(i).get("fjsl");

                if ("3".equals(ydlx)) {// 国内酒店
                    passengerDomestic.add(ccry);
                    toCityDomestic.add(rzcs);
                    fjslDomestic += Integer.parseInt(fjsl);
                }
                if ("4".equals(ydlx)) {// 海外酒店
                    passengerInternational.add(ccry);
                    toCityInternational.add(rzcs);
                    fjslInternational += Integer.parseInt(fjsl);
                }
            }
            //国内
            if (passengerDomestic != null && !passengerDomestic.isEmpty()) {
                HotelEndorsementDetail hotelEndorsementDetailDomestic = new HotelEndorsementDetail();
                hotelEndorsementDetailDomestic.setProductType(HotelProductType.Domestic);
                hotelEndorsementDetailDomestic.setPassengerList(getPassengerDetails(passengerDomestic));
                hotelEndorsementDetailDomestic.setRoomCount(fjslDomestic);
                hotelEndorsementDetailDomestic.setCheckInDateBegin(this.fromDate);
                hotelEndorsementDetailDomestic.setCheckInDateEnd(this.toDate);
                hotelEndorsementDetailDomestic.setCheckOutDateBegin(this.fromDate);
                hotelEndorsementDetailDomestic.setCheckOutDateEnd(this.toDate);
                hotelEndorsementDetailDomestic.setToCities((ArrayList<String>) getFlightCityList(toCityDomestic));
                hotelEndorsementDetailDomestic.setCurrency(CurrencyType.RMB);

                hotelEndorsementDetails.add(hotelEndorsementDetailDomestic);
            }
            //国外
            if (passengerInternational != null && !passengerInternational.isEmpty()) {
                HotelEndorsementDetail hotelEndorsementDetailInternational = new HotelEndorsementDetail();
                hotelEndorsementDetailInternational.setProductType(HotelProductType.International);
                hotelEndorsementDetailInternational.setPassengerList(getPassengerDetails(passengerInternational));
                hotelEndorsementDetailInternational.setRoomCount(fjslInternational);
                hotelEndorsementDetailInternational.setCheckInDateBegin(this.fromDate);
                hotelEndorsementDetailInternational.setCheckInDateEnd(this.toDate);
                hotelEndorsementDetailInternational.setCheckOutDateBegin(this.fromDate);
                hotelEndorsementDetailInternational.setCheckOutDateEnd(this.toDate);
                hotelEndorsementDetailInternational.setToCities((ArrayList<String>) getFlightCityList(toCityInternational));
                hotelEndorsementDetailInternational.setCurrency(CurrencyType.RMB);

                hotelEndorsementDetails.add(hotelEndorsementDetailInternational);
            }
            setApprovalRequest.setHotelEndorsementDetails(hotelEndorsementDetails);
        }
        // 设置酒店参数信息end

        // 设置火车的参数信息start
        /**
         * 明细3火车 ccry 出差人员 ccrs 出差人数 ydlx 预定类型 cfrq 出发日期 cfcs 出发城市 ddcs 到达城市
         * zxlx 坐席类型 price 价格 xclx 行程类型 bz 备注
         */
        if (detailTableDataMap3 != null && detailTableDataMap3.size() > 0) {
            ArrayList<TrainEndorsementDetail> trainEndorsementDetails = new ArrayList<TrainEndorsementDetail>();

            ArrayList<String> fromCities = new ArrayList<String>();
            ArrayList<String> toCities = new ArrayList<String>();
            ArrayList<String> passengerList = new ArrayList<String>();
            Integer ccrss = 0;
            for (int i = 0; i < detailTableDataMap3.size(); i++) {
                String ccry = detailTableDataMap3.get(i).get("ccry");
                String ccrs = detailTableDataMap3.get(i).get("ccrs");
                String cfcs = detailTableDataMap3.get(i).get("cfcs");
                String ddcs = detailTableDataMap3.get(i).get("ddcs");
                ccrss += Integer.parseInt(ccrs);
                fromCities.add(cfcs);
                toCities.add(ddcs);
                passengerList.add(ccry);
            }
            TrainEndorsementDetail trainEndorsementDetail = new TrainEndorsementDetail();
            trainEndorsementDetail.setProductType(TrainProductType.Domestic);// 默认国内
            trainEndorsementDetail.setTripType(TripTypeEnum.SingleWay);// 目前火车票只有单程
            trainEndorsementDetail.setDepartDateBegin(this.fromDate);
            trainEndorsementDetail.setDepartDateEnd(this.toDate);
            trainEndorsementDetail.setPassengerList(getPassengerDetails(passengerList));
            trainEndorsementDetail.setFromCities((ArrayList<String>) removeDuplicate(fromCities));
            trainEndorsementDetail.setToCities((ArrayList<String>) removeDuplicate(toCities));
            trainEndorsementDetail.setCurrency(CurrencyType.CNY);
            trainEndorsementDetail.setTravelerCount(ccrss);
            trainEndorsementDetails.add(trainEndorsementDetail);

            setApprovalRequest.setTrainEndorsementDetails(trainEndorsementDetails);
        }

        // 设置火车的参数信息end

        setApprovalServiceRequest.setRequest(setApprovalRequest);

        writeLog("setApprovalRequest=================" + JSON.toJSONString(setApprovalServiceRequest));
        return setApprovalServiceRequest;
    }

    /**
     * 判断舱等类型
     *
     * @param cw
     * @return
     */
    public SeatClassType getCw(String cw) {
        if (cw != null && "1".equals(cw)) {
            return SeatClassType.UnKnow;// 未知
        }
        if (cw != null && "2".equals(cw)) {
            return SeatClassType.SaloonCabin; // 头等舱
        }
        if (cw != null && "3".equals(cw)) {
            return SeatClassType.BusinessClass;// 公务舱
        }
        if (cw != null && "4".equals(cw)) {
            return SeatClassType.SuperTouristClass;// 超级经济舱
        }
        if (cw != null && "5".equals(cw)) {
            return SeatClassType.TouristClass;// 经济舱
        }
        return null;
    }


    public ArrayList<PassengerDetail> getPassengerDetails(List<String> passengerList) {
        if (passengerList == null) {
            return null;
        }
        passengerList = removeDuplicate(passengerList);
        ArrayList<PassengerDetail> passengerDetails = new ArrayList<PassengerDetail>();
        StringBuffer sb = new StringBuffer();
        for (String name : passengerList) {
            PassengerDetail passengerDetail = new PassengerDetail();
            passengerDetail.setName(getLastNameList(name));
            sb.append(passengerDetail.getName()).append(",");
            passengerDetails.add(passengerDetail);
        }
        writeLog("==========================" + sb.toString());
        return passengerDetails;
    }

    /**
     * 根据id获取姓名
     *
     * @param ids
     * @return
     */
    public String getLastNameList(String ids) {
        StringBuilder sb = new StringBuilder();
        String sql = "select lastname from hrmresource where id in(" + ids
                + ")";
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        while (rs.next()) {
            sb.append(rs.getString(1)).append(",");
        }
        if (sb.toString().contains(",")) {
            return sb.toString().substring(0, sb.toString().length() - 1);
        }
        return null;
    }

    /**
     * 根据property数组返回一个map结构的key-value结构的map
     *
     * @param mainProperty
     * @return
     */
    protected Map<String, String> getMainTableInfo(Property[] mainProperty) {
        Map<String, String> map = new HashMap<String, String>();
        for (Property property : mainProperty) {
            String key = property.getName();
            String value = Util.null2String(property.getValue());
            map.put(key, value);
        }
        return map;
    }

    /**
     * 根据sql语句返回list结构的数据
     *
     * @param sql
     * @return
     */
    protected ArrayList<HashMap<String, String>> getDetailTableDataMap(
            String sql) {
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> localHashMap = null;
        RecordSet rs = new RecordSet();
        rs.executeSql(sql);
        String[] columnName = rs.getColumnName();
        while (rs.next()) {
            writeLog("------------------------------getDetailTableDataMap-------------------------------------------");
            localHashMap = new HashMap<String, String>();
            for (int i = 0; i < columnName.length; i++) {
                String fileName = columnName[i];
                String fileValue = Util.null2String(rs.getString(fileName));
                localHashMap.put(fileName.toLowerCase(), fileValue);
                writeLog("getDetailTableDataMap\t\t " + fileName + "\t\t"
                        + fileValue);
            }
            arrayList.add(localHashMap);
        }
        return arrayList;
    }

    /**
     * 根据workflowID返回表单名
     *
     * @param workflowid
     * @return
     */
    protected String getTableNameByWorkflowId(String workflowid) {
        String sql = "select tablename from workflow_bill where id=(select formid from workflow_base where id="
                + workflowid + ")";
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        if (rs.next()) {
            return rs.getString(1);
        }
        return null;
    }

    /**
     * 给定一个日期和一个整数返回一个日期,为正整数则向后加,负数则向前
     *
     * @param date
     * @param days
     * @return
     */
    public String getCurrentDate(String date, int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date dates = new Date();
        try {
            dates = sdf.parse(date);
            calendar.setTime(dates);
            calendar.add(Calendar.DAY_OF_MONTH, days);
            Date time = calendar.getTime();
            return sdf.format(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据list的日期 和类型 返回 其中的最大日期或者最小日期
     *
     * @param dateList
     * @param type
     * @return
     */
    public String getCurrentDate(List<String> dateList, int type) {
        if (dateList == null || dateList.isEmpty()) {
            return null;
        }
        String[] strings = new String[dateList.size()];
        String[] arrayDate = dateList.toArray(strings);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date tempDate = null;
            Date currentDate = null;
            for (int i = 0; i < arrayDate.length - 1; i++) {
                for (int j = 0; j < arrayDate.length - i - 1; j++) {
                    tempDate = sdf.parse(arrayDate[j]);
                    currentDate = sdf.parse(arrayDate[j + 1]);
                    if (tempDate.getTime() > currentDate.getTime()) {
                        String temp = arrayDate[j];
                        arrayDate[j] = arrayDate[j + 1];
                        arrayDate[j + 1] = temp;
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (type == 0) {
            return arrayDate[0];
        }
        if (type == 1) {
            return arrayDate[arrayDate.length - 1];
        }
        return null;
    }


    /**
     * 返回价格合计
     *
     * @param priceList
     * @return
     */
    public String getSumPrice(List<String> priceList) {
        if (priceList == null || priceList.isEmpty()) {
            return null;
        }
        Integer sumInteger = 0;
        for (String string : priceList) {
            sumInteger += Integer.parseInt(string);
        }
        return String.valueOf(sumInteger);
    }

    /**
     * 返回航班城市列表(中文)
     *
     * @param citykey
     * @return
     */
    public List<String> getFlightCityList(String citykey) {
        String sql = "select distinct cityname from ctrip_aircity where citykey in("
                + citykey + ")";
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        List<String> cityList = new ArrayList<String>();
        if (rs.next()) {
            cityList.add(rs.getString(1));
        }
        return cityList;
    }

    public List<String> getFlightCityList(List<String> cityList) {
        List<String> list = removeDuplicate(cityList);
        String strByList = getStrByList(list);
        String sql = "select distinct cityname from ctrip_aircity where citykey in("
                + strByList + ")";
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        List<String> cityLists = new ArrayList<String>();
        while (rs.next()) {
            cityLists.add(rs.getString(1));
        }
        return cityLists;
    }

    /**
     * list去重 返回字符串
     *
     * @param list
     * @return
     */
    public List<String> removeDuplicate(List<String> list) {
        HashSet<String> h = new HashSet<String>(list);
        list.clear();
        list.addAll(h);
        return list;
    }


    public String getStrByList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringBuffer sbf = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            int len = list.size();
            if (i < len - 1) {
                sbf.append(list.get(i)).append(",");
            } else {
                sbf.append(list.get(i));
            }
        }
        writeLog("===========" + sbf.toString());
        return sbf.toString();
    }
}
