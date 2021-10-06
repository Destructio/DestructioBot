import audio.LavaPlayerAudioProvider
import audio.TrackScheduler
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.VoiceChannelJoinSpec
import discord4j.voice.AudioProvider
//import org.apache.log4j.Logger
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


object DestructioBot {
    private val commands: MutableMap<String, Command> = HashMap()
    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager() // Create AudioPlayer instances and translates URLs to AudioTrack instances
    private val player: AudioPlayer = playerManager.createPlayer()  // Create an AudioPlayer so Discord4J can receive audio data
    private val provider: AudioProvider = LavaPlayerAudioProvider(player) // Create LavaPlayerAudioProvider
    //private var LOG: Logger = Logger.getLogger(DestructioBot::class.java) // Create Logger

    @JvmStatic
    fun main(args: Array<String>) {
        val apiToken = args[0]

        //LOG.info("Starting the Destructio Bot \n With token: $apiToken")
        println("Starting the Destructio Bot \nWith token: $apiToken")

        // This is an optimization strategy that Discord4J can utilize. It is not important to understand
        playerManager.configuration.frameBufferFactory = AudioFrameBufferFactory {
                bufferDuration: Int, format: AudioDataFormat?, stopping: AtomicBoolean? ->
                NonAllocatingAudioFrameBuffer(bufferDuration, format, stopping)
        }

        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager)

        // Create Bot object
        val bot = DiscordClientBuilder.create(apiToken)
            .build()
            .login()
            .block()!!

        bot.eventDispatcher.on(MessageCreateEvent::class.java)
            // subscribe is like block, in that it will *request* for action
            // to be done, but instead of blocking the thread, waiting for it
            // to finish, it will just execute the results asynchronously.
            .subscribe { event: MessageCreateEvent ->
                val content = event.message.content

                for ((key, value) in commands) {
                    if (content.startsWith("!$key")) {
                        //LOG.info("Income query from ${event.message.author} - $content")
                        println("Income query from ${event.message.author} - $content")
                        value.execute(event)
                        break
                    }
                }
                if (content.startsWith(">rs") || content.startsWith(">recent"))
                    event.message.delete().block()
            }
        bot.onDisconnect().block()
    }

    init {
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
                val out: String
                val wordCount = command.size
                out = when (wordCount) {
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
                channel.join {
                        spec: VoiceChannelJoinSpec -> spec.setProvider(provider)
                }?.block()
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
}

