package cn.veryfire.ctrip.ticket;

import net.sf.json.JSONSerializer;
import weaver.general.BaseBean;
import cn.veryfire.ctrip.util.HttpUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import corp.openapicalls.contract.h5ssologin.H5SSOLoginModel;
import corp.openapicalls.contract.ordercontroller.ticket.OrderSearchTicketResponse;
import corp.openapicalls.contract.ssologin.SSOLoginModel;
import corp.openapicalls.contract.ssologin.TicketModel;
import corp.openapicalls.contract.ssologin.TicketModelResponse;
import corp.openapicalls.contract.ticket.TicketResponse;
import corp.openapicalls.service.h5ssologin.H5SSOLoginService;
import corp.openapicalls.service.ssologin.SSOLoginService;
import corp.openapicalls.service.ticket.CorpTicketService;

public class CorpTicket extends BaseBean {

	/**
	 * 获取sso登陆的ticket
	 * 
	 * @param ticketModel
	 * @return
	 */
	public TicketModelResponse getSSOTicket(TicketModel ticketModel) {
		if (ticketModel != null) {
			return CorpTicketService.getSSOLoginTicket(ticketModel.getAppKey(),
					ticketModel.getAppSecurity(),
					String.valueOf(ticketModel.getVersion()),
					ticketModel.getTokenType(), ticketModel.getGroupID());
		}
		return null;
	}

	/**
	 * 获取人事批量更新ticket
	 * 
	 * @param appKey
	 * @param appSecurity
	 * @param version
	 * @return
	 */
	public OrderSearchTicketResponse getEmployeeSyncTicket(String appKey,
			String appSecurity, String version) {
		return CorpTicketService.getEmployeeSyncTicket(appKey, appSecurity,
				version);

	}

	/**
	 * 获取员工是否开卡的ticket
	 * 
	 * @param url
	 *            String url = "https://ct.ctrip.com/SwitchAPI/Order/Ticket";
	 * @param appKey
	 * @param appSecurity
	 * @return 返回是否开卡的ticket
	 */
	public static String IsOpenedCard(String url, String appKey,
			String appSecurity) {

		String data = HttpUtil.PostData(url, "{\"appKey\":\"" + appSecurity
				+ "\",\"appSecurity\":\"" + appSecurity + "\"}");
		JSONObject json = (JSONObject) JSON.parse(data);

		String ticket = (String) json.get("Ticket");
		return ticket;
	}

	/**
	 * 获取提前审批的ticket
	 * https://ct.ctrip.com/switchapi/approval.svc/rest/setapproval
	 * 
	 * @param url
	 * @param appKey
	 * @param appSecurity
	 * @return 返回ticket
	 */
	public static String getSetApproval(String url, String appKey,
			String appSecurity) {
		// 请求报文JSON
		String ticketPostString = "{\"appKey\":\"" + appKey
				+ "\",\"appSecurity\":\"" + appSecurity + "\"}";
		// POST
		String ticketResponse = HttpUtil.PostData(url, ticketPostString);

		// 构造JSON对象,需要导入net.sf.json包
		JSONObject jsonObject = (JSONObject) JSONSerializer
				.toJSON(ticketResponse);

		// 获取Ticket
		String ticket = (String) ((JSONObject) jsonObject.get("TicketResult"))
				.get("Ticket");
		return ticket;
	}

	public TicketResponse getSetApproval(String appKey, String appSecurity) {
		TicketResponse ticketResponse = CorpTicketService.getOrderAuditTicket(
				appKey, appSecurity, "1.0");
		return ticketResponse;
	}

	/**
	 * 获取订单查询Ticket
	 * 
	 * @param appKey
	 * @param appSecurity
	 * @param version
	 * @return
	 */
	public OrderSearchTicketResponse getOrderSearchTicket(String appKey,
			String appSecurity, String version) {
		return CorpTicketService.getOrderSearchTicket(appKey, appSecurity,
				version);
	}

	/**
	 * 传入登陆请求参数,返回单点登陆的页面,写入到response 实现自动登陆
	 * 
	 * @param ssoLoginModel
	 * @return
	 */
	public String ssoLoginContent(TicketModel ticketModel,
			SSOLoginModel ssoLoginModel) {
		SSOLoginService loginService = new SSOLoginService();
		return loginService.SSOLogin(ssoLoginModel);

	}

	/**
	 * 传入登陆请求参数,返回单点登陆的H5页面
	 * 
	 * @param loginmodel
	 * @return
	 */
	public String ssoLoginContentH5(H5SSOLoginModel loginmodel) {
		H5SSOLoginService h5ssoLoginService = new H5SSOLoginService();
		String loginUrl = h5ssoLoginService.getLoginUrl();
		System.err.println(loginUrl);
		return h5ssoLoginService.H5SSOLogin(loginmodel);
	}
}
