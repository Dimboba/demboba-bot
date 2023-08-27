data class OverwatchPlayerStats(
    val gameMode: String = "All",
    val name: String,
    val tankStats: WinLossStats,
    val dpsStats: WinLossStats,
    val supStats: WinLossStats
) {
    fun toBeautifulString(): String {
        return """    Tanks:     ${tankStats.toBeautifulString()}
            Dps:       ${dpsStats.toBeautifulString()}
            Supports:  ${supStats.toBeautifulString()}"""
    }


    fun toBeautifulStringFull(): String {
        return """
            Name:      $name
            Game mode: $gameMode
            Tanks:     ${tankStats.toBeautifulString()}
            Dps:       ${dpsStats.toBeautifulString()}
            Supports:  ${supStats.toBeautifulString()}
        """.trimIndent()
    }
}

data class WinLossStats(
    val wins: Int?,
    val losses: Int?
) {
    fun toBeautifulString(): String {
        return """
            $wins W - $losses L
        """.trimIndent()
    }
}

