package utils;

import adapters.KafkaProcessor;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerCreator {

    public static Producer<String, String> createProducer(){

        final Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaProcessor.KAFKA_BROKERS);
        //props.put(ProducerConfig.ACKS_CONFIG, KafkaProcessor.ACKS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        final Producer<String, String> producer = new KafkaProducer<String, String>(props);

        return producer;

    }

    public static String sendMessage(Producer producer, String topic, String message)
            throws ExecutionException, InterruptedException {

        RecordMetadata recordMetadata = (RecordMetadata) producer.send(
                new ProducerRecord(topic, message)).get();

        if (recordMetadata.hasOffset())
            return ("Message sent successfully");

        return ("Message sending error");
    }
}
