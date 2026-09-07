package net.soullapse.soullives

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.entity.Player

class ImmortalityListener(
    private val plugin: SoulLives
) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDamage(event: EntityDamageEvent) {

        val player = event.entity as? Player ?: return

        val threshold = plugin.immortalityManager.getThreshold(player)
            ?: return

        val resultingHealth = player.health - event.finalDamage

        if (resultingHealth < threshold) {
            event.damage = player.health - threshold

            if (event.damage <= 0.0) {
                event.isCancelled = true
            }
        }
    }
}