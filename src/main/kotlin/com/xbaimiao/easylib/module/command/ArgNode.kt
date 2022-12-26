package com.xbaimiao.easylib.module.command

open class ArgNode<O, T>(private val parser: CommandExecutor.(O) -> T) : BaseNode<T>(), ExecSpec<T> {
    override val literalToken: String
        get() = "<${token ?: " "}>"

    override fun accept(ctx: CommandContext): Boolean = true

    @Suppress("UNCHECKED_CAST")
    override fun step(ctx: CommandContext): T {
        val v = parser.invoke(ctx, ctx.args.first() as O)
        ctx._args = ctx._args.subList(1, ctx._args.size)
        ctx.fullArgsIndex++
        return v
    }
}
