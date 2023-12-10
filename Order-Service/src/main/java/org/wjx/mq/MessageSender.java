package org.wjx.mq;

import cn.hutool.log.Log;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ClientException;
import org.wjx.Exception.ServiceException;

import javax.annotation.PostConstruct;
import javax.validation.metadata.Scope;

/**
 * @author xiu
 * @create 2023-12-08 16:03
 */
@Component
public class MessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    @PostConstruct
    public void init(){

    }


    public void sendMessage(String exchange, String routingKey, Object message) {
        sendMessageWithRetry(exchange, routingKey, message, 1);
    }

    private void sendMessageWithRetry(String exchange, String routingKey, Object message, int retryCount) {
        CorrelationData correlationData = new CorrelationData(String.valueOf(retryCount));

        rabbitTemplate.setConfirmCallback((correlationDataCallback, ack, cause) -> {
            if (!ack) {
                // 消息发送失败
                handleFailure(correlationDataCallback, exchange, routingKey, message, retryCount);
            }
            // 可以在此处添加成功的回调逻辑
        });

        // 发送消息
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }

    private void handleFailure(CorrelationData correlationData, String exchange, String routingKey, Object message, int retryCount) {
        if (retryCount <= MAX_RETRY_ATTEMPTS) {
            // 重试发送消息
            sendMessageWithRetry(exchange, routingKey, message, retryCount + 1);
        } else {
            // 达到最大重试次数，可以进行相应的处理，例如记录日志或者发送到死信队列
            System.out.println("消息发送失败，达到最大重试次数，correlationData：" + correlationData);
        }
    }
}