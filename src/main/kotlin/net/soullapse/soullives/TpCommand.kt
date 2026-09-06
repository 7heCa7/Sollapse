package net.soullapse.soullives

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TpCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        val player = sender as? Player ?: run {
            sender.sendMessage("§cOnly players can use this command.")
            return true
        }

        if (args.size != 1) {
            player.sendMessage("§cUsage: /tpc <player>")
            return true
        }

        val target = Bukkit.getPlayerExact(args[0])

        if (target == null) {
            player.sendMessage("§cPlayer not found.")
            return true
        }

        player.teleport(target)

        player.sendMessage(
            "§aTeleported to ${target.name}."
        )

        return true
    }
}