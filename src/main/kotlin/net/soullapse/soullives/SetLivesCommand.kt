package net.soullapse.soullives

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetLivesCommand(
    private val plugin: SoulLives
) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (!sender.isOp && !sender.hasPermission("soullives.admin")) {
            sender.sendMessage("You don't have permission to use this command.")
            return true
        }

        if (args.size != 2) {
            sender.sendMessage("Usage: /setlives <number> <player>")
            return true
        }

        val amount = args[0].toIntOrNull()

        if (amount == null) {
            sender.sendMessage("Lives must be a whole number.")
            return true
        }

        if (amount < 0) {
            sender.sendMessage("Lives cannot be negative.")
            return true
        }

        val target = Bukkit.getPlayerExact(args[1])

        if (target == null) {
            sender.sendMessage("That player is not online.")
            return true
        }

        plugin.lifeManager.setLives(target, amount)

        if (amount > 0) {
            plugin.shatteredManager.unshatter(target)
        }

        sender.sendMessage(
            "${target.name}'s lives have been set to $amount."
        )

        return true
    }
}