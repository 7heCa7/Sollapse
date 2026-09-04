package net.soullapse.soullives

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class SoulStateListener(
    private val plugin: SoulLives
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        updateSoulState(event.player)
    }

    fun updateSoulState(player: org.bukkit.entity.Player) {

        val lives = plugin.lifeManager.getLives(player)

        val component = when {
            lives >= 3 -> Component.text("✦ SOUL: PURE ✦")
                .color(NamedTextColor.GREEN)

            lives == 2 -> Component.text("✦ SOUL: CRACKED ✦")
                .color(NamedTextColor.YELLOW)

            lives == 1 -> Component.text("✦ SOUL: FRACTURED ✦")
                .color(NamedTextColor.GOLD)

            else -> Component.text("✦ SOUL: SHATTERED ✦")
                .color(NamedTextColor.RED)
        }

        player.sendActionBar(component)
    }
}