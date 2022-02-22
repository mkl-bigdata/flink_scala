package function;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.types.Row;
import tableapi.SensorReading;

import javax.xml.transform.Templates;

public class udf3_aggreFunction {

    public static void main(String[] args) throws Exception{


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

        myAggr myAggr = new myAggr();


        tableEnv.registerFunction("myaggr",myAggr);
        tableEnv.createTemporaryView("inputtable",inputTable);

        Table resultTable = tableEnv.sqlQuery("select id,myaggr(temperature) from inputtable group by id");

        tableEnv.toRetractStream(resultTable, Row.class).print();


        env.execute();

    }

    public static class myAggr extends AggregateFunction<Double, Tuple2<Double,Integer>> {


        @Override
        public Double getValue(Tuple2<Double, Integer> doubleIntegerTuple2) {

            return doubleIntegerTuple2.f0 / doubleIntegerTuple2.f1;

        }

        @Override
        public Tuple2<Double, Integer> createAccumulator() {
            return new Tuple2<Double,Integer>(0.0,0);
        }


        public void accumulate(Tuple2<Double,Integer> acc , Double temp){

            acc.f0 += temp;
            acc.f1 += 1;

        }
    }


}
