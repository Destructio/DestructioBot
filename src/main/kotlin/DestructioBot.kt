import audio.TrackScheduler
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class DestructioBot(private val apiToken: String) {
    private val commands: MutableMap<String, Command> = HashMap() // Create a list of commands
    private val commandsOWO: MutableMap<String, Command> = HashMap() // Create a list of OWO bot commands
    private val playerManager =
        DefaultAudioPlayerManager() // Create AudioPlayer instances and translates URLs to AudioTrack instances
    private val player = playerManager.createPlayer()  // Create an AudioPlayer so Discord4J can receive audio data
    private val log = LogManager.getLogger(DestructioBot::class.java) // Create Logger
    private var bot = DiscordClientBuilder.create(apiToken).build().login().block()!! // Create DiscordClient for bot


    private val moder = bot.getChannelById(Snowflake.of(812673610757832706))
        .cast(MessageChannel::class.java).block()!!
    private val main = bot.getChannelById(Snowflake.of(534384759552344075))
        .cast(MessageChannel::class.java).block()!!

    fun start() {
        configurePlayer()

        bot.eventDispatcher.on(MessageCreateEvent::class.java)
            /* subscribe is like block, in that it will *request* for action
             to be done, but instead of blocking the thread, waiting for it
             to finish, it will just execute the results asynchronously.*/
            .subscribe { event: MessageCreateEvent ->
                newMessageReact(event)
            }

        stop("Normal stop.")
    }

    fun stop(reason: String) {
        bot.onDisconnect().block()
        log.info("Closing Bot. Reason: $reason")
    }

    private fun newMessageReact(event: MessageCreateEvent) {

        if (event.message.channelId.equals(Snowflake.of(812673610757832706))
            || event.message.channelId.equals(Snowflake.of(534384759552344075)))
            return

        val message = event.message.content
        val username = event.message.author.get().username
        val specialSymbol = message.substring(0, 1)

        if (specialSymbol == "!") {
            log.info("Query from $username - Message: $message")
            for ((key, value) in commands) {
                if (message.startsWith("!$key")) {
                    value.execute(event)
                    break
                }
            }
        } else if (specialSymbol == ">") {
            log.info("Query from $username to OWO bot - Message: $message")
            for ((key, value) in commandsOWO) {
                if (message.startsWith(">$key")) {
                    value.execute(event)
                    break
                }
            }

        } else {
            log.debug("Message from $username - Text: $message")
            // TODO: Antispam protection
        }
    }

    private fun configurePlayer() {
        log.info("Starting the Destructio Bot with token: $apiToken")

        // This is an optimization strategy that Discord4J can utilize. It is not important to understand
        playerManager.configuration.frameBufferFactory =
            AudioFrameBufferFactory { bufferDuration: Int, format: AudioDataFormat?, stopping: AtomicBoolean? ->
                NonAllocatingAudioFrameBuffer(bufferDuration, format, stopping)
            }

        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager)
    }

    init {
        // OWO bot commands list
        commandsOWO["rs"] = object : Command {
            override fun execute(event: MessageCreateEvent) {
                event.message.delete().block()
            }
        }
        commandsOWO["recent"] = object : Command {
            override fun execute(event: MessageCreateEvent) {
                event.message.delete().block()
            }
        }

        // DestructioBot commands list
        commands["post"] = object : Command {
            override fun execute(event: MessageCreateEvent) {
                postFun(event)
            }
        }
        commands["ping"] = object : Command {
            override fun execute(event: MessageCreateEvent) {
                event.message
                    .channel.block()!!
                    .createMessage("pong!")
                    .block()
            }
        }
        commands["roll"] = object : Command {
            override fun execute(event: MessageCreateEvent) {
                val content = event.message.content
                val command = listOf(*content.split(" ").toTypedArray())
                val out = when (command.size) {
                    1 -> Random().nextInt(100 + 1).toString()
                    2 -> Random().nextInt(command[1].toInt() + 1).toString()
                    else -> "Please enter correct range! e.g !roll 100"
                }
                event.message.channel
                    .block()!!
                    .createMessage(out)
                    .block()
            }
        }
        commands["help"] = object : Command {
            override fun execute(event: MessageCreateEvent) {
                event.message
                    .channel.block()!!
                    .createMessage(
                        "More information about bot you can find here: " +
                                "https://github.com/Destructio/DistructioBot/blob/master/README.md"
                    )
                    .block()
            }
        }
        commands["join"] = object : Command {
            override fun execute(event: MessageCreateEvent) {
                val member = event.member.orElse(null)
                val voiceState = member.voiceState.block()!!
                val channel: VoiceChannel = voiceState.channel.block()!!
                channel.join().block()
            }
        }
        val scheduler = TrackScheduler(player)
        commands["play"] = object : Command {
            override fun execute(event: MessageCreateEvent) {
                val content = event.message.content
                val command = listOf(*content.split(" ").toTypedArray())
                playerManager.loadItem(command[1], scheduler)
            }
        }
        commands["stop"] = object : Command {
            override fun execute(event: MessageCreateEvent) {
                player.stopTrack()
            }
        }

    }

    private fun postFun(event: MessageCreateEvent) {

        val content = event.message.content
        val post = content.removePrefix("!post")

        val postButton = Button.primary("1", "Опубликовать")
        val cancelButton = Button.primary("0", "Не публиковать")

        val mdr = moder.createMessage(post)
            .withComponents(
                ActionRow.of(postButton, cancelButton)
            ).block()!!

        bot.eventDispatcher.on(ButtonInteractionEvent::class.java)
            .timeout(Duration.ofDays(1))
            .subscribe {
                    buttonEvent: ButtonInteractionEvent ->
                postLogic(buttonEvent,main,post)
                mdr.edit().withComponents(
                    ActionRow.of(postButton.disabled(), cancelButton.disabled())
                ).block()!!
            }
    }

    private fun postLogic(buttonEvent: ButtonInteractionEvent, main: MessageChannel, post: String) {
        log.info("ButtonInteractionEvent event - ${buttonEvent.customId}")

        if (buttonEvent.customId.equals("1")) {
            main.createMessage(post)
                .block()
            log.info("Опубликован пост в Main Channel message: $post")
            buttonEvent.reply("Пост опубликован!").block()
        }
        // TODO: Fix double post (when -> no -> yes = 2 posts)
        else if (buttonEvent.customId.equals("0")){
            log.info("Отмена публикации поста: $post")
            buttonEvent.reply("Пост отклонён!").block()
        }
        else log.error(buttonEvent.customId)




    }
}

