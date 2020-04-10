package covid19.stats.micasa.com.services.impl;

import covid19.stats.micasa.com.domain.Location;
import covid19.stats.micasa.com.services.LocationsService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static covid19.stats.micasa.com.utils.CSVUtils.getAllRecords;
import static covid19.stats.micasa.com.utils.ExceptionUtils.getMessage;
import static java.util.stream.Collectors.toSet;

/**
 * Loads the locations from the resources folder
 */
public class ResourcesLocationsServiceImpl implements LocationsService {

    static Logger logger = LoggerFactory.getLogger(ResourcesLocationsServiceImpl.class);
    static String parsingError = "Record number %d of %s [ %s ] is invalid : %s -> %s";

    @Override
    public Set<Location> loadLocations() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("metadata.csv");
        return parse.apply("locations").apply(inputStream).collect(toSet());
    }

    private static Function<String, Function<InputStream, Stream<Location>>> parse = source -> inputStream -> {

        try {

            CSVParser parser = CSVParser.parse(inputStream, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withFirstRecordAsHeader());

            return parser.getRecords().stream()
                .map(record -> {
                    try {
                        return new Location(record.get(2), record.get(4), Float.valueOf(record.get(5)), Float.valueOf(record.get(6)));
                    } catch (NumberFormatException nfe) {
                        logger.warn(String.format(parsingError, record.getRecordNumber(), source, getAllRecords(record), "location can't be parsed: " + getMessage(nfe), "location ignored"));
                        return null;
                    }
                })
                .filter(Objects::nonNull);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    };

}
