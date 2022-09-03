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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.tsihen.cdqzScore.data.QueryStepTwoViewModel
import me.tsihen.cdqzScore.ui.theme.DefaultTheme

class QueryStepTwoActivity : ComponentActivity() {
    companion object {
        const val PARAM_TEST_TITLE = "@@Param_TestTitle"
    }

    private val model: QueryStepTwoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val testTitle = intent.getStringExtra(PARAM_TEST_TITLE)!!
        model.load(testTitle)

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
                            QueryStepTwoViewModel.LoadState.Loading -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            QueryStepTwoViewModel.LoadState.Failed -> {
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

                            QueryStepTwoViewModel.LoadState.Success -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.Top,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 16.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "↓ $testTitle",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    
                                    item {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    
                                    model.uiState.examinations.forEach {
                                        item {
                                            ExaminationItem(
                                                id = it.id,
                                                title = it.title,
                                                upgradeTime = it.upgradeTime
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

    @Composable
    fun ExaminationItem(
        id: Int,
        title: String,
        upgradeTime: String
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 12.dp)
                .clickable {
                    val intent = Intent(this@QueryStepTwoActivity, QueryStepThreeActivity::class.java)
                    intent.putExtra(QueryStepThreeActivity.PARAM_ID, id)
                    this@QueryStepTwoActivity.startActivity(intent)
                }
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = upgradeTime,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall
            )
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
                        text = "细分考试",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}