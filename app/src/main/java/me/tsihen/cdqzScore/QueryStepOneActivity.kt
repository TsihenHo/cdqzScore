package me.tsihen.cdqzScore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.tsihen.cdqzScore.data.QueryStepOneViewModel
import me.tsihen.cdqzScore.ui.theme.DefaultTheme
import me.tsihen.cdqzScore.util.d
import me.tsihen.cdqzScore.util.e

class QueryStepOneActivity : ComponentActivity() {
    private val model: QueryStepOneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DefaultTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    var loadResult: Boolean? by rememberSaveable { mutableStateOf(null) }

                    if (loadResult == null) {
                        model.loadTestEntries()
                            .success {
                                d("加载成功")
                                loadResult = true
                            }
                            .failed {
                                e(it)
                                loadResult = false
                            }
                            .start()
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        TopBar()
                        when (loadResult) {
                            true -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.Top,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 16.dp)
                                ) {
                                    if (model.uiState.entriesToDisplay.isEmpty()) {
                                        item {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = "啥也没，看看筛选？",
                                                    style = MaterialTheme.typography.titleLarge
                                                )
                                            }
                                        }
                                    } else {
                                        model.uiState.entriesToDisplay.forEach {
                                            item {
                                                Column(
                                                    verticalArrangement = Arrangement.Top,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(all = 12.dp)
                                                        .clickable {
                                                            val intent = Intent(this@QueryStepOneActivity, QueryStepTwoActivity::class.java)
                                                            intent.putExtra(QueryStepTwoActivity.PARAM_TEST_TITLE, it.item)
                                                            this@QueryStepOneActivity.startActivity(intent)
                                                        }
                                                ) {
                                                    Text(
                                                        text = it.item,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        style = MaterialTheme.typography.titleSmall
                                                    )

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    Text(
                                                        text = it.grade,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                            false -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = "加载失败，请重试",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                            null -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    CircularProgressIndicator()
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
        var showFilterDialog by rememberSaveable { mutableStateOf(false) }
        var showOrderDialog by rememberSaveable { mutableStateOf(false) }

        if (showFilterDialog) {
            val scrollState = rememberScrollState()
            AlertDialog(
                onDismissRequest = {
                    showFilterDialog = false
                },
                title = @Composable { Text(text = "筛选") },
                text = @Composable {
                    Column(
                        modifier = Modifier.verticalScroll(scrollState)
                    ) {
                        model.getGrades().forEach {
                            CheckBoxWithText(
                                text = it,
                                model.getFilter().contains(it)
                            ) { checked, str ->
                                if (checked) {
                                    model.addToFilter(str)
                                } else {
                                    model.removeFromFilter(str)
                                }
                            }
                        }
                    }
                },
                confirmButton = @Composable {
                    TextButton(
                        onClick = {
                            showFilterDialog = false
                        }
                    ) {
                        Text("确认")
                    }
                }
            )
        }

        if (showOrderDialog) {
            val scrollState = rememberScrollState()
            AlertDialog(
                onDismissRequest = {
                    showOrderDialog = false
                },
                title = @Composable { Text(text = "排序") },
                text = @Composable {
                    Column(
                        modifier = Modifier.verticalScroll(scrollState)
                    ) {
                        Text("* 当前界面只支持默认排序", style = MaterialTheme.typography.titleSmall)

                        SwitchWithText(text = "逆序", model.reverseOrder) { z, _ ->
                            model.reverseOrder = z
                        }
                    }
                },
                confirmButton = @Composable {
                    TextButton(
                        onClick = {
                            showOrderDialog = false
                        }
                    ) {
                        Text("确认")
                    }
                }
            )
        }

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
                        text = "选择考试",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "filter")
                    }

                    IconButton(onClick = { showOrderDialog = true }) {
                        Icon(Icons.Filled.Sort, contentDescription = "sort")
                    }
                }
            }
        }
    }
}

@Composable
fun SwitchWithText(
    text: String,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean, String) -> Unit = { _, _ -> }
) {
    val checkedState = remember { mutableStateOf(isChecked) }
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            checkedState.value = !checkedState.value
            onCheckedChange(checkedState.value, text)
        }
    ) {
        Switch(
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it
                onCheckedChange(it, text)
            },
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(text = text)
        }
    }
}

@Composable
fun CheckBoxWithText(
    text: String,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean, String) -> Unit = { _, _ -> }
) {
    val checkedState = remember { mutableStateOf(isChecked) }
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            checkedState.value = !checkedState.value
            onCheckedChange(checkedState.value, text)
        }
    ) {
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it
                onCheckedChange(it, text)
            },
        )

        Spacer(modifier = Modifier.width(2.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(text = text)
        }
    }
}