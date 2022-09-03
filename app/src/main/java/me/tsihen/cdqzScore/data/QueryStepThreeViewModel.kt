package me.tsihen.cdqzScore.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tencent.mmkv.MMKV
import me.tsihen.cdqzScore.data.QueryStepThreeViewModel.DbHelper.Companion.getSafetyColumn
import me.tsihen.cdqzScore.data.QueryStepThreeViewModel.DbHelper.Companion.re
import me.tsihen.cdqzScore.util.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import kotlin.NumberFormatException

class QueryStepThreeViewModel : ViewModel() {
    enum class LoadState {
        Success,
        Failed,
        Loading
    }

    class DbHelper(
        ctx: Context,
        private val tableName: String,
        val columns: List<String>,
        private val types: List<DbColumnType>
    ) :
        SQLiteOpenHelper(ctx, tableName, null, 1) {
        enum class DbColumnType {
            Text,
            Int,
            Double
        }

        companion object {
            val re = Regex("""[()',\s.\-+&*$^#@%=?\[\]{}":;<>/\\]""")

            fun getSafetyColumn(source: String): String {
                return "_" + re.replace(source, "")
            }
        }

        private val safeColumns = columns.map { getSafetyColumn(it) }

        override fun onCreate(db: SQLiteDatabase) {
            val sb = StringBuilder()
            sb.append("CREATE TABLE ")
            sb.append("'$tableName' (")

            var hasPrimaryKey = false
            for (i: Int in safeColumns.indices) {
                val column = safeColumns[i]
                val type = when (types[i]) {
                    DbColumnType.Text -> "TEXT"
                    DbColumnType.Int -> "INTEGER"
                    DbColumnType.Double -> "REAL"
                }

                sb.append("$column $type ")
                if (!hasPrimaryKey && column == "_姓名") {
                    sb.append("PRIMARY KEY, ")
                    hasPrimaryKey = true
                } else {
                    sb.append(',')
                }
            }
            // 多了一个逗号
            sb.deleteCharAt(sb.lastIndex)
            sb.append(")")

            db.execSQL("DROP TABLE IF EXISTS '$tableName'")
            db.execSQL(sb.toString())
        }

        fun addLine(values: List<String>) {
            val db = writableDatabase

            val newValue = ContentValues().apply {
                for (i: Int in safeColumns.indices) {
                    val column = safeColumns[i]
                    getPutMethodByType(types[i])(column, values[i])
                }
            }

            db?.insert(tableName, null, newValue)
        }

        fun getDataByName(
            name: String
        ): List<String> {
            val cursor = execQuery(
                null,
                whereClause = "_姓名 = ?",
                whereArgs = arrayOf(name),
                null
            )

            val list = mutableListOf<String>()
            var i = 0
            cursor.moveToNext()
            while (i < types.size) {
                val res = getSourceString(cursor, i)
                list.add(res)
                ++i
            }

            cursor.close()
            return list
        }

        internal fun execQuery(
            columnNames: Array<String>?,
            whereClause: String?,
            whereArgs: Array<String>?,
            order: String?
        ): Cursor {
            val db = readableDatabase

            return db.query(
                tableName,
                columnNames,
                whereClause,
                whereArgs,
                null,
                null,
                order
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // 不会被更新
            TODO()
        }
    }

    companion object {
        private const val ABSENT = -114514_1
        private const val CHEAT = -114514_2
        private const val EMPTY = -114514_3
        private const val LINE = -114514_4

        private fun String.isDouble(): Boolean {
            return try {
                java.lang.Double.parseDouble(this)
                true
            } catch (_: NumberFormatException) {
                false
            }
        }

        private fun String.isInt(): Boolean {
            return try {
                Integer.parseInt(this)
                true
            } catch (_: NumberFormatException) {
                false
            }
        }

        private fun getPutMethodByType(type: DbHelper.DbColumnType): ContentValues.(String, String) -> Unit {
            return when (type) {
                DbHelper.DbColumnType.Int -> { k, v -> put(k, getTranslatedString(v).toInt()) }
                DbHelper.DbColumnType.Double -> { k, v ->
                    put(
                        k,
                        getTranslatedString(v).toDouble()
                    )
                }
                DbHelper.DbColumnType.Text -> { k, v -> put(k, getTranslatedString(v)) }
            }
        }

        private fun getTranslatedString(v: String): String {
            return when (v) {
                "缺考", "缺" -> ABSENT.toString()
                "作弊" -> CHEAT.toString()
                "—", "——", "-", "--", "---", "/", "\\" -> LINE.toString()
                "" -> EMPTY.toString()
                else -> v
            }
        }

        private fun getSourceString(cursor: Cursor, column: Int): String {
            val v = when (cursor.getType(column)) {
                Cursor.FIELD_TYPE_INTEGER -> cursor.getInt(column)
                Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(column)
                Cursor.FIELD_TYPE_STRING -> cursor.getString(column)
                else -> {
                    throw IllegalArgumentException("不支持的类型")
                }
            }

            return when (v) {
                is Int -> {
                    when (v) {
                        ABSENT -> "缺考"
                        CHEAT -> "作弊"
                        EMPTY -> "（空）"
                        LINE -> "－"
                        else -> v.toString()
                    }
                }
                is Double -> {
                    when (v) {
                        ABSENT.toDouble() -> "缺考"
                        CHEAT.toDouble() -> "作弊"
                        EMPTY.toDouble() -> "（空）"
                        LINE.toDouble() -> "－"
                        else -> v.toString()
                    }
                }
                is String -> {
                    when (v) {
                        ABSENT.toString() -> "缺考"
                        CHEAT.toString() -> "作弊"
                        EMPTY.toString() -> "（空）"
                        LINE.toString() -> "－"
                        else -> v.toString()
                    }
                }
                else -> {
                    throw IllegalArgumentException("无效参数")
                }
            }
        }

        private fun translateRuleToSqlClause(rule: String, columns: List<String>): String {
            val rules = rule.split('\n')
            val sb = StringBuilder()
            // ====================================================
            // 我知道存在 SQL 注入问题，但是这不重要
            // ===================================================
            rules.filter { it.isNotBlank() }.forEach {
                if (it.count { c -> c == ' ' } != 2) {
                    sb.append("_姓名 LIKE '%$it%' ")
                } else {
                    val words = it.split(' ')
                    val verb = words[1]
                        .replace("＜", "<")
                        .replace("＞", ">")
                        .replace("≥", ">=")
                        .replace("≤", "<=")
                        .replace("＝", "=")
                    val prefix = getSafetyColumn(words[0])
                    val suffix =
                        if (verb == "包含") words[2]
                        else if (words[2].isDouble()) words[2]
                        else if (columns.contains(words[2])) getSafetyColumn(words[2])
                        else "'${words[2]}'"

                    val clause = when (verb) {
                        "包含" -> "$prefix LIKE '%$suffix%' "
                        ">" -> "$prefix > $suffix "
                        "<" -> "$prefix < $suffix "
                        ">=" -> "$prefix >= $suffix "
                        "<=" -> "$prefix <= $suffix "
                        "=" -> "$prefix = $suffix "
                        else -> "$prefix > $suffix "
                    }

                    sb.append(clause)
                }
                sb.append("AND ")
            }
            // 懒一点，不用删除最后的 “AND ”
            sb.append("1 = 1 ")

            return sb.toString()
        }
    }

    data class MyUiState(
        private val names: List<String> = listOf(),
        val db: DbHelper? = null,
        val tableName: String = "",
        val state: LoadState = LoadState.Loading,
        val err: Throwable? = null,
        val reverseOrder: Boolean = false,
        val orderBy: String = "姓名",
        val filterRule: String = ""
    ) {
        fun hasName(name: String): Boolean = names.contains(name)

        val length: Int
            get() { return names.size }
        val isFilterEnabled: Boolean
            get() { return filterRule.isNotBlank() }
        val filteredNamesAndOrderFields: Pair<List<String>, List<String>>
            get() {
                try {
                    val order = getSafetyColumn(orderBy)
                    val fieldsToQuery: Array<String> = if ("_姓名" != order) {
                        arrayOf("_姓名", order)
                    } else {
                        arrayOf("_姓名")
                    }

                    val cursor = db!!.execQuery(
                        fieldsToQuery,
                        translateRuleToSqlClause(filterRule, db.columns),
                        null,
                        order + if (reverseOrder) " DESC" else ""
                    )

                    val names = mutableListOf<String>()
                    val orderFields = mutableListOf<String>()
                    while (cursor.moveToNext()) {
                        names.add(cursor.getString(0))
                        if ("_姓名" != order) {
                            orderFields.add(getSourceString(cursor, 1))
                        }
                    }

                    cursor.close()
                    return Pair(names, orderFields)
                } catch (e: Exception) {
                    i(e)
                    return Pair(listOf(), listOf())
                }
            }
    }

    var uiState by mutableStateOf(MyUiState())
        private set

    fun setFilterRule(rule: String) {
        uiState = uiState.copy(filterRule = rule)
    }

    fun setReverseOrder(z: Boolean) {
        uiState = uiState.copy(reverseOrder = z)
    }

    fun setOrderBy(column: String) {
        uiState = uiState.copy(orderBy = column)
    }

    fun load(id: Int, ctx: Context) {
        AsyncHelper(viewModelScope) {
            val finalUrl = VIEW_EXAMINATION + id
            val mmkv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
            val cookie = mmkv.decodeString(AccountManager.SESSION_ID_COOKIE)!!

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
            val table = doc.getElementsByTag("tbody")[1]
            val title = doc.getElementsByTag("strong")[0].text()
            val children = table.children()
            // 首先，读取表头
            val tableHeader = mutableListOf<String>()
            children[0].children().forEach {
                // TMD 居然会有重复的表头，脑壳有包吧？？！
                var str = it.text()
                while (tableHeader.contains(str)) {
                    str = "${str}_重复"
                }

                tableHeader.add(str)
            }
            // 然后，读取每列的数据类型
            val content = children.subList(1, children.size)
            val columnNum = tableHeader.size
            val columnTypes = mutableListOf<DbHelper.DbColumnType>()
            for (i: Int in 0 until columnNum) {
                var guess: DbHelper.DbColumnType? = null
                content
                    .map { it.child(i) }
                    .forEach {
                        if (it.text().contains("—")) {
                            v("FAKE")
                        }
                        val str = getTranslatedString(it.text())

                        if (guess != DbHelper.DbColumnType.Text) {
                            if (guess != DbHelper.DbColumnType.Double && str.isInt()) {
                                guess = DbHelper.DbColumnType.Int
                            } else if (str.isDouble()) {
                                guess = DbHelper.DbColumnType.Double
                            } else {
                                guess = DbHelper.DbColumnType.Text
                                return@forEach
                            }
                        }
                    }

                columnTypes.add(guess ?: DbHelper.DbColumnType.Text)
            }
            // 创建数据库
            val helper = DbHelper(
                ctx, "_${
                    re.replace(title, "")
                }", tableHeader, columnTypes
            )
            // 写入数据库
            content.forEach {
                val values = mutableListOf<String>()
                it.children().forEach { item ->
                    val text = item.text()
                    values.add(text)
                }

                if (!values.all { t -> t.isBlank() }) {
                    helper.addLine(values)
                }

                // 这是空行，滚蛋
            }

            // 读取姓名
            val cursor = helper.execQuery(arrayOf("_姓名"), null, null, null)
            val names = mutableListOf<String>()
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0))
            }

            cursor.close()
            uiState = uiState.copy(
                names = names,
                err = null,
                state = LoadState.Success,
                tableName = title,
                db = helper
            )
        }
            .failed {
                e(it)
                uiState = uiState.copy(names = listOf(), err = it, state = LoadState.Failed)
            }
            .start()
    }
}