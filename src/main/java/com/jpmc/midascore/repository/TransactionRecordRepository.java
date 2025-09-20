package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionRecordRepository extends CrudRepository<TransactionRecord, Long> {
    
    List<TransactionRecord> findBySender(UserRecord sender);
    
    List<TransactionRecord> findByRecipient(UserRecord recipient);
    
    List<TransactionRecord> findBySenderOrRecipient(UserRecord sender, UserRecord recipient);
}