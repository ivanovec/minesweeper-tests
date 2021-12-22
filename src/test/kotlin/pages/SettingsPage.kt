package pages

import com.codeborne.selenide.Selenide

object SettingsPage : BasePage(){

    val rowCount = Selenide.`$x`("//label[text()='Row count']")
    val columnCount = Selenide.`$x`("//label[text()='Column count']")
    val bombCount = Selenide.`$x`("//label[text()='Bomb count']")
    val startButton = Selenide.`$x`("//button[text()='Start the game']")

    val rowCountInput = Selenide.`$x`("//label[text()='Row count']/input")
    val columnCountInput = Selenide.`$x`("//label[text()='Column count']/input")
    val bombCountInput = Selenide.`$x`("//label[text()='Bomb count']/input")


    fun enterFields(row :String, column :String, bomb :String) : SettingsPage{
        rowCountInput.clear()
        rowCountInput.sendKeys(row)

        columnCountInput.clear()
        columnCountInput.sendKeys(column)

        bombCountInput.clear()
        bombCountInput.sendKeys(bomb)

        return this
    }
}