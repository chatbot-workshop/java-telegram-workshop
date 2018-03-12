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
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

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

}
