package cn._23hours;

import java.util.Map;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.ConfigurationException;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaSink extends AbstractSink implements Configurable {
	private static final Logger LOG = LoggerFactory.getLogger(KafkaSink.class);
	private String topic;
	private Producer<String, byte[]> producer;

	@Override
	public Status process() throws EventDeliveryException {
		Channel channel = getChannel();
		Transaction tx = channel.getTransaction();
		try {
			tx.begin();
			Event e = channel.take();
			if (e == null) {
				tx.rollback();
				return Status.BACKOFF;
			}
			producer.send(new KeyedMessage<String, byte[]>(topic, e.getBody()));
			LOG.trace("Message: {}", new String(e.getBody()));
			tx.commit();
			return Status.READY;
		} catch (Exception e) {
			LOG.error("KafkaSink Exception:{}", e);
			tx.rollback();
			return Status.BACKOFF;
		} finally {
			tx.close();
		}
	}

	@Override
	public void configure(Context context) {
		topic = context.getString("topic");
		if (topic == null) {
			throw new ConfigurationException("Kafka topic must be specified.");
		}
		Properties props = new Properties();
		Map<String, String> contextMap = context.getParameters();
		for (String key : contextMap.keySet()) {
			if (!key.equals("type") && !key.equals("channel")) {
				props.setProperty(key, context.getString(key));
				LOG.info("key={},value={}", key, context.getString(key));
			}
		}
		producer = new Producer<String, byte[]>(new ProducerConfig(props));
	}

	@Override
	public synchronized void stop() {
		producer.close();
		super.stop();
	}

}
