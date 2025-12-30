package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf

/**
 * Guest-side Navigator that owns all navigation state.
 * The Host has no knowledge of navigation - it just renders what the Guest tells it to.
 */
class Navigator {
    // The navigation stack - Guest owns this!
    private val stack = mutableStateListOf<Screen>()
    
    /**
     * Whether there's a screen to go back to.
     */
    val canGoBack: Boolean
        get() = stack.size > 1
    
    /**
     * The currently visible screen.
     */
    val currentScreen: Screen?
        get() = stack.lastOrNull()
    
    /**
     * Current stack size (for debugging).
     */
    val stackSize: Int
        get() = stack.size
    
    /**
     * Push a new screen onto the stack.
     */
    fun push(screen: Screen) {
        println("Navigator: push(${screen::class.simpleName}), stack size will be ${stack.size + 1}")
        stack.add(screen)
    }
    
    /**
     * Pop the top screen from the stack (go back).
     * Returns true if pop was successful, false if at root.
     */
    fun pop(): Boolean {
        if (canGoBack) {
            val removed = stack.removeLast()
            println("Navigator: pop() removed ${removed::class.simpleName}, stack size is now ${stack.size}")
            return true
        }
        println("Navigator: pop() failed - already at root")
        return false
    }
    
    /**
     * Replace the current screen without adding to history.
     */
    fun replaceCurrent(screen: Screen) {
        if (stack.isNotEmpty()) {
            println("Navigator: replaceCurrent(${screen::class.simpleName})")
            stack[stack.lastIndex] = screen
        } else {
            push(screen)
        }
    }
    
    /**
     * Replace entire stack with a single screen (e.g., for logout).
     */
    fun replaceAll(screen: Screen) {
        println("Navigator: replaceAll(${screen::class.simpleName})")
        stack.clear()
        stack.add(screen)
    }
}

/**
 * Base interface for all screens in the app.
 * Each screen renders its content and has access to the Navigator for navigation.
 */
interface Screen {
    /**
     * Render the screen content.
     * @param navigator The navigator for performing navigation actions
     */
    @Composable
    fun Content(navigator: Navigator)
}
