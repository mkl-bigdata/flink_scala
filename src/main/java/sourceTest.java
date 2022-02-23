import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import scala.collection.Seq;
import tableapi.SensorReading;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class sourceTest {

    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> inputStream = env.readTextFile("/Users/mkl/IdeaProjects/flink_scala/data/sensor.txt");


        SingleOutputStreamOperator<SensorReading> mappedStream = inputStream.map(data -> {

            String[] fields = data.split(",");

            return new SensorReading(fields[0], Long.valueOf(fields[1]), Double.valueOf(fields[2]));

        });

        mappedStream.split(sensor -> {

            if(sensor.getTemperature() > 30){

                return Collections.singletonList("high");

            }else{

                return Collections.singletonList("low");

            }


        });


    }
}
