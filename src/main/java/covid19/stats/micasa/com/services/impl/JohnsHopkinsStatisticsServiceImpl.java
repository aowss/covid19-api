package covid19.stats.micasa.com.services.impl;

import covid19.stats.micasa.com.domain.*;
import covid19.stats.micasa.com.services.StatisticsService;

import org.apache.commons.csv.*;
import org.slf4j.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.Charset;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.*;

import static covid19.stats.micasa.com.utils.CSVUtils.getAllRecords;
import static covid19.stats.micasa.com.utils.ExceptionUtils.getMessage;

import static java.util.stream.Collectors.*;

/**
 * Loading data from Johns Hopkins CSSE.
 * The entire time series data is located in https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series.
 * The US brekdown is in separate files and doesn't contain recovered cases.
 * Each file contains the same dates.
 * The locations in the recovered or deaths files are a subset of the ones in the confirmed file.
 */
public class JohnsHopkinsStatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(JohnsHopkinsStatisticsServiceImpl.class);
    private static final String parsingError = "Record number %d of %s [ %s ] is invalid : %s -> %s";
    private static final String discardedEntry = "Record number %d of %s [ %s ] is discarded";
    private static final String unexpectedHeader = "The date header [ %s ] is invalid : this date will be ignored for all records";
    private static final String unexpectedData = "The data doesn't conform to the assumptions : %s";

    private static final Set<Location> locationsToDiscard = Set.of(
        new Location("Canada", "Recovered"),
        new Location("Canada", "Diamond Princess"),
        new Location("Canada", "Grand Princess"),
        new Location("Diamond Princess", ""),
        new Location("MS Zaandam", "")
    );

    private static final Map<String, String> countryNameMapping = Map.of(
        "US", "United States of America",
        "Burma", "Myanmar",
        "Korea, South", "South Korea",
        "West Bank and Gaza", "Palestine",
        "Taiwan*", "Taiwan",
        "Cote d'Ivoire", "Ivory Coast",
        "Congo (Kinshasa)", "Democratic Republic of the Congo",
        "Congo (Brazzaville)", "Republic of the Congo"
    );

    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private static final int timeout = 30;

    private String confirmedUrl, deathsUrl, recoveredUrl;

    public JohnsHopkinsStatisticsServiceImpl(String confirmedUrl, String deathsUrl, String recoveredUrl) {
        this.confirmedUrl = confirmedUrl;
        this.deathsUrl = deathsUrl;
        this.recoveredUrl = recoveredUrl;
    }

    @Override
    public CompletableFuture<Map<Location, SortedSet<Reading<Statistic>>>> loadStatistics() {

        var confirmedCF = get(confirmedUrl)
                .thenApply(parse.apply("confirmed cases").andThen(index));

        var deathCF = get(deathsUrl)
                .thenApply(parse.apply("death cases").andThen(index));

        var recoveredCF = get(recoveredUrl)
                .thenApply(parse.apply("recovered cases").andThen(index));

        return CompletableFuture.allOf(confirmedCF, deathCF, recoveredCF)
                .thenApply(done -> mergeEntries(confirmedCF.join(), deathCF.join(), recoveredCF.join()));

    }

    private static CompletableFuture<InputStream> get(String url) {

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeout))
                .GET()
                .build();

        return httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response ->
                    switch (response.statusCode()) {
                        case 200 -> response.body();
                        default -> throw new RuntimeException("Can't fetch data from " + url);
                    }
                );

    }

    static Function<Stream<Reading<Integer>>, Map<Location, List<Reading<Integer>>>> index = stream -> stream
        .collect(groupingBy(Reading::location));

    static BiFunction<Location, LocalDate, Function<Map<Location, List<Reading<Integer>>>, Optional<Integer>>> getValue = (location, date) -> map -> {
        if (!map.containsKey(location)) return Optional.empty();
        return map.get(location).stream().filter(reading -> reading.date().equals(date)).map(Reading::value).findFirst();
    };

    static Map<Location, SortedSet<Reading<Statistic>>> mergeEntries(Map<Location, List<Reading<Integer>>> confirmed,
                                                                     Map<Location, List<Reading<Integer>>> death,
                                                                     Map<Location, List<Reading<Integer>>> recovered) {

        /*
        All the maps don't have the exact same locations.
        The locations might be different or the granularity might be different, i.e. the confirmed cases are broken down by province but not the recovered ones.
         */
        Set<Location> locations = new HashSet<>(confirmed.keySet());
        locations.addAll(death.keySet());
        locations.addAll(recovered.keySet());

        //  All the maps should be the same
        Set<LocalDate> allDates = confirmed.values().stream().flatMap(List::stream).map(Reading::date).collect(toSet());
        boolean differentDeathDates = allDates.addAll(death.values().stream().flatMap(List::stream).map(Reading::date).collect(toSet()));
        if (differentDeathDates) logger.warn(String.format(unexpectedData, "the deaths dates are different from the confirmed cases dates"));
        boolean differentRecoveredDates = allDates.addAll(recovered.values().stream().flatMap(List::stream).map(Reading::date).collect(toSet()));
        if (differentRecoveredDates) logger.warn(String.format(unexpectedData, "the recovered dates are different from the confirmed cases dates"));

        return locations.stream()
            .collect(
                toMap(
                    Function.identity(),
                    location -> allDates.stream()
                        .map(date -> {
                            var value = getValue.apply(location, date);
                            return new Reading<>(
                                location,
                                date,
                                new Statistic(
                                    value.apply(confirmed).orElse(0),
                                    value.apply(death).orElse(0),
                                    value.apply(recovered).orElse(0)
                                )
                            );
                        })
                        .collect(Collectors.toCollection(TreeSet::new))
                )
            );

    }

    private static Function<String, Function<String, LocalDate>> parseDate = pattern -> {
        var formatter = DateTimeFormatter.ofPattern(pattern);
        return date -> {
            try {
                return LocalDate.parse(date, formatter);
            } catch (DateTimeParseException dtpe) {
                logger.warn(String.format(unexpectedHeader, date));
                return null;
            }
        };
    };

    static Function<String, Function<InputStream, Stream<Reading<Integer>>>> parse = source -> inputStream -> {

        try {

            CSVParser parser = CSVParser.parse(inputStream, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withFirstRecordAsHeader());

            List<LocalDate> dates = parser.getHeaderNames().subList(4, parser.getHeaderNames().size())
                    .stream()
                    .map(parseDate.apply("M/d/yy"))
                    .filter(Objects::nonNull)
                    .collect(toList());

            var formatter = DateTimeFormatter.ofPattern("M/d/yy");

            return parser.getRecords().stream()
                .flatMap(record -> {
                    try {
                        String countryName = record.get(1);
                        if (countryNameMapping.containsKey(countryName)) countryName = countryNameMapping.get(countryName);
                        Location location = new Location(countryName, record.get(0), Float.valueOf(record.get(2)), Float.valueOf(record.get(3)));
                        if (locationsToDiscard.contains(location)) {
                            logger.warn(String.format(discardedEntry, record.getRecordNumber(), source, location));
                            return null;
                        }
                        return dates.stream()
                            .map(date -> {
                                var dateHeader = date.format(formatter);
                                try {
                                    return new Reading<>(location, date, Integer.valueOf(record.get(dateHeader)));
                                } catch (NumberFormatException nfe) {
                                    logger.warn(String.format(parsingError, record.getRecordNumber(), source, getAllRecords(record), "value can't be parsed: '" + record.get(dateHeader) + "' is invalid", dateHeader + " ignored for " + location));
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull);
                    } catch (NumberFormatException nfe) {
                        logger.warn(String.format(parsingError, record.getRecordNumber(), source, getAllRecords(record), "location's coordinates can't be parsed: " + getMessage(nfe), "location ignored"));
                        return null;
                    }
                })
                .filter(Objects::nonNull);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    };

}
