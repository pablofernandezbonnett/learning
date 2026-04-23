package learning.examples.common

object Console {
    fun banner(title: String) {
        println()
        println("=".repeat(80))
        println(title)
        println("=".repeat(80))
    }

    fun section(title: String) {
        println()
        println("--- $title ---")
    }

    fun step(message: String) {
        println("[step] $message")
    }

    fun result(label: String, value: Any?) {
        println("[result] $label = $value")
    }

    fun docs(sourceDocs: List<String>) {
        if (sourceDocs.isEmpty()) return
        println("[docs]")
        sourceDocs.forEach { println("  - $it") }
    }
}
