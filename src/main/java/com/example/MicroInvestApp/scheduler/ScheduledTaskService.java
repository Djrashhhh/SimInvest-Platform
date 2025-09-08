package com.example.MicroInvestApp.scheduler;

import com.example.MicroInvestApp.service.order.TransactionService;
import com.example.MicroInvestApp.domain.orders.Transaction;
import com.example.MicroInvestApp.repositories.orders.TransactionRepository;
import com.example.MicroInvestApp.events.TransactionSettlementEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Enhanced Settlement Processing Service
 * Supports both batch processing and individual transaction settlement scheduling
 */
@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final TaskScheduler taskScheduler;

    // Track scheduled settlement tasks to avoid duplicates
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    public ScheduledTaskService(TransactionService transactionService,
                                TransactionRepository transactionRepository,
                                @Qualifier("settlementTaskScheduler")TaskScheduler taskScheduler) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.taskScheduler = taskScheduler;

    }

    // ✅ ADD EVENT LISTENER METHOD
    /**
     * Handle transaction settlement events
     * This breaks the circular dependency by using events instead of direct injection
     */
    @EventListener
    @Async // Process asynchronously to avoid blocking transaction creation
    public void handleTransactionSettlementEvent(TransactionSettlementEvent event) {
        logger.info("Received settlement event for transaction {} at {}",
                event.getTransactionId(), event.getSettlementDateTime());

        scheduleTransactionSettlement(event.getTransactionId(), event.getSettlementDateTime());
    }

    // ==================== ENHANCED INDIVIDUAL SETTLEMENT ====================

    /**
     * Schedule settlement for a specific transaction exactly at T+2
     * This method should be called when a transaction is created
     */
    public void scheduleTransactionSettlement(Long transactionId, LocalDateTime settlementDateTime) {
        // Convert LocalDateTime to Date for TaskScheduler
        ZonedDateTime zonedDateTime = settlementDateTime.atZone(ZoneId.systemDefault());
        Date settlementDate = Date.from(zonedDateTime.toInstant());

        // Check if settlement date is in the future
        if (settlementDate.before(new Date())) {
            logger.warn("Settlement date {} is in the past for transaction {}. Processing immediately.",
                    settlementDateTime, transactionId);
            processIndividualTransaction(transactionId);
            return;
        }

        // Cancel existing scheduled task if any
        ScheduledFuture<?> existingTask = scheduledTasks.get(transactionId);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
            logger.info("Cancelled existing settlement task for transaction {}", transactionId);
        }

        // Schedule new settlement task
        ScheduledFuture<?> settlementTask = taskScheduler.schedule(() -> {
            logger.info("Processing scheduled settlement for transaction {} at exact T+2: {}",
                    transactionId, LocalDateTime.now());
            processIndividualTransaction(transactionId);

            // Clean up completed task
            scheduledTasks.remove(transactionId);
        }, settlementDate);

        // Store the scheduled task
        scheduledTasks.put(transactionId, settlementTask);

        logger.info("Scheduled settlement for transaction {} at {}", transactionId, settlementDateTime);
    }

    /**
     * Process settlement for an individual transaction
     */
    private void processIndividualTransaction(Long transactionId) {
        try {
            // Get the transaction
            var transactionOpt = transactionRepository.findById(transactionId);

            if (transactionOpt.isEmpty()) {
                logger.warn("Transaction {} not found for scheduled settlement", transactionId);
                return;
            }

            Transaction transaction = transactionOpt.get();

            // Check if transaction is eligible for settlement
            if (!transaction.getTransactionStatus().name().equals("PENDING")) {
                logger.info("Transaction {} is not in PENDING status ({}), skipping settlement",
                        transactionId, transaction.getTransactionStatus());
                return;
            }

            // Process the settlement
            logger.info("Settling individual transaction: {}", transactionId);
            transaction.markAsCompleted();
            transactionRepository.save(transaction);

            logger.info("Successfully settled transaction {} individually", transactionId);

        } catch (Exception e) {
            logger.error("Failed to settle individual transaction {}: {}", transactionId, e.getMessage(), e);

            // Mark as failed
            try {
                var transactionOpt = transactionRepository.findById(transactionId);
                if (transactionOpt.isPresent()) {
                    Transaction transaction = transactionOpt.get();
                    transaction.markAsFailed("Individual settlement failed: " + e.getMessage());
                    transactionRepository.save(transaction);
                }
            } catch (Exception saveException) {
                logger.error("Failed to mark transaction {} as failed: {}", transactionId, saveException.getMessage());
            }
        }
    }

    /**
     * Cancel scheduled settlement for a transaction (e.g., if transaction is cancelled)
     */
    public void cancelScheduledSettlement(Long transactionId) {
        ScheduledFuture<?> existingTask = scheduledTasks.remove(transactionId);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
            logger.info("Cancelled scheduled settlement for transaction {}", transactionId);
        }
    }

    // ==================== BATCH PROCESSING (BACKUP) ====================

    /**
     * Daily batch processing as a backup to catch any missed settlements
     * Runs every day at 2 AM to avoid conflicts with business hours
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2:00 AM every day
    public void processMissedSettlements() {
        logger.info("Starting daily batch settlement processing (backup)...");
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Transaction> missedTransactions = transactionRepository.findUnsettledTransactions(now);

            if (missedTransactions.isEmpty()) {
                logger.info("No missed settlements found");
                return;
            }

            logger.info("Found {} transactions with missed settlements", missedTransactions.size());

            int processed = 0;
            for (Transaction transaction : missedTransactions) {
                try {
                    transaction.markAsCompleted();
                    transactionRepository.save(transaction);
                    processed++;
                    logger.info("Processed missed settlement for transaction {}", transaction.getTransactionId());
                } catch (Exception e) {
                    logger.error("Failed to process missed settlement for transaction {}: {}",
                            transaction.getTransactionId(), e.getMessage());
                }
            }

            logger.info("Batch settlement processing completed: {}/{} transactions processed",
                    processed, missedTransactions.size());

        } catch (Exception e) {
            logger.error("Daily batch settlement processing failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleanup completed tasks every hour to prevent memory leaks
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupCompletedTasks() {
        int initialSize = scheduledTasks.size();
        scheduledTasks.entrySet().removeIf(entry -> entry.getValue().isDone());
        int removedTasks = initialSize - scheduledTasks.size();

        if (removedTasks > 0) {
            logger.debug("Cleaned up {} completed settlement tasks", removedTasks);
        }
    }

    // ==================== SYSTEM MONITORING ====================

    /**
     * Get current scheduled settlement count
     */
    public int getScheduledSettlementCount() {
        return scheduledTasks.size();
    }

    /**
     * Manual trigger for emergency settlement processing
     */
    public void processEmergencySettlements() {
        logger.warn("Emergency settlement processing triggered manually");
        processMissedSettlements();
    }

    /**
     * Initialize settlements for existing transactions on startup
     * This runs once when the application starts
     */
    @PostConstruct
    public void initializeExistingTransactionSettlements() {
        try {
            logger.info("Initializing scheduled settlements for existing transactions...");

            // Find all pending transactions
            List<Transaction> pendingTransactions = transactionRepository
                    .findByTransactionStatusOrderByTransactionDateDesc(
                            com.example.MicroInvestApp.domain.enums.TransactionStatus.PENDING);

            int scheduled = 0;
            LocalDateTime now = LocalDateTime.now();

            for (Transaction transaction : pendingTransactions) {
                // Only schedule if settlement date is in the future
                if (transaction.getSettlementDate() != null &&
                        transaction.getSettlementDate().isAfter(now)) {

                    scheduleTransactionSettlement(transaction.getTransactionId(),
                            transaction.getSettlementDate());
                    scheduled++;
                } else {
                    // Settlement date has passed, process immediately
                    logger.info("Processing overdue settlement for transaction {}",
                            transaction.getTransactionId());
                    processIndividualTransaction(transaction.getTransactionId());
                }
            }

            logger.info("Initialized {} scheduled settlements on startup", scheduled);

        } catch (Exception e) {
            logger.error("Failed to initialize existing transaction settlements: {}", e.getMessage(), e);
        }
    }
}