package com.example.data.ai.gemini

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
  data class Success(
    val rawText: String,
    val modelUsed: String
  ) : GeminiResult()

  data class Error(
    val errorMessage: String,
    val isQuotaExceeded: Boolean = false,
    val isNetworkError: Boolean = false,
    val isMissingKey: Boolean = false
  ) : GeminiResult()
}

class GeminiApiClient(
  private val context: Context,
  private val defaultModel: String = "gemini-2.5-flash",
  private val apiKeyOverride: String? = null
) {

  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  private val systemPrompt = """
Tu es Eli, un assistant pédagogique chrétien d'orientation protestante et évangélique pour l'application « Ma Vie avec Dieu ».
Ta langue de communication est exclusivement le français.
Ton ton est chaleureux, respectueux, clair, bibliquement rigoureux, pastoral et encourageant.

SOCLE DOCTRINAL & RÈGLES FORMELLES :
1. Tu t'appuies sur le socle évangélique et protestant :
   - Autorité souveraine des Saintes Écritures (Sola Scriptura).
   - Centralité exclusive de Jésus-Christ (Solus Christus).
   - Salut par grâce seule au moyen de la foi seule (Sola Gratia, Sola Fide).
   - Nécessité de la nouvelle naissance et régénération par le Saint-Esprit.
   - Vie de prière fervente, sanctification et discipulat actif.
2. LIMITES ABSOLUES :
   - Tu ne prétends JAMAIS être Dieu, Jésus-Christ ou le Saint-Esprit.
   - Tu ne formules JAMAIS de prophétie certaine ou de révélation divine personnelle.
   - Tu reconnais toujours avec humilité que tu es une intelligence artificielle créée par l'homme.
   - Tu n'inventes JAMAIS de versets bibliques imaginaires.
3. DIVERGENCES PROTESTANTES :
   - Lorsque des interprétations protestantes diffèrent (ex: baptême des croyants vs pédobaptisme, positions réformées vs arminiennes, cessationisme vs continuationisme), présente fidèlement et charitablement chaque point de vue sans animosité.
4. STRUCTURE ATTENDUE DANS TA RÉPONSE :
   - EXPLICATION : Réponse claire, pédagogique et théologique à la question.
   - CITATIONS BIBLIQUES : Mentionne les références précises (Livre Chapitre:Verset) et leur contexte.
   - PERSPECTIVES : S'il y a lieu, nuances théologiques protestantes.
   - PRIÈRE GUIDÉE : Une courte prière d'édification pour appliquer la Parole.
"""

  fun isNetworkAvailable(): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  suspend fun askGemini(
    userPrompt: String,
    conversationHistory: List<Pair<String, Boolean>> = emptyList(),
    configuredModel: String = defaultModel,
    apiKeyOverride: String? = null
  ): GeminiResult = withContext(Dispatchers.IO) {
    // 1. Check network connectivity
    if (!isNetworkAvailable()) {
      return@withContext GeminiResult.Error(
        errorMessage = "Pas de connexion Internet. Eli a basculé sur le moteur théologique hors-ligne pour continuer à vous accompagner.",
        isNetworkError = true
      )
    }

    // 2. Check API key presence
    val apiKey = apiKeyOverride ?: this.apiKeyOverride ?: BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext GeminiResult.Error(
        errorMessage = "Clé Gemini API non configurée dans le panneau Secrets. Le moteur biblique local prend le relais.",
        isMissingKey = true
      )
    }

    try {
      val url = "https://generativelanguage.googleapis.com/v1beta/models/$configuredModel:generateContent"

      val contentsArray = JSONArray()

      // Add relevant history turns (last 4 to maintain context without token overload)
      conversationHistory.takeLast(4).forEach { (msg, isUser) ->
        val turnObj = JSONObject().apply {
          put("role", if (isUser) "user" else "model")
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", msg) })
          })
        }
        contentsArray.put(turnObj)
      }

      // Add current user prompt
      contentsArray.put(JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().apply {
          put(JSONObject().apply { put("text", userPrompt) })
        })
      })

      val requestBodyJson = JSONObject().apply {
        put("contents", contentsArray)
        put("systemInstruction", JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", systemPrompt) })
          })
        })
        put("generationConfig", JSONObject().apply {
          put("temperature", 0.7)
          put("maxOutputTokens", 1200)
        })
      }

      val request = Request.Builder()
        .url(url)
        .addHeader("x-goog-api-key", apiKey)
        .post(requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val responseCode = response.code
      val responseBodyString = response.body?.string() ?: ""

      when (responseCode) {
        200 -> {
          val json = JSONObject(responseBodyString)
          val candidates = json.optJSONArray("candidates")
          val firstCandidate = candidates?.optJSONObject(0)
          val content = firstCandidate?.optJSONObject("content")
          val parts = content?.optJSONArray("parts")
          val text = parts?.optJSONObject(0)?.optString("text")

          if (!text.isNullOrBlank()) {
            GeminiResult.Success(rawText = text, modelUsed = configuredModel)
          } else {
            GeminiResult.Error("Réponse vide reçue de l'assistant.")
          }
        }
        429 -> {
          GeminiResult.Error(
            errorMessage = "Le quota de requêtes Gemini a été dépassé. Eli a basculé sur les réponses bibliques vérifiées en local.",
            isQuotaExceeded = true
          )
        }
        403, 401 -> {
          GeminiResult.Error(
            errorMessage = "La clé d'authentification Gemini est invalide ou non autorisée.",
            isMissingKey = true
          )
        }
        else -> {
          Log.e("GeminiApiClient", "API error: $responseCode - $responseBodyString")
          GeminiResult.Error("Erreur serveur ($responseCode). Utilisation de la sagesse biblique locale.")
        }
      }
    } catch (e: IOException) {
      Log.e("GeminiApiClient", "Network / timeout exception", e)
      GeminiResult.Error(
        errorMessage = "Délai d'attente dépassé ou instabilité réseau. Eli active le mode local sécurisé.",
        isNetworkError = true
      )
    } catch (e: Exception) {
      Log.e("GeminiApiClient", "Unexpected error", e)
      GeminiResult.Error("Erreur inattendue : ${e.localizedMessage}")
    }
  }
}
