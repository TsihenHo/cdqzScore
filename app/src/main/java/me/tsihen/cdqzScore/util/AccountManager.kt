package me.tsihen.cdqzScore.util

import com.googlecode.tesseract.android.TessBaseAPI
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File

class AccountManager(
    private val scope: CoroutineScope,
    private val tmpDir: File? = null,
    private val tesseractDir: File
) {
    class NoSuchGradeException(msg: String) : Exception(msg)
    class LoginFailedException(msg: String) : Exception(msg)
    class ValidateCodeException(msg: String) : Exception(msg)

    companion object {
        const val SESSION_ID_COOKIE = "ASP.NET_SessionId"

        // 下面的常量用于提交登录请求
        const val VIEW_STATE = "__VIEWSTATE"
        const val VIEW_STATE_GENERATOR = "__VIEWSTATEGENERATOR"
        const val EVENT_VALIDATION = "__EVENTVALIDATION"
        const val EVENT_TARGET = "__EVENTTARGET"
        const val EVENT_ARGUMENT = "__EVENTARGUMENT"
        const val GRADE = "nj"
        const val NAME = "idcardTxt"
        const val PASSWORD = "idcardPwd"
        const val VALIDATE_CODE = "vali"

        val isLogin
            get() =  MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null).containsKey(SESSION_ID_COOKIE)

        val grade
            get() = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null).decodeString(GRADE)!!

        val password
            get() = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null).decodeString(PASSWORD)!!

        val name
            get() = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null).decodeString(NAME)!!

        init {
            if (!isLogin) {
                val mmkv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
                mmkv.encode(NAME, "")
                mmkv.encode(PASSWORD, "")
                mmkv.encode(GRADE, "")
            }
        }
    }

    fun login(): AsyncHelper<Unit> {
        val mmkv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
        return login(mmkv.decodeString(GRADE)!!, mmkv.decodeString(NAME)!!, mmkv.decodeString(PASSWORD)!!)
    }

    fun login(
        grade: String,
        name: String,
        passwd: String
    ): AsyncHelper<Unit> =
        AsyncHelper(scope) {
            // 我们已经进入了后台线程
            if (name.isEmpty()) {
                throw IllegalArgumentException("账号为空")
            }
            if (passwd.length < 8) {
                throw IllegalArgumentException("密码最短为 8 位")
            }

            val firstRes = Jsoup.connect(LOGIN_URL).execute()
            val doc = firstRes.parse()
            val grades = doc.getElementById("nj")!!
                .children()
                .map { child -> child.text() }

            if (!grades.contains(grade)) {
                throw NoSuchGradeException("年级 “$grade” 不存在，可选年级：${grades.joinToString("，")}")
            }

            // 首先，读取一些参数
            val viewState = doc.getElementById(VIEW_STATE)!!.attr("value")
            val viewStateGenerator =
                doc.getElementById(VIEW_STATE_GENERATOR)!!.attr("value")
            val eventValidation = doc.getElementById(EVENT_VALIDATION)!!.attr("value")

            // 然后，读取验证码
            sendMsg("识别验证码...")
            val secondRes = Jsoup.connect(VALIDATE_CODE_URL)
                // 格式为 jpeg，Jsoup 本来是不支持图片的，使用这一行来强制解析
                .ignoreContentType(true)
                .execute()
            val tmp = File.createTempFile("validateCode", ".jpg", tmpDir)
            val bytes = secondRes.bodyAsBytes()
            val tess = TessBaseAPI()
            val dataPath = tesseractDir.absolutePath
            if (!tess.init(dataPath, "eng")) {
                throw ValidateCodeException("验证码识别系统初始化失败")
            }

            tmp.writeBytes(bytes)
            tess.setImage(tmp)
            var validateCode: String? = null
            try {
                validateCode = tess.utF8Text
            } catch (e: Throwable) {
                e(e)
            }
            if (validateCode == null) {
                throw ValidateCodeException("验证码识别失败")
            }

            val sessionId = secondRes.cookie(SESSION_ID_COOKIE)!!
            tess.recycle()

            tmp.delete()

            // 最后，提交登录请求
            sendMsg("提交登录请求...")
            val thirdRes =
                Jsoup
                    .connect(LOGIN_URL)
                    .data(EVENT_TARGET, "")
                    .data(EVENT_ARGUMENT, "")
                    .data(VIEW_STATE, viewState)
                    .data(VIEW_STATE_GENERATOR, viewStateGenerator)
                    .data(EVENT_VALIDATION, eventValidation)
                    .data(GRADE, grade)
                    .data(NAME, name)
                    .data(PASSWORD, passwd)
                    .data(VALIDATE_CODE, validateCode)
                    .data("login", "登录系统")
                    .cookie(SESSION_ID_COOKIE, sessionId)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh-HK;q=0.9,zh-TW;q=0.8,zh;q=0.7,en;q=0.6,en-GB;q=0.5")
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", HOST_IP)
                    .header("Origin", BASE_URL)
                    .header("Pragma", "no-cache")
                    .header("Referer", LOGIN_URL)
                    .header("Upgrade-Insecure-Requests:", "1")
                    .method(Connection.Method.POST)
                    .execute()

            val doc2 = thirdRes.parse()
            doc2.getElementById("ErrorTxt")?.let {
                val err = it.text()
                throw LoginFailedException(err.ifEmpty { "未知错误" })
            }

            val mmkv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
            mmkv.encode(NAME, name)
            mmkv.encode(PASSWORD, passwd)
            mmkv.encode(GRADE, grade)
            mmkv.encode(SESSION_ID_COOKIE, sessionId)
        }
    // 千万不要 start，把 start 的工作交给外界
}