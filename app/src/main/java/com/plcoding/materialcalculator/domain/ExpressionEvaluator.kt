package com.plcoding.materialcalculator.domain

class ExpressionEvaluator(
    private val expression: List<ExpressionPart>
) {
    fun evaluate(): Double {
        return evalExpression(expression).value
    }

    private fun evaluateFactor(expression: List<ExpressionPart>): ExpressionResult {
        return when (val part = expression.firstOrNull()) {
            ExpressionPart.Op(Operation.ADD) -> {
                evaluateFactor(expression.drop(1))
            }
            ExpressionPart.Op(Operation.SUBTRACT) -> {
                evaluateFactor(expression.drop(1)).run {
                    ExpressionResult(remainingExpression, -value)
                }
            }
            ExpressionPart.Parentheses(ParenthesesType.Opening) -> {
                evalExpression(expression.drop(1)).run {
                    ExpressionResult(expression.drop(1), value)
                }
            }
            ExpressionPart.Op(Operation.PERCENT) -> evalTerm(expression.drop(1))

            is ExpressionPart.Number -> ExpressionResult(expression.drop(1),part.number)
            else -> throw java.lang.RuntimeException("Invalid part")
        }
    }

    private fun evalTerm(expression: List<ExpressionPart>): ExpressionEvaluator.ExpressionResult {
        val result = evaluateFactor(expression)
        var remaining = result.remainingExpression
        var sum = result.value
        while (true){
            when(remaining.firstOrNull()){
                ExpressionPart.Op(Operation.MULTIPLY) -> {
                    val factor = evaluateFactor(remaining.drop(1))
                    sum *= factor.value
                    remaining = factor.remainingExpression
                }
                ExpressionPart.Op(Operation.DIVIDE) -> {
                    val factor = evaluateFactor(remaining.drop(1))
                    sum /= factor.value
                    remaining = factor.remainingExpression
                }
                ExpressionPart.Op(Operation.PERCENT) -> {
                    val factor = evaluateFactor(remaining.drop(1))
                    sum *=( factor.value / 100)
                    remaining = factor.remainingExpression
                }
                else -> return ExpressionResult(remaining, sum)
            }
        }
    }

    private fun evalExpression(expression: List<ExpressionPart>): ExpressionEvaluator.ExpressionResult {
        val result = evalTerm(expression)
        var remaining = result.remainingExpression
        var sum = result.value
        while (true){
            when(remaining.firstOrNull()){
                ExpressionPart.Op(Operation.ADD) -> {
                    val term = evalTerm(remaining.drop(1))
                    sum += term.value
                    remaining = term.remainingExpression
                }
                ExpressionPart.Op(Operation.SUBTRACT) -> {
                    val term = evalTerm(remaining.drop(1))
                    sum -= term.value
                    remaining = term.remainingExpression
                }
                else -> return ExpressionResult(remaining, sum)
            }
        }
    }

    data class ExpressionResult(
        val remainingExpression: List<ExpressionPart>,
        val value: Double
    )
}