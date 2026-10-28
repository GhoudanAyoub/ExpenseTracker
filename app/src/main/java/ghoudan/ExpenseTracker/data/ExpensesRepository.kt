package ghoudan.ExpenseTracker.data

import ghoudan.ExpenseTracker.model.ExpensesCategory
import ghoudan.ExpenseTracker.model.ExpensesModels
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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

class ExpensesRepository  {
    private var listOfExpenses = mutableListOf(
        ExpensesModels("1", "Groceries", 120.0, ExpensesCategory.Food),
        ExpensesModels("2", "Petrol", 150.0, ExpensesCategory.Transport),
        ExpensesModels("3", "Coffee", 25.0, ExpensesCategory.Food),
        ExpensesModels("4", "Internet", 300.0, ExpensesCategory.Bills)
    )


    //return the list of  the expenses as flow
    fun getExpenses(): Flow<List<ExpensesModels>> {
        return flow {
            emit(listOfExpenses)
        }
    }

    fun filterExpensesByCategory(category: ExpensesCategory): Flow<List<ExpensesModels>> {
        return flow {
            val filteredExpenses = listOfExpenses.filter { it.category == category }
            emit(filteredExpenses)
        }
    }

    fun addExpense(expense: ExpensesModels) {
        listOfExpenses.add(expense)
    }
}
