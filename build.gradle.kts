// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d76c070 (beta 1.0)
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force ("androidx.lifecycle:lifecycle-common:2.6.1")
        }
    }
<<<<<<< HEAD
=======
>>>>>>> abcc495 (Add NSGs work)
=======
>>>>>>> d76c070 (beta 1.0)
=======
>>>>>>> ceba9e1 (beta 1.0)
}