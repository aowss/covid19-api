package covid19.stats.micasa.com.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record Reading<T>(Location location, @JsonProperty LocalDate date, @JsonProperty T value) implements Comparable<Reading> {

    @Override
    public int compareTo(Reading o) {
        int locationComparison = location.compareTo(o.location);
        if (locationComparison != 0) return locationComparison;
        return date.compareTo(o.date());
    }

}
