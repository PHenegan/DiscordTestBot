package com.github.phenegan;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface Command {
    void execute(MessageCreateEvent event);
}
