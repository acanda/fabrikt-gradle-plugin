package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class FabriktArgumentsTest : StringSpec({

    "should handle any combination of arguments" {
        checkAll(argsGen) { args ->
            val cliArgs = args.getCliArgs()
            cliArgs shouldContainInOrder listOf("--api-file", args.apiFile.absolutePathString())
            cliArgs shouldContainInOrder listOf("--base-package", args.basePackage.toString())
            cliArgs shouldContainInOrder listOf("--output-directory", args.outputDirectory.absolutePathString())
            args.targets.forEach { target ->
                cliArgs shouldContainInOrder listOf("--targets", target.name)
            }
            args.apiFragments.forEach { fragment ->
                cliArgs shouldContainInOrder listOf("--api-fragment", fragment.toAbsolutePath().toString())
            }
        }
    }

}) {

    companion object {

        private val argsGen: Arb<FabriktArguments> = arbitrary {
            FabriktArguments(
                pathGen.bind(),
                Arb.set(pathGen, 0..3).bind(),
                Arb.stringPattern("[a-z]{1,5}(\\.[a-z]{1,5}){0,3}").bind(),
                pathGen.bind(),
                enumSet<CodeGenerationType>().bind(),
                enumSet<ClientCodeGenOptionType>().bind(),
                Arb.enum<ClientCodeGenTargetType>().orNull(0.2).bind()
            )
        }

        private val pathGen: Arb<Path> = arbitrary {
            Paths.get(Arb.stringPattern("[A-Za-z0-9]{1,5}(/[A-Za-z0-9]{1,5}){0,3}").bind())
        }

        private inline fun <reified T : Enum<T>> enumSet(): Arb<Set<T>> = arbitrary { randomSource ->
            val values = T::class.java.enumConstants.toList().shuffled().toMutableList()
            var count = randomSource.random.nextInt(0, values.size + 1)
            while (count-- > 0) {
                values.removeLast()
            }
            values.toSet()
        }

    }

}
