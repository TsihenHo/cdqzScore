package me.tsihen.cdqzScore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.tsihen.cdqzScore.ui.theme.DefaultTheme

class QueryStepFourActivity : ComponentActivity() {
    companion object {
        const val PARAM_NAME = "@@Param_Name"
        const val PARAM_COLUMNS = "@@Param_Columns"
        const val PARAM_DATA = "@@Param_Data"
    }

    val name by lazy { intent.getStringExtra(PARAM_NAME) }
    val data: Array<String> by lazy { intent.getStringArrayExtra(PARAM_DATA)!! }
    private val columns: Array<String> by lazy { intent.getStringArrayExtra(PARAM_COLUMNS)!! }

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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .padding(1.dp)
                        ) {
                            for (i: Int in data.indices) {
                                item {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                    ) {
                                        TableCell(text = columns[i], weight = .4f)
                                        TableCell(text = data[i], weight = .6f)
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
                        text = name!!,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }

    @Composable
    fun RowScope.TableCell(
        text: String,
        weight: Float
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .weight(weight)
                .padding(8.dp)
        )
    }
}