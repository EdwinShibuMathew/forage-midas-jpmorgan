package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessingService.class);
    
    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final IncentiveApiClient incentiveApiClient;
    
    public TransactionProcessingService(UserRepository userRepository, 
                                      TransactionRecordRepository transactionRecordRepository,
                                      IncentiveApiClient incentiveApiClient) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.incentiveApiClient = incentiveApiClient;
    }
    
    @Transactional
    public boolean processTransaction(Transaction transaction) {
        logger.info("Processing transaction: {}", transaction);
        
        // Validate sender exists
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        if (sender == null) {
            logger.warn("Transaction rejected: Invalid sender ID {}", transaction.getSenderId());
            return false;
        }
        
        // Validate recipient exists
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        if (recipient == null) {
            logger.warn("Transaction rejected: Invalid recipient ID {}", transaction.getRecipientId());
            return false;
        }
        
        // Validate sender has sufficient balance
        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("Transaction rejected: Insufficient balance. Sender {} has {}, needs {}", 
                       sender.getName(), sender.getBalance(), transaction.getAmount());
            return false;
        }
        
        // All validations passed - process the transaction
        logger.info("Transaction validation successful, processing transfer");
        
        // Call incentive API to get incentive amount
        double incentiveAmount = incentiveApiClient.getIncentive(transaction);
        logger.info("Received incentive amount: {} for transaction: {}", incentiveAmount, transaction);
        
        // Update balances - deduct from sender, add transaction amount + incentive to recipient
        float newSenderBalance = sender.getBalance() - (float) transaction.getAmount();
        float newRecipientBalance = recipient.getBalance() + (float) transaction.getAmount() + (float) incentiveAmount;
        
        sender.setBalance(newSenderBalance);
        recipient.setBalance(newRecipientBalance);
        
        // Save updated user records
        userRepository.save(sender);
        userRepository.save(recipient);
        
        // Create and save transaction record with incentive
        TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount);
        transactionRecordRepository.save(transactionRecord);
        
        logger.info("Transaction completed successfully: {} -> {} amount: {}, incentive: {}", 
                   sender.getName(), recipient.getName(), transaction.getAmount(), incentiveAmount);
        logger.info("Updated balances - {}: {}, {}: {}", 
                   sender.getName(), newSenderBalance, recipient.getName(), newRecipientBalance);
        
        return true;
    }
    
    public UserRecord getUserByName(String name) {
        return userRepository.findByName(name).orElse(null);
    }
    
    public UserRecord getUserById(long id) {
        return userRepository.findById(id);
    }
}