package covid19.stats.micasa.com.services.impl;

import covid19.stats.micasa.com.domain.*;
import covid19.stats.micasa.com.services.StatisticsService;

import org.apache.commons.csv.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.Charset;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

/**
 * Loading data from Johns Hopkins CSSE
 * The entire time series data is located in https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series.
 */
public class JHStatisticsServiceImpl implements StatisticsService {

    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    public static final int timeout = 30;

    private String confirmedUrl, deathsUrl, recoveredUrl;

    public JHStatisticsServiceImpl(String confirmedUrl, String deathsUrl, String recoveredUrl) {
        this.confirmedUrl = confirmedUrl;
        this.deathsUrl = deathsUrl;
        this.recoveredUrl = confirmedUrl;
    }

    @Override
    public CompletableFuture<Map<Location, SortedSet<Reading<Statistic>>>> loadStatistics() {

        var confirmedCF = get(confirmedUrl)
                .thenApply(parse.andThen(index));

        var deathCF = get(deathsUrl)
                .thenApply(parse.andThen(index));

        var recoveredCF = get(recoveredUrl)
                .thenApply(parse.andThen(index));

        return CompletableFuture.allOf(confirmedCF, deathCF, recoveredCF)
                .thenApply(done -> mergedEntries(confirmedCF.join(), deathCF.join(), recoveredCF.join()));

    }

    public CompletableFuture<InputStream> get(String url) {

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

    public static Map<Location, SortedSet<Reading<Integer>>> formatStats(InputStream inputStream) {
        return parse.apply(inputStream)
                .collect(
                    groupingBy(
                        Reading::location,
                        mapping(stat -> new Reading<Integer>(stat.location(), stat.date(), stat.value()), Collectors.toCollection(TreeSet::new))
                    )
                );
    }

    public static BiFunction<Location, LocalDate, Function<Map<Location, List<Reading<Integer>>>, Optional<Integer>>> getValue = (location, date) -> map -> {
        if (!map.containsKey(location)) return Optional.empty();
        return map.get(location).stream().filter(reading -> reading.date().equals(date)).map(Reading::value).findFirst();
    };

    public static Map<Location, SortedSet<Reading<Statistic>>> mergedEntries(Map<Location, List<Reading<Integer>>> confirmed,
                                                                             Map<Location, List<Reading<Integer>>> death,
                                                                             Map<Location, List<Reading<Integer>>> recovered) {

        //  All the maps don't have the exact same locations or dates
        Set<Location> allLocations = new HashSet<>(confirmed.keySet());
        allLocations.addAll(death.keySet());
        allLocations.addAll(recovered.keySet());

        Set<LocalDate> allDates = confirmed.values().stream().flatMap(List::stream).map(Reading::date).collect(toSet());
        allDates.addAll(death.values().stream().flatMap(List::stream).map(Reading::date).collect(toSet()));
        allDates.addAll(recovered.values().stream().flatMap(List::stream).map(Reading::date).collect(toSet()));

        return allLocations.stream()
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
                                        value.apply(confirmed).orElse(-1),
                                        value.apply(death).orElse(-1),
                                        value.apply(recovered).orElse(-1)
                                    )
                                );
                            })
                        .collect(Collectors.toCollection(TreeSet::new))
                )
            );

    }

    public static Function<Stream<Reading<Integer>>, Map<Location, List<Reading<Integer>>>> index = stream -> stream
        .collect(groupingBy(Reading::location));

    private static Function<InputStream, Stream<Reading<Integer>>> parse = inputStream -> {

        try {

            CSVParser parser = CSVParser.parse(inputStream, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withFirstRecordAsHeader());
            List<String> dates = parser.getHeaderNames().subList(4, parser.getHeaderNames().size());

            return parser.getRecords().stream()
                .map(record -> {
                    Location location = new Location(record.get(1), record.get(0), Float.valueOf(record.get(2)), Float.valueOf(record.get(3)));
                    return dates.stream()
                            .map(date -> {
                                LocalDate readingDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("M/d/yy"));
                                Reading<Integer> reading = new Reading(location, readingDate, Integer.valueOf(record.get(date)));
                                return reading;
                            });
                })
                .flatMap(Function.identity());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    };

}
