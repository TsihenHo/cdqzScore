package me.tsihen.cdqzScore

import android.app.Application
import com.tencent.mmkv.MMKV
import java.io.File

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        MMKV.initialize(this)

        // 释放 tesseract 资源文件
        val dir = File(
            getExternalFilesDir("tesseract")!!.also { if (!it.exists()) it.createNewFile() },
            "tessdata"
        )
        if (!dir.exists()) dir.mkdir()
        val output = File(dir, "eng.traineddata")
        if (!output.exists()) {
            output.createNewFile()
            assets.open("eng.traineddata").copyTo(output.outputStream())
        }
    }
}