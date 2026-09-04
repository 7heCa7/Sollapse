package net.soullapse.soullives

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class SetLivesTabCompleter : TabCompleter {

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {

        return when (args.size) {

            // First argument: lives
            1 -> listOf("0", "1", "2", "3")
                .filter { it.startsWith(args[0]) }

            // Second argument: player
            2 -> Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter { it.startsWith(args[1], ignoreCase = true) }

            else -> emptyList()
        }
    }
}