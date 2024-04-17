package sulic.androidproject.edith.service

import okhttp3.Call
import sulic.androidproject.edith.dto.CompletionDto

interface LLMRemoteService {
    fun completion(dto: CompletionDto): Call
}