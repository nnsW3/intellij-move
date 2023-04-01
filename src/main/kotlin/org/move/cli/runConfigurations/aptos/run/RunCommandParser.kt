package org.move.cli.runConfigurations.aptos.run

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.intellij.util.execution.ParametersListUtil

data class RunCommandParser(
    val functionId: String,
    val typeArgs: List<String>,
    val args: List<String>,
    val profile: String
) {
    companion object {
        class Parser : CliktCommand() {
            val functionId: String by option("--function-id").required()
            val typeParams: List<String> by option("--type-args").multiple()
            val params: List<String> by option("--args").multiple()
            val profile: String? by option("--profile")
            override fun run() {}
        }

        fun parse(rawCommand: String): RunCommandParser? {
            val command = ParametersListUtil.parse(rawCommand).joinToString(" ")
            if (!command.startsWith("move run")) return null

            val arguments =
                command.drop("move run".length + 1).let { ParametersListUtil.parse(it) }
            val runParser = Parser()
            try {
                runParser.parse(arguments)
            } catch (e: CliktError) {
                return null
            }
            val functionId = runParser.functionId
            val profile = runParser.profile ?: "default"
            return RunCommandParser(functionId, runParser.typeParams, runParser.params, profile)
        }
    }
}
