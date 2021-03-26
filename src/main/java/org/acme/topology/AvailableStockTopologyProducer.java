package org.acme.topology;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.quarkus.kafka.client.serialization.JsonbSerde;
import org.acme.beans.Product;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

@ApplicationScoped
public class AvailableStockTopologyProducer {

    public static final String STOCK_LEVELS_TOPIC = "stock-levels";
    public static final String RESERVED_STOCK_TOPIC = "reserved-stock";

    public static final String STOCK_AVAILABLE_KEYSTORE = "stockAvailableKeystore";

    private final JsonbSerde<Product> productSerde = new JsonbSerde<>(Product.class);

    @Produces
    public Topology buildTopology() {
        final StreamsBuilder builder = new StreamsBuilder();

        final KTable<Product, Integer> stockLevels = builder.table(
                STOCK_LEVELS_TOPIC,
                Consumed.with(productSerde, Serdes.Integer()));
        final KStream<Product, Integer> stockReservations = builder.stream(
                RESERVED_STOCK_TOPIC,
                Consumed.with(productSerde, Serdes.Integer()));

        final KTable<Product, Integer> stockReserved = stockReservations.groupByKey().reduce(Integer::sum);

        stockLevels.leftJoin(
                stockReserved,
                (level, reserved) -> level - (reserved != null ? reserved : 0),
                Materialized.<Product, Integer, KeyValueStore<Bytes, byte[]>>as(STOCK_AVAILABLE_KEYSTORE)
                        .withKeySerde(productSerde)
                        .withValueSerde(Serdes.Integer()));

        return builder.build();
    }

}
