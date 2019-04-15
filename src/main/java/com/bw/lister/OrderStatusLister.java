package com.bw.lister;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.bw.pojo.Orders;
import com.bw.service.OrdersService;
import com.bw.utils.AlipayConfig;
import com.bw.utils.OrderStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 监听数据库中未完成的订单修改状态
 */
@Component
@EnableScheduling
public class OrderStatusLister {

    @Autowired
    private OrdersService ordersService;

    private Logger logger = LoggerFactory.getLogger(OrderStatusLister.class);
    /**
     * 获取数据中的所有待完成的订单集合
     * @return
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void listenOrdersStatus() throws AlipayApiException {
        //获取状态为10而且两天之内的订单
        List<Orders> orders = ordersService.getOrdersByStatus();
        logger.info("执行定时器任务");

        for (Orders order:orders) {
            AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{" +
                    "\"out_trade_no\":\""+order.getOrderNum()+"\"" + "}");
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                /**
                 *  如果支付宝后台支付成功那么就修改数据库状态为 20 已付款 同时增加流水
                 *  updateOrderStatus参数：订单号 2、支付宝交易号（流水号） 3、订单金额
                 */
                ordersService.updateOrderStatus(response.getOutTradeNo(),response.getTradeNo(),response.getTotalAmount());
                logger.info("添加流水"+response.getTradeNo());
            } else {
                //如果支付宝后台交易失败 修改状态为 50 交易失败
                order.setOrderStatus(OrderStatusEnum.FAILURE.key);
                ordersService.updateOrderStatusByError(order);
                logger.info("修改"+order.getOrderNum()+"状态为"+OrderStatusEnum.FAILURE.key);
            }
        }


    }



}
