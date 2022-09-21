package application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.kafka.clients.producer.Producer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import params.*;
import utils.ProducerCreator;
import utils.VariablesUtil;

import java.util.HashSet;

@Data
@AllArgsConstructor
public class EventProcessor {

    private static final Logger log = LogManager.getLogger(EventProcessor.class.getName());

    private KafkaEvent kafkaEvent;
    private RedisCommands<String, String> commands;
    private Producer producer;
    private static final String topicName = "processed_events";
    private static final GsonBuilder builder = new GsonBuilder();
    public static final Gson gson = builder.create();

    public EventProcessor(KafkaEvent kafkaEvent, Producer finalProducer, RedisCommands<String, String> commands) {
        this.kafkaEvent = kafkaEvent;
        this.commands = commands;
        this.producer = finalProducer;
    }


    public void process() {


        try {
            // Обработка события по типам (Событие, Email, SMS)
            switch (kafkaEvent.getAlert_type()) {
                case "Событие":
                    processEvent();

                case "Email":
                    processEmail();

                case "SMS":
                    break;
                default:

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void processEvent() {
        log.info("Get alert info from Redis with format: ALERT: " + kafkaEvent.getAlert_id());
        String alert_info = getAlertInfo();

        log.info("Formatting alert info to Template class");
        Template template = gson.fromJson(alert_info, Template.class);

        Props props = getProps(template);

        log.info("Creating event");
        Event event;
        try {
            event = new Event(template, props, kafkaEvent);
            log.info("Successful");
            log.debug("Event to kafka: " + event.toJson());
        } catch (Exception e) {
            log.error("Event creating end with error: " + e.getMessage());
            return;
        }

        log.debug("Event to kafka: " + event.toJson());
        try {
            ProducerCreator.sendMessage(producer, topicName, event.toJson());
        } catch (Exception e) {
            log.error("Event didn't send to kafka: " + e.getMessage());
        }

    }


    private void processEmail() {

        log.info("Get alert info from Redis with format: ALERT: " + kafkaEvent.getAlert_id());
        String email_alert_info = getAlertInfo();

        TemplateEmail templateEmail = gson.fromJson(email_alert_info, TemplateEmail.class);
        log.debug("Template email: " + templateEmail);

        Props props = getProps(templateEmail);

        EmailEvent emailEvent;
        log.info("Creating email event");
        try {
            emailEvent = new EmailEvent(templateEmail, props, kafkaEvent);
            log.info("Successful");
            log.debug("Email event to kafka: " + emailEvent.toJson());
        } catch (Exception e) {
            log.error("EmailEvent creating end with error: " + e.getMessage());
            return;
        }

        log.debug("Event to kafka: " + emailEvent.toJson());
        try {
            ProducerCreator.sendMessage(producer, topicName, emailEvent.toJson());
        } catch (Exception e) {
            log.error("Event didn't send to kafka: " + e.getMessage());
        }

    }


    private String getAlertInfo() {
        String alert_info;

        try {
            alert_info = commands.get("DIM:ALERT:" + kafkaEvent.getAlert_id());
            log.debug("Alert info: " + alert_info);
            log.info("Successful");
        } catch (Exception e) {
            log.error("Alert info not found. " + e);
            return null;
        }

        return alert_info;
    }


    private Props getProps(Template template) {

        log.info("Parsing template to find variables");
        HashSet<String> variables;
        try {
            variables = VariablesUtil.parseTemplate(template);
            log.info("Variables found");
        } catch (Exception e ) {
            log.error("Parsing ends with error: " + e);
            return null;
        }

        log.info("Creating properties for Event...");
        Props props;
        try {
            props = VariablesUtil.getVariables(variables, kafkaEvent, commands);
            log.info("Properties successfully created");
        } catch (Exception e) {
            log.error("Properties creating fault with error: " + e.getMessage());
            return null;
        }

        return props;
    }


    private Props getProps(TemplateEmail template) {

        log.info("Parsing template to find variables");
        HashSet<String> variables;
        try {
            variables = VariablesUtil.parseTemplate(template);
            log.info("Variables found");
        } catch (Exception e ) {
            log.error("Parsing ends with error: " + e);
            return null;
        }

        log.info("Creating properties for Email event...");
        Props props;
        try {
            props = VariablesUtil.getVariables(variables, kafkaEvent, commands);
            log.info("Properties successfully created");
        } catch (Exception e) {
            log.error("Properties creating fault with error: " + e.getMessage());
            return null;
        }

        return props;
    }
}
