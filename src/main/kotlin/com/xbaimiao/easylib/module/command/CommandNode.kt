package com.xbaimiao.easylib.module.command

open class CommandNode : BaseNode<String>(), CommandSpec {
    override var description: String = ""

    override val literalToken: String
        get() = token!!

    override fun step(ctx: CommandContext): String {
        val v = ctx.fullArgs.subList(0, ctx.fullArgs.size - ctx.args.size).joinToString(" ")
        ctx._args = ctx._args.subList(1, ctx._args.size)
        ctx.fullArgsIndex++
        return v
    }

    override fun feedCompletion(ex: CommandExecutor, pattern: String): List<String>? {
        if (token?.lowercase()?.startsWith(pattern.lowercase()) == true) {
            return listOf(token!!)
        }
        return null
    }
}
