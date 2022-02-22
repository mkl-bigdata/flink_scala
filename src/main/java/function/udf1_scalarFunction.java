package function;


import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;
import tableapi.SensorReading;

public class udf1_scalarFunction {

    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);


        String inputPath = "/Users/mkl/IdeaProjects/flink_scala/data/sensor.txt";

        SingleOutputStreamOperator<SensorReading> inputStream = env.readTextFile(inputPath)
                .map(line -> {


                    String[] fields = line.split(",");

                    return new SensorReading(fields[0], Long.valueOf(fields[1]), Double.valueOf(fields[2]));

                });


        Table inputTable = tableEnv.fromDataStream(inputStream);

        tableEnv.createTemporaryView("inputtable",inputTable);

        myHashcode myHashcode = new myHashcode(23);


        tableEnv.registerFunction("myhash",myHashcode);

        Table resultTable = tableEnv.sqlQuery("select id, myhash(id) from inputtable");


        tableEnv.toAppendStream(resultTable, Row.class).print();




        env.execute();


    }

    public static class myHashcode extends ScalarFunction {

        private int factor = 13;

        public myHashcode(int factor){

            this.factor = factor;
        }

        public int eval(String id){

            return id.hashCode() * factor;

        }

    }
}
