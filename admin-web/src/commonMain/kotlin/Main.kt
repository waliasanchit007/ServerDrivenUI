import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.example.serverdrivenui.core.data.SupabaseConfig
import com.example.serverdrivenui.core.data.SupabaseGymRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import ui.AdminDashboard
import ui.theme.AdminTheme

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val client = HttpClient(Js)
    val repo = SupabaseGymRepository(client, SupabaseConfig.PROJECT_URL, SupabaseConfig.ANON_KEY)

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        AdminTheme {
            AdminDashboard(repo)
        }
    }
}
