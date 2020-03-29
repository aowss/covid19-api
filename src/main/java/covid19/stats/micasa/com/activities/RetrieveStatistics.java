package covid19.stats.micasa.com.activities;

import covid19.stats.micasa.com.domain.Location;
import covid19.stats.micasa.com.domain.Reading;
import covid19.stats.micasa.com.domain.Statistic;
import covid19.stats.micasa.com.repositories.StatisticsRepository;

import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class RetrieveStatistics {

    StatisticsRepository statisticsRepository;

    public RetrieveStatistics(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public Map<Location, SortedSet<Reading<Statistic>>> retrieve(Optional<String> location,
                                                                 Optional<LocalDate> from,
                                                                 Optional<LocalDate> to) {

        return statisticsRepository.getStatistics().entrySet()
                .stream()
                .filter(entry -> location.isPresent() ? entry.getKey().country().equals(location.get()) : true)
                .map(entry -> {
                    var readings = entry.getValue();
                    if (from.isPresent()) readings = readings.tailSet(new Reading<>(readings.first().location(), from.get(), null));
                    if (to.isPresent()) readings = readings.headSet(new Reading<>(readings.first().location(), to.get().plusDays(1), null));
                    return Map.entry(entry.getKey(), readings);
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

}
