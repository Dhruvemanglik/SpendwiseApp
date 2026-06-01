import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Room will automatically implement this function for you
    abstract fun expenseDao(): ExpenseDao
}
