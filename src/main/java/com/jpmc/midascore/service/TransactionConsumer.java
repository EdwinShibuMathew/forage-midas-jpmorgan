package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {
    private static final Logger logger = LoggerFactory.getLogger(TransactionConsumer.class);
    private int transactionCount = 0;
    
    private final TransactionProcessingService transactionProcessingService;
    
    public TransactionConsumer(TransactionProcessingService transactionProcessingService) {
        this.transactionProcessingService = transactionProcessingService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "transaction-consumer-group")
    public void consumeTransaction(Transaction transaction) {
        transactionCount++;
        logger.info("Received transaction #{}: {}", transactionCount, transaction);
        
        // Log the amount for debugging purposes - especially the first 4
        if (transactionCount <= 4) {
            logger.info("*** DEBUG: Transaction #{} amount is: {} ***", transactionCount, transaction.getAmount());
        }
        
        // For debugging - set a breakpoint here to examine transaction details
        System.out.println("Processing transaction: " + transaction);
        
        // Process the transaction through the validation and persistence service
        boolean processed = transactionProcessingService.processTransaction(transaction);
        if (processed) {
            logger.info("Transaction #{} processed successfully", transactionCount);
        } else {
            logger.warn("Transaction #{} was rejected", transactionCount);
        }
    }
}