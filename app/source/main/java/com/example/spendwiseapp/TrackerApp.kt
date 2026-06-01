package com.example.spendwiseapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val CreamBackground = Color(0xFFFAF9F6)
val DarkText = Color(0xFF1C1C1E)

@Composable
fun TrackerApp(viewModel: ExpenseViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val budget = viewModel.monthlyBudget
    
    var amountInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(24.dp)
    ) {
        Text("Spendwise Overview", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(modifier = Modifier.height(16.dp))
        
        BudgetProgressCard(totalSpent, budget)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = descInput,
            onValueChange = { descInput = it },
            label = { Text("What did you buy?") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                val amount = amountInput.toDoubleOrNull()
                if (amount != null && descInput.isNotBlank()) {
                    viewModel.addExpense(amount, descInput)
                    amountInput = ""
                    descInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DarkText)
        ) {
            Text("Add Expense", color = CreamBackground)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Recent Spends", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(expenses) { expense ->
                ExpenseRow(expense)
            }
        }
    }
}

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
    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
}
