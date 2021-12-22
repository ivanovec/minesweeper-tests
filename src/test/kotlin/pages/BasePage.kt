package pages

import com.codeborne.selenide.Selenide

open class BasePage {
    val pageHeader = Selenide.`$x`("//span[text()='Minesweeper']")
}