package tableapi;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.FileSystem;
import org.apache.flink.table.descriptors.Rowtime;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.types.Row;

public class TimeAndWindow {

    public static void main(String[] args) throws Exception{


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        String inputPath = "/Users/mkl/IdeaProjects/flink_scala/data/sensor.txt";

        //todo 方法一 连接外部系统
        /*tableEnv.connect(new FileSystem().path(inputPath))
                .withFormat(new Csv())
                .withSchema(new Schema()
                .field("id", DataTypes.INT())
                .field("timestamp",DataTypes.BIGINT())
                .field("temperature",DataTypes.DOUBLE())
                        //TODO 指定
                        .rowtime(
                                new Rowtime()
                                        //todo 指定抽取哪个字段为事件时间
                                .timestampsFromField("timestamp")
                                .watermarksPeriodicBounded(1000)

                        )
                        .proctime()
                .field("rt",DataTypes.TIMESTAMP()))
                .createTemporaryTable("inputtable");
*/

        // todo 方法二 pojo直接指定
        DataStreamSource<String> inputStream = env.readTextFile(inputPath);

        SingleOutputStreamOperator<SensorReading> dataStream = inputStream.map(line -> {

            String[] fields = line.split(",");

            return new SensorReading(fields[0], Long.valueOf(fields[1]), Double.valueOf(fields[2]));

        })
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<SensorReading>(Time.seconds(2)) {
                    @Override
                    public long extractTimestamp(SensorReading element) {

                        return element.getTimestamp() * 1000;

                    }
                });


        // todo 通过方式一注册成表
       // Table inputtable = tableEnv.from("inputtable");

        // todo 通过方式二注册成表 ,指定事件时间
        Table inputtable2 = tableEnv.fromDataStream(dataStream,"id, timestamp as ts, temperature as temp, rt.rowtime");
        tableEnv.createTemporaryView("inputtable2",inputtable2);


        // SQL
        Table resultSqlTable = tableEnv.sqlQuery("select id, count(id) as cnt, avg(temp) as avgTemp, tumble_end(rt, interval '10' second) " +
                "from inputtable2 group by id, tumble(rt, interval '10' second)");


        //tableEnv.toAppendStream(resultSqlTable, Row.class).print();

        // over window SQL
        Table overWindowTable = tableEnv.sqlQuery("select id,rt,count(id) over ow,avg(temp) over ow" +
                " from inputtable2 " +
                " window ow as (partition by id order by rt rows between 2 preceding and current row)"); // 当前行和之前的两个聚合做计算


        tableEnv.toAppendStream(overWindowTable,Row.class).print();


        env.execute();


    }
}
