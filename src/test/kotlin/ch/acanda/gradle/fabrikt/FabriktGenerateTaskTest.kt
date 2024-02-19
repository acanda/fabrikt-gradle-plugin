package ch.acanda.gradle.fabrikt

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll

class FabriktGenerateTaskTest : StringSpec({

    "copy() should copy all properties" {
        checkAll(generateTaskConfigGen) { config ->
            val copy = config.copy()
            config shouldMatch copy
        }
    }

})
