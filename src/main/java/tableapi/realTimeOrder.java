package tableapi;

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.util.HashMap;
import java.util.Random;

public class realTimeOrder {

    public static class mySource extends RichParallelSourceFunction<Tuple2<String,Integer>>{

        private boolean flag = true;

        @Override
        public void run(SourceContext<Tuple2<String, Integer>> ctx) throws Exception {

            Random random = new Random();

            while(flag){

                Thread.sleep((getRuntimeContext().getIndexOfThisSubtask() + 1) * 1000 * 5);
                String key = "类别" + (char) ('A' + random.nextInt(3));
                int value = random.nextInt(10) + 1;

                System.out.println(String.format("Emits\t(%s, %d)", key, value));
                ctx.collect(new Tuple2<>(key, value));

            }
        }
        @Override
        public void cancel() {

            flag = false;

        }
    }


    public static void main(String[] args) throws Exception{


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(2);

        SingleOutputStreamOperator<HashMap<String, Integer>> foldedStream = env.addSource(new mySource())
                .keyBy(0)
                //todo 先把相同的key进行一个预聚合
                .sum(1)
                // todo 把所有的key都分到一个组进行统计
                .keyBy(new KeySelector<Tuple2<String, Integer>, Object>() {
                    @Override
                    public Object getKey(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {

                        // todo return ""的目的是都发到一个分组
                        return "";
                    }
                })
                .fold(new HashMap<String, Integer>(), new FoldFunction<Tuple2<String, Integer>, HashMap<String, Integer>>() {
                    @Override
                    public HashMap<String, Integer> fold(HashMap<String, Integer> acc, Tuple2<String, Integer> input) throws Exception {


                        // TODO: 2022/2/23 acc是传入的初始值，保存每个类别下的成交量，input是传入进来的source数据
                        acc.put(input.f0, input.f1);

                        return acc;

                    }
                });

        foldedStream
                .addSink(new SinkFunction<HashMap<String, Integer>>() {

                    @Override
                    public void invoke(HashMap<String, Integer> value) throws Exception {

                        System.out.println(value);

                        //商品总成交量
                        System.out.println(value.values().stream().mapToInt(v -> v).sum());

                    }
                });


        env.execute();


    }

}
