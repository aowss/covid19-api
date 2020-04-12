package covid19.stats.micasa.com.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Statistic(@JsonProperty int confirmedCases, @JsonProperty int deaths, @JsonProperty int recoveries) {

    public Statistic add(Statistic statistic) {
        return new Statistic(
            this.confirmedCases + statistic.confirmedCases,
            this.deaths + statistic.deaths,
            this.recoveries + statistic.recoveries
        );
    }

}
