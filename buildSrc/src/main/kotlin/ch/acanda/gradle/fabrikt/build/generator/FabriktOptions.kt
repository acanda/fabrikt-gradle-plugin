package ch.acanda.gradle.fabrikt.build.generator

import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.ValidationLibrary

sealed interface FabriktOption {
    val fabriktOption: Enum<*>?
}

enum class ValidationLibraryOption(override val fabriktOption: ValidationLibrary) : FabriktOption {
    Jakarta(ValidationLibrary.JAKARTA_VALIDATION),
    Javax(ValidationLibrary.JAVAX_VALIDATION),
}

enum class DateTimeOverrideType(override val fabriktOption: CodeGenTypeOverride?) : FabriktOption {
    OffsetDateTime(null),
    Instant(CodeGenTypeOverride.DATETIME_AS_INSTANT),
    LocalDateTime(CodeGenTypeOverride.DATETIME_AS_LOCALDATETIME),
}
