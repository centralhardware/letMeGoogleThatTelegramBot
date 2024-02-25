import com.clickhouse.jdbc.ClickHouseDataSource
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.answers.answerInlineQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.telegramBotWithBehaviourAndLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onAnyInlineQuery
import dev.inmo.tgbotapi.types.InlineQueries.InlineQueryResult.InlineQueryResultArticle
import dev.inmo.tgbotapi.types.InlineQueries.InputMessageContent.InputTextMessageContent
import dev.inmo.tgbotapi.types.LinkPreviewOptions
import dev.inmo.tgbotapi.types.message.HTML
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotliquery.queryOf
import kotliquery.sessionOf
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource
import kotlin.collections.map
import kotlin.text.isBlank

val log = LoggerFactory.getLogger("root")

val dataSource: DataSource = try {
    ClickHouseDataSource(System.getenv("CLICKHOUSE_URL"))
} catch (e: SQLException) {
    throw RuntimeException(e)
}

private fun getHtmlLink(link: String, title: String): String = "<a href=\"${link}\">${title}</a>"

private fun urlEncode(url: String): String = URLEncoder.encode(url, StandardCharsets.UTF_8)

private fun getIconUrl(site: String): String = "https://www.google.com/s2/favicons?sz=64&domain_url=${site}"

val services = mapOf(
    "letmegooglethat.com" to "https://letmegooglethat.com/?q=",
    "googlethatforyou.com" to "https://googlethatforyou.com?q=",
    "lmgtfy.app" to "https://lmgtfy.app/?q=",
    "www.google.com" to "https://www.google.com/search?q=",
    "stackoverflow.com" to "https://stackoverflow.com/search?q="
)

fun getArticles(query: String): List<InlineQueryResultArticle>{
    val i = AtomicInteger(1);
    return services.map { getArticle(
        i.getAndIncrement().toString(),
        it.key,
        getHtmlLink(it.value + urlEncode(query), query),
        getIconUrl(it.key)
    ) }
}

private fun getArticle(
    id: String,
    title: String,
    content: String,
    thumbUrl: String
): InlineQueryResultArticle = InlineQueryResultArticle(
    id,
    title,
    InputTextMessageContent(content, HTML, LinkPreviewOptions.Disabled),
    thumbnailUrl = thumbUrl,
    hideUrl = true
)

suspend fun main() {
    val clickhouse = Clickhouse()
    telegramBotWithBehaviourAndLongPolling(System.getenv("BOT_TOKEN"),
        CoroutineScope(Dispatchers.IO),
        defaultExceptionsHandler = { log.warn("", it)}) {
        onAnyInlineQuery {
            log.info(it.query)
            clickhouse.log(it.query, true, it.user, it.user.id.chatId)
            if (it.query.isBlank()) {
                answer(
                    it,
                    listOf(
                        getArticle("1", "shrugs", "¯\\_(ツ)_/¯", ""),
                        getArticle(
                            "2",
                            "use pastebin",
                            "Please use pastebin.com, gist.github.com for share code or other long read text material",
                            ""
                        )
                    )
                )
                return@onAnyInlineQuery
            }

            answerInlineQuery(
                it,
                results = getArticles(it.query)
            )

            sessionOf(dataSource).execute(queryOf(
                """
                    INSERT INTO letMeGoogleThatForYouStatistic (
                        date_time,
                        chat_id,
                        username,
                        first_name,
                        last_name,
                        lang,
                        is_premium,
                        text
                    ) VALUES (
                        :dateTime,
                        :chatId,
                        :username,
                        :firstName,
                        :lastName,
                        :lang,
                        :isPremium,
                        :text
                    )
                """,
                mapOf("dateTime" to LocalDateTime.now(),
                    "chatId" to it.from.id.chatId,
                    "username" to it.user.username,
                    "firstName" to it.user.firstName,
                    "lastName" to it.user.lastName,
                    "lang" to it.user.languageCode,
                    "isPremium" to it.user.isPremium,
                    "text" to it.query)
            ))
        }
    }.second.join()
}