package ch.acanda.gradle.fabrikt.build.generator

import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.ValidationLibrary

sealed interface FabriktOption {
    val fabriktOption: Enum<*>?
}

enum class ExternalReferencesResolutionOption(override val fabriktOption: ExternalReferencesResolutionMode) :
    FabriktOption {
    @Suppress("EnumNaming", "EnumEntryNameCase")
    targeted(ExternalReferencesResolutionMode.TARGETED),

    @Suppress("EnumNaming", "EnumEntryNameCase")
    aggressive(ExternalReferencesResolutionMode.AGGRESSIVE),
}

enum class ValidationLibraryOption(override val fabriktOption: ValidationLibrary) : FabriktOption {
    Jakarta(ValidationLibrary.JAKARTA_VALIDATION),
    Javax(ValidationLibrary.JAVAX_VALIDATION),
}

enum class DateTimeOverrideOption(override val fabriktOption: CodeGenTypeOverride?) : FabriktOption {
    OffsetDateTime(null),
    Instant(CodeGenTypeOverride.DATETIME_AS_INSTANT),
    LocalDateTime(CodeGenTypeOverride.DATETIME_AS_LOCALDATETIME),
}

enum class ClientTargetOption(override val fabriktOption: ClientCodeGenTargetType) : FabriktOption {
    OkHttp(ClientCodeGenTargetType.OK_HTTP),
    OpenFeign(ClientCodeGenTargetType.OPEN_FEIGN),
}

enum class ControllerTargetOption(override val fabriktOption: ControllerCodeGenTargetType) : FabriktOption {
    Spring(ControllerCodeGenTargetType.SPRING),
    Micronaut(ControllerCodeGenTargetType.MICRONAUT),
}
