package azkarra;

import io.streamthoughts.azkarra.api.annotations.Component;
import io.streamthoughts.azkarra.api.config.Conf;
import io.streamthoughts.azkarra.api.config.Configurable;
import io.streamthoughts.azkarra.api.streams.TopologyProvider;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

@Component
public class WordCountOffsetTopologyProvider implements TopologyProvider, Configurable {

    private String topicWordCountSource;
    private String topicWordOffsetSource;
    private String topicSink;
    private String stateStoreName;

    private static Long mapToLongOrNull(String s, String s2) {
        try {
            return Long.parseLong(s2);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void configure(final Conf conf) {
        topicWordCountSource = conf.getOptionalString("topic.source.wordcount")
                .orElse("streams-wordcount-output");
        topicWordOffsetSource = conf.getOptionalString("topic.source.wordoffset")
                .orElse("streams-plaintext-offset-input");
        topicSink = conf.getOptionalString("topic.sink")
                .orElse("streams-wordcount-offset-output");
        stateStoreName = conf.getOptionalString("state.store.name")
                .orElse("OffsetWordCount");
    }

    @Override
    public String version() {
        return Version.getVersion();
    }

    @Override
    public Topology get() {
        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, Long> countSource = builder.stream(topicWordCountSource, Consumed.with(Serdes.String(), Serdes.Long(), null, null));
        final KStream<String, String> offsetSource = builder.stream(topicWordOffsetSource);

        final KStream<String, Long> result = countSource.toTable().outerJoin(offsetSource.toTable(), (count, offset) -> {
            try {
                return Long.parseLong(offset) + count;
            } catch (Exception e) {
                System.out.println("AAAAA " + e.getLocalizedMessage());
                return null;
            }
        }, Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(stateStoreName).withValueSerde(Serdes.Long()))
                .toStream()
                .filter((s, aLong) -> aLong != null);

        result.to(topicSink, Produced.with(Serdes.String(), Serdes.Long()));

        return builder.build();
    }
}
