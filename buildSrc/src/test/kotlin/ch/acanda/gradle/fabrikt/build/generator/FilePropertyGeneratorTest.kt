package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FilePropertyGeneratorTest : StringSpec({

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

})
