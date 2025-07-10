package com.example.MessageService.message.MessagingSystem.kafka.partitioner;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.utils.Utils;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Profile("kafka")
public class TenantIdPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int numPartitions = cluster.partitionsForTopic(topic).size();

        if (keyBytes != null) {
            return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
        }
        return Utils.toPositive(Utils.murmur2(valueBytes)) % numPartitions;
    }


    @Override
    public void close() {}
    @Override
    public void configure(Map<String, ?> configs) {}
}