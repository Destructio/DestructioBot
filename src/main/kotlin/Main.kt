fun main(args: Array<String>) {

    if(args.isNotEmpty())
    {
        val apiToken = args[0] // Get token value from execution argument
        val bot = DestructioBot(apiToken) // Create DestructioBot object with apiToken
        bot.start() // Start DestructionBot
    }
    else
        println("API key is an empty string!")

}