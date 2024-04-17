package sulic.androidproject.edith.service

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import sulic.androidproject.edith.service.impl.DefaultLLMRemoteService
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
interface DefaultServicesModule {

    @Binds
    fun provideRemoteService(impl: DefaultLLMRemoteService): LLMRemoteService
}