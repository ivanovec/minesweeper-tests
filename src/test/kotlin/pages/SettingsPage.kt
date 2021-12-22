package pages

import com.codeborne.selenide.Selenide.`$x`

object SettingsPage : BasePage(){

    val rowCount = `$x`("//label[text()='Row count']")
    val columnCount = `$x`("//label[text()='Column count']")
    val bombCount = `$x`("//label[text()='Bomb count']")
    val startButton = `$x`("//button[text()='Start the game']")

    val rowCountInput = `$x`("//label[text()='Row count']/input")
    val columnCountInput = `$x`("//label[text()='Column count']/input")
    val bombCountInput = `$x`("//label[text()='Bomb count']/input")

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