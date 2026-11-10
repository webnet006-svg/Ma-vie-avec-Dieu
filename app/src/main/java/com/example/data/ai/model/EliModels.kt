package com.example.data.ai.model

import com.example.data.ai.verifier.VerifiedCitationResult

data class EliMessage(
  val id: String,
  val isUser: Boolean,
  val content: String,
  val timestamp: String,
  val verifiedCitations: List<VerifiedCitationResult> = emptyList(),
  val theologicalPerspectives: String? = null,
  val suggestedPrayer: String? = null,
  val suggestedQuestions: List<String> = emptyList(),
  val isError: Boolean = false,
  val isReported: Boolean = false
)

data class EliQuotaState(
  val dailyLimit: Int = 20,
  val usedToday: Int = 0,
  val resetHour: String = "00:00"
) {
  val remaining: Int
    get() = (dailyLimit - usedToday).coerceAtLeast(0)

  val isExceeded: Boolean
    get() = remaining <= 0
}
