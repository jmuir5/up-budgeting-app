package com.example.upbudgetapp

sealed class Paths(val Path:String) {
    object Buffer : Paths("Buffer")
    object Home : Paths("Home")
    object Login : Paths("Login")
}