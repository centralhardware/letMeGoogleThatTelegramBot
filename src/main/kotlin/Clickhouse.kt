import dev.inmo.tgbotapi.types.chat.CommonUser
import kotliquery.queryOf
import kotliquery.sessionOf
import java.time.LocalDateTime

class Clickhouse {

    fun log(text: String, user: CommonUser?) {
        user?.let {
            sessionOf(dataSource).execute(
                //language=GenericSQL
                queryOf(
                    """
           INSERT INTO default.bot_log (
                date_time,
                bot_name,
                user_id,
                usernames,
                first_name,
                last_name,
                is_premium,
                is_inline,
                lang,
                text
           ) VALUES (
                now(),
                'letMeGoogleThatForYou',
                :user_id,
                array(:usernames),
                :first_name,
                :last_name,
                :is_premium,
                True,
                :lang,
                :text
           ) 
        """, mapOf(
                        "user_id" to it.id.chatId,
                        "usernames" to if (it.username != null) it.username!!.full else null,
                        "first_name" to it.firstName,
                        "last_name" to it.lastName,
                        "is_premium" to it.isPremium,
                        "lang" to it.languageCode,
                        "text" to text
                    )
                )
            )
        }
    }


}