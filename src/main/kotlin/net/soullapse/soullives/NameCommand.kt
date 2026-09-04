package net.soullapse.soullives

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scoreboard.Team
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID

class NameCommand(
    private val plugin: SoulLives
) : CommandExecutor, TabCompleter, Listener {

    private val miniMessage = MiniMessage.miniMessage()

    /*
     * Tracks the TextDisplay belonging to each player.
     *
     * This is ONLY for managing the displays.
     * Movement is handled by mounting the display as a passenger.
     */
    private val displays = mutableMapOf<UUID, TextDisplay>()

    /*
     * Team used to hide the vanilla player nametag.
     */
    private val hiddenNameTeam: Team by lazy {
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

        scoreboard.getTeam("soullives_hidden_names")
            ?: scoreboard.registerNewTeam("soullives_hidden_names").apply {
                setOption(
                    Team.Option.NAME_TAG_VISIBILITY,
                    Team.OptionStatus.NEVER
                )
            }
    }

    private lateinit var stateTask: BukkitTask

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)

        stateTask = plugin.server.scheduler.runTaskTimer(
            plugin,
            Runnable {
                for ((uuid, display) in displays) {

                    val player = Bukkit.getPlayer(uuid)

                    if (player == null || !player.isOnline || !display.isValid) {
                        continue
                    }

                    if (player.isSneaking) {
                        // While sneaking:
                        display.isSeeThrough = false
                        display.textOpacity = 100.toByte()
                    } else {
                        // Standing:
                        display.isSeeThrough = true
                        display.textOpacity = 255.toByte()
                    }
                }
            },
            0L,
            1L
        )
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        /*
         * /setname <player> <name>
         */
        if (command.name.equals("setname", ignoreCase = true)) {

            if (args.size < 2) {
                sender.sendMessage("Usage: /setname <player> <name>")
                return true
            }

            val player = Bukkit.getPlayerExact(args[0])

            if (player == null) {
                sender.sendMessage("§cPlayer not found.")
                return true
            }

            val plainName = args
                .drop(1)
                .joinToString(" ")

            val displayName = miniMessage.deserialize(plainName)

            setName(
                player,
                displayName,
                plainName
            )

            sender.sendMessage(
                "§aSet ${player.name}'s name to §f$plainName"
            )

            return true
        }

        /*
         * /resetname <player>
         */
        if (command.name.equals("resetname", ignoreCase = true)) {

            if (args.size < 1) {
                sender.sendMessage("Usage: /resetname <player>")
                return true
            }

            val player = Bukkit.getPlayerExact(args[0])

            if (player == null) {
                sender.sendMessage("§cPlayer not found.")
                return true
            }

            resetName(player)

            sender.sendMessage(
                "§aReset ${player.name}'s name."
            )

            return true
        }

        return false
    }

    /**
     * Applies the custom name to:
     * 1. Chat
     * 2. Tab
     * 3. Overhead nametag
     */
    private fun setName(
        player: Player,
        name: Component,
        plainName: String
    ) {

        /*
         * Chat name
         */
        player.displayName(name)

        /*
         * Tablist name
         */
        player.playerListName(name)

        /*
         * Hide the vanilla Minecraft nametag.
         */
        hiddenNameTeam.addEntry(player.name)

        /*
         * Remove any old TextDisplay before creating
         * the new one.
         */
        removeDisplay(player)

        /*
         * Create the custom overhead display at the
         * player's current location.
         */
        val display = player.world.spawn(
            player.location,
            TextDisplay::class.java
        )

        /*
         * Text
         */
        display.text(Component.text(plainName))

        /*
         * Make the text face the viewer.
         */
        display.billboard = Display.Billboard.CENTER

        /*
         * No text shadow.
         */
        display.isShadowed = false

        /*
         * No background.
         */
        display.backgroundColor = Color.fromARGB(
            50,
            0,
            0,
            0
        )

        /*
         * Standing defaults.
         */
        display.isSeeThrough = true
        display.textOpacity = 255.toByte()

        display.transformation = Transformation(
            Vector3f(
                0f,
                0.3f,
                0f
            ),

            AxisAngle4f(),

            Vector3f(
                1f,
                1f,
                1f
            ),

            AxisAngle4f()
        )

        /*
         * Don't interpolate the transformation.
         * This makes the transformation apply immediately.
         */
        display.interpolationDuration = 0
        display.interpolationDelay = 0

        /*
         * Mount the display onto the player.
         *
         * This is what makes the display follow the player
         * smoothly without a teleport-every-tick task.
         */
        player.addPassenger(display)

        display.setRotation(
            player.yaw,
            90f
        )

        /*
         * Store it so we can update/remove it later.
         */
        displays[player.uniqueId] = display
    }

    /**
     * Resets all three name locations back to the
     * player's actual Minecraft username.
     */
    private fun resetName(player: Player) {

        val realName = Component.text(player.name)

        /*
         * Restore chat.
         */
        player.displayName(realName)

        /*
         * Restore tab.
         */
        player.playerListName(realName)

        /*
         * Restore vanilla overhead nametag.
         */
        hiddenNameTeam.removeEntry(player.name)

        /*
         * Remove custom overhead display.
         */
        removeDisplay(player)
    }

    /**
     * Removes the TextDisplay belonging to a player.
     */
    private fun removeDisplay(player: Player) {

        val display = displays.remove(player.uniqueId)

        if (display != null) {
            display.remove()
        }
    }

    /**
     * Clean up when a player leaves.
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        // Use the custom nickname if one exists.
        event.quitMessage(
            Component.text()
                .append(player.displayName().color(NamedTextColor.YELLOW))
                .append(Component.text(" has left the game", NamedTextColor.YELLOW))
                .build()
        )

        removeDisplay(player)
        hiddenNameTeam.removeEntry(player.name)
    }
    /**
     * Called by SoulLives.onDisable().
     */
    fun shutdown() {

        /*
         * Stop the sneaking-state task.
         */
        stateTask.cancel()

        /*
         * Remove all custom displays and restore
         * vanilla nametags.
         */
        Bukkit.getOnlinePlayers().forEach { player ->
            removeDisplay(player)
            hiddenNameTeam.removeEntry(player.name)
        }

        /*
         * Extra safety in case anything remains in the map.
         */
        displays.values.forEach { display ->
            if (display.isValid) {
                display.remove()
            }
        }

        displays.clear()
    }

    /**
     * Tab completion for player names.
     */
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {

        if (args.size == 1) {

            return Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter {
                    it.startsWith(
                        args[0],
                        ignoreCase = true
                    )
                }
        }

        return emptyList()
    }
}