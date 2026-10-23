package isel.pdm.droidmentor.domain.chat

/**
 * A piece of a mentor message: either plain text or a block of code.
 */
sealed class MessagePart {
    data class Plain(val text: String) : MessagePart()
    data class Code(val code: String) : MessagePart()
}

/**
 * Splits [text] into plain and code parts, using lines that start with ``` as fences.
 *
 * - The opening fence line is dropped whole, so a language name after it (```kotlin) is dropped too.
 * - An opening fence without a closing one makes the rest of the text code.
 * - Blank lines around each part are trimmed, and empty parts are skipped.
 */
fun splitIntoParts(text: String): List<MessagePart> {
    val parts = mutableListOf<MessagePart>()
    val currentLines = mutableListOf<String>()
    var insideCode = false

    for (line in text.lines()) {
        if (line.trimStart().startsWith("```")) {
            val part = toPart(currentLines, insideCode)
            if (part != null) parts.add(part)
            currentLines.clear()
            insideCode = !insideCode
        } else {
            currentLines.add(line)
        }
    }

    val lastPart = toPart(currentLines, insideCode)
    if (lastPart != null) parts.add(lastPart)

    return parts
}

/**
 * Builds a part from [lines] without the blank lines around them,
 * or returns `null` if nothing is left.
 */
private fun toPart(lines: List<String>, isCode: Boolean): MessagePart? {
    val content = lines
        .dropWhile { it.isBlank() }
        .dropLastWhile { it.isBlank() }
        .joinToString("\n")
    if (content.isEmpty()) return null
    return if (isCode) MessagePart.Code(content) else MessagePart.Plain(content)
}