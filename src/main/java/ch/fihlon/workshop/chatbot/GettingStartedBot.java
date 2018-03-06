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

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class GettingStartedBot extends TelegramLongPollingBot {

    public static void main(final String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();
        final TelegramBotsApi api = new TelegramBotsApi();
        final GettingStartedBot bot = new GettingStartedBot();
        api.registerBot(bot);
    }

    @Override
    public String getBotUsername() {
        return "McPringleBot";
    }

    @Override
    public String getBotToken() {
        return "381467743:AAGJAtBZpqcqFHRFcSfzd3LZZTlYKf674ow";
    }

    @Override
    public void onUpdateReceived(final Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            final Long chatId = update.getMessage().getChatId();
            final String text = update.getMessage().getText();
            final SendMessage message = new SendMessage()
                    .setChatId(chatId)
                    .setText(text);
            try {
                execute(message);
            } catch (final TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

}
