package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DirectoryPropertyGeneratorTest : StringSpec({

    "directoryProperty(name) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .directoryProperty("pictures")
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import java.io.File
            |import java.nio.`file`.Path
            |import kotlin.CharSequence
            |import kotlin.jvm.JvmName
            |import org.gradle.api.`file`.Directory
            |import org.gradle.api.`file`.DirectoryProperty
            |import org.gradle.api.provider.Provider
            |
            |public class Dog {
            |  public val pictures: DirectoryProperty = objects.directoryProperty()
            |
            |  public fun pictures(pictures: File) {
            |    this.pictures.set(pictures)
            |  }
            |
            |  @JvmName("picturesFromFileProvider")
            |  public fun pictures(pictures: Provider<File>) {
            |    this.pictures.set(objects.directoryProperty().fileProvider(pictures))
            |  }
            |
            |  public fun pictures(pictures: Path) {
            |    this.pictures.set(pictures.toFile())
            |  }
            |
            |  @JvmName("picturesFromPathProvider")
            |  public fun pictures(pictures: Provider<out Path>) {
            |    this.pictures.set(objects.directoryProperty().fileProvider(pictures.map { it.toFile() }))
            |  }
            |
            |  public fun pictures(pictures: CharSequence) {
            |    this.pictures.set(File(pictures.toString()))
            |  }
            |
            |  @JvmName("picturesFromCharSequenceProvider")
            |  public fun pictures(pictures: Provider<out CharSequence>) {
            |    val provider = objects.directoryProperty().fileProvider(pictures.map { File(it.toString()) })
            |    this.pictures.set(provider)
            |  }
            |
            |  public fun pictures(pictures: Directory) {
            |    this.pictures.set(pictures)
            |  }
            |
            |  @JvmName("picturesFromDirectoryProvider")
            |  public fun pictures(pictures: Provider<out Directory>) {
            |    this.pictures.set(pictures)
            |  }
            |}
            |
        """.trimMargin()
    }

})
