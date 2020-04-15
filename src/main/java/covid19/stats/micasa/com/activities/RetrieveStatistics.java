package covid19.stats.micasa.com.activities;

import covid19.stats.micasa.com.domain.Location;
import covid19.stats.micasa.com.domain.Reading;
import covid19.stats.micasa.com.domain.Statistic;
import covid19.stats.micasa.com.repositories.StatisticsRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class RetrieveStatistics {

    StatisticsRepository statisticsRepository;

    public RetrieveStatistics(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public Map<Location, SortedSet<Reading<Statistic>>> retrieveCountryStatistics(Optional<String> location, Optional<LocalDate> from, Optional<LocalDate> to) {

        validateFilters(location, from, to);

        return filter(statisticsRepository, location, from, to)
                .collect(
                    toMap(
                        entry -> new Location(entry.getKey().country()),
                        entry -> entry.getValue(),
                        (existingReadings, newReadings) -> {
                            SortedSet<Reading<Statistic>> result = new TreeSet<>();
                            var existingIterator = existingReadings.iterator();
                            var newIterator = newReadings.iterator();
                            while (existingIterator.hasNext()) {
                                var existingReading = existingIterator.next();
                                var newReading = newIterator.next();
                                result.add(new Reading<Statistic>(existingReading.location(), existingReading.date(), existingReading.value().add(newReading.value())));
                            }
                            return result;
                        }
                    )
                );

    }

    private Stream<Map.Entry<Location, SortedSet<Reading<Statistic>>>> filter(StatisticsRepository statisticsRepository, Optional<String> location, Optional<LocalDate> from, Optional<LocalDate> to) {
        return statisticsRepository.getStatistics().entrySet()
                .stream()
                .filter(entry -> location.isPresent() ? entry.getKey().country().startsWith(location.get()) : true)
                .map(entry -> {
                    var readings = entry.getValue();
                    if (from.isPresent()) readings = readings.tailSet(new Reading<>(readings.first().location(), from.get(), null));
                    if (to.isPresent()) readings = readings.headSet(new Reading<>(readings.first().location(), to.get().plusDays(1), null));
                    return Map.entry(entry.getKey(), readings);
                });
    }

    private void validateFilters(Optional<String> locationFilter, Optional<LocalDate> fromFilter, Optional<LocalDate> toFilter) {
        List<String> messages = new ArrayList<>();
        locationFilter.ifPresent(location -> {
            if (!statisticsRepository.getStatistics().keySet().stream().map(Location::country).anyMatch(country -> country.startsWith(location))) {
                messages.add(String.format("The 'location' parameter [ %s ] is invalid", location));
            }
        });
        fromFilter.ifPresent(from -> {
            if (from.isBefore(statisticsRepository.getFrom()) || from.isAfter((statisticsRepository.getTo()))) {
                messages.add(String.format("The 'from' parameter [ %s ] is invalid. It must be between %s and %s", from, statisticsRepository.getFrom(), statisticsRepository.getTo()));
            }
        });
        toFilter.ifPresent(to -> {
            if (to.isAfter(statisticsRepository.getTo()) || to.isBefore(statisticsRepository.getFrom())) {
                messages.add(String.format("The 'from' parameter [ %s ] is invalid. It must be between %s and %s", to, statisticsRepository.getFrom(), statisticsRepository.getTo()));
            }
        });
        if (!messages.isEmpty()) {
            throw new IllegalArgumentException(messages.stream().collect(Collectors.joining("; ", "[ ", " ]")));
        }
    }

}
