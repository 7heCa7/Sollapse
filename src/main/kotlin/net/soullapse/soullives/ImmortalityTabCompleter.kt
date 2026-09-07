package net.soullapse.soullives

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ImmortalityTabCompleter : TabCompleter {

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {

        return when (args.size) {

            // /immortality <player>
            1 -> Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter {
                    it.startsWith(args[0], ignoreCase = true)
                }
                .sorted()

            // /immortality <player> <hearts>
            2 -> (0..10)
                .map { it.toString() }
                .filter {
                    it.startsWith(args[1])
                }

            else -> emptyList()
        }
    }
}