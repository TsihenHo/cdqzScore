package me.tsihen.cdqzScore.data

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tencent.mmkv.MMKV
import me.tsihen.cdqzScore.util.AccountManager
import me.tsihen.cdqzScore.util.AsyncHelper
import me.tsihen.cdqzScore.util.HOST_IP
import me.tsihen.cdqzScore.util.QUERY_URL
import org.jsoup.Connection
import org.jsoup.Jsoup

class QueryStepOneViewModel : ViewModel() {
    data class TestEntry(
        val grade: String,
        val item: String,
    )

    data class MyUiState(
        val entriesToDisplay: List<TestEntry> = emptyList()
    )

    var uiState by mutableStateOf(MyUiState())
        private set

    var reverseOrder: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                uiState = uiState.copy(entriesToDisplay = uiState.entriesToDisplay.asReversed())
            }
        }

    private lateinit var testEntries: List<TestEntry>

    private val filter = mutableSetOf(AccountManager.grade)

    fun addToFilter(str: String) {
        filter.add(str)
        uiState = uiState.copy(entriesToDisplay = testEntries.filter { filter.contains(it.grade) })
    }

    fun removeFromFilter(str: String) {
        filter.remove(str)
        uiState = uiState.copy(entriesToDisplay = testEntries.filter { filter.contains(it.grade) })
    }

    fun getFilter(): Set<String> {
        return filter.toSet()
    }

    fun loadTestEntries(): AsyncHelper<Unit> {
        return AsyncHelper(viewModelScope) {
            val mmkv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
            val id = mmkv.decodeString(AccountManager.SESSION_ID_COOKIE)!!
            val entryList = ArrayList<TestEntry>()

            val res = Jsoup
                .connect(QUERY_URL)
                .cookie(AccountManager.SESSION_ID_COOKIE, id)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
                .header(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
                )
                .header("Accept-Encoding", "gzip, deflate")
                .header(
                    "Accept-Language",
                    "zh-CN,zh-HK;q=0.9,zh-TW;q=0.8,zh;q=0.7,en;q=0.6,en-GB;q=0.5"
                )
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Host", HOST_IP)
                .header("Pragma", "no-cache")
                .header("Upgrade-Insecure-Requests:", "1")
                .method(Connection.Method.GET)
                .execute()

            val doc = res.parse()
            val table = doc.getElementById("lbDatalist")!!.child(0)
            val children = table.children()
            children.subList(1, children.size)
                .forEach {
                    val grade = it.child(0).text()
                    val item = it.child(1).text()
                    val entry = TestEntry(grade, item)
                    entryList.add(entry)
                }

            testEntries = entryList.toList()
            uiState =
                uiState.copy(entriesToDisplay = testEntries.filter { filter.contains(it.grade) })
        }
    }

    fun getGrades(): Set<String> {
        return testEntries
            .map { it.grade }
            .toSet()
    }
}