package ghoudan.ExpenseTracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ghoudan.ExpenseTracker.data.ExpensesRepository
import ghoudan.ExpenseTracker.ui.home.ExpensesModelsScreen
import ghoudan.ExpenseTracker.ui.home.ExpensesViewModel
import ghoudan.ExpenseTracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ExpenseTrackerTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            ExpensesModelsScreen(ExpensesViewModelFactory.create())
                        }
                    }
                }
            }
        }
    }
}

object ExpensesViewModelFactory {
    fun create(): ExpensesViewModel {
        return ExpensesViewModel(ExpensesRepository())
    }
}
