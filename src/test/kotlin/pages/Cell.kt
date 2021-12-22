package pages

import com.codeborne.selenide.SelenideElement

class Cell(private val element :SelenideElement) {
    val row = element.attr("data-row-index")!!.toInt()
    val col = element.attr("data-column-index")!!.toInt()

    fun getElement(){

    }
}