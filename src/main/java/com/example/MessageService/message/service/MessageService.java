package com.example.MessageService.message.service;


// Entry Point for the Message Service (Router)
//If the message is immediate: It is sent directly to the Kafka Producer.
//If the message is scheduled: It is saved to the database for the MessageSchedulingService to find and process later.


public interface MessageService {

}