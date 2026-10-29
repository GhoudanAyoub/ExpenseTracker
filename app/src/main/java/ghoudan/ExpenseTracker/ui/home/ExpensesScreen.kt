package ghoudan.ExpenseTracker.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ghoudan.ExpenseTracker.model.ExpensesCategory
import ghoudan.ExpenseTracker.model.ExpensesModels
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesModelsScreen(
    modifier: Modifier = Modifier,
    expensesViewModel: ExpensesViewModel = hiltViewModel(),
) {
    val uiState by expensesViewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val expenses by remember { derivedStateOf { (uiState as? ExpensesUiState.Success)?.expenses.orEmpty() } }
    val total by remember { derivedStateOf { expenses.sumOf { it.amount } } }

    // Stable lambdas — ViewModel reference never changes
    val onFilterAll = remember { { expensesViewModel.filterExpensesByCategory(ExpensesCategory.All) } }
    val onFilterFood = remember { { expensesViewModel.filterExpensesByCategory(ExpensesCategory.Food) } }
    val onFilterTransport = remember { { expensesViewModel.filterExpensesByCategory(ExpensesCategory.Transport) } }
    val onFilterBills = remember { { expensesViewModel.filterExpensesByCategory(ExpensesCategory.Bills) } }
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Expenses") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Expenses")
            }
        }
    ) { padding ->
        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No Expenses yet. Tap + to add one.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    LazyRow {
                        item { FilterButton(ExpensesCategory.All, onFilter = onFilterAll) }
                        item { FilterButton(ExpensesCategory.Food, onFilter = onFilterFood) }
                        item { FilterButton(ExpensesCategory.Transport, onFilter = onFilterTransport) }
                        item { FilterButton(ExpensesCategory.Bills, onFilter = onFilterBills) }
                    }
                }
                item {
                    TotalCard(total = total)
                }
                items(expenses, key = { it.id }) { expense ->
                    ExpensesModelsItem(expense)
                }
            }
        }
    }

    if (showDialog) {
        AddExpensesModelsDialog(
            onDismiss = { showDialog = false },
            onConfirm = { ExpensesModels ->
                expensesViewModel.addExpense(ExpensesModels)
                showDialog = false
            }
        )
    }
}

@Composable
fun FilterButton(expensesCategory: ExpensesCategory,onFilter: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        TextButton(
            onClick = { onFilter() },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("Filter ${expensesCategory.name}")
        }
    }
}

@Composable
private fun TotalCard(total: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total", style = MaterialTheme.typography.titleMedium)
            Text(formatAmount(total), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ExpensesModelsItem(ExpensesModels: ExpensesModels) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ExpensesModels.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    ExpensesModels.category.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(formatAmount(ExpensesModels.amount), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpensesModelsDialog(
    onDismiss: () -> Unit,
    onConfirm: (ExpensesModels) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(ExpensesCategory.All) }
    // Only show errors after the user tries to save, so the form doesn't open "red".
    var submitted by rememberSaveable { mutableStateOf(false) }

    val amount = amountText.replace(',', '.').toBigDecimalOrNull()
    val titleError = if (title.isBlank()) "Title can't be empty" else null
    val amountError = when {
        amountText.isBlank() -> "Amount is required"
        amount == null -> "Enter a valid number"
        amount <= BigDecimal.ZERO -> "Amount must be greater than 0"
        else -> null
    }
    val isValid = titleError == null && amountError == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New ExpensesModels") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    isError = submitted && titleError != null,
                    supportingText = {
                        if (submitted && titleError != null) Text(titleError)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        // Allow only digits and one decimal separator
                        if (input.isEmpty() || input.matches(Regex("""^\d*[.,]?\d{0,2}$"""))) {
                            amountText = input
                        }
                    },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = submitted && amountError != null,
                    supportingText = {
                        if (submitted && amountError != null) Text(amountError)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpensesCategory.entries.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                submitted = true
                if (isValid && amount != null) {
                    onConfirm(
                        ExpensesModels(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            amount = amount.toDouble(),
                            category = category
                        )
                    )
                }
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


private fun formatAmount(amount: Double): String =
    NumberFormat.getCurrencyInstance().format(amount)
