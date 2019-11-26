package io.sweers.catchup.injection

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

class ResumedActivityProvider(val application: Application) {

  val resumedActivities = mutableListOf<Activity>()

  fun initialize() {
    application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

      override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
      ) {
        resumedActivities += activity
      }

      override fun onActivityStarted(activity: Activity) {
      }

      override fun onActivityDestroyed(activity: Activity) {
      }

      override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
      ) {
      }

      override fun onActivityPaused(activity: Activity) {
        resumedActivities -= activity
      }

      override fun onActivityStopped(activity: Activity) {
      }

      override fun onActivityResumed(activity: Activity) {
        resumedActivities += activity
      }
    })
  }
}