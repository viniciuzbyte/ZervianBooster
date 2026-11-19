package com.zervian.booster.data.model

data class AppEntry(
    val packageName: String,
    val label: String,
    val isLikelyGame: Boolean,
)

/** Game yang sudah ditambahkan user ke daftar boost Zervian + profil terakhir dipakai. */
data class BoostedGame(
    val packageName: String,
    val label: String,
    val lastProfile: String = "STANDARD",
)
