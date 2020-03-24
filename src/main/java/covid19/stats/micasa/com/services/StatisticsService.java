package covid19.stats.micasa.com.services;

import covid19.stats.micasa.com.domain.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface StatisticsService {

    CompletableFuture<Map<Location, SortedSet<Reading<Statistic>>>> loadStatistics();

}
