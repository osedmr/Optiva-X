package com.optivax.app.core.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class BannerType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
}

data class BannerMessage(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val message: String? = null,
    val type: BannerType = BannerType.INFO,
    val durationMs: Long = 3500L
)

/**
 * App-wide singleton controller managing modern top-sliding cyber notifications.
 * Can be triggered from any ViewModel, repository, or screen.
 */
@Singleton
class OptivaBannerController @Inject constructor() {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var dismissJob: Job? = null

    private val _currentBanner = MutableStateFlow<BannerMessage?>(null)
    val currentBanner: StateFlow<BannerMessage?> = _currentBanner.asStateFlow()

    fun show(message: BannerMessage) {
        dismissJob?.cancel()
        _currentBanner.value = message

        dismissJob = scope.launch {
            delay(message.durationMs)
            if (_currentBanner.value?.id == message.id) {
                _currentBanner.value = null
            }
        }
    }

    fun showSuccess(title: String, message: String? = null, durationMs: Long = 3500L) {
        show(
            BannerMessage(
                title = title,
                message = message,
                type = BannerType.SUCCESS,
                durationMs = durationMs
            )
        )
    }

    fun showWarning(title: String, message: String? = null, durationMs: Long = 4500L) {
        show(
            BannerMessage(
                title = title,
                message = message,
                type = BannerType.WARNING,
                durationMs = durationMs
            )
        )
    }

    fun showError(title: String, message: String? = null, durationMs: Long = 4500L) {
        show(
            BannerMessage(
                title = title,
                message = message,
                type = BannerType.ERROR,
                durationMs = durationMs
            )
        )
    }

    fun showInfo(title: String, message: String? = null, durationMs: Long = 3500L) {
        show(
            BannerMessage(
                title = title,
                message = message,
                type = BannerType.INFO,
                durationMs = durationMs
            )
        )
    }

    fun dismiss() {
        dismissJob?.cancel()
        _currentBanner.value = null
    }
}
