package me.tsihen.cdqzScore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.tencent.mmkv.MMKV
import me.tsihen.cdqzScore.data.MainViewModel
import me.tsihen.cdqzScore.ui.theme.DefaultTheme
import me.tsihen.cdqzScore.util.AccountManager
import me.tsihen.cdqzScore.util.i

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_LOGIN_EXCEPTION_MSG = "@@Extra_LoginExceptionMsg"
        const val EXTRA_LOGIN_EXCEPTION_TYPE = "@@Extra_LoginExceptionType"
    }

    private var actionWhenLogin: () -> Unit = {}

    private val startLoginActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            actionWhenLogin()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: MainViewModel by viewModels()

        setContent {
            val mmkv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
            var titleText by rememberSaveable { mutableStateOf("????????????") }

            val mgr: AccountManager
            actionWhenLogin = {
                titleText = if (AccountManager.isLogin) {
                    "???????????????${mmkv.decodeString(AccountManager.NAME)}"
                } else {
                    "????????????"
                }
            }

            if (AccountManager.isLogin) {
                mgr = AccountManager(
                    model.viewModelScope,
                    getExternalFilesDir("validateCode"),
                    getExternalFilesDir("tesseract")!!
                )

                titleText = "?????????..."

                mgr.login()
                    .success {
                        titleText = "???????????????${mmkv.decodeString(AccountManager.NAME)}"
                    }
                    .failed {
                        if (it is AccountManager.LoginFailedException && it.message!!.contains("?????????")) {
                            // ???????????????????????????
                            start()
                        } else {
                            i(it)
                            mmkv.remove(AccountManager.SESSION_ID_COOKIE)
                            titleText = "????????????"
                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            intent.putExtra(
                                EXTRA_LOGIN_EXCEPTION_MSG,
                                if (it.message.isNullOrEmpty()) "????????????" else it.message
                            )
                            intent.putExtra(EXTRA_LOGIN_EXCEPTION_TYPE, it.javaClass.name)
                            startLoginActivity.launch(intent)
                        }
                    }
                    .message {
                        titleText = it
                    }
                    .start()
            }

            DefaultTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Column(
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val scrollState = rememberScrollState()

                        DisableSelection {
                            Text(
                                text = titleText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(all = 16.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        if (!AccountManager.isLogin) {
                                            val intent =
                                                Intent(this@MainActivity, LoginActivity::class.java)
                                            startLoginActivity.launch(intent)
                                        }
                                    }
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier
                                .verticalScroll(scrollState)
                                .fillMaxWidth()
                                .padding(all = 16.dp)
                        ) {
                            ButtonCardClickToActivity(
                                title = "??????????????????", desc = "???????????????????????????????????????",
                                needLoginWhenClick = true,
                                activity = QueryStepOneActivity::class.java
                            )
                            ButtonCardClickShowMsg(
                                title = "????????????",
                                desc = "?????????????????????",
                                needLoginWhenClick = false,
                                "????????????",
                                "????????????"
                            )
                            ButtonCardClickShowMsg(
                                title = "????????????",
                                desc = "???????????????????????????????????????",
                                true,
                                "????????????",
                                "????????????"
                            )
                            ButtonCardClickToActivity(
                                title = "Q & A",
                                desc = "??????????????????",
                                needLoginWhenClick = false,
                                QuestionAnswerActivity::class.java
                            )
                            ButtonCardClickShowMsg(
                                title = "?????????????????????",
                                desc = "Open Source License",
                                needLoginWhenClick = false,
                                "?????????????????????",
                                "???Jsoup\n" +
                                        "The jsoup code-base (including source and compiled packages) are distributed under the open source MIT license as described below.\n" +
                                        "\n" +
                                        "The MIT License\n" +
                                        "Copyright ?? 2009 - 2022 Jonathan Hedley (https://jsoup.org/)\n" +
                                        "\n" +
                                        "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n" +
                                        "\n" +
                                        "The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n" +
                                        "\n" +
                                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n" +
                                        "\n" +
                                        "???tesseract4android\n" +
                                        "Copyright 2019 Adaptech s.r.o., Robert P??sel\n" +
                                        "\n" +
                                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                                        "you may not use this file except in compliance with the License.\n" +
                                        "You may obtain a copy of the License at\n" +
                                        "\n" +
                                        "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                                        "\n" +
                                        "Unless required by applicable law or agreed to in writing, software\n" +
                                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                                        "See the License for the specific language governing permissions and\n" +
                                        "limitations under the License.\n\n" +
                                        "???MMKV\n" +
                                        "Tencent is pleased to support the open source community by making MMKV available.  \n" +
                                        "Copyright (C) 2018 THL A29 Limited, a Tencent company.  All rights reserved.\n" +
                                        "If you have downloaded a copy of the MMKV binary from Tencent, please note that the MMKV binary is licensed under the BSD 3-Clause License.\n" +
                                        "If you have downloaded a copy of the MMKV source code from Tencent, please note that MMKV source code is licensed under the BSD 3-Clause License, except for the third-party components listed below which are subject to different license terms.  Your integration of MMKV into your own projects may require compliance with the BSD 3-Clause License, as well as the other licenses applicable to the third-party components included within MMKV.\n",
                                MaterialTheme.typography.bodySmall
                            )
                            ButtonCardClickShowMsg(
                                title = "??????",
                                desc = "??????????????????????????? 22 ???????????????",
                                needLoginWhenClick = false,
                                "??????",
                                        "????????????????????????????????????????????????????????????\n???????????????????????????????????????????????????\n" +
                                        "???????????????\n?????????ziheng@gmx.cn\nQQ???3318448676\n?????????????????????????????? 80h ????????????????????????????????????"
                            )
                            ButtonCard(title = "????????????", desc = "????????????????????????????????????????????????") {
                                mmkv.remove(AccountManager.SESSION_ID_COOKIE)
                                mmkv.remove(AccountManager.GRADE)
                                mmkv.remove(AccountManager.NAME)
                                mmkv.remove(AccountManager.PASSWORD)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonCardClickToActivity(
    title: String,
    desc: String,
    needLoginWhenClick: Boolean = true,
    activity: Class<*>,
    vararg data: Pair<String, Any>
) {
    val ctx = LocalContext.current
    ButtonCard(title = title, desc = desc, needLoginWhenClick = needLoginWhenClick) {
        val intent = Intent(ctx, activity)
        data.forEach {
            val key = it.first
            when (val value = it.second) {
                is Int -> {
                    intent.putExtra(key, value)
                }
                is Float -> {
                    intent.putExtra(key, value)
                }
                is Byte -> {
                    intent.putExtra(key, value)
                }
                is Double -> {
                    intent.putExtra(key, value)
                }
                is Long -> {
                    intent.putExtra(key, value)
                }
                is Char -> {
                    intent.putExtra(key, value)
                }
                is Short -> {
                    intent.putExtra(key, value)
                }
                is Boolean -> {
                    intent.putExtra(key, value)
                }
                is IntArray -> {
                    intent.putExtra(key, value)
                }
                is FloatArray -> {
                    intent.putExtra(key, value)
                }
                is ByteArray -> {
                    intent.putExtra(key, value)
                }
                is DoubleArray -> {
                    intent.putExtra(key, value)
                }
                is LongArray -> {
                    intent.putExtra(key, value)
                }
                is CharArray -> {
                    intent.putExtra(key, value)
                }
                is ShortArray -> {
                    intent.putExtra(key, value)
                }
                is BooleanArray -> {
                    intent.putExtra(key, value)
                }
                is String -> {
                    intent.putExtra(key, value)
                }
                is CharSequence -> {
                    intent.putExtra(key, value)
                }

                else -> {
                    throw IllegalArgumentException("???????????????${value.javaClass.name}")
                }
            }
        }
        ctx.startActivity(intent)
    }
}

@Composable
fun ButtonCardClickShowMsg(
    title: String,
    desc: String,
    needLoginWhenClick: Boolean,
    msgTitle: String,
    msg: String,
    style: TextStyle = LocalTextStyle.current
) {
    var dialog by rememberSaveable { mutableStateOf(false) }
    val scroll = rememberScrollState(0)

    if (dialog) {
        AlertDialog(
            onDismissRequest = { dialog = false },
            confirmButton = {
                TextButton(onClick = { dialog = false }) {
                    Text(text = "??????")
                }
            },
            title = {
                LazyColumn() {
                    item {
                        Text(msgTitle)
                    }
                }
            },
            text = { Text(msg, style = style, modifier = Modifier.verticalScroll(scroll)) }
        )
    }

    ButtonCard(title = title, desc = desc, needLoginWhenClick = needLoginWhenClick) {
        dialog = true
    }
}

@Composable
fun ButtonCard(
    title: String,
    desc: String,
    needLoginWhenClick: Boolean = true,
    onClick: () -> Unit = {}
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = @Composable { Text(text = "??????") },
            text = @Composable { Text(text = "?????????????????????????????????????????????????????????") },
            confirmButton = @Composable {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("??????")
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .padding(0.dp, 0.dp, 0.dp, 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                if ((needLoginWhenClick && AccountManager.isLogin) || !needLoginWhenClick) {
                    onClick()
                } else {
                    showDialog = true
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 10.dp, 8.dp, 10.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = desc,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}