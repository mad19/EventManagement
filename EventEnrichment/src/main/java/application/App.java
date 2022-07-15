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

import adapters.KafkaProcessor;
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
import params.*;
import utils.ConsumerCreator;
import utils.ProducerCreator;
import utils.RedisConnector;
import utils.VariablesUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class App {

    private static final Logger log = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {

        // Запускаем приемщик в тестовом режиме на получение сырого события
        log.info("Starting application");

        //Тестовый запуск с чтением событие из json файла вместо Kafka
        //runConsumerTest();

        //Запуск с чтением событий из Kafka
        runConsumer();


    }

    // Вычитывание файла
    public static String readFileAsString(String file) throws Exception{
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    // Тестовый вариант запуска, если Kafka не работает
    /*
    static void runConsumerTest() throws Exception {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String file = "src/test/java/application/kafka_event.json";
        String json = readFileAsString(file);
        KafkaEvent kafkaEvent = gson.fromJson(json, KafkaEvent.class);
        log.info("KafkaEvent: " + kafkaEvent.toString());

        try {
            RedisClient client = RedisConnector.getClient();
            StatefulRedisConnection<String, String> connection = client.connect();
            RedisCommands<String, String> commands = connection.sync();

            switch (kafkaEvent.getAlert_type()) {
                case "Событие":

                    String alert_info = commands.get("ALERT:" + kafkaEvent.getAlert_id());

                    Template template = gson.fromJson(alert_info, Template.class);
                    log.info("Template: " + template.toString());

                    ArrayList variables = template.parseTemplate();

                    if (!variables.isEmpty()) {
                        Props props = template.getVariables(variables, kafkaEvent, connection);
                        Event event = new Event(template, props, kafkaEvent);
                        //log.info("EVENT: " + event.toString());
                        log.info("EVENT: " + event.toJson());

                    } else {
                        Event event = new Event(template);
                    }
                    break;

                case "Email":

                    //String email_alert_info = commands.get("ALERT:" + kafkaEvent.getAlert_id());
                    String email_alert_info = null;
                    GsonBuilder builderTest = new GsonBuilder();
                    Gson gsonTest = builder.create();
                    String fileTest = "src/test/java/application/email.json";
                    String jsonTest = readFileAsString(file);


                    //TemplateEmail templateEmail = gson.fromJson(email_alert_info, TemplateEmail.class);
                    TemplateEmail templateEmail = gson.fromJson(jsonTest, TemplateEmail.class);
                    //templateEmail.getProps().put("theme", "Тестовая тема 2. Метрика <metric>");
                    log.info("Template email: " + templateEmail);

                    ArrayList email_variables = templateEmail.parseTemplate();

                    if (!email_variables.isEmpty()) {
                        Props props = templateEmail.getVariables(email_variables, kafkaEvent, connection);
                        log.info(props.getProps_list());
                        EmailEvent emailEvent = new EmailEvent(templateEmail, props, kafkaEvent);
                        //log.info(emailEvent);
                        log.info(emailEvent.toJson());
                        break;

                    } else {
                        log.error("Error. No variables");
                    }

                case "SMS":
                    break;
                default:

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

     */



    // Запуск обработки поступающих из Kafka событий
    static void runConsumer() {

        log.info("Creating kafka consumer...");
        Consumer<Long, String> consumer = null;
        try {
            consumer = ConsumerCreator.createConsumer();
            log.info("Consumer created.");
        } catch (Exception e) {
            log.error("Consumer error: " + e);
            return;
        }

        log.info("Creating kafka producer...");
        Producer producer = null;
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

        //consumer.close();
        //producer.close();
        //client.shutdown();

    }

}