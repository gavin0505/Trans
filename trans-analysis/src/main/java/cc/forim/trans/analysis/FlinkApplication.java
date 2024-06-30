package cc.forim.trans.analysis;

import cc.forim.trans.analysis.action.MessageFlatMapAction;
import cc.forim.trans.analysis.infra.dto.MessageOutputDTO;
import cn.hutool.setting.dialect.Props;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import static cc.forim.trans.analysis.infra.constant.TransFlinkEnum.CK_JOB_NAME;
import static cc.forim.trans.analysis.infra.constant.TransFlinkEnum.RECORD_SINK;

/**
 * 刷洗短网址转换的数据到ClickHouse的Flink作业
 *
 * @author Gavin Zhang
 * @version V1.0
 * @see <a href="https://nightlies.apache.org/flink/flink-docs-release-1.15/zh/docs/connectors/datastream/kafka/
 * ">Flink V1.15.2 连接Kafka</a>
 */
public class FlinkApplication {
    public static void main(String[] args) throws Exception {
        //初始化环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Props props = new Props("secret.properties");

        //构建Kafka连接器
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(props.getProperty("kafka_host"))
                .setTopics(props.getProperty("kafka_topic"))
                .setProperty("enable.auto.commit", "true")
                .setGroupId(props.getProperty("kafka_group"))
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        DataStreamSource<String> kafkaSource = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka_Analysis");

        // 转换数据格式
        SingleOutputStreamOperator<MessageOutputDTO> dataStream = kafkaSource.flatMap(
                new MessageFlatMapAction());

        // sink到CK
        dataStream.addSink(JdbcSink.sink("insert into analysis " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                , (ps, output) -> {
                    ps.setString(1, output.getShortUrl());
                    ps.setString(2, output.getTargetUrl());
                    ps.setString(3, output.getIp());
                    ps.setString(4, output.getIsp());
                    ps.setString(5, output.getConvince());
                    ps.setString(6, output.getCity());
                    ps.setLong(7, output.getDatetime());
                    ps.setString(8, output.getOs());
                    ps.setString(9, output.getOsVersion());
                    ps.setString(10, output.getBrowser());
                    ps.setString(11, output.getBrowserVersion());
                    ps.setInt(12, output.getMobile());
                    ps.setLong(13, output.getUrlMapId());
                    ps.setLong(14, output.getUserId());
                }
                , JdbcExecutionOptions.builder().withBatchIntervalMs(3000).withBatchSize(3000).build()
                , new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(props.getProperty("clickhouse_jdbc"))
                        .withDriverName("com.clickhouse.jdbc.ClickHouseDriver")
                        .build()
        )).name(RECORD_SINK.getDescription());

        // 提交作业
        env.execute(CK_JOB_NAME.getDescription());
    }
}
