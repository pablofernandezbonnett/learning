package learning.examples.common

interface Topic {
    val id: String
    val title: String
    val sourceDocs: List<String>

    fun run()
}
