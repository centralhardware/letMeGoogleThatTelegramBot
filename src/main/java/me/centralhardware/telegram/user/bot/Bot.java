package me.centralhardware.telegram.user.bot;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Bot extends TelegramLongPollingBot {

    private final Clickhouse clickhouse = new Clickhouse();

   public Bot(){
       super(System.getenv("BOT_TOKEN"));
   }

   private final List<Function<String, InlineQueryResultArticle>> articles = List.of(
           query -> getArticle("1",
                   "letmegooglethat.com",
                   getHtmlLink(getLetMeGoogleThatUrl(query), query),
                   getIconUrl("letmegooglethat.com")),
           query -> getArticle("2",
                   "lmgtfy.app",
                   getHtmlLink(getLmgtfyUrl(query), query),
                   getIconUrl("lmgtfy.app")),
           query -> getArticle("3",
                   "google.com",
                   getHtmlLink(getGoogleUrl(query), query),
                   getIconUrl("google.com")),
           query -> getArticle("4",
                   "stackoverflow.com",
                   getHtmlLink(getStackoverflowUrl(query), String.format("Search stackoverflow: %s", query)),
                   getIconUrl("stackoverflow.com"))
   );

    @Override
    public void onUpdateReceived(Update update){
        StopWatch sw = StopWatch.createStarted();

        if (!update.hasInlineQuery()) return;

        InlineQuery inlineQuery = update.getInlineQuery();

        String query = inlineQuery.getQuery();

        if (StringUtils.isBlank(query)){
            var shrugs = getArticle("shrugs",  "¯\\_(ツ)_/¯", "1", "");
            var usePastebin = getArticle("2",
                    "use pastebin",
                    "Please use pastebin.com, gist.github.com for share code or other long read text material",
                    "");
            send(inlineQuery, List.of(shrugs, usePastebin));
        }

        send(inlineQuery, articles.stream().map(it -> it.apply(query)).toList());

        sw.stop();
        CompletableFuture.runAsync(() -> clickhouse.insert(update));
        CompletableFuture.runAsync(() ->
                clickhouse.insertStat(inlineQuery.getFrom().getId(), sw.getTime(TimeUnit.MILLISECONDS)));
    }

    @SneakyThrows
    private void send(InlineQuery inlineQuery, List<InlineQueryResultArticle> article){
        AnswerInlineQuery answer = AnswerInlineQuery.builder()
                .results(article)
                .inlineQueryId(inlineQuery.getId())
                .build();
        execute(answer);
    }

    private InlineQueryResultArticle getArticle(String id,
                                                String title,
                                                String content,
                                                String thumbUrl){
        return InlineQueryResultArticle.builder()
                .id(id)
                .title(title)
                .inputMessageContent(InputTextMessageContent.builder()
                        .messageText(content)
                        .parseMode("HTML")
                        .disableWebPagePreview(true)
                        .build())
                .thumbUrl(thumbUrl)
                .build();
    }

    private String getHtmlLink(String link, String title){
       return String.format("<a href=\"%s\">%s</a>", link, title);
    }

    private static String urlEncode(String url){
       return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    private static String getLetMeGoogleThatUrl(String query){
       return String.format("https://letmegooglethat.com/?q=%s", urlEncode(query));
    }

    private static String getLmgtfyUrl(String query){
       return String.format("https://lmgtfy.app/?q=%s", urlEncode(query));
    }

    private static String getGoogleUrl(String query){
        return String.format("https://www.google.com/search?q=%s", urlEncode(query));
    }

    private static String getStackoverflowUrl(String query){
       return String.format("https://stackoverflow.com/search?q=%s", urlEncode(query));
    }

    private static String getIconUrl(String site){
        return String.format("https://www.google.com/s2/favicons?sz=64&domain_url=%s", site);
    }

    @Getter
    public String botUsername = System.getenv("BOT_USERNAME");
}
