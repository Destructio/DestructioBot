fun main(args: Array<String>) {
    val apiToken = args[0]

    val bot = DestructioBot(apiToken)
    bot.start()
}


