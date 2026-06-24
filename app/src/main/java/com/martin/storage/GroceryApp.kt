package com.martin.storage

import android.app.Application

/**
 * Custom Application class.
 * WorkManager self-initialises via Jetpack App Startup; no manual init required.
 * Add any global SDK initialisation here if needed in future.
 */
class GroceryApp : Application()