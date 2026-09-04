package net.soullapse.soullives

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LivesCommand(
    private val plugin: SoulLives
) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        val lives = plugin.lifeManager.getLives(sender)

        val player = sender as? Player ?: return true

        plugin.soulStateListener.updateSoulState(player)

        return true
    }
}