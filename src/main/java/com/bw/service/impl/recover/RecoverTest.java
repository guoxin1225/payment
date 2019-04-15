package com.bw.service.impl.recover;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.bw.pojo.Orders;
import com.bw.service.OrdersService;
import com.bw.utils.AlipayConfig;
import com.bw.utils.OrderStatusEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 冲正任务
 */
public class RecoverTest implements Runnable{

    //订单号
    private String orderNum;

    private static final int timeCount = 5;

    //
    private OrdersService ordersService;
    //创建一个不可变的map集合
    private static final Map<Integer, Integer> map = new HashMap<>();
    //通过构造函数初始化订单号和ordreService类
    public RecoverTest(String orderNum, OrdersService ordersService) {
        this.orderNum = orderNum;
        this.ordersService = ordersService;
    }

    @Override
    public void run() {
        for(int i=0;i<timeCount;i++){
            AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);
            AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
            request.setBizContent("{" +
                    "\"out_trade_no\":\""+orderNum+"\"" + "}");
            AlipayTradeCancelResponse response = null;
            try {
                response = alipayClient.execute(request);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            //如果冲正成功 就修改数据库状态为 60
            if(response.isSuccess()){
                Orders orders = new Orders();
                orders.setOrderNum(orderNum);
                orders.setOrderStatus(OrderStatusEnum.RECOVER.key);
                ordersService.updateOrderStatusByError(orders);
                break;
            }
            //冲正一次停顿一段时间
            try {
                Thread.sleep(map.get(i));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //初始化map集合
    static {

        map.put(0,60);
        map.put(1,300);
        map.put(2,900);
    }
}
