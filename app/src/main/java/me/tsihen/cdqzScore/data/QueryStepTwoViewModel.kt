package me.tsihen.cdqzScore.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tencent.mmkv.MMKV
import me.tsihen.cdqzScore.util.*
import org.jsoup.Connection
import org.jsoup.Jsoup

class QueryStepTwoViewModel : ViewModel() {
    enum class LoadState {
        Success,
        Failed,
        Loading
    }

    data class Examination(
        val id: Int,
        val title: String,
        val upgradeTime: String
    )

    data class MyUiState(
        val examinations: List<Examination> = listOf(),
        val state: LoadState = LoadState.Loading,
        val err: Throwable? = null
    )

    var uiState by mutableStateOf(MyUiState())
        private set

    fun load(name: String) {
        AsyncHelper(viewModelScope) {
            val finalUrl = VIEW_TEST_ENTRY + name
            val mmkv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
            val cookie = mmkv.decodeString(AccountManager.SESSION_ID_COOKIE)!!
            val entryList = ArrayList<Examination>()

            val res = Jsoup
                .connect(finalUrl)
                .cookie(AccountManager.SESSION_ID_COOKIE, cookie)
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
                    val id = Integer.parseInt(it.child(0).text())
                    val title = it.child(1).text()
                    val upgradeTime = it.child(4).text()
                    val examination = Examination(id, title, upgradeTime)
                    entryList.add(examination)
                }

            uiState = uiState.copy(examinations = entryList, state = LoadState.Success, err = null)
        }
            .failed {
                e(it)
                uiState = uiState.copy(examinations = listOf(), state = LoadState.Failed, err = it)
            }
            .start()
    }
}