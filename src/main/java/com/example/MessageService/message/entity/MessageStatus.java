package com.example.MessageService.message.entity;

public enum MessageStatus {
    SCHEDULED,   // Waiting for its scheduled time
    PENDING,     // Sent to Kafka, waiting for a consumer to pick it up
    SENT,        // Successfully sent by the provider (e.g., SendGrid)
    FAILED,      // Sending failed
    DELIVERED,   // Confirmed delivery via webhook
    RETRYING     // In a retry state
}