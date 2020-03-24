package covid19.stats.micasa.com.services.impl;

import covid19.stats.micasa.com.domain.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Component
public class StatsServiceImpl {

    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    public static final int timeout = 30;

    public CompletableFuture<Map<Location, Map<LocalDate, Reading<Statistic>>>> retrieveStats() {

        var confirmedCF = get("https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv")
                .thenApply(parse.andThen(index));

        var deathCF = get("https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Deaths.csv")
                .thenApply(parse.andThen(index));

        var recoveredCF = get("https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Recovered.csv")
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

    public static BiFunction<Location, LocalDate, Function<Map<Location, Map<LocalDate, List<Reading<Integer>>>>, Optional<Integer>>> getValue = ((location, date) -> map ->
            Optional.of(map).map(locations -> locations.get(location)).map(dates -> dates.get(date)).map(readings -> readings.get(0).value()));

    public static Map<Location, Map<LocalDate, Reading<Statistic>>> mergedEntries(Map<Location, Map<LocalDate, List<Reading<Integer>>>> confirmed,
                                                                                  Map<Location, Map<LocalDate, List<Reading<Integer>>>> death,
                                                                                  Map<Location, Map<LocalDate, List<Reading<Integer>>>> recovered) {

        //  All the maps don't have the exact same locations or dates
        Set<Location> allLocations = new HashSet<>(confirmed.keySet());
        allLocations.addAll(death.keySet());
        allLocations.addAll(recovered.keySet());

        Set<LocalDate> allDates = confirmed.values().stream().map(Map::keySet).flatMap(Set::stream).collect(toSet());
        allDates.addAll(death.values().stream().map(Map::keySet).flatMap(Set::stream).collect(toSet()));
        allDates.addAll(recovered.values().stream().map(Map::keySet).flatMap(Set::stream).collect(toSet()));

        return allLocations.stream()
            .collect(
                toMap(
                    Function.identity(),
                    location -> allDates.stream()
                        .collect(
                            toMap(
                                Function.identity(),
                                date -> {
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
                                }
                            )
                        )
                )
            );

    }

    public static Function<Stream<Reading<Integer>>, Map<Location, Map<LocalDate, List<Reading<Integer>>>>> index = stream -> stream
        .collect(groupingBy(Reading::location, groupingBy(Reading::date)));

    private static Function<InputStream, Stream<Reading<Integer>>> parse = inputStream -> {

        try {

            CSVParser parser = CSVParser.parse(inputStream, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withFirstRecordAsHeader());
            List<String> dates = parser.getHeaderNames().subList(4, parser.getHeaderNames().size() - 1);

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
