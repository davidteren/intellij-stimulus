package stimulus

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

open class StimulusTestBase : BasePlatformTestCase()

class StimulusTest : StimulusTestBase() {
    @Test
    fun testSomething() {
        assertTrue(true)
    }
}