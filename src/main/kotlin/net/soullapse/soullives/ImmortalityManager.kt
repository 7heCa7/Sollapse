package net.soullapse.soullives

import org.bukkit.entity.Player
import java.util.UUID

class ImmortalityManager {

    private val thresholds = mutableMapOf<UUID, Double>()

    fun setImmortality(player: Player, health: Double) {
        thresholds[player.uniqueId] = health

        // If they're already below the threshold, immediately restore them.
        if (player.health < health) {
            player.health = health
        }
    }

    fun removeImmortality(player: Player) {
        thresholds.remove(player.uniqueId)
    }

    fun isImmortal(player: Player): Boolean {
        return thresholds.containsKey(player.uniqueId)
    }

    fun getThreshold(player: Player): Double? {
        return thresholds[player.uniqueId]
    }
}