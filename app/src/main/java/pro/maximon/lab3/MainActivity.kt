package pro.maximon.lab3

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.koin.androidx.compose.koinViewModel
import pro.maximon.lab3.models.TimerItem
import pro.maximon.lab3.service.TimerService
import pro.maximon.lab3.theme.AndroidAppTheme
import pro.maximon.lab3.viewmodels.MainViewModel
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val serviceIntent = Intent(this, TimerService::class.java)
        startForegroundService(serviceIntent)

        setContent {
            AndroidAppTheme {
                AppView(
                    onExit = {
                        stopService(serviceIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun AppView(
    viewModel: MainViewModel = koinViewModel(),
    onExit: () -> Unit = {},
) {
    val items by viewModel.items.collectAsState()
    val editing by viewModel.editingState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.addItem()
            }) {
                Icon(Icons.Default.Add, "add item")
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = items.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = { viewModel.startAllTimers() }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start all timers")
                            Text("Start All")
                        }
                        Button(onClick = { viewModel.stopAllTimers() }) {
                            Icon(painterResource(R.drawable.ic_pause), contentDescription = "Pause all timers")
                            Text("Pause All")
                        }
                        Button(
                            onClick = onExit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit app")
                            Text("Exit")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                TimerItemView(
                    item,
                    onRemoveRequest = { viewModel.removeItem(item.id) },
                    onToggleTicking = { viewModel.toggleTimerTicking(item.id) },
                    onReset = { viewModel.resetTimer(item.id) },
                    onClick = { viewModel.startEditing(item.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }
        }
    }

    val editingItem = remember(items, editing) {
        items.find { it.id == editing }
    }
    if (editingItem != null) {
        EditItemDialog(
            editingItem,
            onDismissRequest = { viewModel.stopEditing() },
            onUpdate = { updatedItem ->
                viewModel.updateItem(updatedItem.id, updatedItem)
                viewModel.stopEditing()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    item: TimerItem,
    onDismissRequest: () -> Unit,
    onUpdate: (TimerItem) -> Unit,
) {
    var newName by rememberSaveable(item) { mutableStateOf(item.name) }
    val timePickerState = rememberTimePickerState(
        initialHour = item.timerDuration().toComponents { hours, minutes, seconds, nanoseconds ->
            hours
        }.toInt(),
        initialMinute = item.timerDuration().toComponents { hours, minutes, seconds, nanoseconds ->
            minutes
        },
        is24Hour = true,
    )

    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(min = 280.dp, max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Edit Item",
                    style = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
                // Text(text = "Name:")
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Timer:")
                TimeInput(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = {
                        val updatedItem = item.copy(
                            name = newName,
                            timer = (timePickerState.hour.hours + timePickerState.minute.minutes).inWholeSeconds,
                            defaultValue = (timePickerState.hour.hours + timePickerState.minute.minutes).inWholeSeconds,
                            ticking = false,
                        )
                        onUpdate(updatedItem)
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TimerItemView(
    item: TimerItem,
    onClick: () -> Unit,
    onRemoveRequest: () -> Unit,
    onToggleTicking: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                colors = IconButtonDefaults.filledIconButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.onError,
                    containerColor = MaterialTheme.colorScheme.error,
                ),
                onClick = onRemoveRequest
            ) {
                Icon(Icons.Default.Delete, "remove item")
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = item.name,
                    style = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                )

                val timerText = remember(item) {
                    item.timerDuration().toComponents { hours, minutes, seconds, nanoseconds ->
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    }
                }
                Text(
                    text = timerText,
                    style = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.alpha(0.8f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onToggleTicking) {
                    if (item.ticking) Icon(
                        painterResource(R.drawable.ic_pause),
                        "pause timer"
                    ) else Icon(
                        Icons.Default.PlayArrow,
                        "start timer"
                    )
                }
                IconButton(onClick = onReset) {
                    Icon(
                        Icons.Default.Refresh,
                        "reset timer"
                    )
                }
            }
        }
    }
}