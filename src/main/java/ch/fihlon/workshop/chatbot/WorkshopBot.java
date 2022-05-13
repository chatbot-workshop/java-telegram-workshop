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

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static java.util.stream.Collectors.joining;
import static org.telegram.abilitybots.api.objects.Flag.TEXT;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@SuppressWarnings("SameParameterValue")
public class WorkshopBot extends AbilityBot {

    private static final String BOT_TOKEN = "1234567890:ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String BOT_USERNAME = "MyWorkshopBot";
    private static final long CREATOR_ID = 1234567890L;

    public static void main(String[] args) throws TelegramApiException {
        final var db = MapDBContext.onlineInstance("bot.db");
        final var bot = new WorkshopBot(db);
        final var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
    }

    WorkshopBot(final DBContext db) {
        super(BOT_TOKEN, BOT_USERNAME, db);
    }

    @Override
    public long creatorId() {
        return CREATOR_ID;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
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

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Reply replyToPhoto() {
        return Reply.of(
            (bot, update) -> silent.send("Nice pic!", getChatId(update)),
                Flag.PHOTO);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Ability sayHi() {
        return Ability
                .builder()
                .name("hi")
                .info("says hi")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> {
                    final String firstName = context.user().getFirstName();
                    silent.send("Hi, " + firstName, context.chatId());
                })
                .reply(
                    (bot, update) -> silent.send("Wow, nice name!", update.getMessage().getChatId()),
                    TEXT,
                    update -> update.getMessage().getText().startsWith("/hi"),
                    isMarcus()
                )
                .build();
    }

    private Predicate<Update> isMarcus() {
        return update -> update.getMessage().getFrom().getFirstName().equalsIgnoreCase("Marcus");
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Ability counter() {
        return Ability.builder()
                .name("count")
                .info("increments a counter per user")
                .privacy(PUBLIC)
                .locality(ALL)
                .action(context -> {
                    final Map<String, Integer> counterMap = db.getMap("COUNTERS");
                    final long userId = context.user().getId();
                    final Integer counter = counterMap.compute(
                            String.valueOf(userId), (id, count) -> count == null ? 1 : ++count);
                    final String message = String.format("%s, your count is now %d!",
                            context.user().getUserName(), counter);
                    silent.send(message, context.chatId());
                })
                .build();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Ability contacts() {
        return Ability.builder()
                .name("contacts")
                .info("lists all users who contacted this bot")
                .privacy(PUBLIC)
                .locality(ALL)
                .action(context -> {
                    final Map<String, User> usersMap = db.getMap("USERS");
                    final String users = usersMap.values().stream().map(User::getUserName).collect(joining(", "));
                    final String message = "The following users already contacted me: " + users;
                    silent.send(message, context.chatId());
                })
                .build();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Reply savePhoto() {
        return Reply.of(
            (bot, update) -> {
                    final List<PhotoSize> photos = update.getMessage().getPhoto();
                    final PhotoSize photoSize = photos.stream()
                            .max(Comparator.comparing(PhotoSize::getFileSize))
                            .orElse(null);
                    if (photoSize != null) {
                        final String filePath = getFilePath(photoSize);
                        final File file = downloadPhoto(filePath);
                        System.out.println("Temporary file: " + file);
                        silent.send("Yeah, I got it!", getChatId(update));
                        sendPhotoFromFileId(photoSize.getFileId(), getChatId(update));
                    } else {
                        silent.send("Houston, we have a problem!", getChatId(update));
                    }
                },
                Flag.PHOTO);
    }

    private String getFilePath(final PhotoSize photo) {
        final var filePath = photo.getFilePath();
        if (filePath != null && !filePath.isBlank()) {
            return filePath;
        }
        final GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(photo.getFileId());
        try {
            final org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);
            return file.getFilePath();
        } catch (final TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File downloadPhoto(final String filePath) {
        try {
            return downloadFile(filePath);
        } catch (final TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Ability sendLogo() {
        return Ability
                .builder()
                .name("logo")
                .info("send the logo")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> sendPhotoFromUrl("https://avatars3.githubusercontent.com/u/13538066?s=200&v=5", context.chatId()))
                .build();
    }

    private void sendPhotoFromUrl(final String url, final Long chatId) {
        final SendPhoto sendPhotoRequest = new SendPhoto(); // 1
        sendPhotoRequest.setChatId(String.valueOf(chatId)); // 2
        sendPhotoRequest.setPhoto(new InputFile(url));      // 3
        try {
            execute(sendPhotoRequest);                      // 4
        } catch (final TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPhotoFromFileId(final String fileId, final Long chatId) {
        final SendPhoto sendPhotoRequest = new SendPhoto(); // 1
        sendPhotoRequest.setChatId(String.valueOf(chatId)); // 2
        sendPhotoRequest.setPhoto(new InputFile(fileId));   // 3
        try {
            execute(sendPhotoRequest);                      // 4
        } catch (final TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Ability sendIcon() {
        return Ability
                .builder()
                .name("icon")
                .info("send the icon")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> sendPhotoFromUpload("src/main/resources/chatbot.jpg", context.chatId()))
                .build();
    }

    private void sendPhotoFromUpload(final String filePath, final Long chatId) {
        final SendPhoto sendPhotoRequest = new SendPhoto();           // 1
        sendPhotoRequest.setChatId(String.valueOf(chatId));           // 2
        sendPhotoRequest.setPhoto(new InputFile(new File(filePath))); // 3
        try {
            execute(sendPhotoRequest);                                // 4
        } catch (final TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Ability sendKeyboard() {
        return Ability
                .builder()
                .name("keyboard")
                .info("send a custom keyboard")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> {
                    final SendMessage message = new SendMessage();
                    message.setChatId(String.valueOf(context.chatId()));
                    message.setText("Enjoy this wonderful keyboard!");

                    final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                    final List<KeyboardRow> keyboard = new ArrayList<>();

                    // row 1
                    KeyboardRow row = new KeyboardRow();
                    row.add("/hello");
                    row.add("/hi");
                    row.add("/count");
                    keyboard.add(row);

                    // row 2
                    row = new KeyboardRow();
                    row.add("/contacts");
                    row.add("/logo");
                    row.add("/icon");
                    keyboard.add(row);

                    // activate the keyboard
                    keyboardMarkup.setKeyboard(keyboard);
                    message.setReplyMarkup(keyboardMarkup);

                    silent.execute(message);
                })
                .build();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Ability format() {
        return Ability
                .builder()
                .name("format")
                .info("formats the message")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> {
                    silent.sendMd("You can make text *bold* or _italic_.", context.chatId());
                    silent.sendMd("`This is code.`", context.chatId());
                    silent.sendMd("```\nThis\nis\nmulti\nline\ncode.\n```", context.chatId());
                })
                .build();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Ability add() {
        return Ability
                .builder()
                .name("add")
                .info("adds to numbers")
                .locality(ALL)
                .privacy(PUBLIC)
                .input(2)
                .action(context -> {
                    final int a = Integer.parseInt(context.firstArg());
                    final int b = Integer.parseInt(context.secondArg());
                    final int sum = a + b;
                    silent.send(String.format("The sum of %d and %d is %d", a, b, sum), context.chatId());
                })
                .build();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Ability sayNo() {
        return Ability.builder()
                .name(DEFAULT)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(context -> silent.send("Sorry, I have no answer for you today.", context.chatId()))
                .build();
    }

    @VisibleForTesting
    void setSender(final MessageSender sender) {
        this.sender = sender;
    }

    @VisibleForTesting
    void setSilent(final SilentSender silent) {
        this.silent = silent;
    }

}
