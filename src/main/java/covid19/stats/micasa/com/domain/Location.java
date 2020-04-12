package covid19.stats.micasa.com.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record Location(@JsonProperty String country, @JsonProperty String province, float latitude, float longitude) implements Comparable<Location> {

    public Location(String country) {
        this(country, "");
    }

    public Location(String country, String province) {
        this(country, province, 0,0);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return country.equals(location.country) &&
                Objects.equals(province, location.province);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, province);
    }

}
