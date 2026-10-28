package ghoudan.ExpenseTracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ghoudan.ExpenseTracker.data.ExpensesRepository
import ghoudan.ExpenseTracker.model.ExpensesCategory
import ghoudan.ExpenseTracker.model.ExpensesModels
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ExpensesUiState {
    data object Loading : ExpensesUiState()
    data class Success(val expenses: List<ExpensesModels>) : ExpensesUiState()
    data object Empty : ExpensesUiState()
}

class ExpensesViewModel(
    private val expensesRepository: ExpensesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpensesUiState>(ExpensesUiState.Loading)
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    init {
        startCollectingData()
    }

    fun startCollectingData() {
        collect(expensesRepository.getExpenses())
    }

    fun filterExpensesByCategory(category: ExpensesCategory) {
        collect(expensesRepository.filterExpensesByCategory(category))
    }

    fun addExpense(expense: ExpensesModels) {
        viewModelScope.launch {
            expensesRepository.addExpense(expense)
        }
    }

    private fun collect(source: Flow<List<ExpensesModels>>) {
        collectJob?.cancel()
        _uiState.value = ExpensesUiState.Loading
        collectJob = viewModelScope.launch {
            source.collect { expenses ->
                _uiState.value =
                    if (expenses.isEmpty()) ExpensesUiState.Empty
                    else ExpensesUiState.Success(expenses)
            }
        }
    }
}
