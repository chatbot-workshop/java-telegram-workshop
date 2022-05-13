/*
 * ChatBot Workshop
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

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class GettingStartedBot extends TelegramLongPollingBot {

    public static void main(final String[] args) throws TelegramApiException {
        final var api = new TelegramBotsApi(DefaultBotSession.class);
        final var bot = new GettingStartedBot();
        api.registerBot(bot);
    }

    @Override
    public String getBotUsername() {
//        return "MyWorkshopBot";
        return "mf1testbot";
    }

    @Override
    public String getBotToken() {
//        return "1234567890:ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return "5100817778:AAGwCM11bAUlEIwmBj7nWOjZvRD5hDfCDKg";
    }

    @Override
    public void onUpdateReceived(final Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            final var message = update.getMessage();
            final var chatId = message.getChatId();
            final var text = message.getText();
            final var answer = new SendMessage();
            answer.setChatId(String.valueOf(chatId)); // Why String? Because it can be a username, too.
            answer.setText(text);
            try {
                execute(answer);
            } catch (final TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

}
