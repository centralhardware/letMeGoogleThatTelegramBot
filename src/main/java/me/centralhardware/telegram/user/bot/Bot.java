package me.centralhardware.telegram.user.bot;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bot extends TelegramLongPollingBot {

    private final Clickhouse clickhouse = new Clickhouse();

   public Bot(){
       super(System.getenv("BOT_TOKEN"));
   }

    @SneakyThrows(TelegramApiException.class)
    @Override
    public void onUpdateReceived(Update update){
        if (!update.hasInlineQuery()) return;

        clickhouse.insert(update);

        InlineQuery inlineQuery = update.getInlineQuery();

        String query = inlineQuery.getQuery();

        InlineQueryResultArticle article = InlineQueryResultArticle.builder()
                .id("1")
                .title("let me google that for you")
                .inputMessageContent(InputTextMessageContent.builder()
                        .messageText(String.format("https://letmegooglethat.com/?q=%s", query))
                        .build())
                .build();

        AnswerInlineQuery answer = AnswerInlineQuery.builder()
                .result(article)
                .inlineQueryId(inlineQuery.getId())
                .build();

        execute(answer);

    }

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_USERNAME");
    }
}
