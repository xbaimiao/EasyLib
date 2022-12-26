package com.xbaimiao.easylib.module.command

class VarargNode : BaseNode<List<String>>() {

    override val literalToken: String
        get() = "<${token ?: " "}>..."

    override fun accept(ctx: CommandContext): Boolean = true

    override fun step(ctx: CommandContext): List<String> {
        val lst = mutableListOf<String>()
        ctx.args.all { lst.add(it) }
        ctx._args = ctx._args.subList(ctx.args.size, ctx.args.size)
        ctx.fullArgsIndex += ctx.args.size
        return lst
    }

    override fun sub(command: CommandSpec) {
        error("subnode of vararg is unsupported")
    }

    override fun vararg(spec: ExecSpec<List<String>>.(ArgToken<List<String>>) -> Unit): ExecSpec<List<String>> {
        error("subnode of vararg is unsupported")
    }

    override fun arg(spec: ExecSpec<String>.(ArgToken<String>) -> Unit): ExecSpec<String> {
        error("subnode of vararg is unsupported")
    }

    override fun <C> arg(parser: ArgParser<C>, spec: ExecSpec<C>.(ArgToken<C>) -> Unit): ExecSpec<C> {
        error("subnode of vararg is unsupported")
    }
}
