package covid19.stats.micasa.com.activities;

import covid19.stats.micasa.com.repositories.StatisticsRepository;
import covid19.stats.micasa.com.services.StatisticsService;

import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;

import static covid19.stats.micasa.com.utils.ExceptionUtils.getMessage;

public class LoadStatistics {

    Logger logger = LoggerFactory.getLogger(LoadStatistics.class);

    StatisticsRepository statisticsRepository;
    StatisticsService statisticsService;

    public LoadStatistics(StatisticsRepository statisticsRepository, StatisticsService statisticsService) {
        this.statisticsRepository = statisticsRepository;
        this.statisticsService = statisticsService;
    }

    @Scheduled(fixedRateString = "${data.reload-interval}")
    public void reloadStatistics() {
        var start = System.nanoTime();
        statisticsService.loadStatistics()
                .thenAccept(statisticsRepository::loadStatistics)
                .thenApply(flag -> System.nanoTime())
                .thenAcceptAsync(end -> logger.info(String.format("reloading the statistics [ duration = %d ms ] [ from = %s, to = %s ]", ( end - start ) / 1_000_000, statisticsRepository.getFrom(), statisticsRepository.getTo())))
                .exceptionally(exception -> {
                    //  using the logger's built-in string replacement since it handles arrays our of the box
                    logger.error("an exception occured while reloading the statistics : {} <- {}", getMessage(exception), exception.getStackTrace());
                    return null;
                });
    }

}
