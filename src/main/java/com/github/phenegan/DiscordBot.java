package com.github.phenegan;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Permission;

import java.util.HashMap;
import java.util.Map;

public final class DiscordBot {

    private static final Map<String, Command> commands = new HashMap<>();
    private static final Map<String, Command> adminCommands = new HashMap<>();
    private static String token;

    public static void main(String[] args) {
        token = args[0];

        //Creates the Bot and allows it to go online
        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0])
                .build()
                .login()
                .block();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .subscribe(event -> {
                    //gets the user message
                    final String content = event.getMessage().getContent();

                    //iterates through the command list and executes the matching command
                    for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                        if (content.startsWith("!" + entry.getKey())) {
                            entry.getValue().execute(event);
                        }
                    }

                    boolean isAdmin = event.getMember().get().getBasePermissions().block().contains(Permission.ADMINISTRATOR);

                    //commands that can only be run by admins are checked separately
                    if (isAdmin)
                        for (final Map.Entry<String, Command> entry : adminCommands.entrySet())
                            if (content.startsWith("!" + entry.getKey())) {
                                entry.getValue().execute(event);
                            }
                });

        client.onDisconnect().block();
    }

    static {
        commands.put("ping", event -> {
            event.getMessage()
                    .getChannel().block()
                    .createMessage("Pong!").block();
        });
        commands.put("coinflip", event -> {
            int randNum = (int)(Math.random() * 2);
            event.getMessage().getChannel().block()
                    .createMessage(randNum == 0 ? "Heads!" : "Tails!").block();
        });

        adminCommands.put("clear", event -> {
            Message command = event.getMessage();
            command.getChannel().block().getMessagesBefore(command.getId())
                    .doOnEach(messageSignal -> messageSignal.get().delete().block()).subscribe();
            command.delete().block();
        });
    }
}
