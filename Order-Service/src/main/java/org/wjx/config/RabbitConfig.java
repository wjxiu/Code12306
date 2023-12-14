package org.wjx.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiu
 * @create 2023-12-07 20:10
 */
@Configuration
public class RabbitConfig {
    @Autowired
    CachingConnectionFactory cachingConnectionFactory;
    /**
     * 设置订单延时关闭时间
     */
    public  static final int DELAYTIME = 15 * 60 * 1000;


    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }
//-----------------延迟交换机
    @Bean(exchange_delayed)
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(exchange_delayed, "x-delayed-message", true, false, args);
    }


    //  --------------------------------------------------------------------------------  创建订单
    public static final String creatOrder_delayed_queue = "delay.create.order.queue";
    public static final String exchange_delayed = "delay.exchange";
    public static final String createOrder_routingkey = "delay.create";
    @Bean(creatOrder_delayed_queue)
    public Queue delayCreateOrderQueue() {
        return QueueBuilder.durable(creatOrder_delayed_queue).build();
    }

    @Bean
    public Binding bindingNotify(@Qualifier(creatOrder_delayed_queue) Queue delayCreateOrderQueue,
                                 @Qualifier(exchange_delayed) CustomExchange customExchange) {
        return BindingBuilder.bind(delayCreateOrderQueue).to(customExchange).with(createOrder_routingkey).noargs();
    }

    //    ----------------------------------------------------------------------------------------------------

    //    支付绑定延迟队列
    public static final String pay_Queue = "delay.pay.queue";
    public static final String pay_routingkey = "pay.routingKey";

    @Bean("payQueue")
    public Queue pay_Queue(@Qualifier(exchange_delayed) CustomExchange delayExchange) {
        return QueueBuilder.durable(pay_Queue).build();
    }

    @Bean
    public Binding payBinding(@Qualifier("payQueue") Queue pay_Queue,
                              @Qualifier(exchange_delayed) CustomExchange customExchange) {
        return  BindingBuilder.bind(pay_Queue).to(customExchange).with(pay_routingkey).noargs();
    }

//    ------------------------------------------------------------------------------------

    //refund 不绑着延迟队列
    public static final String refund_exchange = "refund.exchange";
    public static final String refund_Queue = "refund.queue";
    public static final String refund_routingkey = "refund.routingKey";


    @Bean("refundExchange")
    public DirectExchange refundExchange() {
        return new DirectExchange(refund_exchange, true, false);
    }

    @Bean("refunQueue")
    public Queue refunQueue() {
        return QueueBuilder.durable(refund_Queue).build();
    }

    @Bean
    public Binding refunbind(@Qualifier("refundExchange") DirectExchange refundExchange, @Qualifier("refunQueue") Queue refunQueue) {
        return BindingBuilder.bind(refunQueue).to(refundExchange).with(refund_routingkey);
    }



}
