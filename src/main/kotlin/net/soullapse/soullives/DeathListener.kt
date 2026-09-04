package net.soullapse.soullives

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.ChatColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class DeathListener(
    private val plugin: SoulLives
) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDamage(event: EntityDamageEvent) {

        val player = event.entity as? Player ?: return

        if (plugin.totemPlayers.remove(player.uniqueId)) {
            return
        }

        if (player.health - event.finalDamage > 0) {
            return
        }

        val lives = plugin.lifeManager.getLives(player)

        if (player.inventory.itemInMainHand.type == Material.TOTEM_OF_UNDYING ||
            player.inventory.itemInOffHand.type == Material.TOTEM_OF_UNDYING
        ) {
            return
        }

        // Final life: soul is shattered.
        if (lives <= 1) {

            plugin.lifeManager.setLives(player, 0)
            plugin.shatteredManager.shatter(player)
            plugin.shatteredManager.markPendingKick(player)

            plugin.pendingShatter.add(player.uniqueId)

            return
        }

        // Prevent the actual death.
        event.isCancelled = true

        // Remove one life.
        plugin.lifeManager.removeLife(player)
        plugin.soulStateListener.updateSoulState(player)

        val remainingLives = lives - 1

        // Restore the player.
        player.health = 1.0

        player.addPotionEffect(
            PotionEffect(
                PotionEffectType.RESISTANCE,
                200,
                200,
                false,
                false,
                false
            )
        )

        player.addPotionEffect(
            PotionEffect(
                PotionEffectType.REGENERATION,
                220,
                2,
                false,
                false,
                false
            )
        )

        player.world.spawnParticle(
            Particle.SOUL,
            player.location.add(0.0, 1.0, 0.0),
            100,
            0.5,
            1.0,
            0.5,
            0.2
        )

        for (onlinePlayer in plugin.server.onlinePlayers) {
            onlinePlayer.playSound(
                onlinePlayer,
                Sound.ITEM_TRIDENT_THUNDER,
                1.2f,
                0.8f
            )
        }

        // Soul-state message.
        when (remainingLives) {

            2 -> {
                player.sendMessage(
                    "${ChatColor.YELLOW}Cracks slither across your soul."
                )
            }

            1 -> {
                player.sendMessage(
                    "${ChatColor.GOLD}Your soul breaks into fractures."
                )
            }
        }

        plugin.logger.info(
            "${player.name} survived a lethal hit. Lives remaining: $remainingLives"
        )
    }

    /*
     * Replace real player names in vanilla death messages
     * with their current display names/nicknames.
     *
     * Example:
     *
     *   Steve was slain by Alex
     *
     * becomes:
     *
     *   Raven was slain by Shadow
     */
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {

        val victim = event.player
        val killer = victim.killer

        var message = event.deathMessage ?: return

        // Replace victim's real name with their nickname.
        message = message.replace(
            victim.name,
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection()
                .serialize(victim.displayName())
        )

        // Replace killer's real name with their nickname.
        if (killer != null) {
            message = message.replace(
                killer.name,
                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacySection()
                    .serialize(killer.displayName())
            )
        }

        event.deathMessage = message
    }
    private fun getDeathReason(event: EntityDamageEvent): String {

        if (event is EntityDamageByEntityEvent) {

            val damager = event.damager

            if (damager is Player) {
                return damager.name
            }

            return damager.name
        }

        return when (event.cause) {

            EntityDamageEvent.DamageCause.FALL ->
                "falling"

            EntityDamageEvent.DamageCause.LAVA ->
                "lava"

            EntityDamageEvent.DamageCause.FIRE ->
                "fire"

            EntityDamageEvent.DamageCause.FIRE_TICK ->
                "fire"

            EntityDamageEvent.DamageCause.DROWNING ->
                "drowning"

            EntityDamageEvent.DamageCause.VOID ->
                "the void"

            EntityDamageEvent.DamageCause.KILL ->
                "being killed"

            EntityDamageEvent.DamageCause.STARVATION ->
                "starvation"

            else ->
                event.cause.name
                    .lowercase()
                    .replace("_", " ")
        }
    }
}