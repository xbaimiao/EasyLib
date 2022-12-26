package com.xbaimiao.easylib.module.command

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

internal var driver: (String, CommandSpec.() -> Unit) -> CommandHandler =
    { _, _ -> error("command service not available") }

interface CommandHandler : CommandSpec, org.bukkit.command.CommandExecutor, TabCompleter {
    fun register(plugin: JavaPlugin)
}

fun command(token: String, block: CommandSpec.() -> Unit): CommandHandler = driver.invoke(token, block)

@CommandDsl
interface CommandSpec : ExecSpec<String>

@CommandDsl
class ArgParser<ArgType>(
    val argName: String,
    val parser: CommandExecutor.(String) -> ArgType,
    val completer: (CommandExecutor.(String) -> List<String>)? = null,
)

class CommandException(
    override val message: String,
    val component: Component? = null,
) : IllegalArgumentException(null as Throwable?) {
    constructor(component: Component) : this(
        "", component
    )

    override fun fillInStackTrace(): Throwable? = null
}

@CommandDsl
interface ExecSpec<T> {
    var token: String?
    var description: String
    var permission: String?
    var permissionMessage: Component?

    fun command(token: String, block: CommandSpec.() -> Unit): CommandSpec

    fun arg(spec: ExecSpec<String>.(ArgToken<String>) -> Unit): ExecSpec<String>

    fun <C> arg(parser: ArgParser<C>, spec: ExecSpec<C>.(ArgToken<C>) -> Unit): ExecSpec<C>

    fun onlinePlayer(spec: ExecSpec<Player>.(ArgToken<Player>) -> Unit): ExecSpec<Player>

    fun vararg(spec: ExecSpec<List<String>>.(ArgToken<List<String>>) -> Unit): ExecSpec<List<String>>

    fun completer(completer: CommandExecutor.(String) -> List<String>)

    fun exec(executor: CommandExecutor.() -> Unit)

    fun sub(command: CommandSpec)
}

@CommandDsl
interface ArgToken<T>

@CommandDsl
interface CommandExecutor {
    val sender: CommandSender
    val player: Player?
    val args: List<String>
    var async: Boolean
    val cmd: String

    fun error(message: String): Nothing = throw CommandException(message)

    fun error(component: Component): Nothing = throw CommandException(component)

    fun error(): Nothing = throw CommandException("")

    fun argError(message: String): Nothing

    fun argError(): Nothing = argError("")

    fun argError(argIndex: Int, message: String): Nothing

    fun valueOf(token: String): Any?

    fun <T> valueOf(token: ArgToken<T>): T

    fun requireHandItem(): CommandSenderAccessor<ItemStack> {
        val player = player ?: error("only player can execute this command")
        val item = player.getHandItemOrNull() ?: error("player must hold an item")
        return object : CommandSenderAccessor<ItemStack> {
            override fun get(): ItemStack = item
            override fun set(item: ItemStack) = player.setItemInHand(item)
            override fun clear() = player.setItemInHand(ItemStack(Material.AIR))
        }
    }

    fun optionalHandItem(): CommandSenderAccessor<ItemStack?> {
        val player = player ?: error("only player can execute this command")
        val item = player.getHandItemOrNull()
        return object : CommandSenderAccessor<ItemStack?> {
            override fun get(): ItemStack? = item
            override fun set(item: ItemStack?) = player.setItemInHand(item)
            override fun clear() = player.setItemInHand(ItemStack(Material.AIR))
        }
    }
}

interface CommandSenderAccessor<T> {
    fun get(): T
    fun set(item: T)
    fun clear()
}

internal val playerParser: ArgParser<Player> = ArgParser("player",
    parser = { token ->
        Bukkit.getPlayerExact(token) ?: error("unknown player \"$token\"")
    },
    completer = { token ->
        Bukkit.getOnlinePlayers().filter { it.name.lowercase().startsWith(token.lowercase()) }.map { it.name }
    }
)

private fun Player.getHandItemOrNull(): ItemStack? {
    return this.itemInHand.let { if (it.type == Material.AIR) null else it }
}
