# SpendwiseApp - Initial Architecture & CI/CD Documentation

This document serves as a complete reference guide for the initial draft of **SpendwiseApp**. It outlines the project directory structure, configuration files, core source code architecture, and the automated CI/CD pipeline built via GitHub Actions.

---

## 1. Project Directory Structure

The repository is structured following standard Android modern development practices, utilizing a single-module (`app`) layout with the Kotlin DSL (`.gradle.kts`) for build configuration.

```text
SpendwiseApp/
│
├── .github/
│   └── workflows/
│       └── build.yml             # GitHub Actions CI pipeline configuration
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/spendwiseapp/
│   │       │   ├── AppDatabase.kt          # Room Database definition
│   │       │   ├── DatabaseModule.kt       # Hilt Dependency Injection module
│   │       │   ├── Expense.kt              # Room Entity (Data model)
│   │       │   ├── ExpenseDao.kt           # Room Data Access Object (DAO)
│   │       │   ├── ExpenseViewModel.kt     # Jetpack Architecture ViewModel
│   │       │   ├── MainActivity.kt         # App Entry Point Activity
│   │       │   ├── SpendwiseApplication.kt # Application Class (Hilt setup)
│   │       │   └── TrackerApp.kt           # Jetpack Compose UI layout
│   │       │
│   │       └── AndroidManifest.xml         # Android App Manifest (Permissions & Components)
│   │
│   └── build.gradle.kts          # App-level build configurations & dependencies
│
├── gradle.properties             # Global Gradle configuration (AndroidX, Jetifier)
├── settings.gradle.kts           # Project-level module definitions
└── README.md                     # Main repository documentation


## 2. Core Build & Project Configuration
gradle.properties
Configures the build environment to support the modern AndroidX ecosystem and automatically transform legacy libraries.

Properties
android.useAndroidX=true
android.enableJetifier=true
app/build.gradle.kts
Defines the compiler settings, targets Android SDK 34, enables Jetpack Compose, and declares dependencies for Hilt (Dependency Injection) and Room (Local Storage).

Kotlin
plugins {
    id("com.android.application") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.0"
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version "2.48.1"
}

android {
    namespace = "com.example.spendwiseapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.spendwiseapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version") 

    // Hilt Dependency Injection
    val hilt_version = "2.48.1"
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-android-compiler:$hilt_version")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}

## 3. GitHub Actions CI/CD Pipeline
.github/workflows/build.yml
Automates code integration and compiling. Whenever code is pushed to the main branch, a clean cloud runner starts up, handles environmental dependencies, builds the application, and exports a downloadable debug APK.

YAML
name: Android CI Build

on:
  push:
    branches: [ "main" ]
  workflow_dispatch: 

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout Code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    # Pin to Gradle 8.6 to match project compiler plugin configurations
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3
      with:
        gradle-version: '8.6'

    - name: Compile and Build Debug APK
      run: gradle assembleDebug

    - name: Upload APK Artifact
      uses: actions/upload-artifact@v4
      with:
        name: SpendwiseApp-Debug-APK
        path: app/build/outputs/apk/debug/app-debug.apk
4. Application Source Architecture
Manifest File
app/src/main/AndroidManifest.xml
The application manifest registers the entry point and bridges the custom Application subclass (SpendwiseApplication) to initialize Hilt at startup.

Data Layer (Room Local Storage)
Expense.kt (Entity)
Kotlin
package com.example.spendwiseapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
ExpenseDao.kt (Data Access Object)
Kotlin
package com.example.spendwiseapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalSpent(): Flow<Double?>
}
AppDatabase.kt
Kotlin
package com.example.spendwiseapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
Dependency Injection Layer (Hilt)
SpendwiseApplication.kt
Kotlin
package com.example.spendwiseapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SpendwiseApplication : Application()
DatabaseModule.kt
Kotlin
package com.example.spendwiseapp

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "spendwise_database"
        ).build()
    }

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao {
        return database.expenseDao()
    }
}
Business Logic & UI Layer (Jetpack Compose & ViewModel)
ExpenseViewModel.kt
Manages state conversion from cold data flows to UI-ready async StateFlow streams and encapsulates user interaction logic.

Kotlin
package com.example.spendwiseapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val dao: ExpenseDao
) : ViewModel() {
    
    val monthlyBudget = 5000.0 

    val expenses: StateFlow<List<Expense>> = dao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpent: StateFlow<Double> = dao.getTotalSpent()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addExpense(amount: Double, description: String) {
        viewModelScope.launch {
            dao.insertExpense(Expense(amount = amount, description = description))
        }
    }
}
MainActivity.kt
Kotlin
package com.example.spendwiseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: ExpenseViewModel = hiltViewModel()
            TrackerApp(viewModel = viewModel)
        }
    }
}
TrackerApp.kt
Declares the visual interface layer using standard components, basic inputs, list view handling, and color foundations.

Kotlin
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
    // ... UI implementation ...
}

*(Note: I abbreviated the final `TrackerApp.kt` and `AndroidManifest.xml` files slightly
