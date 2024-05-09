package ch.acanda.gradle.fabrikt

import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.ValidationLibrary

sealed interface FabriktOption {
    val fabriktOption: Enum<*>?
}

@Suppress("EnumNaming")
enum class ExternalReferencesResolutionOption(
    override val fabriktOption: ExternalReferencesResolutionMode,
) : FabriktOption {
    targeted(ExternalReferencesResolutionMode.TARGETED),
    aggressive(ExternalReferencesResolutionMode.AGGRESSIVE),
}

enum class DateTimeOverrideOption(
    override val fabriktOption: CodeGenTypeOverride?,
) : FabriktOption {
    OffsetDateTime(null),
    Instant(CodeGenTypeOverride.DATETIME_AS_INSTANT),
    LocalDateTime(CodeGenTypeOverride.DATETIME_AS_LOCALDATETIME),
}

enum class ValidationLibraryOption(
    override val fabriktOption: ValidationLibrary,
) : FabriktOption {
    Jakarta(ValidationLibrary.JAKARTA_VALIDATION),
    Javax(ValidationLibrary.JAVAX_VALIDATION),
    NoValidation(ValidationLibrary.NO_VALIDATION),
}

enum class ClientTargetOption(
    override val fabriktOption: ClientCodeGenTargetType,
) : FabriktOption {
    OkHttp(ClientCodeGenTargetType.OK_HTTP),
    OpenFeign(ClientCodeGenTargetType.OPEN_FEIGN),
}

enum class ControllerTargetOption(
    override val fabriktOption: ControllerCodeGenTargetType,
) : FabriktOption {
    Spring(ControllerCodeGenTargetType.SPRING),
    Micronaut(ControllerCodeGenTargetType.MICRONAUT),
    Ktor(ControllerCodeGenTargetType.KTOR),
}
