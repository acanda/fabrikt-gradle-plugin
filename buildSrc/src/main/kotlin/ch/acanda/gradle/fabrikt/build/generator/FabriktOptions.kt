package ch.acanda.gradle.fabrikt.build.generator

import com.cjbooms.fabrikt.cli.CodeGenTypeOverride

sealed interface FabriktOption {
    val fabriktOption: Enum<*>?
}

enum class DateTimeOverrideType(override val fabriktOption: CodeGenTypeOverride?) : FabriktOption {
    OffsetDateTime(null),
    Instant(CodeGenTypeOverride.DATETIME_AS_INSTANT),
    LocalDateTime(CodeGenTypeOverride.DATETIME_AS_LOCALDATETIME),
}
