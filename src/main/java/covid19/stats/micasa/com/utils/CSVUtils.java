package covid19.stats.micasa.com.utils;

import org.apache.commons.csv.CSVRecord;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class CSVUtils {

    public String getRecords(CSVRecord record, int... indices) {
        if (indices == null || indices.length == 0) return "";
        IntStream values = Arrays.stream(indices);
        return values
                .mapToObj(record::get)
                .collect(joining(", "));
    }

    public static String getAllRecords(CSVRecord record) {
        return IntStream.range(0, record.size())
                .mapToObj(record::get)
                .collect(joining(", "));
    }

}
