//package org.stella
//
//import org.junit.jupiter.api.assertThrows
//import org.junit.jupiter.params.ParameterizedTest
//import org.junit.jupiter.params.provider.MethodSource
//import java.io.FileInputStream
//import java.io.IOException
//import java.nio.file.Files
//import java.nio.file.Path
//import java.nio.file.Paths
//import java.util.stream.Stream
//
//internal class MainTest {
//    @ParameterizedTest(name = "{index} Typechecking well-typed program {0}")
//    @MethodSource("wellTypedPathStream")
//    @Throws(
//        IOException::class,
//        Exception::class
//    )
//    fun testWellTyped(filepath: Path) {
//        println(filepath.toUri())
//        val original = System.`in`
//        val fips = FileInputStream(filepath.toFile())
//        System.setIn(fips)
//        main()
//        System.setIn(original)
//    }
//
//    @ParameterizedTest(name = "{index} Typechecking ill-typed program {0}")
//    @MethodSource("illTypedPathStream")
//    @Throws(
//        IOException::class,
//        Exception::class
//    )
//    fun testIllTyped(filepath: Path) {
//        println(filepath.toUri())
//        val original = System.`in`
//        val fips = FileInputStream(filepath.toFile())
//        System.setIn(fips)
//        assertThrows<Exception>("expected the typechecker to fail!") {
//            main()
//        }
//        System.setIn(original)
//    }
//
//    companion object {
//        private const val BASE_DIR = "src/test/resources/tests"
//        private const val WELL_TYPED_DIR = "$BASE_DIR/well-typed"
//        private const val ILL_TYPED_DIR = "$BASE_DIR/ill-typed"
//
//        @JvmStatic
//        fun wellTypedPathStream(): Stream<Path> = getFilesStream(WELL_TYPED_DIR)
//
//        @JvmStatic
//        fun illTypedPathStream(): Stream<Path> = getFilesStream(ILL_TYPED_DIR)
//
//        private fun getFilesStream(path: String) = Files.list(Paths.get(path))
//    }
//}

package org.stella

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.io.FileInputStream
import java.io.IOException

internal class MainTest {
    @ParameterizedTest(name = "{index} Typechecking well-typed program {0}")

    @ValueSource(strings = [
        "src/test/resources/tests/records/well-typed/records-1.stella",
//        "src/test/resources/tests/variants/well-typed/variants-2.stella",
//        "src/test/resources/tests/variants/well-typed/variants-1.stella",
        "src/test/resources/tests/core/well-typed/logical-operators.stella",
        "src/test/resources/tests/core/well-typed/factorial.stella",
        "src/test/resources/tests/core/well-typed/increment_twice.stella",
        "src/test/resources/tests/core/well-typed/squares.stella",
        "src/test/resources/tests/core/well-typed/higher-order-1.stella",
        "src/test/resources/tests/lists/well-typed/lists-1.stella",
//        "src/test/resources/tests/references/well-typed/refs-3.stella",
//        "src/test/resources/tests/references/well-typed/refs-1.stella",
//        "src/test/resources/tests/references/well-typed/refs-2.stella",
        "src/test/resources/tests/pairs/well-typed/pairs-1.stella",
        "src/test/resources/tests/tuples/well-typed/tuples-1.stella",
//        "src/test/resources/tests/exceptions/well-typed/panic-2.stella",
//        "src/test/resources/tests/exceptions/well-typed/panic-3.stella",
//        "src/test/resources/tests/exceptions/well-typed/panic-1.stella",
        "src/test/resources/tests/subtyping/well-typed/subtyping-1.stella",
        "src/test/resources/tests/subtyping/well-typed/subtyping-3.stella",
//        "src/test/resources/tests/subtyping/well-typed/subtyping-7.stella",
        "src/test/resources/tests/subtyping/well-typed/subtyping-5.stella",
        "src/test/resources/tests/subtyping/well-typed/subtyping-2.stella",
        "src/test/resources/tests/subtyping/well-typed/subtyping-6.stella",
        "src/test/resources/tests/subtyping/well-typed/subtyping-4.stella",
//        "src/test/resources/tests/subtyping/well-typed/subtyping-8.stella",
        "src/test/resources/tests/sum-types/well-typed/sum-types-2.stella",
        "src/test/resources/tests/sum-types/well-typed/sum-types-1.stella"
    ])
    @Throws(
        IOException::class,
        Exception::class
    )
    fun testWellTyped(filepath: String) {
        val original = System.`in`
        val fips = FileInputStream(File(filepath))
        System.setIn(fips)
        main()
        System.setIn(original)
    }

    @ParameterizedTest(name = "{index} Typechecking ill-typed program {0}")
    @ValueSource(strings = [
        "src/test/resources/tests/records/ill-typed/bad-records-1.stella",
        "src/test/resources/tests/variants/ill-typed/bad-variants-1.stella",
        "src/test/resources/tests/core/ill-typed/applying-non-function-1.stella",
        "src/test/resources/tests/core/ill-typed/argument-type-mismatch-2.stella",
        "src/test/resources/tests/core/ill-typed/bad-succ-3.stella",
        "src/test/resources/tests/core/ill-typed/bad-succ-1.stella",
        "src/test/resources/tests/core/ill-typed/applying-non-function-3.stella",
        "src/test/resources/tests/core/ill-typed/bad-if-1.stella",
        "src/test/resources/tests/core/ill-typed/undefined-variable-1.stella",
        "src/test/resources/tests/core/ill-typed/bad-if-3.stella",
        "src/test/resources/tests/core/ill-typed/bad-squares-1.stella",
        "src/test/resources/tests/core/ill-typed/bad-succ-2.stella",
        "src/test/resources/tests/core/ill-typed/argument-type-mismatch-3.stella",
        "src/test/resources/tests/core/ill-typed/applying-non-function-2.stella",
        "src/test/resources/tests/core/ill-typed/argument-type-mismatch-1.stella",
        "src/test/resources/tests/core/ill-typed/bad-if-4.stella",
        "src/test/resources/tests/core/ill-typed/undefined-variable-2.stella",
        "src/test/resources/tests/core/ill-typed/bad-squares-2.stella",
        "src/test/resources/tests/core/ill-typed/bad-if-2.stella",
        "src/test/resources/tests/core/ill-typed/shadowed-variable-1.stella",
        "src/test/resources/tests/lists/ill-typed/bad-variants-1.stella",
//        "src/test/resources/tests/references/ill-typed/bad-refs-3.stella",
//        "src/test/resources/tests/references/ill-typed/bad-refs-1.stella",
//        "src/test/resources/tests/references/ill-typed/bad-refs-2.stella",
        "src/test/resources/tests/pairs/ill-typed/bad-pairs-1.stella",
        "src/test/resources/tests/tuples/ill-typed/bad-tuples-1.stella",
//        "src/test/resources/tests/exceptions/ill-typed/bad-panic-1.stella",
//        "src/test/resources/tests/subtyping/ill-typed/bad-subtyping-5.stella",
        "src/test/resources/tests/subtyping/ill-typed/bad-subtyping-3.stella",
        "src/test/resources/tests/subtyping/ill-typed/bad-subtyping-1.stella",
        "src/test/resources/tests/subtyping/ill-typed/bad-subtyping-4.stella",
        "src/test/resources/tests/subtyping/ill-typed/bad-subtyping-2.stella",
        "src/test/resources/tests/sum-types/ill-typed/bad-sum-types-1.stella"
    ])
    @Throws(
        IOException::class,
        Exception::class
    )
    fun testIllTyped(filepath: String) {
        val original = System.`in`
        val fips = FileInputStream(File(filepath))
        System.setIn(fips)
        var typecheckerFailed = false
        try {
            main()
        } catch (e: java.lang.Exception) {
            typecheckerFailed = true
        }
        if (!typecheckerFailed) {
            throw java.lang.Exception("expected the typechecker to fail!")
        }        // TODO: check that there is a type error actually, and not a problem with implementation
        System.setIn(original)
    }
}