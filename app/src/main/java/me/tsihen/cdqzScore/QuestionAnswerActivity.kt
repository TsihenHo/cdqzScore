package me.tsihen.cdqzScore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.tsihen.cdqzScore.ui.theme.DefaultTheme

class QuestionAnswerActivity : ComponentActivity() {
    companion object {
        val questionAndAnswer = mapOf(
            "应用程序安全吗？" to "除了将您的密码明文保存在需要 Root 权限才能读取的文件夹下，其他方面和官网有相同的安全度。",
            "数据从哪来？" to "应用程序的数据均为来自官网的公开数据。",
            "来自官网的公开数据？我怎么没看到？" to "你需要钻洞，但我敢保证数据绝对是公开的。",
            "登录时怎么没叫识别验证码？" to "软件里藏了个人帮你识别了。",
            "能以 Excel 格式保存查询的结果吗？" to "当前版本不可以。牙膏要一点一点挤，PPT 要一张一张放，显卡要一刀一刀切，代码要一行一行写，本功能会在下个版本出现。",
            "数字 -114514X 是怎么回事？" to "你能看见它就说明软件出 Bug 了；请你自动转义：-1145141->缺考，-1145142->作弊，-1145143->（空），-1145144->—。",
            "软件怎么这么大？" to "帮你识别验证码的人比较大。",
            "你这应用有些表格显示有问题啊？" to "已知问题。我的竞争对手只有一个，只要做得比它好看就行了（其实是这个问题修复不来",
            "日志在哪？" to "暂时还没写入到文件，如有需要，自己打开 Logcat ，搜索 Tag “成都七中成绩查询”。",
            "开源吗？" to "遵循 GPLv3.0 在 https://github.com/TsihenHo/cdqzScore 开放源代码",
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DefaultTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TopBar()

                        LazyColumn(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            questionAndAnswer.forEach {
                                item {
                                    ButtonCard(title = "问：${it.key}", desc = "答：${it.value}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun TopBar() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { finish() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "return")
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "常见问题解答",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}