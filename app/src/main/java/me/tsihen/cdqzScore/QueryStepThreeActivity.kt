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
                                            text = "??????????????????????????????",
                                            style = MaterialTheme.typography.titleLarge
                                        )

                                        Text(
                                            text = "?????????????????????????????? Log??????\n${model.uiState.err}",
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
                                            text = "???${model.uiState.tableName}???????????????${if (model.uiState.isFilterEnabled) "??????????????????" else ""}",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    
                                    item {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    item {
                                        Text(
                                            text = "????????????${model.uiState.length}",
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
                                                    text = "???????????????????????????????????????????????????",
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
                                                            // ???????????? '_' ?????????????????????
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
                title = { Text("????????????") },
                text = {
                    Text(
                        text = "??????????????????????????????????????????????????????????????????????????????/??????\n" +
                                "????????????????????????\n" +
                                "???????????????  ??? ????????? ?????? ?????????\n" +
                                "??????>??? ???<??? ???>=??? ???<=??? ???=???  ??? ????????? > 120??? ????????? <= ????????? ????????? <= 10??? ????????? = 5???\n" +
                                "?????????????????????????????????????????????\n" +
                                "????????????????????????${
                                    model.uiState.db!!.columns.joinToString(
                                        "???"
                                    )
                                }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text(text = "??????")
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
                title = @Composable { Text(text = "?????? - ??????????????????") },
                text = @Composable {
                    TextField(
                        value = rule,
                        onValueChange = { rule = it },
                        placeholder = {
                            Text(text = "?????????????????????")
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
                        Text("??????")
                    }
                },
                dismissButton = @Composable {
                    TextButton(
                        onClick = {
                            showHelpDialog = true
                        }
                    ) {
                        Text("??????")
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
                title = @Composable { Text(text = "??????") },
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

                        SwitchWithText(text = "??????", model.uiState.reverseOrder) { z, _ ->
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
                        Text("??????")
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
                        text = "????????????",
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