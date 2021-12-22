package tests

import com.codeborne.selenide.Selenide
import org.junit.jupiter.api.BeforeEach

open class BaseTest {
    @BeforeEach
    fun prepare(){
        Selenide.open("http://localhost:3000/") //TBD: move to config file
    }
}