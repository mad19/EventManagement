package adapters;


/**
 * Интерфейс взаимодействия с API для получения событий из Kafka
 */
public interface KafkaProcessor {
    public static String KAFKA_BROKERS = "192.168.200.13:9092";

    public static String TOPIC_NAME = "raw_events";
    /*public static String TOPIC_NAME = "metrics_with_severity_by_threshold";*/

    public static String CLIENT_ID = "enchant";

    public static Integer MESSAGE_COUNT = 1000;

    public static String GROUP_ID_CONFIG = "enricher";

    public static Integer MAX_NO_MESSAGES_FOUND_COUNT = 1000000;

    public static String OFFSET_RESET_LATEST = "latest";

    public static String OFFSET_RESET_EARLIER = "earliest";

    public static Integer MAX_POLL_RECORDS = 1;

    public static String ACKS = "all";
}
