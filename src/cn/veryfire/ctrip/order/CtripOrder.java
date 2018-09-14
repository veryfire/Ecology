package cn.veryfire.ctrip.order;

import cn.veryfire.ctrip.ticket.CorpTicket;
import cn.veryfire.ctrip.util.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import corp.openapicalls.contract.Authentification;
import corp.openapicalls.contract.ordercontroller.SearchOrderRequest;
import corp.openapicalls.contract.ordercontroller.ticket.OrderSearchTicketResponse;
import weaver.general.BaseBean;

/**
 * 订单处理以及数据获取类
 *
 * @author lichu
 */
public class CtripOrder extends BaseBean {
    private String appKey;
    private String appSecurity;
    private String version;

    public CtripOrder(String appkey, String appSecurity, String version) {
        this.appKey = appkey;
        this.appSecurity = appSecurity;
        this.version = version;
    }

    /**
     * 通过行程号和日期范围获取订单信息
     *
     * @param journeyNo 行程号
     * @param dateFrom  开始日期
     * @param dateTo    结束日期
     * @return 详细订单信息
     */
    public String SearchOrders(String journeyNo, String dateFrom, String dateTo) {
        CorpTicket getCorpTicket = new CorpTicket();

        SearchOrderRequest searchOrderRequest = new SearchOrderRequest();

        OrderSearchTicketResponse orderSearchTicket = getCorpTicket.getOrderSearchTicket(this.appKey, this.appSecurity, this.version);
        String ticket = orderSearchTicket.getTicket();
        Authentification authentfication = new Authentification(this.appKey, ticket);
        searchOrderRequest.setAuth(authentfication);
        // 行程号
        searchOrderRequest.setJourneyNo(journeyNo);
        searchOrderRequest.setDateFrom(dateFrom);
        searchOrderRequest.setDateTo(dateTo);
        String postData = HttpUtil.PostData("https://ct.ctrip.com/switchapi/Order/SearchOrder", JSONObject.toJSONString(searchOrderRequest));
        return postData;
    }

}
