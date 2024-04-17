package sulic.androidproject.edith.dto

data class CompletionDto(val frequencyPenalty: Int,
    val maxTokens: Int,
    val messages: Array<Message>,
    val model: String,
    val presencePenalty: Int,
    val presystem: Boolean,
    val stream: Boolean,
    val temperature: Int,
    val topP: Double
    ) {

    companion object {
        fun Array<String>.toDefaultCompletionDto(): CompletionDto {
            return CompletionDto(1, 1000,
                this.toUserMessage(), "rwkv",
                0, true, false, 1, 0.3
            )
        }

        fun Array<String>.toUserMessage(): Array<Message> = Array(this.size){ i ->
            Message(this[i], false, "user")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompletionDto

        if (frequencyPenalty != other.frequencyPenalty) return false
        if (maxTokens != other.maxTokens) return false
        if (!messages.contentEquals(other.messages)) return false
        if (model != other.model) return false
        if (presencePenalty != other.presencePenalty) return false
        if (presystem != other.presystem) return false
        if (stream != other.stream) return false
        if (temperature != other.temperature) return false
        if (topP != other.topP) return false

        return true
    }

    override fun hashCode(): Int {
        var result = frequencyPenalty
        result = 31 * result + maxTokens
        result = 31 * result + messages.contentHashCode()
        result = 31 * result + model.hashCode()
        result = 31 * result + presencePenalty
        result = 31 * result + presystem.hashCode()
        result = 31 * result + stream.hashCode()
        result = 31 * result + temperature
        result = 31 * result + topP.hashCode()
        return result
    }
}

data class Message(val content: String,
    val raw: Boolean,
    val role: String)