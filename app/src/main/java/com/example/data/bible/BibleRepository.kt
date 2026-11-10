package com.example.data.bible

import android.content.Context
import com.example.data.model.BibleChapter
import com.example.data.model.BibleVerseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Loads chapters from the bundled Louis Segond 1910 assets.
 *
 * Each asset contains one book as
 * {"name":"...","chapters":[[[verseNumber,"text"], ...], ...]}.
 * Only a few books are kept in memory at a time.
 */
class BibleRepository(private val context: Context) {

  private val cache = object : LinkedHashMap<String, Map<Int, List<BibleVerseItem>>>(4, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Map<Int, List<BibleVerseItem>>>): Boolean {
      return size > CACHE_SIZE
    }
  }

  suspend fun getChapter(bookId: String, bookName: String, chapterNumber: Int): BibleChapter? =
    withContext(Dispatchers.IO) {
      val chapters = loadBook(bookId) ?: return@withContext null
      val verses = chapters[chapterNumber] ?: return@withContext null
      BibleChapter(bookId, bookName, chapterNumber, verses)
    }

  private fun loadBook(bookId: String): Map<Int, List<BibleVerseItem>>? {
    synchronized(cache) {
      cache[bookId]?.let { return it }
    }

    val json = runCatching {
      context.assets.open("bible/$bookId.json")
        .bufferedReader()
        .use { it.readText() }
    }.getOrNull() ?: return null

    val parsed = runCatching { parseBook(json) }.getOrNull() ?: return null
    synchronized(cache) {
      cache[bookId] = parsed
    }
    return parsed
  }

  private fun parseBook(json: String): Map<Int, List<BibleVerseItem>> {
    val root = JSONObject(json)
    val chaptersArray = root.getJSONArray("chapters")
    val result = LinkedHashMap<Int, List<BibleVerseItem>>(chaptersArray.length())
    for (chapterIndex in 0 until chaptersArray.length()) {
      val chapterArray = chaptersArray.getJSONArray(chapterIndex)
      val verses = ArrayList<BibleVerseItem>(chapterArray.length())
      for (verseIndex in 0 until chapterArray.length()) {
        val pair = chapterArray.getJSONArray(verseIndex)
        verses.add(BibleVerseItem(pair.getInt(0), pair.getString(1)))
      }
      result[chapterIndex + 1] = verses
    }
    return result
  }

  private companion object {
    const val CACHE_SIZE = 3
  }
}
