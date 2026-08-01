package com.banking.transactionservice.entity;

/**
 * Transaction lifecycle states.
 * Note: 'PROCCESSING' is intentionally kept as-is to match existing references in code.
 * Consider normalizing the spelling to 'PROCESSING' in a coordinated migration if persisted values
 * already exist in a production database.
 */
public enum TransactionStatus {

    PENDING,
    PROCCESSING,
    PENDING_VERIFICATION,
    COMPLETED,
    FAILED,
    FLAGGED,

}
