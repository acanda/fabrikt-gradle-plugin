package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.CodeGenerationType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
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
            cliArgs shouldContainInOrder listOf("--base-package", args.basePackage)
            cliArgs shouldContainInOrder listOf("--output-directory", args.outputDirectory.absolutePathString())
            cliArgs shouldContainInOrder listOf("--targets", CodeGenerationType.HTTP_MODELS.name)
        }
    }

}) {

    companion object {

        private val argsGen: Arb<FabriktArguments> = arbitrary {
            FabriktArguments(
                pathGen.bind(),
                Arb.stringPattern("[a-z]{1,5}(\\.[a-z]{1,5}){0,3}").bind(),
                pathGen.bind()
            )
        }

        private val pathGen: Arb<Path> = arbitrary {
            Paths.get(Arb.stringPattern("[A-Za-z0-9]{1,5}(/[A-Za-z0-9]{1,5}){0,3}").bind())
        }

    }

}
