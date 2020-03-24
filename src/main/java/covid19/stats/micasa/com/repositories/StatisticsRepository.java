package covid19.stats.micasa.com.repositories;

import covid19.stats.micasa.com.domain.*;

import java.time.*;
import java.util.*;

import static java.util.stream.Collectors.toCollection;

public class StatisticsRepository {

    private ZonedDateTime lastUpdateTime;
    private LocalDate from;
    private LocalDate to;
    private Map<Location, SortedSet<Reading<Statistic>>> statistics;

    public void loadStatistics(Map<Location, SortedSet<Reading<Statistic>>> statistics) {
        this.statistics = statistics;
        lastUpdateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        var dates = statistics.values().stream().flatMap(Set::stream).map(Reading::date).collect(toCollection(TreeSet::new));
        from = dates.first();
        to = dates.last();
    }

    public ZonedDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    public Map<Location, SortedSet<Reading<Statistic>>> getStatistics() {
        return statistics;
    }

}
