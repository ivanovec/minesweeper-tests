package pages

import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.SelenideElement
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.collections.ArrayList

class GamePage(val row: String = "10", val col: String = "10" , val bomb: String = "1") : BasePage() {

    init {
        SettingsPage.enterFields(row, col,  bomb).startButton.click()
    }

    val backButton = `$x`("//button[text()='Back']")
    val retryButton = `$x`("//button[text()='Retry']")
    val bombSpan = `$x`("//span[contains(text(),'Bombs')]")
    val markerSpan = `$x`("//span[contains(text(),'Markers')]")
    val mineTable = `$x`("//div[div[div[@data-column-index]]]")
    val cells = `$$x`("//div[@data-column-index]")
    val bombCells = `$$x`("//div[text()='✱']")
    val lostText = `$x`("//div[text()='you lost']")
    val allCells = `$$x`("//div[@data-column-index]")
    val scrollHeight = Selenide.executeJavaScript<Long>("return (arguments[0]).scrollHeight", mineTable)!!
    val scrollWidth = Selenide.executeJavaScript<Long>("return (arguments[0]).scrollWidth", mineTable)!!


    fun cellsWithState(state: CellState) = `$$`("div[content='${state.code}']")

    fun getCell(row :String, column :String) :SelenideElement{
        return `$`("div[data-row-index = '$row'][data-column-index = '$column']")
    }

    fun gotLostState(timeout :Duration = Duration.of(1, ChronoUnit.MINUTES)){
//        val deadline = System.currentTimeMillis() + timeout.toMillis()
//
//        while (!lostText.exists() && System.currentTimeMillis() < deadline) {
//            for (cell in cells) {
//                cell.click()
//                if (lostText.exists()) return
//            }
//            retryButton.click()
//        }
        cells.first().click()
        cellsWithState(CellState.BOMB_HIDDEN).first().click()
    }


    fun getCellsAroundIt(cell :SelenideElement): List<SelenideElement> {
        val cells = ArrayList<SelenideElement>()
        val currentRow = cell.attr("data-row-index")!!.toInt()
        val currentCol = cell.attr("data-column-index")!!.toInt()

        for(i in currentRow - 1 .. currentRow + 1 ){
            if( i < 0 || i >= row.toInt()) continue

            for( j in currentCol - 1 .. currentCol +1){
                if(j % 22 == 0 && j != 0){
                    scrollTableTo(scrollWidth.toInt() % col.toInt(), i)
                }
                if(i % 22 == 0 && i !=0) {
                    scrollTableTo(j, scrollHeight.toInt() % row.toInt())
                }
                if( j < 0 || j >= col.toInt()) continue
                if( i == currentRow && j == currentCol ) continue
                cells.add(getCell(i.toString(), j.toString()))
            }
        }

        return cells
    }

    fun scrollTableTo(x: Int, y :Int) = executeJavaScript<Void>("(arguments[0]).scrollTo($x, $y);", mineTable)

    fun getCellsNotHaveBombsAround(): List<SelenideElement> {
        val cellsWithBombs = cellsWithState(CellState.BOMB_HIDDEN)
        val cellsAroundBombs = ArrayList<SelenideElement>()
        cellsWithBombs.forEach { cellsAroundBombs.addAll(getCellsAroundIt(it)) }

        val emptyCells = ArrayList<SelenideElement>()
        for ( cell in cellsWithState(CellState.NOT_BOMB_HIDDEN).snapshot()){
            if(!cellsAroundBombs.contains(cell)) emptyCells.add(cell)
        }
        return emptyCells
//        return cellsWithState(CellState.NOT_BOMB_HIDDEN).filterNot { cellsAroundBombs.contains(it) }
    }

    fun revealCellWithNumber(number :String){
        val deadline = System.currentTimeMillis() + Duration.of(1, ChronoUnit.MINUTES).toMillis()

        val hiddenCells = cellsWithState(CellState.NOT_BOMB_HIDDEN).snapshot()
        while (true) {//&& System.currentTimeMillis() < deadline) {
            for (cell in cellsWithState(CellState.NOT_BOMB_HIDDEN)){
                if(cell.text() == number) return
                if(getCellState(cell) == CellState.NOT_BOMB_HIDDEN) cell.click()
            }
            retryButton.click()
        }
    }

    fun getCellStateByCode(code :String) = CellState.values().firstOrNull { s -> s.code == code } ?: CellState.AROUND_BOMB
    fun getCellState(cell :SelenideElement) = getCellStateByCode(cell.attr("content")!!)


    enum class CellState(val code :String){
        NOT_BOMB_HIDDEN("0"),
        MARK_HIDDEN_BOMB("6"),
        MARK_OPEN_BOMB("7"),
        MARK_NOT_BOMB("4"),
        BOMB("3"),
        BOMB_HIDDEN("2"),
        EMPTY("9"),
        AROUND_BOMB("")
    }

    inner class Cell(private val element :SelenideElement) {
        val row = element.attr("data-row-index")!!.toInt()
        val col = element.attr("data-column-index")!!.toInt()

        fun getElement(){
            scrollTableTo(col, row)
        }
    }
}