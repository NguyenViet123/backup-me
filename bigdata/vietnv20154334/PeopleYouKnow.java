package peopleyouknow;

import org.apache.hadoop.io.serializer.DeserializerComparator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import scala.Tuple3;

import java.util.*;

public class PeopleYouKnow {

    SparkConf conf = new SparkConf().setAppName("People you know")
            .set("spark.executor.memory", "2g");

    JavaSparkContext sc = new JavaSparkContext(conf);

    public static void main(String[] args) {
        PeopleYouKnow p = new PeopleYouKnow();
        p.run();

    }

    public void run() {
        JavaPairRDD<Integer, List<Integer>> data = readFile("hdfs:/user/viet-user/peopleyouknow.txt");

                JavaPairRDD<Integer, Integer> dataFreinds = data.flatMapValues(fr -> fr).mapValues(i -> i);

        JavaPairRDD<Integer, Integer> fullData = data.flatMap(s -> {
            List<Tuple2<Integer, Integer>> temp = new ArrayList<>();

            for (int i = 0; i < s._2.size(); i++) {
                for (int j = 0; j < s._2.size(); j++) {
                    if (i != j) {
                        temp.add(new Tuple2<>(s._2.get(i), s._2.get(j)));
                    }
                }
            }
            return temp.iterator();
        }).mapToPair(s1 -> {
            return new Tuple2<>(s1._1(), s1._2());
        });
//        fullData.saveAsTextFile("file:///home/viet-user/Desktop/test");

        fullData.subtract(dataFreinds).mapToPair(s -> {
            return new Tuple2<>(new Tuple2<>(s._1, s._2), 1);
        }).reduceByKey((a, b) -> {
            return a + b;
        }).mapToPair(s2 -> {
            return new Tuple2<>(s2._1._1, new Tuple2<>(s2._1._2, s2._2));
        }).groupByKey().mapToPair(s3 -> {
            List<Tuple2<Integer, Integer>> tempResult = new ArrayList<>();
            List<Integer> result = new ArrayList<>();
            Iterator<Tuple2<Integer, Integer>> i = s3._2.iterator();

            while (i.hasNext()) {
                tempResult.add(i.next());
            }

            for (int m = 0; m < tempResult.size(); m++){
                for (int n = 0; n <= m; n++){
                    if (tempResult.get(n)._2 < tempResult.get(m)._2){
                        Tuple2<Integer, Integer> t = tempResult.get(n);
                        tempResult.set(n, tempResult.get(m));
                        tempResult.set(m, t);
                    }
                }
            }

            for (Tuple2<Integer, Integer> t : tempResult) {
                result.add(t._1);
            }

            return new Tuple2<>(s3._1, result);
        })
                .saveAsTextFile("file:///home/viet-user/Desktop/resultPeopleYouKnow");


    }

    public JavaPairRDD<Integer, List<Integer>> readFile(String path) {

        return sc.textFile(path).mapToPair(s -> {

//                System.out.println("==========" + s.split("    ")[0]);
            Integer idUser = Integer.parseInt(s.split("    ")[0]);
            List<Integer> freinds = new ArrayList<>();

            if (s.split("    ").length == 2) {
                for (String f : s.split("    ")[1].split(",")) {
                    freinds.add(Integer.parseInt(f));
                }
            }

            return new Tuple2<>(idUser, freinds);
        });

    }

}