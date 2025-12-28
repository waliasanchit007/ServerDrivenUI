package com.example.serverdrivenui.shared

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import app.cash.redwood.Modifier
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.AppLifecycle
import app.cash.redwood.widget.Widget
import app.cash.redwood.protocol.RedwoodVersion
import app.cash.zipline.Zipline
import com.example.serverdrivenui.schema.widget.MyButton
import com.example.serverdrivenui.schema.widget.MyText
import com.example.serverdrivenui.schema.widget.MyColumn
import com.example.serverdrivenui.schema.widget.SduiSchemaWidgetFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.example.serverdrivenui.shared.SduiAppService

class CmpMyText : MyText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var text by mutableStateOf("")

    init {
        println("CmpMyText: Widget created")
    }

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        println("CmpMyText: Rendering with text='$text'")
        Text(text = text, modifier = modifier)
    }

    override var modifier: Modifier = Modifier

    override fun text(text: String) {
        println("CmpMyText: text() called with '$text'")
        this.text = text
    }
}

class CmpMyButton : MyButton<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var text by mutableStateOf("")
    private var onClick by mutableStateOf({})

    init {
        println("CmpMyButton: Widget created")
    }

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        println("CmpMyButton: Rendering with text='$text'")
        Button(onClick = onClick, modifier = modifier) {
            Text(text = text)
        }
    }

    override var modifier: Modifier = Modifier

    override fun text(text: String) {
        println("CmpMyButton: text() called with '$text'")
        this.text = text
    }

    override fun onClick(onClick: () -> Unit) {
        println("CmpMyButton: onClick() handler set")
        this.onClick = onClick
    }
}

class CmpMyColumn : MyColumn<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    override val children: app.cash.redwood.widget.Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        androidx.compose.foundation.layout.Column(
            modifier = modifier,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            (children as CmpChildren).render()
        }
    }

    override var modifier: Modifier = Modifier
}

class CmpChildren : app.cash.redwood.widget.Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private val _widgets = mutableStateListOf<Widget<@Composable (androidx.compose.ui.Modifier) -> Unit>>()

    override val widgets: List<Widget<@Composable (androidx.compose.ui.Modifier) -> Unit>>
        get() = _widgets

    override fun insert(index: Int, widget: Widget<@Composable (androidx.compose.ui.Modifier) -> Unit>) {
        _widgets.add(index, widget)
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
         for (i in 0 until count) {
             val element = _widgets.removeAt(fromIndex)
             val dest = if (fromIndex < toIndex) toIndex - 1 else toIndex
             _widgets.add(dest, element)
         }
    }

    override fun remove(index: Int, count: Int) {
        _widgets.removeRange(index, index + count)
    }

    override fun onModifierUpdated(index: Int, widget: Widget<@Composable (androidx.compose.ui.Modifier) -> Unit>) {
    }

    override fun detach() {
        // No-op for Compose children typically, as they are removed from composition when removed from list
    }

    @Composable
    fun render() {
        _widgets.forEach { widget ->
            widget.value(androidx.compose.ui.Modifier)
        }
    }
}

object CmpWidgetFactory : SduiSchemaWidgetFactory<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    override fun MyText(): MyText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating MyText widget")
        return CmpMyText()
    }
    override fun MyButton(): MyButton<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating MyButton widget")
        return CmpMyButton()
    }
    override fun MyColumn(): MyColumn<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating MyColumn widget")
        return CmpMyColumn()
    }
}

class RealHostConsole : HostConsole {
    init {
        println("HOST: RealHostConsole initialized")
    }
    override fun log(message: String) {
        println("JS: $message")
    }
}

class SduiAppSpec(
    override val manifestUrl: Flow<String>,
    override val name: String = "sdui",
) : TreehouseApp.Spec<SduiAppService>() {
    override suspend fun bindServices(treehouseApp: TreehouseApp<SduiAppService>, zipline: Zipline) {
        println("HOST: bindServices called")
        zipline.bind<HostConsole>("console", RealHostConsole())
        println("HOST: console service bound")
    }
    
    override fun create(zipline: Zipline): SduiAppService {
        return zipline.take<SduiAppService>("app")
    }
}
