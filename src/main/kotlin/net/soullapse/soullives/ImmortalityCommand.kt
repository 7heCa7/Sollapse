package net.soullapse.soullives

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ImmortalityCommand(
    private val plugin: SoulLives
) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (args.size != 2) {
            sender.sendMessage("§cUsage: /immortality <player> <1-10>")
            return true
        }

        val target = Bukkit.getPlayerExact(args[0])

        if (target == null) {
            sender.sendMessage("§cPlayer not found.")
            return true
        }

        val health = args[1].toDoubleOrNull()

        if (health == null || health < 0.0 || health > 10.0) {
            sender.sendMessage("§cHealth must be between 0 and 10.")
            return true
        }

        if (health == 0.0) {
            plugin.immortalityManager.removeImmortality(target)

            sender.sendMessage(
                "§c${target.name} is no longer immortal."
            )

            return true
        }

        plugin.immortalityManager.setImmortality(target, health)

        sender.sendMessage(
            "§a${target.name} is now immortal below §f$health HP§a."
        )

        return true
    }
}