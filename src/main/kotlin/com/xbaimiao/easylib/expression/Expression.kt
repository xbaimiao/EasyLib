package com.xbaimiao.easylib.expression

import org.wltea.expression.ExpressionEvaluator
import org.wltea.expression.datameta.Variable

/**
 * @author 小白
 * @date 2023/5/15 15:48
 **/
fun String.expression(): ExpressionResult {
    val eval = ExpressionEvaluator.evaluate(this)
    return ExpressionResult(eval)
}

fun String.expression(bindings: Map<String, Any>): ExpressionResult {
    val variables = ArrayList<Variable>()
    bindings.forEach { (key, value) ->
        variables.add(Variable.createVariable(key, value))
    }
    val eval = ExpressionEvaluator.evaluate(this, variables)
    return ExpressionResult(eval)
}