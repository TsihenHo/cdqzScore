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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.tsihen.cdqzScore.data.QueryStepThreeViewModel
import me.tsihen.cdqzScore.ui.theme.DefaultTheme
import me.tsihen.cdqzScore.util.AccountManager

class QueryStepThreeActivity : ComponentActivity() {
    companion object {
        const val PARAM_ID = "@@Param_Id"
    }

    private val model: QueryStepThreeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(PARAM_ID, 0)
        model.load(id, this)

        setContent {
            DefaultTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TopBar()

                        when (model.uiState.state) {
                            QueryStepThreeViewModel.LoadState.Loading -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            QueryStepThreeViewModel.LoadState.Failed -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column() {
                                        Text(
                                            text = "加载失败，重进一下吧",
                                            style = MaterialTheme.typography.titleLarge
                                        )

                                        Text(
                                            text = "简明信息（详细信息看 Log）：\n${model.uiState.err}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            QueryStepThreeViewModel.LoadState.Success -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.Top,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 16.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "“${model.uiState.tableName}”的结果：${if (model.uiState.isFilterEnabled) "（存在筛选）" else ""}",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    
                                    item {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    item {
                                        Text(
                                            text = "总人数：${model.uiState.length}",
                                            color = MaterialTheme.colorScheme.secondary,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                    item {
                                        Divider(
                                            color = MaterialTheme.colorScheme.outline,
                                            thickness = 2.dp,
                                            modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 4.dp)
                                        )
                                    }

                                    val names = model.uiState.filteredNamesAndOrderFields.first
                                    if (names.isEmpty()) {
                                        item {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = "啥也没，看看筛选，或者是语法错误？",
                                                    style = MaterialTheme.typography.titleLarge
                                                )
                                            }
                                        }
                                    } else {
                                        val orderFields =
                                            model.uiState.filteredNamesAndOrderFields.second
                                        if (orderFields.isEmpty()) {
                                            names.forEach {
                                                item {
                                                    Row(modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            val intent = Intent(
                                                                this@QueryStepThreeActivity,
                                                                QueryStepFourActivity::class.java
                                                            )
                                                            intent.putExtra(
                                                                QueryStepFourActivity.PARAM_NAME,
                                                                it
                                                            )
                                                            intent.putExtra(
                                                                QueryStepFourActivity.PARAM_DATA,
                                                                model.uiState.db!!
                                                                    .getDataByName(it)
                                                                    .toTypedArray()
                                                            )
                                                            intent.putExtra(
                                                                QueryStepFourActivity.PARAM_COLUMNS,
                                                                model.uiState.db!!.columns.toTypedArray()
                                                            )
                                                            this@QueryStepThreeActivity.startActivity(
                                                                intent
                                                            )
                                                        }
                                                        .padding(8.dp)
                                                    ) {

                                                        Text(
                                                            text = it,
                                                            color = MaterialTheme.colorScheme.secondary,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            for (i: Int in names.indices) {
                                                item {
                                                    Row(modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            val intent = Intent(
                                                                this@QueryStepThreeActivity,
                                                                QueryStepFourActivity::class.java
                                                            )
                                                            intent.putExtra(
                                                                QueryStepFourActivity.PARAM_NAME,
                                                                names[i]
                                                            )
                                                            intent.putExtra(
                                                                QueryStepFourActivity.PARAM_DATA,
                                                                model.uiState.db!!
                                                                    .getDataByName(names[i])
                                                                    .toTypedArray()
                                                            )
                                                            // 因为使用 '_' 开头，所以删掉
                                                            intent.putExtra(
                                                                QueryStepFourActivity.PARAM_COLUMNS,
                                                                model.uiState.db!!.columns.toTypedArray()
                                                            )
                                                            this@QueryStepThreeActivity.startActivity(
                                                                intent
                                                            )
                                                        }
                                                        .padding(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "#${i + 1}\t\t",
                                                            color = MaterialTheme.colorScheme.secondary,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                        )
                                                        Text(
                                                            text = names[i],
                                                            color = MaterialTheme.colorScheme.secondary,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Box(
                                                            contentAlignment = Alignment.TopEnd,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                text = orderFields[i],
                                                                color = MaterialTheme.colorScheme.secondary,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                textAlign = TextAlign.End
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
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
    fun TopBar() {
        var showFilterDialog by rememberSaveable { mutableStateOf(false) }
        var showHelpDialog by rememberSaveable { mutableStateOf(false) }
        var showOrderDialog by rememberSaveable { mutableStateOf(false) }

        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = { Text("语法帮助") },
                text = {
                    Text(
                        text = "○满足每条规则的将被展示；每行一条规则；空格不可省略/多余\n" +
                                "○可用规则列表：\n" +
                                "◆“包含”  如 “姓名 包含 子恒”\n" +
                                "◆“>” “<” “>=” “<=” “=”  如 “语文 > 120” “数学 <= 英语” “班序 <= 10” “班级 = 5”\n" +
                                "◆若没有空格，则将在姓名中查找\n" +
                                "○可使用的参数：${
                                    model.uiState.db!!.columns.joinToString(
                                        "，"
                                    )
                                }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text(text = "好的")
                    }
                }
            )
        }

        if (showFilterDialog) {
            var rule by rememberSaveable { mutableStateOf(model.uiState.filterRule) }

            AlertDialog(
                onDismissRequest = {
                    showFilterDialog = false
                },
                title = @Composable { Text(text = "筛选 - 输入过滤规则") },
                text = @Composable {
                    TextField(
                        value = rule,
                        onValueChange = { rule = it },
                        placeholder = {
                            Text(text = "在这里输入规则")
                        },
                        modifier = Modifier.height(120.dp)
                    )
                },
                confirmButton = @Composable {
                    TextButton(
                        onClick = {
                            model.setFilterRule(rule)
                            showFilterDialog = false
                        }
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = @Composable {
                    TextButton(
                        onClick = {
                            showHelpDialog = true
                        }
                    ) {
                        Text("帮助")
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
                        RadioButtonsWithTexts(
                            texts = model.uiState.db!!.columns,
                            default = model.uiState.db!!.columns.indexOf(model.uiState.orderBy),
                            onSelectedChange = {
                                model.setOrderBy(it)
                            }
                        )

                        SwitchWithText(text = "逆序", model.uiState.reverseOrder) { z, _ ->
                            model.setReverseOrder(z)
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
                        text = "查看成绩",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            val intent = Intent(
                                this@QueryStepThreeActivity,
                                QueryStepFourActivity::class.java
                            )
                            intent.putExtra(
                                QueryStepFourActivity.PARAM_NAME,
                                AccountManager.name
                            )
                            intent.putExtra(
                                QueryStepFourActivity.PARAM_DATA,
                                model.uiState.db!!
                                    .getDataByName(AccountManager.name)
                                    .toTypedArray()
                            )
                            intent.putExtra(
                                QueryStepFourActivity.PARAM_COLUMNS,
                                model.uiState.db!!.columns.toTypedArray()
                            )
                            this@QueryStepThreeActivity.startActivity(
                                intent
                            )
                        },
                        enabled = model.uiState.state == QueryStepThreeViewModel.LoadState.Success
                                && model.uiState.hasName(AccountManager.name)
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = "me")
                    }

                    IconButton(
                        onClick = { showFilterDialog = true },
                        enabled = model.uiState.state == QueryStepThreeViewModel.LoadState.Success
                    ) {
                        Icon(Icons.Filled.FilterList, contentDescription = "filter")
                    }

                    IconButton(
                        onClick = { showOrderDialog = true },
                        enabled = model.uiState.state == QueryStepThreeViewModel.LoadState.Success
                    ) {
                        Icon(Icons.Filled.Sort, contentDescription = "sort")
                    }
                }
            }
        }
    }

    @Composable
    fun RadioButtonsWithTexts(
        texts: List<String>,
        default: Int,
        onSelectedChange: (String) -> Unit = { _ -> }
    ) {
        var selected by rememberSaveable { mutableStateOf(texts[default]) }

        texts.forEach {
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selected = it
                    onSelectedChange(it)
                }
            ) {
                RadioButton(
                    selected = selected == it,
                    onClick = {
                        selected = it
                        onSelectedChange(it)
                    },
                )

                Spacer(modifier = Modifier.width(2.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(text = it)
                }
            }
        }
    }
}