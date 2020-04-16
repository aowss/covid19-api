package covid19.stats.micasa.com.services.impl;

import covid19.stats.micasa.com.domain.*;

import org.junit.jupiter.api.*;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

import static covid19.stats.micasa.com.services.impl.JohnsHopkinsStatisticsServiceImpl.*;

import static java.util.stream.Collectors.toList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Johns Hopkins Statistics Service")
public class JohnsHopkinsStatisticsServiceImplTest {

    public static final String folder = "src/test/resources/johns-hopkins/";

    @Test
    @DisplayName("Invalid dates are ignored for all records")
    public void invalidDate() throws FileNotFoundException {
        var input = new FileInputStream(folder + "/Canada-confirmed.csv");
        var readings = parse.apply("test").apply(input).collect(toList());
        assertThat(readings.stream().map(Reading::location).map(Location::country).filter(province -> !province.equals("Invalid Value")).count(), is(12 * 81L));
        assertThat(readings.stream().map(Reading::date).filter(date -> date.equals(LocalDate.of(2020, 4, 11))).count(), is(13L));
        assertThat(readings.stream().map(Reading::date).anyMatch(date -> date.equals(LocalDate.of(2020, 4, 12))), is(false));
    }

    @Test
    @DisplayName("Cruises and mistakes are skipped")
    public void skipRecords() throws FileNotFoundException {
        var input = new FileInputStream(folder + "/Canada-confirmed.csv");
        var readings = parse.apply("test").apply(input);
        assertThat(readings.map(Reading::location).map(Location::province).anyMatch(province -> province.equals("Recovered") || province.equals("Diamond Princess") || province.equals("Grand Princess")), is(false));
    }

    @Test
    @DisplayName("Locations with invalid coordinates are ignored entirely")
    public void invalidCoordinates() throws FileNotFoundException {
        var input = new FileInputStream(folder + "/Canada-confirmed.csv");
        var readings = parse.apply("test").apply(input);
        assertThat(readings.map(Reading::location).map(Location::country).anyMatch(province -> province.equals("Invalid Coordinates")), is(false));
    }

    @Test
    @DisplayName("Readings with invalid values are ignored")
    public void invalidValue() throws FileNotFoundException {
        var input = new FileInputStream(folder + "/Canada-confirmed.csv");
        var readings = parse.apply("test").apply(input);
        assertThat(readings.map(Reading::location).map(Location::country).filter(province -> province.equals("Invalid Value")).count(), is(80L));
    }

    @Test
    @DisplayName("Readings are merged correctly")
    public void merge() throws FileNotFoundException {

        var confirmedReadings = parse.apply("test").apply(new FileInputStream(folder + "/Canada-confirmed.csv"));
        var deathsReadings = parse.apply("test").apply(new FileInputStream(folder + "/Canada-deaths.csv"));
        var recoveredReadings = parse.apply("test").apply(new FileInputStream(folder + "/Canada-recovered.csv"));
        Map<Location, SortedSet<Reading<Statistic>>> mergedReadings = mergeEntries(index.apply(confirmedReadings), index.apply(deathsReadings), index.apply(recoveredReadings));

        assertThat(mergedReadings.keySet().size(), is(14));
        assertThat(mergedReadings.keySet().contains(new Location("Canada")), is(true));

    }

}
