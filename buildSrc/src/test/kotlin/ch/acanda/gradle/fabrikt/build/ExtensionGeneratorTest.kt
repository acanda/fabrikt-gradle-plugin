package ch.acanda.gradle.fabrikt.build

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator.Companion.directoryProperty
import ch.acanda.gradle.fabrikt.build.ExtensionGenerator.Companion.enumSetProperty
import ch.acanda.gradle.fabrikt.build.ExtensionGenerator.Companion.fileProperty
import ch.acanda.gradle.fabrikt.build.ExtensionGenerator.Companion.named
import ch.acanda.gradle.fabrikt.build.ExtensionGenerator.Companion.stringProperty
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ExtensionGeneratorTest : StringSpec({

    "named() should implement the interface Named" {
        val typeSpec = TypeSpec.classBuilder("Dog").named().build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import kotlin.String
            |import org.gradle.api.Named
            |
            |public class Dog : Named {
            |  private val name: String = name
            |
            |  override fun getName(): String = name
            |}
            |
        """.trimMargin()
    }

    "stringProperty(name) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .stringProperty("breed")
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import kotlin.CharSequence
            |import org.gradle.api.provider.Property
            |import org.gradle.api.provider.Provider
            |
            |public class Dog {
            |  public val breed: Property<CharSequence> = objects.property(CharSequence::class.java)
            |
            |  public fun breed(breed: CharSequence) {
            |    this.breed.set(breed)
            |  }
            |
            |  public fun breed(breed: Provider<out CharSequence>) {
            |    this.breed.set(breed)
            |  }
            |}
            |
        """.trimMargin()
    }

    "fileProperty(name) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .fileProperty("healthRecord")
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import java.io.File
            |import java.nio.`file`.Path
            |import kotlin.CharSequence
            |import kotlin.jvm.JvmName
            |import org.gradle.api.`file`.RegularFile
            |import org.gradle.api.`file`.RegularFileProperty
            |import org.gradle.api.provider.Provider
            |
            |public class Dog {
            |  public val healthRecord: RegularFileProperty = objects.fileProperty()
            |
            |  public fun healthRecord(healthRecord: File) {
            |    this.healthRecord.set(healthRecord)
            |  }
            |
            |  @JvmName("healthRecordFromFileProvider")
            |  public fun healthRecord(healthRecord: Provider<File>) {
            |    this.healthRecord.set(objects.fileProperty().fileProvider(healthRecord))
            |  }
            |
            |  public fun healthRecord(healthRecord: Path) {
            |    this.healthRecord.set(healthRecord.toFile())
            |  }
            |
            |  @JvmName("healthRecordFromPathProvider")
            |  public fun healthRecord(healthRecord: Provider<out Path>) {
            |    this.healthRecord.set(objects.fileProperty().fileProvider(healthRecord.map { it.toFile() }))
            |  }
            |
            |  public fun healthRecord(healthRecord: CharSequence) {
            |    this.healthRecord.set(File(healthRecord.toString()))
            |  }
            |
            |  @JvmName("healthRecordFromCharSequenceProvider")
            |  public fun healthRecord(healthRecord: Provider<out CharSequence>) {
            |    val provider = objects.fileProperty().fileProvider(healthRecord.map { File(it.toString()) })
            |    this.healthRecord.set(provider)
            |  }
            |
            |  public fun healthRecord(healthRecord: RegularFile) {
            |    this.healthRecord.set(healthRecord)
            |  }
            |
            |  @JvmName("healthRecordFromRegularFileProvider")
            |  public fun healthRecord(healthRecord: Provider<out RegularFile>) {
            |    this.healthRecord.set(healthRecord)
            |  }
            |}
            |
        """.trimMargin()
    }

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

    "enumSetProperty(name, type) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .enumSetProperty("size", Size::class)
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest
            |import kotlin.collections.Iterable
            |import org.gradle.api.provider.Provider
            |import org.gradle.api.provider.SetProperty
            |
            |public class Dog {
            |  public val size: SetProperty<ExtensionGeneratorTest.Companion.Size> =
            |      objects.setProperty(ExtensionGeneratorTest.Companion.Size::class.java).convention(null as Set<ExtensionGeneratorTest.Companion.Size>?)
            |
            |  /**
            |   * small
            |   */
            |  public val SMALL: ExtensionGeneratorTest.Companion.Size =
            |      ExtensionGeneratorTest.Companion.Size.SMALL
            |
            |  /**
            |   * large
            |   */
            |  public val LARGE: ExtensionGeneratorTest.Companion.Size =
            |      ExtensionGeneratorTest.Companion.Size.LARGE
            |
            |  public fun size(vararg size: ExtensionGeneratorTest.Companion.Size) {
            |    this.size.set(size.toSet())
            |  }
            |
            |  public fun size(size: Iterable<ExtensionGeneratorTest.Companion.Size>) {
            |    this.size.set(size.toSet())
            |  }
            |
            |  public fun size(size: Provider<out Iterable<ExtensionGeneratorTest.Companion.Size>>) {
            |    this.size.set(size)
            |  }
            |}
            |
        """.trimMargin()
    }

}) {

    companion object {
        private fun TypeSpec.writeToString(): String {
            val fileSpec = FileSpec.builder("ch.acanda", "Dog").addType(this).build()
            val file = StringBuilder()
            fileSpec.writeTo(file)
            return file.toString()
        }

        internal enum class Size(val description: String) { SMALL("small"), LARGE("large") }
    }

}
