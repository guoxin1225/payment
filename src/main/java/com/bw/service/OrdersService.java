package com.bw.service;

import com.bw.pojo.Orders;
import com.bw.utils.OrderStatusEnum;

import java.util.List;

/**
 * 订单操作 service
 *
 */
public interface OrdersService {

	/**
	 * 新增订单
	 * @param order
	 */
	public void saveOrder(Orders order);
	
	/**
	 * 
	 * @Title: OrdersService.java
	 * @Description: 修改叮当状态，改为 支付成功，已付款; 同时新增支付流水
	 * Copyright: Copyright (c) 2017
	 * Company:FURUIBOKE.SCIENCE.AND.TECHNOLOGY
	 * 
	 * @version V1.0
	 */
	public void updateOrderStatus(String orderId, String alpayFlowNum, String paidAmount);
	
	/**
	 * 获取订单
	 * @param orderId
	 * @return
	 */
	public Orders getOrderById(String orderId);

	/**
	 * 获取状态为待付款状态的订单
	 * @return
	 */
	List<Orders> getOrdersByStatus();

	/**
	 * 交易失败的 修改订单状态
	 * @param failure
	 */
	void updateOrderStatusByError(Orders orders);
}
