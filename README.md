# COVID-19 API

I was inspired to create this by Nate Murray's email : `The Code of COVID-19 - Open-source projects to understand - and fight - the global pandemic`.

The data is sourced from [Johns Hopkins CSSE's COVID-19 Data Repository](https://github.com/CSSEGISandData/COVID-19).

They also provide this dashboard to visualize the data : https://coronavirus.jhu.edu/map.html.

## Implementation

### Java & Spring versions

* This project uses Java 14 and the latest [Spring Boot](https://spring.io/projects/spring-boot) version.

At the time of this writing, the only version of Spring Boot that supports Java 14 is `2.3.0.M2`.  
Versions `2.2.5-RELEASE` or `2.3.0.M3` will throw this exception : `java.lang.UnsupportedOperationException: This feature requires ASM8_EXPERIMENTAL`

* Version `3.0.0-M4` of the `surefire` plugin is needed to run the tests.

### Flow

The data is reloaded in memory, in the [`StatisticsRepository`](./src/main/java/covid19/stats/micasa/com/repositories/StatisticsRepository.java) class, from the source, by the [`LoadStatistics`](./src/main/java/covid19/stats/micasa/com/activities/LoadStatistics.java) class, on a fixed schedule.  
The source location and the reload frequency are configured in the [application's config file](./src/main/resources/application.yaml).

Because the reload of the entire data set is fast, we use the [full data sets](https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series) and not the [daily delta sets](https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_daily_reports).  
This simplifies the logic and allows for corrections to past data in case this happens.

## Run

### Command Line

__From the root of the project__ :

* Use Java 14

> `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-14.jdk/Contents/Home`

* Build and package

> `mvn package`

* Start the service

> `java --enable-preview -jar target/covid-19-api.jar`

The server port is `9000` as specified in the [application configuration](./src/main/resources/application.yaml).

* Access the API

> `curl -X GET 'http://localhost:9000/covid19/stats?location=US&from=2020-02-01&to=2020-02-29'`

```json
{
    "US / Sullivan, TN": [
        {
            "date": "2020-02-01",
            "value": {
                "confirmedCases": 0,
                "deaths": 0,
                "recoveries": 0
            }
        },
        ...
        {
            "date": "2020-02-28",
            "value": {
                "confirmedCases": 0,
                "deaths": 0,
                "recoveries": 0
            }
        }
    ],
    ...
    "US / Honolulu County, HI": [
        {
            "date": "2020-02-01",
            "value": {
                "confirmedCases": 0,
                "deaths": 0,
                "recoveries": 0
            }
        },
        ...
        {
            "date": "2020-02-28",
            "value": {
                "confirmedCases": 0,
                "deaths": 0,
                "recoveries": 0
            }
        }
    ]
}
```

> `curl -X GET 'http://localhost:9000/covid19/info'`

```json
{
    "lastUpdateTime": "2020-03-23T22:40:14.272977Z",
    "from": "2020-01-22",
    "to": "2020-03-22",
    "locations": [
        {
            "country": "US",
            "province": "Bon Homme, SD"
        },
        ...
        {
            "country": "Lebanon",
            "province": ""
        },
        {
            "country": "China",
            "province": "Macau"
        }
    ]
}
```

* Debug

If you need to debug the service, use the following command to start the service

> `java --enable-preview -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n -jar target/covid-19-api.jar`

then connect a remote debugging session to port `8000`


