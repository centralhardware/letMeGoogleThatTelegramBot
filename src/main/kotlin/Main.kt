import dev.inmo.micro_utils.common.Warning
import dev.inmo.tgbotapi.AppConfig
import dev.inmo.tgbotapi.Trace
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.answers.answerInlineQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onAnyInlineQuery
import dev.inmo.tgbotapi.longPolling
import dev.inmo.tgbotapi.types.InlineQueries.InlineQueryResult.InlineQueryResultArticle
import dev.inmo.tgbotapi.types.InlineQueries.InputMessageContent.InputTextMessageContent
import dev.inmo.tgbotapi.types.InlineQueryId
import dev.inmo.tgbotapi.types.LinkPreviewOptions
import dev.inmo.tgbotapi.types.message.HTML
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

private fun getHtmlLink(link: String, title: String): String = "<a href=\"${link}\">${title}</a>"

private fun urlEncode(url: String): String = URLEncoder.encode(url, StandardCharsets.UTF_8)

private fun getIconUrl(site: String): String =
    "https://www.google.com/s2/favicons?sz=64&domain_url=${site}"

val services =
    mapOf(
        "letmegooglethat.com" to "https://letmegooglethat.com/?q=",
        "googlethatforyou.com" to "https://googlethatforyou.com?q=",
        "lmgtfy.app" to "https://lmgtfy.app/?q=",
        "www.google.com" to "https://www.google.com/search?q=",
        "stackoverflow.com" to "https://stackoverflow.com/search?q=",
    )

fun getArticles(query: String): List<InlineQueryResultArticle> {
    val i = AtomicInteger(1)
    return services.map {
        getArticle(
            i.getAndIncrement().toString(),
            it.key,
            getHtmlLink(it.value + urlEncode(query), query),
            getIconUrl(it.key),
        )
    }
}

private fun getArticle(
    id: String,
    title: String,
    content: String,
    thumbUrl: String = "",
): InlineQueryResultArticle =
    InlineQueryResultArticle(
        InlineQueryId(id),
        title,
        InputTextMessageContent(content, HTML, LinkPreviewOptions.Disabled),
        thumbnailUrl = thumbUrl,
        hideUrl = true,
    )

@OptIn(Warning::class)
suspend fun main() {
    AppConfig.init("letMeGoogleThatForYou")
    var res = longPolling {
        onAnyInlineQuery {
            Trace.save("query", mapOf("query" to it.query))
            if (it.query.isBlank()) {
                answer(
                    it,
                    listOf(
                        getArticle("1", "shrugs", "¯\\_(ツ)_/¯"),
                        getArticle("2", "nometa", "nometa.xyz"),
                        getArticle(
                            "3",
                            "How do I ask a good question?",
                            "https://stackoverflow.com/help/how-to-ask",
                        ),
                        getArticle(
                            "4",
                            "use pastebin",
                            "Please use pastebin.com, gist.github.com for share code or other long read text material",
                        ),
                    ),
                )
                return@onAnyInlineQuery
            }

            answerInlineQuery(it, results = getArticles(it.query))
        }
    }.second.join()
}
