plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.rentalinn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rentalinn"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Retrofit for network calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Socket.IO for real-time communication
    implementation("io.socket:socket.io-client:2.0.1")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Charts and Graphs
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("co.yml:ycharts:2.1.0")
    
    // Animations
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    
    // Calendar
    implementation("com.kizitonwose.calendar:compose:2.5.0")
    
    // Shimmer loading effect
    implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")
    
    // Material Design Icons Extended
    implementation("androidx.compose.material:material-icons-extended")
    
    // Accompanist for system UI controller and other utilities
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")

    // OpenStreetMap and Location Services
    implementation("org.osmdroid:osmdroid-android:6.1.17")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Add pull-to-refresh dependency
    implementation("androidx.compose.material:material:1.6.1")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
