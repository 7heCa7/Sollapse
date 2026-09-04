package net.soullapse.soullives

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID

class ShatteredManager(private val plugin: SoulLives) {

    private val shattered = mutableSetOf<UUID>()

    private val dataFile = File(plugin.dataFolder, "data.yml")
    private val data = YamlConfiguration()
    private val pendingKick = mutableSetOf<UUID>()

    fun markPendingKick(player: Player) {
        pendingKick.add(player.uniqueId)
    }

    fun consumePendingKick(player: Player): Boolean {
        return pendingKick.remove(player.uniqueId)
    }

    init {
        load()
    }

    fun isShattered(player: Player): Boolean {
        return player.uniqueId in shattered
    }

    fun isShattered(uuid: UUID): Boolean {
        return uuid in shattered
    }

    fun shatter(player: Player) {
        shattered.add(player.uniqueId)
        save()
    }

    fun unshatter(player: Player) {
        shattered.remove(player.uniqueId)
        save()
    }

    private fun load() {

        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        if (!dataFile.exists()) {
            dataFile.createNewFile()
        }

        data.load(dataFile)

        val players = data.getStringList("shattered")

        for (uuidString in players) {
            try {
                shattered.add(UUID.fromString(uuidString))
            } catch (exception: IllegalArgumentException) {
                plugin.logger.warning(
                    "Invalid UUID in shattered list: $uuidString"
                )
            }
        }

        plugin.logger.info(
            "Loaded ${shattered.size} shattered players."
        )
    }

    private fun save() {

        data.set(
            "shattered",
            shattered.map { it.toString() }
        )

        data.save(dataFile)
    }
}