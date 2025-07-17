package com.anonymous.voiceassistantapp



import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

import android.provider.AlarmClock
import android.provider.MediaStore


import android.net.Uri


class AppLauncherModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "AppLauncher"
    }

    private fun launchFromPackageList(packageList: List<String>, errorMessage: String) {
        val pm = reactContext.packageManager
        for (pkg in packageList) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                reactContext.startActivity(launchIntent)
                return
            }
        }
        Toast.makeText(reactContext, errorMessage, Toast.LENGTH_LONG).show()
    }


    @ReactMethod
    fun openApp(appName: String) {
        val pm = reactContext.packageManager
        val context = reactContext

        try {
            when (appName.lowercase()) {
                "phones", "phone", "dialer" -> {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    return
                }

                "messages", "sms" -> {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("smsto:") // no number, opens messages
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Messages app not found", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                "playstore", "play store" -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("market://search?q=")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Play Store not available", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                // GALLERY / PHOTOS
                "gallery", "photos" -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setType("image/*")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Gallery app not found", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                "clock", "alarm" -> {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                    } else {
                        val possibleClockPackages = listOf(
                            "com.google.android.deskclock",   // Pixel, stock Android
                            "com.samsung.android.clockpackage", // Samsung
                            "com.android.deskclock"           // Generic AOSP
                        )
                        var launched = false
                        for (pkg in possibleClockPackages) {
                            val launchIntent = pm.getLaunchIntentForPackage(pkg)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(launchIntent)
                                launched = true
                                break
                            }
                        }
                        if (!launched) {
                            // Open Play Store search for Clock
                            val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=clock"))
                            playIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            if (playIntent.resolveActivity(pm) != null) {
                                context.startActivity(playIntent)
                            } else {
                                Toast.makeText(context, "Clock app not found", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    return
                }


                "camera", "cameras" -> {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                if (intent.resolveActivity(pm) != null) {
                    context.startActivity(intent)
                } else {
                    // Fallback to known camera package names
                    val cameraPackages = listOf(
                        "com.google.android.GoogleCamera",          // Pixel
                        "com.sec.android.app.camera",              // Samsung
                        "com.android.camera",                      // AOSP/others
                        "com.huawei.camera"                        // Huawei
                    )
                    var launched = false
                    for (pkg in cameraPackages) {
                        val launchIntent = pm.getLaunchIntentForPackage(pkg)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launchIntent)
                            launched = true
                            break
                        }
                    }

                    if (!launched) {
                        Toast.makeText(context, "Camera app not found", Toast.LENGTH_LONG).show()
                    }
                }
                return
            }


                "maps", "google maps" -> {
                    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
                    mapsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (mapsIntent.resolveActivity(pm) != null) {
                        context.startActivity(mapsIntent)
                    } else {
                        val mapsPackages = listOf("com.google.android.apps.maps")
                        var launched = false
                        for (pkg in mapsPackages) {
                            val launchIntent = pm.getLaunchIntentForPackage(pkg)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(launchIntent)
                                launched = true
                                break
                            }
                        }
                        if (!launched) {
                            // Open Play Store search for Google Maps
                            val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=google maps"))
                            playIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            if (playIntent.resolveActivity(pm) != null) {
                                context.startActivity(playIntent)
                            } else {
                                Toast.makeText(context, "Google Maps app not found", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    return
                }



            "calculator" -> {
                val calculatorPackages = listOf(
                    "com.android.calculator2",              // AOSP, some Pixel
                    "com.google.android.calculator",        // Google Calculator
                    "com.sec.android.app.calculator",       // Samsung
                    "com.miui.calculator",                  // Xiaomi
                )
                launchFromPackageList(calculatorPackages, "Calculator not found")
                return
            }

            // FALLBACK TO PACKAGE MAP
             else -> {
                    val packageMap = mapOf(
                        "youtube" to "com.google.android.youtube",
                        "chrome" to "com.android.chrome",
                        "gmail" to "com.google.android.gm",
                        "facebook" to "com.facebook.katana",
                        "whatsapp" to "com.whatsapp",
                        "instagram" to "com.instagram.android",
                        "messenger" to "com.facebook.orca",
                        "contacts" to "com.google.android.contacts",
                        "settings" to "com.android.settings"
                    )

                    val pkg = packageMap[appName.lowercase()]
                    if (pkg != null) {
                       try {
                            // Check if package is installed
                            pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)

                            val launchIntent = pm.getLaunchIntentForPackage(pkg)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(launchIntent)
                            } else {
                                Toast.makeText(context, "App is installed but no launchable activity", Toast.LENGTH_LONG).show()
                            }

                        } catch (e: PackageManager.NameNotFoundException) {
                            // Package not installed
                            val playIntent = Intent(Intent.ACTION_VIEW)
                            playIntent.data = Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                            playIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(playIntent)
                        }
                    } else {
                        Toast.makeText(context, "Unsupported app '$appName'", Toast.LENGTH_LONG).show()
                    }
                }


            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
