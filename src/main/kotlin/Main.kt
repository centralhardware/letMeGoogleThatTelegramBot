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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.sql.SQLException
import java.time.LocalDateTime
import javax.sql.DataSource
import kotlin.collections.map
import kotlin.text.isBlank

val dataSource: DataSource = try {
    ClickHouseDataSource(System.getenv("CLICKHOUSE_URL"))
} catch (e: SQLException) {
    throw RuntimeException(e)
}

private fun getHtmlLink(link: String, title: String): String = "<a href=\"${link}\">${title}</a>"

private fun urlEncode(url: String): String = URLEncoder.encode(url, StandardCharsets.UTF_8)

private fun getLetMeGoogleThatUrl(query: String): String = "https://letmegooglethat.com/?q=${urlEncode(query)}"

private fun getGoogleThatForYou(query: String): String = "https://googlethatforyou.com?q=${urlEncode(query)}"

private fun getLmgtfyUrl(query: String): String = "https://lmgtfy.app/?q=${urlEncode(query)}"

private fun getGoogleUrl(query: String): String = "https://www.google.com/search?q=${urlEncode(query)}"

private fun getStackoverflowUrl(query: String): String = "https://stackoverflow.com/search?q=${urlEncode(query)}"

private fun getIconUrl(site: String): String = "https://www.google.com/s2/favicons?sz=64&domain_url=${site}"

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

val articles = listOf<(String) -> InlineQueryResultArticle>(
    { query ->
        getArticle(
            "1",
            "letmegooglethat.com",
            getHtmlLink(getLetMeGoogleThatUrl(query!!), query),
            getIconUrl("letmegooglethat.com")
        )
    },
    {
            query ->
        getArticle(
            "2",
            "googlethatforyou.com",
            getHtmlLink(getGoogleThatForYou(query!!), query),
            getIconUrl("googlethatforyou.com")
        )
    },
    {
            query ->
        getArticle(
            "3",
            "lmgtfy.app",
            getHtmlLink(getLmgtfyUrl(query!!), query),
            getIconUrl("lmgtfy.app")
        )
    },
    {
            query ->
        getArticle(
            "4",
            "google.com",
            getHtmlLink(getGoogleUrl(query!!), query),
            getIconUrl("google.com")
        )
    },
    {
            query ->
        getArticle(
            "5",
            "stackoverflow.com",
            getHtmlLink(getStackoverflowUrl(query!!), query),
            getIconUrl("stackoverflow.com")
        )
    }
)

suspend fun main() {
    telegramBotWithBehaviourAndLongPolling(System.getenv("BOT_TOKEN"), CoroutineScope(Dispatchers.IO)) {
        onAnyInlineQuery {
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
                results = articles.map { article -> article.invoke(it.query) },

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