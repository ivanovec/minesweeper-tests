package tests

import com.codeborne.selenide.Condition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pages.GamePage
import pages.GamePage.CellState
import pages.SettingsPage
import java.time.Duration
import java.time.temporal.ChronoUnit

class GameTests : BaseTest(){

    @Test
    fun checkDefaultFields() {
        val page = GamePage("2", "2", "1")

        page.settingsHeader.shouldBe(Condition.visible)
        page.backButton.shouldBe(Condition.enabled)
        page.retryButton.shouldBe(Condition.enabled)
        page.bombSpan.shouldBe(Condition.visible)
        page.markerSpan.shouldBe(Condition.visible)
        page.mineTable.shouldBe(Condition.exist)
    }

    @Test
    fun revealNumber() {
        val page = GamePage("3", "3", "8")

        var cell = page.getCell("0","0")
        cell.click()
        cell.shouldHave(Condition.text("3"))

        page.retryButton.click()
        cell = page.getCell("0","1")
        cell.click()
        cell.shouldHave(Condition.text("5"))

        page.retryButton.click()
        cell = page.getCell("1","1")
        cell.click()
        cell.shouldHave(Condition.text("8"))
    }

    @Test
    fun reveal2() {
        val page = GamePage("2", "2", "2")

        var cell = page.getCell("0","0")
        cell.click()
        cell.shouldHave(Condition.text("2"))
    }

    @Test
    fun reveal4() {
        val page = GamePage("3", "2", "4")

        var cell = page.getCell("1","0")
        cell.click()
        cell.shouldHave(Condition.text("4"))
    }

    @Test
    fun reveal6() {
        val page = GamePage("3", "3", "6")

        var cell = page.getCell("1","1")
        cell.click()
        cell.shouldHave(Condition.text("6"))
    }

    @Test
    fun reveal7() {
        val page = GamePage("3", "3", "7")

        var cell = page.getCell("1","1")
        cell.click()
        cell.shouldHave(Condition.text("7"))
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
        val cellRow = bombCell.attr("data-row-index")
        val cellCol = bombCell.attr("data-column-index")
        bombCell.click()

        page.getCell(cellRow!!, cellCol!!).shouldHave(Condition.text("✱"))
        page.lostText.shouldBe(Condition.visible)
    }

    @Test
    fun swipeBomb() {
        val page = GamePage("10", "10",  "1")
        page.cellsWithState(CellState.BOMB_HIDDEN).first().click()
        page.cellsWithState(CellState.BOMB_HIDDEN).first().click()
        page.cellsWithState(CellState.BOMB).first().shouldHave(Condition.text("✱"))
    }

    @Test
    fun markCell(){
        val page = GamePage("10", "10",  "1")
        val cell = page.cells.first()
        cell.contextClick()
        cell.shouldHave(Condition.text("⚐"))
    }

    @Test
    fun checkRandomGeneration() {
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
        var emptyCells = page.cellsWithState(CellState.EMPTY).snapshot()
        if(emptyCells.isEmpty()) {
            val cellsHasNoNearBomb = page.getCellsNotHaveBombsAround()
            cellsHasNoNearBomb.first().click()
            emptyCells = page.cellsWithState(CellState.EMPTY).snapshot()
        }

        emptyCells.forEach { emptyCell ->
            assertThat(page.getCellsAroundIt(emptyCell))
                .allMatch {
                    val state = page.getCellState(it)
                    state == CellState.EMPTY || state == CellState.AROUND_BOMB
            }
        }
    }

    @Test
    fun reveal1() {
        val page = GamePage("2", "1", "1")
        var cell = page.getCell("0","0")
        cell.click()
        cell.shouldHave(Condition.text("1"))
    }

}