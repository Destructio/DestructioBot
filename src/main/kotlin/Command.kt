import discord4j.core.event.domain.message.MessageCreateEvent

interface Command {
    fun execute(event: MessageCreateEvent)
}