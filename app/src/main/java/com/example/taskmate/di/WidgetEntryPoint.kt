package com.example.taskmate.di

import com.example.taskmate.database.TodoRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import android.content.Context

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun todoRepository(): TodoRepository
}

fun Context.widgetEntryPoint(): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
