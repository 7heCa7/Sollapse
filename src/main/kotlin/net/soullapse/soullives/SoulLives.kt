package net.soullapse.soullives

import java.util.UUID
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class SoulLives : JavaPlugin(), Listener {

    private lateinit var nameCommand: NameCommand

    lateinit var lifeManager: LifeManager
    lateinit var shatteredManager: ShatteredManager
    lateinit var soulStateListener: SoulStateListener

    val totemPlayers = mutableSetOf<UUID>()
    val pendingShatter = mutableSetOf<UUID>()

    override fun onEnable() {

        lifeManager = LifeManager(this)
        shatteredManager = ShatteredManager(this)
        soulStateListener = SoulStateListener(this)

        server.pluginManager.registerEvents(soulStateListener, this)
        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(DeathListener(this), this)
        server.pluginManager.registerEvents(ShatteredListener(this), this)

        // Name commands
        nameCommand = NameCommand(this)

        getCommand("setname")?.setExecutor(nameCommand)
        getCommand("setname")?.tabCompleter = nameCommand

        getCommand("resetname")?.setExecutor(nameCommand)
        getCommand("resetname")?.tabCompleter = nameCommand

        getCommand("giveflight")?.setExecutor(GiveFlightCommand())
        getCommand("tp")?.setExecutor(TpCommand())


        // Lives commands
        getCommand("checklives")?.setExecutor(LivesCommand(this))

        getCommand("setlives")?.setExecutor(SetLivesCommand(this))
        getCommand("setlives")?.tabCompleter = SetLivesTabCompleter()

        logger.info("SoulLives has been enabled!")
    }

    override fun onDisable() {

        nameCommand.shutdown()

        logger.info("SoulLives has been disabled!")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {

        val player = event.player

        val lives = lifeManager.getLives(player)

        player.sendMessage("You have $lives lives.")
    }
}