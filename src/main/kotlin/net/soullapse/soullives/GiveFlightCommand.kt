package net.soullapse.soullives

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GiveFlightCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (args.size != 1) {
            sender.sendMessage("§cUsage: /giveflight <player>")
            return true
        }

        val player = Bukkit.getPlayerExact(args[0])

        if (player == null) {
            sender.sendMessage("§cPlayer not found.")
            return true
        }

        if (player.gameMode != GameMode.SURVIVAL) {
            sender.sendMessage("§cThat player is not in Survival.")
            return true
        }

        player.allowFlight = !player.allowFlight

        if (player.allowFlight) {
            sender.sendMessage("§aFlight enabled for ${player.name}.")
        } else {
            player.isFlying = false
            sender.sendMessage("§cFlight disabled for ${player.name}.")
        }

        return true
    }
}