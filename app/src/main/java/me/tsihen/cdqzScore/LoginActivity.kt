package me.tsihen.cdqzScore

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.tsihen.cdqzScore.data.LoginViewModel
import me.tsihen.cdqzScore.ui.theme.DefaultTheme
import me.tsihen.cdqzScore.util.AccountManager
import me.tsihen.cdqzScore.util.d
import me.tsihen.cdqzScore.util.i

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: LoginViewModel by viewModels()
        var grade = AccountManager.grade
        var account = AccountManager.name
        var passwd = AccountManager.password
        var isIntentProcessed = false

        d("进入登录界面")
        setContent {
            DefaultTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showLoginFailedDialog by rememberSaveable { mutableStateOf(false) }
                    var loginFailedReason by rememberSaveable { mutableStateOf("") }

                    if (!isIntentProcessed && intent.hasExtra(MainActivity.EXTRA_LOGIN_EXCEPTION_TYPE)) {
                        loginFailedReason =
                            "信息：${intent.getStringExtra(MainActivity.EXTRA_LOGIN_EXCEPTION_MSG)}" +
                                    "\n类型：${intent.getStringExtra(MainActivity.EXTRA_LOGIN_EXCEPTION_TYPE)}"

                        showLoginFailedDialog = true
                        isIntentProcessed = true
                    }

                    if (showLoginFailedDialog) {
                        AlertDialog(
                            onDismissRequest = { showLoginFailedDialog = false },
                            title = @Composable { Text(text = "登录失败") },
                            text = @Composable { Text(text = loginFailedReason) },
                            confirmButton = @Composable {
                                TextButton(
                                    onClick = { showLoginFailedDialog = false }
                                ) {
                                    Text("好的")
                                }
                            }
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        val activity = LocalContext.current as Activity

                        DisableSelection {
                            Text(
                                "登录成都七中教务平台",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                fontSize = 30.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp, 0.dp, 0.dp, 16.dp)
                            )
                        }

                        InputText(
                            "年级",
                            "如“2022级”“2021级”“教务处”",
                            validate = { true /*AccountManager.validGrades.contains(it)*/ }
                        ) { grade = it }
                        InputText("账号", "通常是姓名，用于登录") { account = it }
                        InputText("密码", "明文显示，反正也不重要") { passwd = it }
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                        ) {
                            var enableLoginButton by rememberSaveable { mutableStateOf(true) }
                            var strLoginButton by rememberSaveable { mutableStateOf("登录") }

                            Button(
                                enabled = enableLoginButton,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(0.dp, 0.dp, 8.dp, 0.dp)
                                    .fillMaxWidth(0.5f),
                                onClick = {
                                    d("点击按钮")
                                    enableLoginButton = false
                                    strLoginButton = "登录中"
                                    val mgr = AccountManager(
                                        model.viewModelScope,
                                        activity.getExternalFilesDir("validateCode"),
                                        activity.getExternalFilesDir("tesseract")!!
                                    )
                                    mgr.login(grade, account, passwd)
                                        .success {
                                            strLoginButton = "成功，即将返回"
                                            model.viewModelScope.launch {
                                                delay(3000)
                                                activity.finish()
                                            }
                                        }
                                        .failed {
                                            i(it)
                                            strLoginButton = "登录"
                                            enableLoginButton = true
                                            loginFailedReason =
                                                "信息：${it.message ?: "未知错误"}" +
                                                        "\n类型：${it.javaClass.name}"
                                            showLoginFailedDialog = true
                                        }
                                        .message {
                                            strLoginButton = it
                                        }
                                        .start()
                                }
                            ) {
                                Text(text = strLoginButton)
                            }

                            var strExitButton by rememberSaveable { mutableStateOf("退出程序") }

                            Button(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(8.dp, 0.dp, 0.dp, 0.dp)
                                    .fillMaxWidth(),
                                onClick = {
                                    d("退出程序")
                                    strExitButton = "我还没做 :("
                                    // todo
                                }
                            ) {
                                Text(text = strExitButton)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputText(
    title: String,
    hint: String = "",
    validate: (String) -> Boolean = { true },
    onValueChange: (String) -> Unit = {}
) {
    var text by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        label = { Text(text = title) },
        placeholder = @Composable { Text(text = hint) },
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        onValueChange = {
            text = it
            isError = false
            onValueChange(it)
        },
        isError = isError,
        keyboardActions = KeyboardActions { validate(text) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp) // margin
    )
}