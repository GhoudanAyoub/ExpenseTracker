package ghoudan.ExpenseTracker.model

//id, title, amount, and category.
data class ExpensesModels(
    val id: String,
    val title: String,
    val amount: Double,
    val category: ExpensesCategory,
)

//All, Food, Transport, and Bills.
enum class ExpensesCategory {
    All, Food, Transport, Bills
}
