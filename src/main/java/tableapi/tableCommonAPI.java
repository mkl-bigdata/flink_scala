package tableapi;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.FileSystem;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.table.types.FieldsDataType;
import org.apache.flink.types.Row;

public class tableCommonAPI {


    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        // 创建表执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        String inputPath = "/Users/mkl/IdeaProjects/flink_scala/data/sensor.txt";

        //连接外部文件系统
        tableEnv.connect(new FileSystem()
        .path(inputPath))
                .withFormat(new Csv())
                .withSchema(new Schema().field("id", DataTypes.STRING())
                .field("timestamp",DataTypes.BIGINT())
                .field("temperature",DataTypes.DOUBLE()))
                .createTemporaryTable("inputtable");


        Table result = tableEnv.from("inputtable");

        tableEnv.toAppendStream(result, Row.class).print();

        env.execute();




    }
}
