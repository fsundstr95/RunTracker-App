package com.example.runtracker2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.runtracker2.model.Model
import com.example.runtracker2.model.database.RunDAO
import com.example.runtracker2.other.Constants.ACTION_SHOW_TRACKING_SCREEN
import com.example.runtracker2.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runtracker2.other.Constants.REQUEST_CODE_NOTIFICATION_PERMISSION
import com.example.runtracker2.other.TrackingUtility
import com.example.runtracker2.ui.theme.RunTracker2Theme
import com.example.runtracker2.views.AppNavigation
import com.example.runtracker2.views.LocationPermissionTextProvider
import com.example.runtracker2.views.MapScreen
import com.example.runtracker2.views.PermissionDialog
import com.example.runtracker2.views.PermissionTextProvider
import com.example.runtracker2.views.PostNotificationPermissionTextProvider
import com.vmadalin.easypermissions.EasyPermissions
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), EasyPermissions.PermissionCallbacks {

    @Inject
    lateinit var model: Model

    @set:Inject
    var isFirstAppOpen = true

    lateinit var viewModel: ViewModel

    private val dialogState = mutableStateOf<PermissionDialogState?>(null)

    data class PermissionDialogState(
        val permissionTextProvider: PermissionTextProvider,
        val isPermanentlyDeclined: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.setLifecycleOwner(this)
        viewModel = ViewModel(model,this)
        requestNotificationPermission()



        enableEdgeToEdge()
        setContent {
            RunTracker2Theme {
                if(isFirstAppOpen){
                    AppNavigation(
                        viewModel = viewModel,
                        requestPermissions = { requestPermissions() },
                        startDestination = "SetupView"
                    )
                }else if (intent?.action == ACTION_SHOW_TRACKING_SCREEN){
                    NavigateToRunScreen()
                }else{
                AppNavigation(
                    viewModel = viewModel,
                    requestPermissions = { requestPermissions() },
                    startDestination = "RunView"
                )
            }




                dialogState.value?.let { state ->
                    PermissionDialog(
                        permissionTextProvider = state.permissionTextProvider,
                        isPermanentlyDeclined = state.isPermanentlyDeclined,
                        onDismiss = { dialogState.value = null },
                        onOkClick = {
                            dialogState.value = null

                        },
                        onGoToAppSettingsClick = {
                            dialogState.value = null
                            // Navigate to system settings
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:$packageName")
                            )
                            startActivity(intent)
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }


            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("onNewIntent called in MainActivity")

    }


    @Composable
    private fun NavigateToRunScreen(){
        AppNavigation(
            viewModel = viewModel,
            requestPermissions = { requestPermissions() },
            startDestination = "RunView"
        )
    }

    fun requestPermissions(){
        requestLocationPermissions()
        //requestBackgroundPermission()

    }

    fun requestNotificationPermission(){

        if (TrackingUtility.hasNotificationPermission(this)){
            Timber.d("App already have the permission")
            return
        }

        EasyPermissions.requestPermissions(
            this,
            "You need to accept Notification permissions to use this app",
            REQUEST_CODE_NOTIFICATION_PERMISSION
        )
    }

    fun requestLocationPermissions() {

        if (TrackingUtility.hasLocationPermissions(this)) {
            return
        }


        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_LOCATION_PERMISSION
        )

        /*EasyPermissions.requestPermissions(
            this,
            "You need to accept location permissions to use this app",
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )*/

    }

    fun requestBackgroundPermission() {
        if (!TrackingUtility.hasLocationPermissions(this)) {
            Log.e("Permission", "Location permissions must be granted first.")
            return
        }
        if (TrackingUtility.hasBackgroundPermissions(this)) {
            Log.e("Permission", "Background permissions already seams to be granted.")
            return
        }

        // Request background permission separately
        EasyPermissions.requestPermissions(
            this,
            "Background location permission is needed for tracking in the background.",
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            val provider = when (requestCode) {
                REQUEST_CODE_LOCATION_PERMISSION -> LocationPermissionTextProvider()
                REQUEST_CODE_NOTIFICATION_PERMISSION -> PostNotificationPermissionTextProvider()
                else -> null
            }

            provider?.let {
                dialogState.value = PermissionDialogState(
                    permissionTextProvider = it,
                    isPermanentlyDeclined = true
                )
            }
        } else {
            // Retry permission request if not permanently denied
            when (requestCode) {
                REQUEST_CODE_LOCATION_PERMISSION -> requestPermissions()
                REQUEST_CODE_NOTIFICATION_PERMISSION -> requestNotificationPermission()
            }
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) requestBackgroundPermission()
    }

    // Skickar vidare permission results från android till Easypermissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RunTracker2Theme {
        Greeting("Android")
    }
}