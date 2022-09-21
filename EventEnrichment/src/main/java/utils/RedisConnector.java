package utils;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

public class RedisConnector {

    private static RedisURI redisUri;

    public static RedisClient getClient(){
        redisUri = RedisURI.Builder.redis("192.168.200.32")
                .withPassword("pass")
                .withDatabase(15)
                .build();
        RedisClient client = RedisClient.create(redisUri);
        return client;
    }

}
