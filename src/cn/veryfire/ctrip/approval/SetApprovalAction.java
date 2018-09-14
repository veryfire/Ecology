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
    private String tableName;
    private String requestid;
    private Map<String, String> tableInfo;
    private ArrayList<HashMap<String, String>> detailTableDataMap1;
    private ArrayList<HashMap<String, String>> detailTableDataMap2;
    private ArrayList<HashMap<String, String>> detailTableDataMap3;
    private String fromDate;
    private String toDate;
    private final String SEQ_EN = ",";
    private final String SEQ_CN = "，";


    public SetApprovalAction() {
        this.appKey = getPropValue("ctripInfo", "appKey");
        this.appSecurity = getPropValue("ctripInfo", "appSecurity");
    }

    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("==================================进入提前审批类==================================");
        CorpTicket getCorpTicket = new CorpTicket();
        // 审批单行程号
        this.requestid = requestInfo.getRequestid();

        String workflowid = requestInfo.getWorkflowid();
        this.tableName = getTableNameByWorkflowId(workflowid);
        String mainidSql = "(select id from " + this.tableName + " where requestid="
                + this.requestid + ")";
        Property[] mainProperty = requestInfo.getMainTableInfo().getProperty();
        // 主表信息
        this.tableInfo = getMainTableInfo(mainProperty);
        // 设置行程号
        this.approvalNumber = tableInfo.get("djbh");

        this.fromDate = this.tableInfo.get("ccksrq");
        this.toDate = this.tableInfo.get("ccjsrq");


        /**
         * 明细1 航班信息
         *
         * ccry 出差人员 ccrs 出差人数 ydlx 预定类型 cfrq 出发日期 fcrq 返程日期 cfcs 出发城市 ddcs 到达城市
         * cw 舱位 price 价格 hblx 航班类型 bz 备注 ywmc 英文名称
         *
         */
        String sqlDetail1 = "select ccry,ccrs,ydlx,cfrq,fcrq,cfcs,ddcs,cw,price,hblx,bz,ywmc，wbccry from "
                + this.tableName + "_dt1 where mainid =" + mainidSql;
        this.detailTableDataMap1 = getDetailTableDataMap(sqlDetail1);

        /**
         * 明细2 酒店 rzr 入住人 rzrs 入住人数 ydlx 预定类型 rzrq 入住日期 ldrq 离店日期 rzcs 入住城市
         * price 价格 fjsl 房间数量 jdxj 酒店星级 bz 备注 ywmc 英文名称
         */
        String sqlDetail2 = "select rzr,rzrs,ydlx,rzrq,ldrq,rzcs,price,fjsl,jdxj,bz,ywmc，wbrzry from "
                + this.tableName + "_dt2 where mainid = " + mainidSql;
        this.detailTableDataMap2 = getDetailTableDataMap(sqlDetail2);

        /**
         * 明细3火车 ccry 出差人员 ccrs 出差人数 ydlx 预定类型 cfrq 出发日期 cfcs 出发城市 ddcs 到达城市
         * zxlx 坐席类型 price 价格 xclx 行程类型 bz 备注
         */
        String sqlDetail3 = "select * from " + this.tableName
                + "_dt3 where mainid = " + mainidSql;
        this.detailTableDataMap3 = getDetailTableDataMap(sqlDetail3);

        String ticket = getCorpTicket.getSetApproval(this.appKey,
                this.appSecurity).getTicket();
        SetApprovalServiceRequest requestSetApp = getRequestSetApp(
                this.detailTableDataMap1, this.detailTableDataMap2,
                this.detailTableDataMap3, ticket);
        SetApprovalResponse setApprovalResponse = setApproval(requestSetApp);

        if (!setApprovalResponse.getStatus().getSuccess()) {
            requestInfo.getRequestManager().setMessageid(this.requestid);
            int errorCode = setApprovalResponse.getStatus().getErrorCode();
            String message = setApprovalResponse.getStatus().getMessage();
            requestInfo.getRequestManager().setMessagecontent("提交失败,errorcode:" + errorCode + ",message:" + message + ",请退回申请人修改信息,或者联系OA负责人处理!");
        }
        writeLog("==================================结束提前审批类==================================");
        return Action.SUCCESS;
    }

    /**
     * 航班,酒店,火车提前审批提交接口
     *
     * @param setApprovalServiceRequest
     */
    public SetApprovalResponse setApproval(SetApprovalServiceRequest setApprovalServiceRequest) {
        SetApprovalService setapprovalService = new SetApprovalService();
        SetApprovalResponse setApprovalResponse = setapprovalService.SetApproval(setApprovalServiceRequest);
        if (setApprovalResponse != null
                && setApprovalResponse.getStatus() != null) {
            boolean result = setApprovalResponse.getStatus().getSuccess();
            if (result) {
                setApprovalResult();//更新审批单落地状态
            }
            writeLog("service result:" + JSON.toJSONString(setApprovalResponse),
                    SetApprovalAction.class);
        }
        return setApprovalResponse;
    }

    /**
     * 更新审批单落地状态
     * 0 success
     * 1 fail
     */
    private void setApprovalResult() {
        String sql = "update " + this.tableName + " set result='0' where requestid=" + this.requestid;
        writeLog("update result sql:" + sql, SetApprovalAction.class);
        RecordSet rs = new RecordSet();
        rs.execute(sql);
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
     * 根据字符串返回list
     *
     * @param hrmStr
     * @return
     */
    public List<String> getHrmResourceListByString(String hrmStr) {
        //处理为空情况
        if ("".equals(hrmStr) || hrmStr == null) {
            return null;
        }
        List<String> hrmList = new ArrayList<String>();
        hrmStr = hrmStr.replace("，", ",");
        String[] split = hrmStr.split(",");
        for (String name : split) {
            hrmList.add(name);
        }
        return hrmList;

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
        Authentification authentification = new Authentification(this.appKey, ticket);
        // 认证信息
        setApprovalRequest.setAuth(authentification);
        // 设置提前审批单状态
        setApprovalRequest.setStatus(1);
        // approvalNumber
        setApprovalRequest.setApprovalNumber(this.approvalNumber);
        // 过期时间
        setApprovalRequest.setExpiredTime("");
        /***扩展字段  成本中心  start****/
        ArrayList<ExtendField> xtendFieldList = new ArrayList<ExtendField>();
        ExtendField extendField = new ExtendField();
        //成本中心1
        extendField.setFieldName("CostCenter1");
        String sqbm = this.tableInfo.get("sqbm");
        String departmentName = getDepartmentName(sqbm);
        //费用结算部门
        extendField.setFieldValue(departmentName);
        xtendFieldList.add(extendField);
        setApprovalRequest.setExtendFieldList(xtendFieldList);
        /***扩展字段  成本中心  end****/
        // 设置航班start
        /**
         * 明细1 航班信息 ccry 出差人员 ccrs 出差人数 ydlx 预定类型 cfrq 出发日期 fcrq 返程日期 cfcs 出发城市
         * ddcs 到达城市 cw 舱位 price 价格 hblx 航班类型 bz 备注 ywmc 英文名称
         */
        if (detailTableDataMap1 != null && detailTableDataMap1.size() > 0) {
            ArrayList<FlightEndorsementDetail> flightEndorsementDetails = new ArrayList<FlightEndorsementDetail>();

            //根据航班类型设置人员集合
            //所有出差人员集合
            ArrayList<PassengerDetail> chinaAndEnNameList_CN = new ArrayList<PassengerDetail>();
            ArrayList<PassengerDetail> chinaAndEnNameList_EN = new ArrayList<PassengerDetail>();
            //城市
            ArrayList<String> fromCityInternational = new ArrayList<String>();
            ArrayList<String> toCityInternational = new ArrayList<String>();
            ArrayList<String> fromCityDomestic = new ArrayList<String>();
            ArrayList<String> toCityDomestic = new ArrayList<String>();
            Integer ccrsDomestic = 0;
            Integer ccrsInternational = 0;

            for (int i = 0; i < detailTableDataMap1.size(); i++) {
                //出差人员,系统内人员id,以逗号分隔
                String ccry = detailTableDataMap1.get(i).get("ccry");
                //出差人数
                String ccrs = detailTableDataMap1.get(i).get("ccrs");
                //预定类型
                String ydlx = detailTableDataMap1.get(i).get("ydlx");
                //出发城市
                String cfcs = detailTableDataMap1.get(i).get("cfcs");
                //到达城市
                String ddcs = detailTableDataMap1.get(i).get("ddcs");
                //英文名称
                String ywmc = detailTableDataMap1.get(i).get("ywmc");
                //外部人员姓名
                String wbccry = detailTableDataMap1.get(i).get("wbccry");

                if ("1".equals(ydlx)) {
                    ccrsDomestic += Integer.parseInt(ccrs);
                    fromCityDomestic.add(cfcs);
                    toCityDomestic.add(ddcs);
                    if (!"".equals(ccry) && ccry != null) {
                        ArrayList<String> tmpArr = new ArrayList<String>();
                        tmpArr.add(ccry);
                        chinaAndEnNameList_CN.addAll(getPassengerDetails(tmpArr));
                    }
                    if (!"".equals(wbccry) && wbccry != null) {
                        chinaAndEnNameList_CN.addAll(getEnPassengerDetails(getHrmResourceListByString(wbccry)));
                    }
                    if (!"".equals(ywmc) && ywmc != null) {
                        chinaAndEnNameList_CN.addAll(getEnPassengerDetails(getHrmResourceListByString(ywmc)));
                    }

                }
                if ("2".equals(ydlx)) {
                    ccrsInternational += Integer.parseInt(ccrs);
                    fromCityInternational.add(cfcs);
                    toCityInternational.add(ddcs);
                    if (!"".equals(ccry) && ccry != null) {
                        ArrayList<String> tmpArr = new ArrayList<String>();
                        tmpArr.add(ccry);
                        chinaAndEnNameList_EN.addAll(getPassengerDetails(tmpArr));
                    }
                    if (!"".equals(wbccry) && wbccry != null) {
                        chinaAndEnNameList_EN.addAll(getEnPassengerDetails(getHrmResourceListByString(wbccry)));
                    }
                    if (!"".equals(ywmc) && ywmc != null) {
                        chinaAndEnNameList_EN.addAll(getEnPassengerDetails(getHrmResourceListByString(ywmc)));
                    }
                }
            }

            chinaAndEnNameList_CN = new ArrayList<PassengerDetail>(new LinkedHashSet<PassengerDetail>(chinaAndEnNameList_CN));
            chinaAndEnNameList_EN = new ArrayList<PassengerDetail>(new LinkedHashSet<PassengerDetail>(chinaAndEnNameList_EN));

            //国内
            if (chinaAndEnNameList_CN != null && !chinaAndEnNameList_CN.isEmpty()) {
                FlightEndorsementDetail fightEndorsementDetailDomestic = new FlightEndorsementDetail();
                fightEndorsementDetailDomestic.setFlightWay(FlightWayType.RoundTrip);
                fightEndorsementDetailDomestic.setProductType(ProductType.DomesticFlight);
                fightEndorsementDetailDomestic.setPassengerList(chinaAndEnNameList_CN);
                //需要转换中文城市列表
                fightEndorsementDetailDomestic.setFromCities((ArrayList<String>) getFlightCityList(fromCityDomestic));
                //需要转换中文城市列表
                fightEndorsementDetailDomestic.setToCities((ArrayList<String>) getFlightCityList(toCityDomestic));
                fightEndorsementDetailDomestic.setDepartDateBegin(this.fromDate);
                fightEndorsementDetailDomestic.setDepartDateEnd(this.toDate);
                fightEndorsementDetailDomestic.setReturnDateBegin(this.fromDate);
                fightEndorsementDetailDomestic.setReturnDateEnd(this.toDate);
                fightEndorsementDetailDomestic.setTravelerCount(ccrsDomestic);

                flightEndorsementDetails.add(fightEndorsementDetailDomestic);
            }
            //国际
            if (chinaAndEnNameList_EN != null && !chinaAndEnNameList_EN.isEmpty()) {
                FlightEndorsementDetail fightEndorsementDetailInternational = new FlightEndorsementDetail();
                fightEndorsementDetailInternational.setFlightWay(FlightWayType.RoundTrip);
                fightEndorsementDetailInternational.setProductType(ProductType.InternationalFlight);
                //英文名称列表
                fightEndorsementDetailInternational.setPassengerList(chinaAndEnNameList_EN);
                //需要转换中文城市列表
                fightEndorsementDetailInternational.setFromCities((ArrayList<String>) getFlightCityList(fromCityInternational));
                //需要转换中文城市列表
                fightEndorsementDetailInternational.setToCities((ArrayList<String>) getFlightCityList(toCityInternational));
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

            ArrayList<PassengerDetail> chinaAndEnNameList_CN = new ArrayList<PassengerDetail>();
            ArrayList<PassengerDetail> chinaAndEnNameList_EN = new ArrayList<PassengerDetail>();


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
                String ywmc = detailTableDataMap2.get(i).get("ywmc");
                String wbrzry = detailTableDataMap2.get(i).get("wbrzry");
                // 国内酒店
                if ("3".equals(ydlx)) {
                    toCityDomestic.add(rzcs);
                    fjslDomestic += Integer.parseInt(fjsl);
                    if (!"".equals(ccry) && ccry != null) {
                        ArrayList<String> tmpArr = new ArrayList<String>();
                        tmpArr.add(ccry);
                        chinaAndEnNameList_CN.addAll(getPassengerDetails(tmpArr));
                    }
                    if (!"".equals(wbrzry) && wbrzry != null) {
                        chinaAndEnNameList_CN.addAll(getEnPassengerDetails(getHrmResourceListByString(wbrzry)));
                    }
                    if (!"".equals(ywmc) && ywmc != null) {
                        chinaAndEnNameList_CN.addAll(getEnPassengerDetails(getHrmResourceListByString(ywmc)));
                    }
                }
                // 海外酒店
                if ("4".equals(ydlx)) {
                    toCityInternational.add(rzcs);
                    fjslInternational += Integer.parseInt(fjsl);
                    if (!"".equals(ccry) && ccry != null) {
                        ArrayList<String> tmpArr = new ArrayList<String>();
                        tmpArr.add(ccry);
                        chinaAndEnNameList_EN.addAll(getPassengerDetails(tmpArr));
                    }
                    if (!"".equals(wbrzry) && wbrzry != null) {
                        chinaAndEnNameList_EN.addAll(getEnPassengerDetails(getHrmResourceListByString(wbrzry)));
                    }
                    if (!"".equals(ywmc) && ywmc != null) {
                        chinaAndEnNameList_EN.addAll(getEnPassengerDetails(getHrmResourceListByString(ywmc)));
                    }
                }

            }
            chinaAndEnNameList_CN = new ArrayList<PassengerDetail>(new LinkedHashSet<PassengerDetail>(chinaAndEnNameList_CN));
            chinaAndEnNameList_EN = new ArrayList<PassengerDetail>(new LinkedHashSet<PassengerDetail>(chinaAndEnNameList_EN));
            //国内
            if (chinaAndEnNameList_CN != null && !chinaAndEnNameList_CN.isEmpty()) {
                HotelEndorsementDetail hotelEndorsementDetailDomestic = new HotelEndorsementDetail();
                hotelEndorsementDetailDomestic.setProductType(HotelProductType.Domestic);
                hotelEndorsementDetailDomestic.setPassengerList(chinaAndEnNameList_CN);
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
            if (chinaAndEnNameList_EN != null && !chinaAndEnNameList_EN.isEmpty()) {
                HotelEndorsementDetail hotelEndorsementDetailInternational = new HotelEndorsementDetail();
                hotelEndorsementDetailInternational.setProductType(HotelProductType.International);
                hotelEndorsementDetailInternational.setPassengerList(chinaAndEnNameList_EN);
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

            ArrayList<String> wbccryList = new ArrayList<String>();
            //所有出差人员集合
            ArrayList<PassengerDetail> chinaAndEnNameList = new ArrayList<PassengerDetail>();


            Integer ccrss = 0;
            for (int i = 0; i < detailTableDataMap3.size(); i++) {
                String ccry = detailTableDataMap3.get(i).get("ccry");
                String ccrs = detailTableDataMap3.get(i).get("ccrs");
                String cfcs = detailTableDataMap3.get(i).get("cfcs");
                String ddcs = detailTableDataMap3.get(i).get("ddcs");
                String wbccry = detailTableDataMap3.get(i).get("wbccry");
                if (!"".equals(wbccry)) {
                    wbccryList.addAll(getHrmResourceListByString(wbccry));
                }
                if (!"".equals(ccry) && ccry != null) {
                    passengerList.add(ccry);
                }
                ccrss += Integer.parseInt(ccrs);
                fromCities.add(cfcs);
                toCities.add(ddcs);

            }

            chinaAndEnNameList.addAll(getPassengerDetails(passengerList));
            chinaAndEnNameList.addAll(getEnPassengerDetails(wbccryList));
            chinaAndEnNameList = new ArrayList<PassengerDetail>(new LinkedHashSet<PassengerDetail>(chinaAndEnNameList));

            TrainEndorsementDetail trainEndorsementDetail = new TrainEndorsementDetail();
            // 默认国内
            trainEndorsementDetail.setProductType(TrainProductType.Domestic);
            // 目前火车票只有单程
            trainEndorsementDetail.setTripType(TripTypeEnum.SingleWay);
            trainEndorsementDetail.setDepartDateBegin(this.fromDate);
            trainEndorsementDetail.setDepartDateEnd(this.toDate);
            trainEndorsementDetail.setPassengerList(chinaAndEnNameList);
            trainEndorsementDetail.setFromCities((ArrayList<String>) removeDuplicate(fromCities));
            trainEndorsementDetail.setToCities((ArrayList<String>) removeDuplicate(toCities));
            trainEndorsementDetail.setCurrency(CurrencyType.CNY);
            trainEndorsementDetail.setTravelerCount(ccrss);
            trainEndorsementDetails.add(trainEndorsementDetail);

            setApprovalRequest.setTrainEndorsementDetails(trainEndorsementDetails);
        }

        // 设置火车的参数信息end
        //设置员工编号
        setApprovalRequest.setEmployeeID(this.tableInfo.get("gh"));
        setApprovalServiceRequest.setRequest(setApprovalRequest);

        writeLog("setApprovalRequest=================" + JSON.toJSONString(setApprovalServiceRequest));
        return setApprovalServiceRequest;
    }

    /**
     * 返回人员列表
     *
     * @param passengerList
     * @return
     */
    public ArrayList<PassengerDetail> getPassengerDetails(List<String> passengerList) {
        if (passengerList == null) {
            return null;
        }
        passengerList = removeDuplicate(passengerList);
        ArrayList<PassengerDetail> passengerDetails = new ArrayList<PassengerDetail>();
        for (String name : passengerList) {
            PassengerDetail passengerDetail = new PassengerDetail();
            passengerDetail.setName(getLastNameList(name));
            passengerDetails.add(passengerDetail);
        }
        return passengerDetails;
    }

    /**
     * 国外 英文名集合
     *
     * @param passengerList
     * @return
     */
    public ArrayList<PassengerDetail> getEnPassengerDetails(List<String> passengerList) {
        if (passengerList == null) {
            return null;
        }
        passengerList = removeDuplicate(passengerList);
        ArrayList<PassengerDetail> passengerDetails = new ArrayList<PassengerDetail>();
        StringBuffer sb = new StringBuffer();
        for (String name : passengerList) {
            PassengerDetail passengerDetail = new PassengerDetail();
            passengerDetail.setName(name);
            sb.append(passengerDetail.getName()).append(",");
            passengerDetails.add(passengerDetail);
        }
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
        // String sql = "select lastname from hrmresource where id in(" + ids + ")";
        //从携程人员同步表中获取正确的人名
        String sql = "select lastname from ctriphrm where workcode in (select workcode from hrmresource where id in(" + ids + "))";

        RecordSet rs = new RecordSet();
        rs.execute(sql);
        while (rs.next()) {
            sb.append(rs.getString(1)).append(",");
        }
        if (sb.toString().contains(SEQ_EN)) {
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
        return sbf.toString();
    }
}
