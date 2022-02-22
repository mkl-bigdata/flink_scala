package function;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;
import tableapi.SensorReading;

public class udf2_tableFunction {

    public static void main(String[] args) throws  Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);


        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);


        String inputPath = "/Users/mkl/IdeaProjects/flink_scala/data/sensor.txt";

        SingleOutputStreamOperator<SensorReading> mappedStream = env.readTextFile(inputPath)
                .map(line -> {

                    String[] fields = line.split(",");

                    return new SensorReading(fields[0], Long.valueOf(fields[1]), Double.valueOf(fields[2]));

                });

        Table inputTable = tableEnv.fromDataStream(mappedStream);


        tableFunc tableFunc = new tableFunc("_");

        tableEnv.registerFunction("spilt_func",tableFunc);
        tableEnv.createTemporaryView("inputtable",inputTable);

        Table resultTable = tableEnv.sqlQuery("select id,word,length from inputtable , lateral table(spilt_func(id)) as spiltid(word,length)");


        tableEnv.toAppendStream(resultTable, Row.class).print();


        env.execute();

    }


    public static class tableFunc extends TableFunction<Tuple2<String, Integer>>{

        private String separator = "_";

        public tableFunc(String separator){

            this.separator = separator;
        }

        public void eval(String s){

            String[] fields = s.split(separator);

            for (int i = 0; i < fields.length; i++) {

                collect(new Tuple2<String, Integer>(fields[i],fields[i].length()));
            }

        }

    }
}
