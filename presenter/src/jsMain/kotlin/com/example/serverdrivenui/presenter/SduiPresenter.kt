package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import com.example.serverdrivenui.schema.compose.MyButton
import com.example.serverdrivenui.schema.compose.MyText

@Composable
fun SduiPresenter() {
    var count by remember { mutableStateOf(0) }

    com.example.serverdrivenui.schema.compose.MyColumn {
        MyText(text = "Count: $count")
        MyButton(
            text = "Increment",
            onClick = { count++ }
        )
    }
}
