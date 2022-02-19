package restart;


import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.executiongraph.restart.RestartStrategy;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class checkpoint_restart {

    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //开启checkpoint
        env.enableCheckpointing(5000);

        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(3,2000));

        env.setStateBackend(new FsStateBackend("ss"));




        DataStreamSource<String> inputStream = env.socketTextStream("localhost", 7777);

        inputStream.map((MapFunction<String, String>) s -> {

            if(s.startsWith("mkl")){

                throw new RuntimeException("出现错误");


            }

            return s;

        }).print();

        env.execute("restart");


    }
}
