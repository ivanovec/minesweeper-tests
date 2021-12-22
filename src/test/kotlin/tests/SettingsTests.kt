package tests

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide.`$x`
import com.codeborne.selenide.SelenideElement
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.openqa.selenium.Keys
import pages.SettingsPage


class SettingsTests : BaseTest() {

    companion object {

        @JvmStatic
        fun getValidationParameters() = listOf(
            Arguments.of(SettingsPage.rowCountInput, "10001", "Board size should be less than 10000"),
            Arguments.of(SettingsPage.columnCountInput, "10001", "Board size should be less than 10000"),
            Arguments.of(SettingsPage.rowCountInput, "0", "All inputs should be positive"),
            Arguments.of(SettingsPage.columnCountInput, "0", "All inputs should be positive"),
            Arguments.of(SettingsPage.bombCountInput, "0", "All inputs should be positive"),
            Arguments.of(SettingsPage.bombCountInput, "-1", "All inputs should be positive"),
            Arguments.of(SettingsPage.columnCountInput, "-1", "All inputs should be positive"),
            Arguments.of(SettingsPage.rowCountInput, "-1", "All inputs should be positive"),
            Arguments.of(SettingsPage.bombCountInput, "0,2", "All inputs should be integer"),
            Arguments.of(SettingsPage.columnCountInput, "1,2", "All inputs should be integer")
        )

        @JvmStatic
        fun getTableToBombCountsValid() = listOf(  // rows, columns, bombs
            Arguments.of("2", "1", "1"),
            Arguments.of("1", "2", "1"),
            Arguments.of("10000", "10000", "99999999"),
            Arguments.of("10000", "1", "9999"),
            )

        @JvmStatic
        fun getTableToBombCountsInvalid() = listOf(  // rows, columns, bombs
            Arguments.of("1", "1", "1"),
            Arguments.of("1", "2", "2"),
            Arguments.of("10000", "10000", "100000000"),
        )
    }

    @Test
    fun checkDefaultFields(){
        SettingsPage.rowCount.should(Condition.exist)
        SettingsPage.rowCount.find("input").shouldHave(Condition.value("0"))
        SettingsPage.columnCount.should(Condition.exist)
        SettingsPage.columnCount.find("input").shouldHave(Condition.value("0"))
        SettingsPage.bombCount.should(Condition.exist)
        SettingsPage.bombCount.find("input").shouldHave(Condition.value("0"))

        SettingsPage.startButton.shouldBe(Condition.disabled)
        SettingsPage.settingsHeader.shouldBe(Condition.visible)
    }

    @Test
    fun increaseFieldsWithCounters(){
        val rows = SettingsPage.rowCountInput.value
        SettingsPage.rowCount.click()
        SettingsPage.rowCount.sendKeys(Keys.UP)
        Assertions.assertEquals(rows!!.toInt() + 1, SettingsPage.rowCountInput.value!!.toInt())

        val columns = SettingsPage.columnCountInput.value
        SettingsPage.columnCount.click()
        SettingsPage.columnCount.sendKeys(Keys.UP)
        Assertions.assertEquals(columns!!.toInt() + 1, SettingsPage.columnCountInput.value!!.toInt())

        val bombs = SettingsPage.bombCountInput.value
        SettingsPage.bombCount.click()
        SettingsPage.bombCount.sendKeys(Keys.UP)
        Assertions.assertEquals(bombs!!.toInt() + 1, SettingsPage.bombCountInput.value!!.toInt())
    }

    @Test
    fun startGameWithPositiveCounts(){
        SettingsPage.enterFields("2", "2", "1")
        SettingsPage.startButton.click()
        `$x`("//div[text()='Waiting for your move']").shouldBe(Condition.visible)
    }

    @ParameterizedTest
    @MethodSource("getTableToBombCountsValid")
    fun enterBoundariesCounts(rows: String, columns: String, bombs: String){
        SettingsPage.enterFields(rows, columns, bombs)
        SettingsPage.startButton.shouldBe(Condition.enabled)
    }

    @ParameterizedTest
    @MethodSource("getValidationParameters")
    fun checkFieldsValidation(element: SelenideElement, value: String, error: String){
        SettingsPage.enterFields("2", "2", "1")


        element.value = value
        `$x`("//div[text()='$error']").shouldBe(Condition.visible)
        SettingsPage.startButton.shouldBe(Condition.disabled)
    }

    @ParameterizedTest
    @MethodSource("getTableToBombCountsInvalid")
    fun enterMoreThanRowsXColumnsBombs(rows: String, columns: String, bombs: String){
        SettingsPage.enterFields(rows, columns, bombs)

        `$x`("//div[text()='Bomb count should be less than rows * columns']").shouldBe(Condition.visible)
        SettingsPage.startButton.shouldBe(Condition.disabled)
    }
}