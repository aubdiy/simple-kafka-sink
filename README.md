simple-kafka-sink
=================
flume与kafka集成，kafka的生产者作为flume的sink，用于flume向kafka集群发送数据

### 工具依赖：

    <dependency>
        <groupId>org.apache.flume</groupId>
        <artifactId>flume-ng-core</artifactId>
        <version>1.4.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka_2.9.2</artifactId>
        <version>0.8.1.1</version>
    </dependency>

###使用方式：
1.  将本工具打成jar包，如xxx.jar
2.  将xxx.jar放到 $FLUME_HOME/plugins.d/flume-kafka/lib/目录下
3.  将$KAFKA_HOME/lib下的
    * jopt-simple-3.2.jar
    * kafka_2.8.0-0.8.0.jar
    * metrics-annotation-2.2.0.jar
    * metrics-core-2.2.0.jar
    * scala-compiler.jar
    * scala-library.jar
    * snappy-java-1.0.4.1.jar

    拷贝到$FLUME_HOME/plugins.d/flume-kafka/libext/目录下
  
4.  配置flume的properties（只列出sink部分）
```
agent.sources = avroSrc
agent.channels = memoryChannel
agent.sinks = kafkaSink
//source配置
agent.sources.avroSrc.type = avro
agent.sources.avroSrc.bind = server-166
agent.sources.avroSrc.port= 4141
agent.sources.avroSrc.channels = memoryChannel
//sink配置
//kafka的topic名字
agent.sinks.kafkaSink.topic = log_website
//sink类型（全路径）
agent.sinks.kafkaSink.type = cn._23hours.KafkaSink
//kafka集群地址
agent.sinks.kafkaSink.metadata.broker.list = server-166:9092
agent.sinks.kafkaSink.channel = memoryChannel

agent.channels.memoryChannel.type = memory
agent.channels.memoryChannel.capacity = 100
```

5.  启动flume
    $FLUME_HOME/bin/flume-ng agent -n agent -c conf -f conf/avro-kafka.properties
