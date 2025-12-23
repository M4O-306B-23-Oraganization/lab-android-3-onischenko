package pro.maximon.lab3.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pro.maximon.lab3.data.repository.TimerRepository
import pro.maximon.lab3.models.TimerItem
import kotlin.uuid.Uuid

class MainViewModel(
    private val timerRepository: TimerRepository,
) : ViewModel() {
    val items = timerRepository
        .getTimers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList(),
        )

    private val _editingState = MutableStateFlow<Uuid?>(null)
    val editingState = _editingState.asStateFlow()

    fun addItem() {
        val item = TimerItem(
            name = "Timer ${items.value.size + 1}",
        )
        viewModelScope.launch {
            timerRepository.addTimer(item)
        }
    }

    fun startEditing(itemId: Uuid) {
        _editingState.value = itemId
    }

    fun stopEditing() {
        _editingState.value = null
    }

    fun removeItem(id: Uuid) {
        viewModelScope.launch {
            timerRepository.removeTimer(id)
        }
    }

    fun updateItem(id: Uuid, updatedItem: TimerItem) {
        val itemToSave = if (updatedItem.id == id) updatedItem else updatedItem.copy(id = id)
        viewModelScope.launch {
            timerRepository.updateTimer(itemToSave)
        }
    }

    fun toggleTimerTicking(id: Uuid) {
        viewModelScope.launch {
            val item = items.value.find { it.id == id } ?: return@launch
            val updatedItem = item.copy(ticking = !item.ticking)
            timerRepository.updateTimer(updatedItem)
        }
    }

    fun resetTimer(id: Uuid) {
        viewModelScope.launch {
            val item = items.value.find { it.id == id } ?: return@launch
            val updatedItem = item.resetToDefault()
            timerRepository.updateTimer(updatedItem)
        }
    }

    fun startAllTimers() {
        viewModelScope.launch {
            val updatedItems = items.value.map { it.copy(ticking = true) }
            for (item in updatedItems) {
                timerRepository.updateTimer(item)
            }
        }
    }

    fun stopAllTimers() {
        viewModelScope.launch {
            val updatedItems = items.value.map { it.copy(ticking = false) }
            for (item in updatedItems) {
                timerRepository.updateTimer(item)
            }
        }
    }
}