package tableapi;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.Kafka;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.table.types.DataType;

public class kafkaPipeline {

    public static void main(String[] args) throws Exception{


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //连接kafka topic消费数据
        tableEnv.connect(new Kafka().version("0.11")
        .topic("sensor")
        .property("zookeeper.connect","hadoop102:2181")
        .property("bootstrap.servers","hadoop102:9092"))
                .withFormat(new Csv())
                .withSchema(new Schema()
                .field("id", DataTypes.INT())
                .field("timestamp",DataTypes.BIGINT())
                                .field("tempture",DataTypes.DOUBLE())
                )
                .createTemporaryTable("inputtable");


        Table kafkaTable = tableEnv.from("inputtable");

        //xxxx中间处理操作
        Table result = tableEnv.sqlQuery("select xxx from inputtable where xxx");

        // 结果表写入kafka topic中
        tableEnv.connect(new Kafka().version("0.11")
                .topic("flink_kafka_test")
                .property("zookeeper.connect","hadoop102:2181")
                .property("bootstrap.servers","hadoop102:9092"))
                .withFormat(new Csv())
                .withSchema(new Schema()
                        .field("id", DataTypes.INT())
                        .field("timestamp",DataTypes.BIGINT())
                        .field("tempture",DataTypes.DOUBLE())
                )
                .createTemporaryTable("outputtable");

        result.insertInto("outputtable");

        env.execute();


    }
}
