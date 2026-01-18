# Expenses Tracker

Expenses Tracker is an Android application developed using **Kotlin** and **Jetpack Compose**.  
The app helps users record monthly income and expenses, analyze spending patterns, and view graphical financial reports. Data is stored using **SQLite database** with a modern and user-friendly interface.

## 📌 Project Overview

- Create monthly expense sheets  
- Add / Edit / Delete individual expenses  
- Track income vs expenses  
- View financial trends using charts  
- Fully offline with SQLite storage

## 🧱 Design Philosophy

The application is built on:

- Modular architecture  
- Data protection  
- Ease of use  
- Reactive UI with Jetpack Compose  
- Low boilerplate and maintainable code

## 🛠 Tech Stack

- Kotlin  
- Jetpack Compose  
- SQLite  
- Android SDK  
- MVVM style structure

## 📂 Main Components

### MainActivity.kt
- Initializes database  
- Defines data models  
- Manages app lifecycle  
- Connects UI with data layer

### SharedComposables.kt
- Reusable UI components  
- Navigation structure  
- Expense sheet screens  
- Dialogs for CRUD operations

### MonthActivity.kt
- Detailed monthly view  
- Income & expense management  
- Real-time data updates

### ChartActivity.kt
- Graphical visualization  
- Swipe navigation  
- Income vs expense comparison

### DBHelper.kt
- SQLite operations  
- Insert / Update / Delete  
- Referential integrity  
- Data retrieval

## 🚀 Features

- Monthly expense sheets  
- Expense CRUD  
- Income management  
- Balance calculation  
- Line chart visualization  
- Duplicate sheet prevention  
- Offline support

## ▶ How to Run

1. Clone repository  
2. Open in Android Studio  
3. Sync Gradle  
4. Run on emulator/device

## 📷 Screenshots

_Add your screenshots here later_

## 📄 Documentation

This README is based on project documentation describing architecture, activities, and database design :contentReference[oaicite:1]{index=1}.

