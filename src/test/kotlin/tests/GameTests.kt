package tests

import com.codeborne.selenide.CollectionCondition
import com.codeborne.selenide.Condition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import pages.GamePage
import pages.GamePage.CellState
import java.time.Duration
import java.time.temporal.ChronoUnit

class GameTests : BaseTest(){

    companion object{
        @JvmStatic
        fun getRevealConfigurations() = listOf(  // rows count, columns count, bombs count, reveal coord, nearBombs
            Arguments.of("2", "1", "1", Pair("0", "0"), "1"),
            Arguments.of("2", "2", "2", Pair("0", "0"), "2"),
            Arguments.of("3", "3", "8", Pair("0", "0"), "3"),
            Arguments.of("3", "2", "4", Pair("1", "0"), "4"),
            Arguments.of("3", "3", "8", Pair("0", "1"), "5"),
            Arguments.of("3", "3", "6", Pair("1", "1"), "6"),
            Arguments.of("3", "3", "8", Pair("1", "1"), "8"),
        )
    }

    @Test
    fun checkDefaultFieldsState() {
        val page = GamePage("2", "2", "1")

        page.pageHeader.shouldBe(Condition.visible)
        page.backButton.shouldBe(Condition.enabled)
        page.retryButton.shouldBe(Condition.enabled)
        page.bombSpan.shouldBe(Condition.visible)
        page.markerSpan.shouldBe(Condition.visible)
        page.mineTable.shouldBe(Condition.exist)
    }

    @ParameterizedTest()
    @MethodSource("getRevealConfigurations")
    fun revealNearBombs(rowCount :String, colCount :String, bombCount :String, revealedCell :Pair<String, String>, expectedNumber :String) {
        val page = GamePage(rowCount, colCount, bombCount)
        var cell = page.getCell(revealedCell.first, revealedCell.second)
        cell.click()
        cell.shouldHave(Condition.text(expectedNumber))
    }

    @Test
    fun checkBombCount() {
        val bombsNumber  = "10"
        val page = GamePage("10", "10",  bombsNumber)
        page.bombSpan.shouldHave(Condition.text(bombsNumber))
        page.gotLostState()

        Assertions.assertEquals(page.bombCells.size.toString(), bombsNumber)
    }

    @Test
    fun revealBomb() {
        val page = GamePage("10", "10",  "1")
        page.cells.first().click()
        val bombCell = page.cellsWithState(CellState.BOMB_HIDDEN).first()
        val cellRow = bombCell.attr("data-row-index") //save coords because state will be changed
        val cellCol = bombCell.attr("data-column-index")
        bombCell.click()

        page.getCell(cellRow!!, cellCol!!).shouldHave(Condition.text("✱"))
        page.lostText.shouldBe(Condition.visible)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "10", "99"])
    fun win(bombs :String) {
        val page = GamePage("10", "10",  bombs)
        page.cellsWithState(CellState.BOMB_HIDDEN).forEach { it.contextClick() }
        page.cellsWithState(CellState.NOT_BOMB_HIDDEN).forEach { it.click() }

        page.victoryText.shouldBe(Condition.visible)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "99"])
    fun swipeBomb(bombsCount :String) { //check that number of bomb was not changed because of swiping
        val page = GamePage("10", "10",  bombsCount)
        page.cellsWithState(CellState.BOMB_HIDDEN).first().click() //trigger swipe
        page.cellsWithState(CellState.BOMB_HIDDEN).first().click()
        page.bombCells.size == bombsCount.toInt()
    }

    @Test
    fun markCell(){
        val page = GamePage("10", "10",  "1")
        val cell = page.cells.first()
        cell.contextClick()
        cell.shouldHave(Condition.text("⚐"))
    }

    @Test
    fun checkRandomGeneration() { //trying open same cell in the same configuration until meet bomb
        val page = GamePage("10", "10", "10")
        val deadline = System.currentTimeMillis() + Duration.of(1, ChronoUnit.MINUTES).toMillis()

        while (!page.lostText.exists() && System.currentTimeMillis() < deadline) {
            page.retryButton.click()
            page.getCell("0", "0").click()
            page.getCell("0", "1").click()
        }

        page.getCell("0", "1").shouldHave(Condition.text("✱"))
    }

    @Test
    fun openEmptyCell() {
        val page = GamePage("10", "10", "4")

        page.cellsWithState(CellState.NOT_BOMB_HIDDEN).first().click()
        var emptyCells = page.cellsWithState(CellState.EMPTY).snapshot() //work with snapshot to speed up
        if(emptyCells.isEmpty()) { //there is a big chance that first click reveal all empty cells for big table and small number of bombs, but big table slow down test a lot
            val cellsHasNoNearBomb = page.getCellsNotHaveBombsAround()
            cellsHasNoNearBomb.first().click()
            emptyCells = page.cellsWithState(CellState.EMPTY).snapshot()
        }

        emptyCells.forEach { emptyCell -> // check that all revealed cells are empty or with digit
            assertThat(page.getCellsAround(emptyCell))
                .allMatch {
                    val state = page.getCellState(it)
                    state == CellState.EMPTY || state == CellState.AROUND_BOMB
            }
        }
    }

    @Test
    fun openEmptyCellUpToDigit(){
        val row = "0"
        val col = "0"

        val page = GamePage("3", "1", "1")
        val cell = page.getCell(row, col)

        while (page.getCellState(cell) != CellState.EMPTY) {
            page.retryButton.click()
            cell.click()
        }

        cell.shouldBe(Condition.empty)
        assertThat(page.getCellState(cell)).isEqualTo(CellState.EMPTY)

        val digitCell = page.getCell("1", col)
        assertThat(digitCell.text().matches("\\d+".toRegex())).isTrue

        val bombCell = page.getCell("2", col)
        assertThat(page.getCellState(bombCell)).isEqualTo(CellState.BOMB_HIDDEN)
    }

}