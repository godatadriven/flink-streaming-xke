# Flink Streaming with Kakfa

During this XKE we want to experiment with [Flink](https://flink.apache.org/) a distributed processing engine like Spark.
Flink exists of a Streaming and a DataSet-API. We are going to look into the Streaming-API.

## What you need
Install the required tools which can be accomplised by downloading the distributions and unzip.
For Mac users 'brew' might be handy.

- Java 8
- Zookeeper
- Kafka (bundled with Zookeeper)
- Flink (also bundled with Zookeeper)
- Maven3 for building the fat-jar


## Create a Project
Create a quickstart java Maven project based on the flink-quickstart archetype.
 
    # this will create a subdir 'flink-streaming'
    mvn archetype:generate\
        -DarchetypeGroupId=org.apache.flink\
        -DarchetypeArtifactId=flink-quickstart-java\
        -DarchetypeVersion=1.1.1\
        -DgroupId=com.gdd.tryouts\
        -DartifactId=flink-streaming\
        -Dversion=0.1\
        -Dpackage=com.gdd\
        -DinteractiveMode=false

And dependency an extra dependency for Kafka in the pom.xml:

		<dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-connector-kafka-0.8_2.10</artifactId>
        <version>${flink.version}</version>
    </dependency>

Build it with 'mvn package'.


## Start a Flink
Start Flink and open the [webinterface](http://localhost:8081/):
    
    FLINK_HOME=$(pwd)
    ./bin/start-local.sh

## Kafka the pub-sub message bus:
Start Kafka and create a topic:

    # Start zookeeper & start Kafka
    bin/zookeeper-server-start.sh -daemon config/zookeeper.properties
    bin/kafka-server-start.sh config/server.properties

    # Create a Kafka topic:
    bin/kafka-topics.sh --list --zookeeper localhost:2181
    bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic texttest
    # Look there it is:
    bin/kafka-topics.sh --list --zookeeper localhost:2181

## How it Works?!
You have to submit your application (fat-jar) to Flink, so first build your jar with maven package.
Go to the [WebUI](http://localhost:8081/#/submit) or use `$FLINK_HOME/bin/flink run target/flink-streaming-0.1.jar [program arguments]`.
Note: in the pom.xml the mainClass is defined line ~350!


### Exercise 1: Count words
Send some text into the Kafka-topic with the Console-Producer and Parse these streaming events with Flink. Print the top words.


    bin/kafka-console-producer.sh --broker-list localhost:9092 --topic texttest
    $FLINK_HOME/bin/flink run target/flink-streaming-0.1.jar com.gdd.KafkaStreamingWordCount localhost:9092 texttopic myGroup1


### Exercise 2: Sliding Window - WordCount
One of Flinks' strong features is the windows functions. These are not limited to ingest event times!
Try make your streaming wordcount more fancy with Trending Words:

- Window over the last minute
- Let the window slide over 30 seconds
- Print only the top 1 words (every minute)


### Exercise 3: Twitter treding topics!
If you have completed the previous example you are very close of implementing a Treding Twitter Topics application!
You need a Twitter api-token and consumer-key to slurp tweets from the interwebz.
You can add the [flink-twitter-source](https://ci.apache.org/projects/flink/flink-docs-master/apis/streaming/connectors/twitter.html) to create your trending topics.

- Extract the #tags from the tweets
- Print the top 3 most tweeted hashtags over 5 minutes
- Check if your results match :-)


## Loading the HeroesOfTheStorm-replay dataset in Kafka
Instead of the console-producer we are going to load some data

- Replays (1 row per match): ReplayID (Unique per match), Game Mode (3=Quick Match, 4=Hero League, 5=Team League), Map, Replay Length, Timestamp (UTC)
- Replay Characters (10 rows per match, one for each Hero): ReplayID (Unique per match, links to other file), Is Auto Select, Hero, Hero Level, Is Winner, MMR Before


    #Download and unzip (brew install p7zip)
    wget https://d1i1jxrdh2kvwy.cloudfront.net/Data/HOTSLogs%20Data%202015-05-14%20-%202015-05-24.zip
    7z x HOTSLogs*.zip

    $KAFKA_HOME/bin/kafka-console-producer.sh --topic hotsReplays    --broker-list localhost:9092 < Replays*.csv
    $KAFKA_HOME/bin/kafka-console-producer.sh --topic hotsCharacters --broker-list localhost:9092 < ReplayCharacters*.csv


# Troubleshoot:

`Caused by: java.lang.NoClassDefFoundError: scala/collection/GenTraversableOnce$class`
`Build jar with -Pbuild-jar to create FAT jar with deps.`

`Caused by: java.lang.RuntimeException: Unable to retrieve any partitions for the requested topics [texttopic].Please check previous log entries`
_Topic does not exists!_

Pom.xml:
`<mainClass>com.gdd.KafkaStreamingWordCount</mainClass>`