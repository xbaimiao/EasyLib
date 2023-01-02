package com.xbaimiao.easylib.module.command

abstract class CommandSpec : CommandHandler {

    companion object {
        val newCommandSpec: (String) -> CommandSpec = { command: String -> CommandLauncher(command) }
    }

    protected val subCommands = mutableMapOf<String, CommandHandler>()

    override var description: String? = null
    override var permission: String? = null
    override var permissionMessage: String? = null

    override var exec: (CommandContext.() -> Unit)? = null
    override var tab: (CommandContext.() -> List<String>)? = null
    override var root: CommandHandler? = null

    override val argNodes = ArrayList<ArgNode>()

    /**
     * 添加子命令
     */
    fun sub(launcher: CommandHandler) {
        if (tab == null) {
            tab = {
                if (args.isEmpty()) {
                    subCommands.keys.toList()
                } else {
                    if (subCommands.containsKey(args[0])) {
                        subCommands[args[0]]!!.tab?.invoke(this) ?: emptyList()
                    } else {
                        subCommands.keys.toList().filter { it.startsWith(args[0]) }
                    }
                }
            }
        }
        launcher.root = this
        subCommands[launcher.command] = launcher
    }

    fun onlinePlayers(block: CommandSpec.() -> Unit = {}) {
        arg(onlinePlayers, block)
    }

    @JvmOverloads
    fun arg(argNode: ArgNode, block: CommandSpec.() -> Unit = {}) {
        argNodes.add(argNode)
        block.invoke(this)
    }

    fun exec(exec: CommandContext.() -> Unit) {
        this.exec = exec
    }

}