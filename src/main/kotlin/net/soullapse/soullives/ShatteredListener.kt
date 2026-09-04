package net.soullapse.soullives

import org.bukkit.Sound
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class ShatteredListener(
    private val plugin: SoulLives
) : Listener {

    private val pendingKick = mutableSetOf<java.util.UUID>()

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {

        val player = event.entity

        if (!plugin.pendingShatter.remove(player.uniqueId)) {
            return
        }

        plugin.server.scheduler.runTask(plugin, Runnable {
            plugin.server.broadcastMessage(
//            "${ChatColor.RED}${player.name}'s soul has been shattered."
                "${ChatColor.RED}A soul has shattered."
            )

            for (onlinePlayer in plugin.server.onlinePlayers) {
                onlinePlayer.playSound(
                    onlinePlayer,
                    Sound.ITEM_TRIDENT_THUNDER,
                    1.2f,
                    0.8f
                )
            }

            for (onlinePlayer in plugin.server.onlinePlayers) {
                onlinePlayer.playSound(
                    onlinePlayer,
                    Sound.ITEM_TOTEM_USE,
                    0.6f,
                    0.8f
                )
            }

            player.kickPlayer("Your soul has been shattered.")
        })
    }}