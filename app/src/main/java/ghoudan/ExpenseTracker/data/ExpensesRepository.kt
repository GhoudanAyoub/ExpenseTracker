package ghoudan.ExpenseTracker.data

//**
// Expense
//Amount
//Category
//Groceries
//AED 120
//Food
//Petrol
//AED 150
//Transport
//Coffee
//AED 25
//Food
//Internet
//AED 300
//Bills
// \***//

import ghoudan.ExpenseTracker.model.ExpensesCategory
import ghoudan.ExpenseTracker.model.ExpensesModels
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class ExpensesRepository @Inject constructor() {
    private val _expenses = MutableStateFlow(
        listOf(
            ExpensesModels("1", "Groceries", 120.0, ExpensesCategory.Food),
            ExpensesModels("2", "Petrol", 150.0, ExpensesCategory.Transport),
            ExpensesModels("3", "Coffee", 25.0, ExpensesCategory.Food),
            ExpensesModels("4", "Internet", 300.0, ExpensesCategory.Bills)
        )
    )

    fun getExpenses(): Flow<List<ExpensesModels>> = _expenses

    fun filterExpensesByCategory(category: ExpensesCategory): Flow<List<ExpensesModels>> =
        if (category == ExpensesCategory.All) _expenses
        else _expenses.map { list -> list.filter { it.category == category } }

    fun addExpense(expense: ExpensesModels) {
        _expenses.update { it + expense }
    }
}
