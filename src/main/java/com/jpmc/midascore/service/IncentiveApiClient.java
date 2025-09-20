package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Service
public class IncentiveApiClient {
    private static final Logger logger = LoggerFactory.getLogger(IncentiveApiClient.class);
    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";
    
    private final RestTemplate restTemplate;
    
    public IncentiveApiClient() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Calls the incentive API to calculate incentive for a transaction
     * @param transaction The transaction to calculate incentive for
     * @return The incentive amount, or 0.0 if API call fails
     */
    public double getIncentive(Transaction transaction) {
        try {
            logger.debug("Calling incentive API for transaction: {}", transaction);
            
            Incentive incentive = restTemplate.postForObject(
                INCENTIVE_API_URL, 
                transaction, 
                Incentive.class
            );
            
            if (incentive != null) {
                logger.debug("Received incentive: {}", incentive.getAmount());
                return incentive.getAmount();
            } else {
                logger.warn("Incentive API returned null response for transaction: {}", transaction);
                return 0.0;
            }
            
        } catch (RestClientException e) {
            logger.error("Failed to call incentive API for transaction: {}. Error: {}", 
                        transaction, e.getMessage());
            return 0.0; // Return 0 incentive if API call fails
        }
    }
}