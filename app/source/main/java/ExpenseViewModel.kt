@Composable
fun BudgetProgressCard(spent: Double, budget: Double) {
    val progress = (spent / budget).toFloat().coerceIn(0f, 1f)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Spent: $$spent", fontWeight = FontWeight.Medium)
                Text("Budget: $$budget", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Updated to modern Material 3 lambda state tracking
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (progress > 0.9f) Color.Red else DarkText,
                trackColor = Color.LightGray
            )
        }
    }
}

@Composable
fun ExpenseRow(expense: Expense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(expense.description, color = DarkText, fontSize = 16.sp)
        Text("$${expense.amount}", fontWeight = FontWeight.Bold, color = DarkText)
    }
    // Updated from 'Divider' to 'HorizontalDivider'
    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
}
