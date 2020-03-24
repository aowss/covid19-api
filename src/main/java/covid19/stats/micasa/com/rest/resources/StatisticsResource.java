package covid19.stats.micasa.com.rest.resources;

import covid19.stats.micasa.com.activities.RetrieveMetadata;
import covid19.stats.micasa.com.activities.RetrieveStatistics;

import covid19.stats.micasa.com.domain.Location;
import covid19.stats.micasa.com.domain.Reading;
import covid19.stats.micasa.com.domain.Statistic;
import covid19.stats.micasa.com.rest.representations.MetadataRepresentation;
import org.slf4j.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("covid19")
public class StatisticsResource {

    Logger logger = LoggerFactory.getLogger(StatisticsResource.class);

    RetrieveStatistics retrieveStatistics;
    RetrieveMetadata retrieveMetadata;

    public StatisticsResource(RetrieveStatistics retrieveStatistics, RetrieveMetadata retrieveMetadata) {
        this.retrieveStatistics = retrieveStatistics;
        this.retrieveMetadata = retrieveMetadata;
    }

    @GetMapping(value = "info")
    public ResponseEntity<MetadataRepresentation> metadata() {
        return ResponseEntity.ok(retrieveMetadata.retrieveMetadata());
    }

    @GetMapping(value = "stats")
    public ResponseEntity<Map<Location, SortedSet<Reading<Statistic>>>> stats(@RequestParam("location") Optional<String> location,
                                                                              @RequestParam("from") Optional<LocalDate> from,
                                                                              @RequestParam("to") Optional<LocalDate> to) {

        var start = System.nanoTime();

        var response = ResponseEntity.ok(retrieveStatistics.retrieve(location, from, to));

        var duration = ( System.nanoTime() - start ) / 1_000_000;
        CompletableFuture.runAsync(() -> logger.info(String.format("retrieve the statistics [ duration = %d ms ] [ location = %s, from = %s, to = %s ]", duration, location.orElse("n/a"), from.orElse(null), to.orElse(null))));

        return response;

    }

}
