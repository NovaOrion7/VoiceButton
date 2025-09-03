plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.novaorion.volumecontrol"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.novaorion.volumecontrol"
        minSdk = 25
        targetSdk = 35
    versionCode = 18
        versionName = "1.14"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // NDK konfigürasyonu debug symboller için
        ndk {
            debugSymbolLevel = "FULL"
        }
        
        // Locale config
        resourceConfigurations.addAll(listOf("en", "tr", "hi", "de", "ar", "es", "bn"))
        
        // Manifest placeholders for AdMob IDs
        manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713"
    }
    
    // Flavor dimensions for different ad configurations
    flavorDimensions += "version"
    productFlavors {
        create("apk") {
            dimension = "version"
            // Test AdMob App ID for APK builds
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("boolean", "USE_REAL_ADS", "false")
        }
        create("aab") {
            dimension = "version"
            // Real AdMob App ID for AAB builds
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-2239637684721708~3562790073"
            buildConfigField("boolean", "USE_REAL_ADS", "true")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("../nova-orion-release.jks")
            storePassword = project.property("RELEASE_STORE_PASSWORD") as String
            keyAlias = project.property("RELEASE_KEY_ALIAS") as String
            keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            
            // Debug symbols and mapping file
            isDebuggable = false
            ndk {
                debugSymbolLevel = "FULL"
            }
            
            // Native crash reporting için ek ayarlar
            packagingOptions {
                pickFirst("**/libc++_shared.so")
                pickFirst("**/libjsc.so")
            }
        }
        
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    // Native debug metadata konfigürasyonu
    androidComponents {
        onVariants(selector().withBuildType("release")) { variant ->
            variant.packaging.jniLibs.useLegacyPackaging = true
        }
    }
    
    bundle {
        language {
            enableSplit = false  // Dil değiştirme için tüm dilleri AAB'ye dahil et
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.compose.ui:ui-graphics:1.7.5")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.gms:play-services-ads:23.3.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}