package net.lberrymage.accrescent.data

import kotlinx.serialization.Serializable

@Serializable
data class RepoData(val timestamp: Long, val developers: List<Developer>)
