package covid19.stats.micasa.com.activities;

import covid19.stats.micasa.com.domain.Location;
import covid19.stats.micasa.com.repositories.StatisticsRepository;
import covid19.stats.micasa.com.services.LocationsService;
import covid19.stats.micasa.com.services.StatisticsService;

import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static covid19.stats.micasa.com.utils.ExceptionUtils.getMessage;

public class LoadStatistics {

    Logger logger = LoggerFactory.getLogger(LoadStatistics.class);

    StatisticsRepository statisticsRepository;
    StatisticsService statisticsService;
    Set<Location> validLocations;

    public LoadStatistics(StatisticsRepository statisticsRepository, StatisticsService statisticsService, LocationsService locationsService) {
        this.statisticsRepository = statisticsRepository;
        this.statisticsService = statisticsService;
        validLocations = locationsService.loadLocations();
    }

    @Scheduled(fixedRateString = "${data.reload-interval}")
    public void reloadStatistics() {
        var start = System.nanoTime();
        statisticsService.loadStatistics()
                .thenApply(map -> {
                    map.keySet().stream().forEach(validateLocation);
                    return map;
                })
                .thenAccept(statisticsRepository::loadStatistics)
                .thenApply(flag -> System.nanoTime())
                .thenAcceptAsync(end -> logger.info(String.format("reloading the statistics [ duration = %d ms ] [ from = %s, to = %s ]", ( end - start ) / 1_000_000, statisticsRepository.getFrom(), statisticsRepository.getTo())))
                .exceptionally(exception -> {
                    //  using the logger's built-in string replacement since it handles arrays our of the box
                    logger.error("an exception occured while reloading the statistics : {} <- {}", getMessage(exception), exception.getStackTrace());
                    return null;
                });
    }

    Function<Double, BiPredicate<Float, Float>> areSimilar = tolerance -> (first, second) -> Math.abs(first - second) <= tolerance;

    Consumer<Location> validateLocation = location -> {
        Optional<Location> referenceLocation = validLocations.stream().filter(validLocation -> validLocation.equals(location)).findFirst();
        if (!referenceLocation.isPresent()) {
            logger.warn(String.format("unknown location [ country = %s; province = %s ]", location.country(), location.province()));
        } else {
            if (!areSimilar.apply(0.0001).test(location.latitude(), referenceLocation.get().latitude()) || !areSimilar.apply(0.0001).test(location.longitude(), referenceLocation.get().longitude())) {
                logger.info(String.format("invalid coordinates [ country = %s; province = %s ] : ( %f, %f ) instead of ( %f, %f )", location.country(), location.province(), location.latitude(), location.longitude(), referenceLocation.get().latitude(), referenceLocation.get().longitude()));
            }
        }
    };

}
