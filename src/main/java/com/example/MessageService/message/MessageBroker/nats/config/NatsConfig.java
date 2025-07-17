package com.example.MessageService.message.MessageBroker.nats.config;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo; // <-- IMPORT THIS
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("nats")
@Slf4j
public class NatsConfig {

    @Value("${nats.server.url}")
    private String natsUrl;

    @Value("${nats.stream.name}")
    private String streamName;

    @Value("${nats.stream.subjects}")
    private String streamSubjects;

    @Bean
    public Connection natsConnection() throws IOException, InterruptedException {
        log.info("Connecting to NATS server at {}", natsUrl);
        return Nats.connect(natsUrl);
    }

    @Bean
    public JetStream jetStream(Connection natsConnection) throws IOException {
        return natsConnection.jetStream();
    }

    @Bean
    public JetStreamManagement jetStreamManagement(Connection natsConnection) throws IOException {
        return natsConnection.jetStreamManagement();
    }

    @Bean
    public CommandLineRunner setupNatsStream(JetStreamManagement jetStreamManagement) {
        return args -> {
            // --- START OF FIX ---

            // Define the desired configuration for our stream
            StreamConfiguration streamConfig = StreamConfiguration.builder()
                    .name(streamName)
                    .subjects(streamSubjects)
                    .storageType(StorageType.File)
                    .build();

            StreamInfo streamInfo = null;
            try {
                // Check if the stream already exists
                streamInfo = jetStreamManagement.getStreamInfo(streamName);
            } catch (JetStreamApiException e) {
                if (e.getApiErrorCode() == 10059) { // 10059 == "stream not found"
                    log.info("NATS stream '{}' not found, will create it.", streamName);
                } else {
                    throw e; // Re-throw other unexpected errors
                }
            }

            if (streamInfo == null) {
                // Stream does not exist, so create it.
                jetStreamManagement.addStream(streamConfig);
                log.info("NATS stream '{}' created successfully.", streamName);
            } else {
                // Stream exists, so update it to match the current configuration.
                jetStreamManagement.updateStream(streamConfig);
                log.info("NATS stream '{}' updated successfully.", streamName);
            }

            // --- END OF FIX ---
        };
    }
}