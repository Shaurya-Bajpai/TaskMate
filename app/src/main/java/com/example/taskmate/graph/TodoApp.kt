package com.example.taskmate.graph

import android.app.Application

class TodoApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}