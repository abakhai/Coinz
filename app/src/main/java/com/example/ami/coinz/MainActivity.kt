package com.example.ami.coinz

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import org.json.JSONObject
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener, LocationEngineListener, SensorEventListener {

    //Variables for the map
    private lateinit var mapView : MapView
    private var map : MapboxMap? = null
    lateinit var icon : Icon
    //Variables for json
    private val baseUrl = "http://homepages.inf.ed.ac.uk/stg/coinz/"
    private lateinit var updatedURL : String
    private val endOfUrl: String = "/coinzmap.geojson"

    //Variables for firebase
    private var mAuth: FirebaseAuth? = null
    private val db = FirebaseFirestore.getInstance()

    //Variables for shared preferences
    private var downloadDate = ""// Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    private var prevuid = ""

    //Variables for location
    private lateinit var permissionManager: PermissionsManager
    private lateinit var originLocation : Location
    private var locationEngine : LocationEngine? = null
    private var locationLayerPlugin : LocationLayerPlugin? = null

    //Variables for the step sensor
    private var running = false
    private var sensorManager: SensorManager? = null

    //The following variables are to update, add and remove
    //the appropriate coins for the wallet
    //As well as assigning the correct rates to each currency
    object Data {
        var wallet = ArrayList<Coin>()
        var coinsList = ArrayList<Coin>()
        var coinlist = ArrayList<Coin>()
        var steps = "0"

        var DOLR = 0f
        var SHIL = 0f
        var PENY = 0f
        var QUID = 0f
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //Creating the sensorManager for the step counter bonus feature
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        //Setting up the unique authenticator
        mAuth = FirebaseAuth.getInstance()
        //Remembering the authenticator
        prevuid = mAuth?.currentUser?.uid as String

        //Getting instance for the map and map setting and population
        Mapbox.getInstance(applicationContext, getString(R.string.access_key))
        mapView = findViewById(R.id.mapboxMapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        //Setting up the BottomNavBar
        val bottomNavigation : BottomNavigationView = findViewById(R.id.navBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val menu = bottomNavigation.getMenu()
        val menuItem = menu.getItem(1)
        menuItem.setChecked(true)
    }

    //Populating the BottomNavBar
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homenav -> {
                val intent1 = Intent(this, HomeActivity::class.java)
                startActivity(intent1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.mapnav -> {
                val intent2 = Intent(this, MainActivity::class.java)
                //Stopping the Activity to overlay itself if user pressed on the activity they are on
                intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent2)
                return@OnNavigationItemSelectedListener true
            }
            R.id.walletnav -> {
                val intent3 = Intent(this, WalletActivity::class.java)
                startActivity(intent3)
                return@OnNavigationItemSelectedListener true
            }
            R.id.banknav -> {
                val intent4 = Intent(this, BankActivity::class.java)
                startActivity(intent4)
                return@OnNavigationItemSelectedListener true
            }
            R.id.transfernav -> {
                val intent5 = Intent(this, TransferActivity::class.java)
                startActivity(intent5)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) {
            Timber.d( "[onMapReady mapboxMap is null")
        } else {
            map = mapboxMap
            //Set user interface options
            map?.uiSettings?.isCompassEnabled = true
            //Make location information available
            enableLocation()
            //AddMarkers().addMarkers(map, title, snippet, iconB, iconG, iconR, iconY)
            getJson()
        }
    }

    //getJson, fetches the elements needed in this activity from the geogson
    private fun getJson() {
        //getting the current date
        val current = LocalDateTime.now()
        val formattedDate = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val now = current.format(formattedDate)

        //Download all the coins for the map if
        //the download date saved is not the same as today, or,
        //if the previous user was different then the current user
        if (now != downloadDate || prevuid != mAuth?.currentUser?.uid) {

            //Clearing the list of coins for new coins
            Data.coinsList.clear()

            //Adding markers after removing the previous ones so they don't overlay
            map?.clear()

            //Save the previous user as the current user
            prevuid = mAuth?.currentUser?.uid as String

            //Make the remembered date the current date
            downloadDate = now
            val dateString = downloadDate

            //Update the geogson url to get the elements for today
            updatedURL = baseUrl + dateString + endOfUrl
            val json = DownloadFileTask(DownloadCompleteRunner).execute(updatedURL).get()

            //Getting the elements (the coins for the map, and the rates)
            val featureCollection = FeatureCollection.fromJson(json)
            val featureList = featureCollection.features()
            val featureSize = featureList?.size as Int
            val jsonObject = JSONObject(json)
            val ratesObject = jsonObject.getJSONObject("rates")
            Data.DOLR = ratesObject.getString("DOLR").toFloat()
            Data.SHIL = ratesObject.getString("SHIL").toFloat()
            Data.PENY = ratesObject.getString("PENY").toFloat()
            Data.QUID = ratesObject.getString("QUID").toFloat()

            //getting the coins with the help of the Coin data class
            for (index in 0 until featureSize - 1) {
                val feature = featureList[index]
                val geo = feature.geometry()
                val point = geo as Point
                val id = feature.properties()?.get("id").toString().replace("\"", "")
                val coords = point.coordinates()
                val currency = feature.properties()?.get("currency").toString().replace("\"", "")
                val exactval = feature.properties()?.get("value").toString().replace("\"", "")
                Data.coinsList.add(Coin(id, coords, currency, exactval))

            }
        }
        //Adding markers after removing the previous ones so they don't overlay
        //Adding the markers associated to the coin on the map
        addMarkers(map, Data.coinsList)
    }

    //addMarkers() adds the markers associated to the coin on the map
    private fun addMarkers(map : MapboxMap?, coins : ArrayList<Coin>) {
        //Adding markers after removing the previous ones so they don't overlay
        map?.removeAnnotations()

        //Getting the IconFactory to add custom icon as marker
        val iconf = IconFactory.getInstance(applicationContext)
        val iconB = iconf.fromResource(R.drawable.marker_blue)
        val iconG = iconf.fromResource(R.drawable.marker_green)
        val iconR = iconf.fromResource(R.drawable.marker_red)
        val iconY = iconf.fromResource(R.drawable.marker_yellow)

        //Associating each coin with the appropriate marker
        for (index in 0..((coins.size)-1)) {
            val coin = coins[index]
            val currency = coin.curr
            val coords = coin.coord
            val value = coin.value
            //Default yellow for quid
            icon = when (currency) {
                "DOLR" -> iconG
                "SHIL" -> iconB
                "PENY" -> iconR
                 else -> iconY
            }
            //Adding the markers with a title, some info and a marker at the coordinates of the coin
            map?.addMarker(
                    MarkerOptions()
                            .title(getString(R.string.maker_title, currency))
                            .snippet(getString(R.string.marker_snippet, value))
                            .icon(icon)
                            .position(LatLng(coords[1], coords[0]))
            )
        }
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Timber.d( "Permissions are granted")
            initLocationEngine()
            initLocationLayer()
        } else {
            Timber.d( "Permissions are not granted")
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        Timber.d( "[onConnected] requesting location update")
        locationEngine?.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            Timber.d( "[onLocationChanged location is null")
        } else {
            //if the location changed then the new location is the origin location
            originLocation = location

            //Get the user unique identification
            val userId = mAuth?.currentUser?.uid as String
            //initialise hashmap to add coin information
            val hm = HashMap<String, Any>()
            //create a location variable
            val loc = Location("map")

            var distance = 20 //Default for hard mode
            if (HomeActivity.DataHome.mode == "EASY") {
                distance = 70
            } else if (HomeActivity.DataHome.mode == "MEDIUM") {
                distance = 35
            }

            Data.coinlist.clear()

            //Make the coin coordinates translate to location
            for (c in Data.coinsList) {
                loc.latitude = c.coord[1]
                loc.longitude = c.coord[0]

            //Check if the coin location is less then the distance set
                // if yes then add is to a list and the wallet
            if (originLocation.distanceTo(loc) <= distance) {
                Data.coinlist.add(c)
                if (!Data.wallet.contains(c)) {
                    Data.wallet.add(c)
                }
            }
            }

            //Add the collected coin detail to firebase
            for (coin in Data.coinlist) {
                    Data.coinsList.remove(coin)
                    hm["currency"] = coin.curr
                    hm["value"] = (coin.value).toFloat()
                    hm["id"] = coin.id
                    hm["coord"] = coin.coord
                    val id = coin.id
                    val check = Data.coinsList.size
                    Timber.d( "[coinsList] check after remove $check ")
                  db.document("User/$userId/Wallet/Coin/IDs/$id").set(hm)
            }

            addMarkers(map, Data.coinsList)
        }
    }


    override fun onPermissionResult(granted: Boolean) {
        Timber.d( "[onPermissionResult] granted == $granted")
       if (granted) {
           enableLocation()
       }
    }

    @SuppressWarnings("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine?.interval = 5000 // preferably every 5 seconds
        locationEngine?.fastestInterval = 1000 // at most every second
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.activate()

        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPostition(lastLocation)
        } else {
            locationEngine?.addLocationEngineListener(this)
        }
    }

    private fun setCameraPostition(location : Location) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom
        (LatLng(location.latitude, location.longitude),13.0))
    }

    @SuppressWarnings("MissingPermission")
    private fun initLocationLayer() {
        if (map == null) {
                Timber.d("map is null")
        } else {
            locationLayerPlugin = LocationLayerPlugin(mapView, map!!, locationEngine)
            locationLayerPlugin?.setLocationLayerEnabled(true)
            locationLayerPlugin?.cameraMode = CameraMode.TRACKING
            locationLayerPlugin?.renderMode = RenderMode.NORMAL
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Timber.d("Permission: $permissionsToExplain")
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        //Check if the user is logged in
        if (mAuth?.currentUser == null) {
            val intentlog = Intent(this, FirebaseUIActivity::class.java)
            startActivity(intentlog)
        } else {
            Timber.d("Someone is logged in" )
        }

        //Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        prevuid = settings.getString("prevuid", mAuth?.currentUser?.uid)
        //use "" as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "")
        //Write a message to "logcat" (for debugging purposes)
        Timber.d("[onStart] Recalled lastDownloadDate is $downloadDate" )
        Data.SHIL  = settings.getFloat("SHIL", 1f)
        Data.DOLR  = settings.getFloat("DOLR", 1f)
        Data.PENY  = settings.getFloat("PENY", 1f)
        Data.QUID  = settings.getFloat("QUID", 1f)

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
        val gson = Gson()
        val json = settings.getString("Coins list", "")
        val type = object : TypeToken<List<Coin>>() {}.type
        //Get the saved coinsList (coins collected list)
        if (!json.isEmpty()) {
            val coinList: List<Coin> = gson.fromJson<List<Coin>>(json, type)
            Data.coinsList = ArrayList(coinList)
        }
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        //Resume counting steps
        running = true
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepsSensor == null) {
            Toast.makeText(this, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        //Pause counting steps
        running = false
        sensorManager?.unregisterListener(this)

        mapView.onPause()
    }

    //Function for the step counter
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
    //Function for the step counter
    override fun onSensorChanged(event: SensorEvent) {
        if (running) {
            Data.steps = event.values[0].toString()
        }
    }

    override fun onStop() {
        super.onStop()

        Timber.d("[onStop] Storing lastDownloadDate of $downloadDate")
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)

        //We need an Editor object to make preferences changes
        val editor = settings.edit()
        //Saving user authenticator
        editor.putString("prevuid", prevuid)
        //Saving download date
        editor.putString("lastDownloadDate", downloadDate)
        //Saving currency rates
        editor.putFloat("SHIL", Data.SHIL)
        editor.putFloat("DOLR", Data.DOLR)
        editor.putFloat("PENY", Data.PENY)
        editor.putFloat("QUID", Data.QUID)
        val gson = Gson()
        //Savinf list of collected coins
        val json = gson.toJson(Data.coinsList)
        editor.putString("Coins list", json)
        //https://stackoverflow.com/questions/14981233/android-arraylist-of-custom-objects-save-to-sharedpreferences-serializable?fbclid=IwAR0TkPyRYJXSszjDFuEGgtariSqLPO0R_TL1KgheJ8rZRk8AN-FNzVJDX2Q
        //Apply the edits!
        editor.apply()

        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        locationEngine?.deactivate()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (outState != null) {
            mapView.onSaveInstanceState(outState)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}
