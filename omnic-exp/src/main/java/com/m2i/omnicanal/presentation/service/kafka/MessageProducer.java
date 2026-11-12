package com.m2i.omnicanal.presentation.service.kafka;

import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, TransactionDTO> transactionKafkaTemplate;

    public void sendMessage(String message, String topicName) {
        logSend(kafkaTemplate.send(topicName, message), message);
    }

    public void sendMessageTransactionEx(String op) {
        logSend(kafkaTemplate.send("transactionExtourne", op), op);
    }

    public void sendApi(String apiID) {
        logSend(kafkaTemplate.send("appelApi", apiID), apiID);
    }

    public void sendMessageTransaction(String id) {
        logSend(kafkaTemplate.send("transactionError", id), id);
    }

    public void sendMessageTransactionDiffere(TransactionDTO op) {
        logSend(transactionKafkaTemplate.send("transactionDiffere", op), String.valueOf(op));
    }

    private void logSend(CompletableFuture<? extends SendResult<?, ?>> future, String payload) {
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Sent message=[" + payload + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                System.out.println("Unable to send message=[" + payload + "] due to : " + ex.getMessage());
            }
        });
    }
}
