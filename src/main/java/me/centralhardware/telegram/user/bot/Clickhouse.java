package me.centralhardware.telegram.user.bot;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

public class Clickhouse {

    public void insert(Update update){
        User from = update.getInlineQuery().getFrom();
        StatisticMapper.insertStatistic(
                LocalDateTime.now(),
                from.getId(),
                from.getUserName(),
                from.getFirstName(),
                from.getLastName(),
                from.getLanguageCode(),
                from.getIsPremium(),
                update.getInlineQuery().getQuery()
        );
    }

    public void insertStat(Long chatId, Long time){
        StatisticMapper.insertStat(
                LocalDateTime.now(),
                chatId,
                time
        );
    }
}
