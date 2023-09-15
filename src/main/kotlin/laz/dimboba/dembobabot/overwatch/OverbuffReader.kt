package laz.dimboba.dembobabot.overwatch

import laz.dimboba.dembobabot.exceptions.NotABattleTagException
import laz.dimboba.dembobabot.exceptions.ParsingHTMLException
import org.jsoup.Jsoup
import java.util.*

class OverbuffReader {
    private val address = "https://www.overbuff.com/players/"

    fun getPlayerStats(battleTag: String, gameMode: String = "All"): OverwatchPlayerStats {

        //println(battleTag + "  " + gameMode)

        checkBattleTag(battleTag)

        var postfixUrl: String = ""

        if(gameMode != "All")
            postfixUrl = "?gameMode=${gameMode.lowercase(Locale.getDefault())}"

        val userAddress = battleTag.replace('#', '-')

        val jsoupConnection = Jsoup.connect(address + userAddress + postfixUrl)
        //println(address + userAddress + postfixUrl)
        val document = jsoupConnection.get()
        try {

            //val body = document.body()

            val sideBar = document.select(".sidebar-column")[0]

            val winStats = sideBar.select(".text-stat-win")
            val loseStats = sideBar.select(".text-stat-loss")

            val nickname = document.select("title")[0]
                ?.text()
                ?.split(" ")
                ?.get(0) ?: battleTag.split('#')[0]

            return OverwatchPlayerStats(
                dpsStats = WinLossStats(winStats[0].text().toInt(), loseStats[0].text().toInt()),
                tankStats = WinLossStats(winStats[1].text().toInt(), loseStats[1].text().toInt()),
                supStats = WinLossStats(winStats[2].text().toInt(), loseStats[2].text().toInt()),
                name = nickname,
                gameMode = gameMode
            )
        } catch (ex: IndexOutOfBoundsException){
            throw ParsingHTMLException(ex.localizedMessage)
        } catch (ex: NumberFormatException){
            throw ParsingHTMLException(ex.localizedMessage)
        }
    }

    fun getSeparatePlayerStats(battleTag: String): List<OverwatchPlayerStats>{

        val allGameModesStats = getPlayerStats(battleTag)

        val validBattleTag = allGameModesStats.name + "#" + battleTag.split("#")[1]

        return listOf(
            allGameModesStats,
            getPlayerStats(validBattleTag, "QuickPlay"),
            getPlayerStats(validBattleTag, "Competitive")
        )
    }

    private fun checkBattleTag(battleTag: String) {
        val regex = Regex("\\w{1,}#\\d{3,5}")

        if(!(regex matches battleTag)){
            throw NotABattleTagException("$battleTag is not a valid battleTag")
        }
    }
}