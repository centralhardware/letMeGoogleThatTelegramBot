package me.centralhardware.telegram.user.bot;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface StatisticMapper {

    @Insert("""
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
                #{dateTime},
                #{chatId},
                #{username},
                #{firstName},
                #{lastName},
                #{lang},
                #{isPremium},
                #{text}
            )
            """)
    void __insertStatistic(@Param("dateTime") LocalDateTime dateTime,
                           @Param("chatId") Long chatId,
                           @Param("username") String username,
                           @Param("firstName") String firstName,
                           @Param("lastName") String lastName,
                           @Param("lang") String lang,
                           @Param("isPremium") Boolean isPremium,
                           @Param("text") String text);

    static void insertStatistic(LocalDateTime dateTime,
                                Long chatId,
                                String username,
                                String firstName,
                                String lastName,
                                String lang,
                                Boolean isPremium,
                                String text){
        Mybatis.sqlSession.getMapper(StatisticMapper.class).__insertStatistic(
                dateTime,
                chatId,
                username,
                firstName,
                lastName,
                lang,
                isPremium != null && isPremium,
                text
        );
    }

    @Insert("""
            INSERT INTO letMeGoogleThatForYouStatistic_stat (
                date_time,
                chat_id,
                time
            )
            VALUES (
                #{dateTime},
                #{chatId},
                #{time}
            )
            """)
    void __insertStat(@Param("dateTime") LocalDateTime dateTime,
                    @Param("chatId") Long chatId,
                    @Param("time") Long time);

    static void insertStat(LocalDateTime dateTime,
                           Long chatId,
                           Long time){
        Mybatis.sqlSession.getMapper(StatisticMapper.class).__insertStat(
                dateTime,
                chatId,
                time
        );
    }

}
