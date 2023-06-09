package me.centralhardware.telegram.user.bot;

import lombok.SneakyThrows;
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

public class Bot extends TelegramLongPollingBot {

    private final Clickhouse clickhouse = new Clickhouse();

   public Bot(){
       super(System.getenv("BOT_TOKEN"));
   }

    @Override
    public void onUpdateReceived(Update update){
        if (!update.hasInlineQuery()) return;

        CompletableFuture.runAsync(() -> clickhouse.insert(update));

        InlineQuery inlineQuery = update.getInlineQuery();

        String query = inlineQuery.getQuery();

        var letMeGoogleThatForYou = getArticle("letmegooglethat.com",
                getHtmlLink(getLetMeGoogleThatUrl(query), query),
                "1");
        var lmgfty = getArticle("lmgtfy.app",
                getHtmlLink(getLmgtfyUrl(query), query),
                "2");
        var google = getArticle("google.com",
                getHtmlLink(getGoogleUrl(query), query),
                "3");
        var stackoverflow = getArticle("stackoverflow.com",
                getHtmlLink(getStackoverflowUrl(query), String.format("Search stackoverflow: %s", query)),
                "4");

        send(inlineQuery, letMeGoogleThatForYou, lmgfty, google, stackoverflow);
    }

    @SneakyThrows
    private void send(InlineQuery inlineQuery, InlineQueryResultArticle...article){
        AnswerInlineQuery answer = AnswerInlineQuery.builder()
                .results(List.of(article))
                .inlineQueryId(inlineQuery.getId())
                .build();
        execute(answer);
    }

    private InlineQueryResultArticle getArticle(String title, String content, String id){
        return InlineQueryResultArticle.builder()
                .id(id)
                .title(title)
                .inputMessageContent(InputTextMessageContent.builder()
                        .messageText(content)
                        .parseMode("HTML")
                        .disableWebPagePreview(true)
                        .build())
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

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_USERNAME");
    }
}
