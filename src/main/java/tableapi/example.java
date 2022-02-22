package tableapi;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import transform_sink.sensor;

public class example {


    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> inputStream = env.readTextFile("/Users/mkl/IdeaProjects/flink_scala/data/sensor.txt");


        SingleOutputStreamOperator<SensorReading> mappedStream = inputStream.map(data -> {

            String[] fields = data.split(",");

            return new SensorReading(fields[0], Long.valueOf(fields[1]), Double.valueOf(fields[2]));

        });

        //创建流式表环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        Table table = tableEnv.fromDataStream(mappedStream);

        //把表对象注册成表
        tableEnv.createTemporaryView("mytable",table);
        //把流注册成表
        tableEnv.createTemporaryView("mytable2",mappedStream);


        String sql = "select id,temperature from mytable where temperature > 10.0";

        Table result = tableEnv.sqlQuery(sql);

        tableEnv.toAppendStream(result, Row.class).print("result");


        env.execute();
    }

}
