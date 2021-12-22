package pages

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
    val victoryText = `$x`("//div[text()='victory']")


    val rowAttributeName = "data-row-index"
    val colAttributeName = "data-column-index"
    private val scrollHeight = executeJavaScript<Long>("return (arguments[0]).scrollHeight", mineTable)!!
    private val scrollWidth = executeJavaScript<Long>("return (arguments[0]).scrollWidth", mineTable)!!
    private val maxDisplayedCells = 22

    fun cellsWithState(state: CellState) = `$$`("div[content='${state.code}']")

    fun getCell(row :String, column :String) :SelenideElement{
        return `$`("div[$rowAttributeName = '$row'][$colAttributeName = '$column']")
    }

    fun gotLostState(){
        cells.first().click()
        cellsWithState(CellState.BOMB_HIDDEN).first().click()
    }

    fun getCellsAround(cell :SelenideElement): List<SelenideElement> {
        val cells = ArrayList<SelenideElement>()
        val currentRow = cell.attr(rowAttributeName)!!.toInt()
        val currentCol = cell.attr(colAttributeName)!!.toInt()

        for(i in currentRow - 1 .. currentRow + 1 ){
            if( i < 0 || i >= row.toInt()) continue // out of range
            for( j in currentCol - 1 .. currentCol +1){
                if(j % maxDisplayedCells == 0 && j != 0){ //border of rendered table
                    scrollTableTo(j + maxDisplayedCells, i) //scroll to next slice to render it in dom
                }
                if(i % maxDisplayedCells == 0 && i !=0) {
                    scrollTableTo(j, i + maxDisplayedCells)
                }
                if( j < 0 || j >= col.toInt()) continue //out of range
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
        cellsWithBombs.forEach { cellsAroundBombs.addAll(getCellsAround(it)) }

        val emptyCells = ArrayList<SelenideElement>()
        for ( cell in cellsWithState(CellState.NOT_BOMB_HIDDEN).snapshot()){
            if(!cellsAroundBombs.contains(cell)) emptyCells.add(cell)
        }
        return emptyCells
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
}