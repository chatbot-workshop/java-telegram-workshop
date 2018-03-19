/*
 * Chatbot Workshop
 * Copyright (C) 2018 Marcus Fihlon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.fihlon.workshop.chatbot;

import com.google.common.annotations.VisibleForTesting;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.EndUser;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;
import static org.telegram.abilitybots.api.objects.Flag.TEXT;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class WorkshopBot extends AbilityBot {

    private static String BOT_TOKEN = "381467743:AAGJAtBZpqcqFHRFcSfzd3LZZTlYKf674ow";
    private static String BOT_USERNAME = "McPringleBot";

    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();
        final TelegramBotsApi api = new TelegramBotsApi();
        final WorkshopBot bot = new WorkshopBot();
        api.registerBot(bot);
    }

    public WorkshopBot() {
        super(BOT_TOKEN, BOT_USERNAME);
    }

    @VisibleForTesting
    public WorkshopBot(final DBContext db) {
        super(BOT_TOKEN, BOT_USERNAME, db);
    }

    @Override
    public int creatorId() {
        return 318585602;
    }

    public Ability sayHelloWorld() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> silent.send("Hello world", context.chatId()))
                .build();
    }

    public Reply replyToPhoto() {
        return Reply.of(
                update -> silent.send("Nice pic!", getChatId(update)),
                Flag.PHOTO);
    }

    public Ability sayHi() {
        return Ability
                .builder()
                .name("hi")
                .info("says hi")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> {
                    final String firstName = context.user().firstName();
                    silent.send("Hi, " + firstName, context.chatId());
                })
                .reply(
                        update -> silent.send("Wow, nice name!", update.getMessage().getChatId()),
                        TEXT,
                        update -> update.getMessage().getText().startsWith("/hi"),
                        isMarcus()
                )
                .build();
    }

    private Predicate<Update> isMarcus() {
        return update -> update.getMessage().getFrom().getFirstName().equalsIgnoreCase("Marcus");
    }

    public Ability counter() {
        return Ability.builder()
                .name("count")
                .info("increments a counter per user")
                .privacy(PUBLIC)
                .locality(ALL)
                .action(context -> {
                    final Map<String, Integer> counterMap = db.getMap("COUNTERS");
                    final int userId = context.user().id();
                    final Integer counter = counterMap.compute(
                            String.valueOf(userId), (id, count) -> count == null ? 1 : ++count);
                    final String message = String.format("%s, your count is now %d!",
                            context.user().shortName(), counter);
                    silent.send(message, context.chatId());
                })
                .build();
    }

    public Ability contacts() {
        return Ability.builder()
                .name("contacts")
                .info("lists all users who contacted this bot")
                .privacy(PUBLIC)
                .locality(ALL)
                .action(context -> {
                    final Map<String, EndUser> usersMap = db.getMap("USERS");
                    final String users = usersMap.values().stream().map(EndUser::username).collect(joining(", "));
                    final String message = "The following users already contacted me: " + users;
                    silent.send(message, context.chatId());
                })
                .build();
    }

}
