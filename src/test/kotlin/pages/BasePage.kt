package pages

import com.codeborne.selenide.Selenide

open class BasePage {
    val settingsHeader = Selenide.`$x`("//span[text()='Minesweeper']")
}