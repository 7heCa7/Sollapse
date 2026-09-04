package net.soullapse.soullives

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID

class LifeManager(private val plugin: SoulLives) {

    private val lives = mutableMapOf<UUID, Int>()

    private val dataFile = File(plugin.dataFolder, "data.yml")
    private val data = YamlConfiguration()

    init {
        load()
    }

    fun getLives(player: Player): Int {
        return lives.getOrPut(player.uniqueId) { 3 }
    }

    fun setLives(player: Player, amount: Int) {
        lives[player.uniqueId] = amount
        save()
    }

    fun removeLife(player: Player) {
        val currentLives = getLives(player)
        setLives(player, currentLives - 1)
    }

    private fun load() {

        // Make sure plugins/SoulLives exists
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        // Create data.yml if it doesn't exist
        if (!dataFile.exists()) {
            dataFile.createNewFile()
        }

        data.load(dataFile)

        val players = data.getConfigurationSection("players")

        if (players != null) {
            for (key in players.getKeys(false)) {

                try {
                    val uuid = UUID.fromString(key)
                    val playerLives = data.getInt("players.$key")

                    lives[uuid] = playerLives

                } catch (exception: IllegalArgumentException) {
                    plugin.logger.warning("Invalid UUID in data.yml: $key")
                }
            }
        }

        plugin.logger.info("Loaded ${lives.size} players from data.yml")
    }

    private fun save() {

        for ((uuid, playerLives) in lives) {
            data.set("players.$uuid", playerLives)
        }

        data.save(dataFile)
    }
}