/*
##################################################
################## INFORMATION ###################
##################################################
# @author: Mishin Andrey                         #
# E-mail: amishin@systematic.ru                  #
# Version 1.4                                    #
# Description: Enrichment main application       #
##################################################
*/

package application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import params.KafkaEvent;
import utils.ConsumerCreator;
import utils.ProducerCreator;
import utils.RedisConnector;

import java.nio.file.Files;
import java.nio.file.Paths;

public class App {

    private static final Logger log = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {

        log.info("Starting application");

        //Запуск с чтением событий из Kafka
        runConsumer();

    }


    // Запуск обработки поступающих из Kafka событий
    static void runConsumer() {

        log.info("Creating kafka consumer...");
        Consumer<Long, String> consumer;
        try {
            consumer = ConsumerCreator.createConsumer();
            log.info("Consumer created.");
        } catch (Exception e) {
            log.error("Consumer error: " + e);
            return;
        }

        log.info("Creating kafka producer...");
        Producer producer;
        try {
            producer = ProducerCreator.createProducer();
            log.info("Producer created");
        } catch (Exception e) {
            log.error("Producer error: " + e);
            return;
        }


        //Создание подключения к кэш-сервису Redis
        log.info("Creating connection to Redis...");
        RedisClient client;
        try {
            client = RedisConnector.getClient();
            log.info("Connection created");
        } catch (Exception e) {
            log.error("Redis connection error: " + e);
            return;
        }

        StatefulRedisConnection<String, String> connection = client.connect();
        RedisCommands<String, String> commands = connection.sync();

        while (true) {
            final ConsumerRecords<Long, String> consumerRecords = consumer.poll(100);

            // Цикл обработки всех полученных сообщений
            Producer finalProducer = producer;
            consumerRecords.forEach(record -> {

                // Обрабатываем Json как экземпляр класса KafkaEvent
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                KafkaEvent kafkaEvent;
                log.info("Receiving event from Kafka...");
                try {
                    kafkaEvent = gson.fromJson(record.value(), KafkaEvent.class);
                    try {
                        Double double_value = Double.parseDouble(kafkaEvent.getMetric_value());
                        kafkaEvent.setMetric_value(String.format("%.2f",double_value));
                    } catch (Exception e) {
                        log.warn("Converting fails: " + e.getMessage());
                    }
                    log.info("Kafka event received");
                    log.debug("KafkaEvent: " + kafkaEvent.toString());
                } catch (Exception e) {
                    log.error("Cannot format kafka event to KafkaEvent class");
                    return;
                }

                EventProcessor eventProcessor = new EventProcessor(kafkaEvent, finalProducer, commands);
                eventProcessor.process();

            });
            consumer.commitAsync();

        }

    }

}