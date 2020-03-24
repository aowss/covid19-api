package covid19.stats.micasa.com.services.impl;

import covid19.stats.micasa.com.domain.Reading;
import org.junit.jupiter.api.*;

import covid19.stats.micasa.com.domain.Location;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@DisplayName("Stats Service")
public class StatisticsServiceImplTest {

    @Test
    @DisplayName("Mapping from Johns Hopkins CSSE format to internal format")
    public void map() throws IOException {

        var is = new FileInputStream("src/test/resources/time_series_19-covid-Confirmed.csv");
        Map<Location, SortedSet<Reading<Integer>>> output = StatsServiceImpl.formatStats(is);

        assertThat("There are 463 locations", output.keySet().size(), is(463));
        assertThat("All locations should have 54 dates", output.values().stream().map(Set::size).filter(size -> size != 54).findAny(), is(Optional.empty()));

        var britishColumbia = new Location("Canada", "British Columbia", 49.2827f,-123.1207f);
        assertThat("The statistics should contain British Columbia", output.containsKey(britishColumbia), is(true));
        assertThat("The first British Columbia case was on the 28th of January", output.get(britishColumbia).stream().filter(reading -> reading.value() != 0).map(Reading::date).findFirst().get(), is(LocalDate.of(2020, 1, 28)));

    }

    @Test
    @DisplayName("All files don't have the same dates")
    public void sameFormat() throws IOException {

        var files = List.of(
            new FileInputStream("src/test/resources/time_series_19-covid-Confirmed.csv"),
            new FileInputStream("src/test/resources/time_series_19-covid-Deaths.csv"),
            new FileInputStream("src/test/resources/time_series_19-covid-Recovered.csv")
        );

        assertThat("Different files have different dates", files.stream().map(StatsServiceImpl::formatStats).map(Map::values).map(Collection::size).distinct().count(), is(not(1L)));

    }

    @Test
    @DisplayName("All files don't have the same locations")
    public void locations() throws IOException {

        var files = List.of(
            new FileInputStream("src/test/resources/time_series_19-covid-Confirmed.csv"),
            new FileInputStream("src/test/resources/time_series_19-covid-Deaths.csv"),
            new FileInputStream("src/test/resources/time_series_19-covid-Recovered.csv")
        );

        assertThat("Different files have different locations", files.stream().map(StatsServiceImpl::formatStats).map(Map::keySet).map(Set::size).distinct().count(), is(not(1L)));

    }

}
