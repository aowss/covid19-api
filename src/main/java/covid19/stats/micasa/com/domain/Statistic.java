package covid19.stats.micasa.com.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Statistic(@JsonProperty int confirmedCases, @JsonProperty int deaths, @JsonProperty int recoveries) {
}
