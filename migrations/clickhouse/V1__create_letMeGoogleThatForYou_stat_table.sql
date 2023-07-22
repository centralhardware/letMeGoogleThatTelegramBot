CREATE TABLE IF NOT EXISTS letMeGoogleThatForYouStatistic_stat
(
    date_time DateTime,
    chat_id BIGINT,
    time BIGINT
)
    engine = MergeTree
        ORDER BY date_time;