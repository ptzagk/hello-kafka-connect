package us.yellosoft.hellokafkaconnect;

import org.apache.kafka.connect.sink.SinkConnector;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Range;
import org.apache.kafka.common.config.ConfigDef.Importance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Manages NameTask's
 */
public final class NameSource extends SinkConnector {
  private static final Logger LOG = LoggerFactory.getLogger(NameSource.class);

  private String[] kafkaTopics;
  private int kafkaPartitions;
  private URI redisAddress;
  private String nameListKey;

  @Override
  public Class<? extends Task> taskClass() {
    LOG.info("NameSource#taskClass");

    return NameTask.class;
  }

  @Override
  public ConfigDef config() {
    LOG.info("NameSource#config");

    final ConfigDef configDef = new ConfigDef();
    // configDef.define(Constants.CONFIG_TOPICS, Type.STRING, "names", Importance.LOW, "Topics to send Redis data");
    configDef.define(Constants.CONFIG_KAFKA_PARTITIONS, Type.INT, Range.atLeast(0), Importance.LOW, "Number of available Kafka partitions");
    configDef.define(Constants.CONFIG_REDIS_ADDRESS, Type.STRING, "redis://localhost:6379", Importance.HIGH, "Redis address (redis://<host>:<port>)");
    configDef.define(Constants.CONFIG_NAME_LIST_KEY, Type.STRING, "names", Importance.HIGH, "Redis key for name list");

    return configDef;
  }

  @Override
  public void start(final Map<String, String> props) {
    LOG.info("NameSource#start(props=" + props + ")");

    kafkaTopics = props.get(Constants.CONFIG_TOPICS).split(Constants.TOPIC_DELIMITER);
    kafkaPartitions = Integer.parseInt(props.get(Constants.CONFIG_KAFKA_PARTITIONS));

    try {
      redisAddress = new URI(props.get(Constants.CONFIG_REDIS_ADDRESS));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    nameListKey = props.get(Constants.CONFIG_NAME_LIST_KEY);
  }

  @Override
  public void stop() {
    LOG.info("NameSource#stop");
  }

  @Override
  public List<Map<String, String>> taskConfigs(final int maxTasks) {
    LOG.info("NameSource#taskConfigs(maxTasks=" + maxTasks +")");

    final List<Map<String, String>> configs = new LinkedList<>();

    for (int i = 0; i < maxTasks; i++) {
      final Map<String, String> config = new HashMap<>();
      // config.put(Constants.CONFIG_TOPICS, String.join(Constants.TOPIC_DELIMITER, kafkaTopics));
      config.put(Constants.CONFIG_KAFKA_PARTITIONS, String.valueOf(kafkaPartitions));
      config.put(Constants.CONFIG_REDIS_ADDRESS, redisAddress.toString());
      config.put(Constants.CONFIG_NAME_LIST_KEY, nameListKey);

      configs.add(config);
    }

    return configs;
  }

  @Override
  public String version() {
    LOG.info("NameSource#version");

    return Constants.VERSION;
  }
}
