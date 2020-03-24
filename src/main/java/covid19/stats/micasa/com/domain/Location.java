package covid19.stats.micasa.com.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Location(@JsonProperty String country, @JsonProperty String province, float latitude, float longitude) implements Comparable<Location> {

    @Override
    public int compareTo(Location o) {
        int countryComparator = country.compareTo(o.country);
        if (countryComparator != 0) return countryComparator;
        return province.compareTo(o.province);
    }

    //  This is what is used by Jackson to serialize the key of the map by default
    @Override
    public String toString() {
        return country + ( province != null && !province.isBlank() ? " / " + province : "" );
    }

}
