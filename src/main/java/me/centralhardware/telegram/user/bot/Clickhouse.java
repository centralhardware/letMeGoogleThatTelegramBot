package me.centralhardware.telegram.user.bot;

import com.clickhouse.client.*;
import com.clickhouse.data.ClickHouseDataStreamFactory;
import com.clickhouse.data.ClickHouseFormat;
import com.clickhouse.data.ClickHousePipedOutputStream;
import com.clickhouse.data.format.BinaryStreamUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Clickhouse {

    private final ClickHouseNode server;

    public Clickhouse(){
        this.server = ClickHouseNode.builder()
                .host(System.getenv("CLICKHOUSE_HOST"))
                .port(ClickHouseProtocol.HTTP)
                .database(System.getenv("CLICKHOUSE_DATABASE"))
                .credentials(ClickHouseCredentials.fromUserAndPassword(System.getenv("CLICKHOUSE_USER"),
                        System.getenv("CLICKHOUSE_PASS")))
                .build();

        createTable();
    }

    private void createTable(){
        try (ClickHouseClient client = openConnection()){
            ClickHouseRequest<?> request = client.read(server);
            request.query("""
                    CREATE TABLE IF NOT EXISTS letMeGoogleThatForYouStatistic
                    (
                        date_time DateTime,
                        chat_id BIGINT,
                        username Nullable(String),
                        first_name Nullable(String),
                        last_name Nullable(String),
                        is_premium bool,
                        lang text,
                        text VARCHAR(256),
                    )
                    engine = MergeTree
                    ORDER BY date_time
                    """)
                    .execute().get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(Update update){
        try (ClickHouseClient client = openConnection()){
            ClickHouseRequest.Mutation request =  client
                    .read(server)
                    .write()
                    .table("letMeGoogleThatForYouStatistic")
                    .format(ClickHouseFormat.RowBinary);

            ClickHouseConfig config = request.getConfig();
            CompletableFuture<ClickHouseResponse> future;

            User from = update.getInlineQuery().getFrom();

            try (ClickHousePipedOutputStream stream = ClickHouseDataStreamFactory.getInstance()
                    .createPipedOutputStream(config, (Runnable) null)){
                future = request.data(stream.getInputStream()).execute();
                write(stream, LocalDateTime.now());
                write(stream,from.getId());
                writeNullable(stream, from.getUserName());
                writeNullable(stream, from.getFirstName());
                writeNullable(stream, from.getLastName());
                write(stream, from.getIsPremium() != null && from.getIsPremium());
                write(stream, from.getLanguageCode());
                write(stream,update.getInlineQuery().getQuery());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (ClickHouseResponse response = future.get()){

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void writeNullable(OutputStream stream, Object value) throws IOException {
        if (value == null){
            BinaryStreamUtils.writeNull(stream);
            return;
        }
        BinaryStreamUtils.writeNonNull(stream);
        write(stream, value);
    }

    private void write(OutputStream stream, Object value) throws IOException {
        if (value instanceof String string){
            BinaryStreamUtils.writeString(stream, string);
        } else if (value instanceof UUID uuid){
            BinaryStreamUtils.writeUuid(stream, uuid);
        } else if (value instanceof Integer integer){
            BinaryStreamUtils.writeInt64(stream, integer);
        } else if (value instanceof  Long bigint){
            BinaryStreamUtils.writeInt64(stream, bigint);
        } else if (value instanceof Boolean bool){
            BinaryStreamUtils.writeBoolean(stream, bool);
        } else if (value instanceof LocalDateTime dateTime){
            BinaryStreamUtils.writeDateTime(stream, dateTime, TimeZone.getDefault());
        }
    }

    private ClickHouseClient openConnection(){
        return ClickHouseClient.newInstance(server.getProtocol());
    }


}
