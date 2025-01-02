package com.shazycode.learnio.ui.main

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.shazycode.learnio.R

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController
    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        bottomNavigationView = findViewById(R.id.bottomNavigation)

        navigationView.setNavigationItemSelectedListener(this)

        // Handle Drawer Icon Click
        val imageView: ImageView = findViewById(R.id.drawer_icon)
        imageView.setOnClickListener {
            if (drawer.isDrawerVisible(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            } else {
                drawer.openDrawer(GravityCompat.START)
            }
        }

        // Set up NavController for both Bottom Navigation and Drawer
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        // Link BottomNavigationView to NavController
        bottomNavigationView.setupWithNavController(navController)

        // Link Drawer to NavController
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.home, R.id.courses, R.id.profile, R.id.enrolledCourses), drawer
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)

        // Synchronize Bottom Navigation with Navigation Drawer
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home, R.id.courses, R.id.profile, R.id.enrolledCourses -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    bottomNavigationView.visibility = View.GONE
                }
            }
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                navController.navigate(R.id.home)
                bottomNavigationView.menu.findItem(R.id.home).isChecked = true // Sync bottom nav state
            }
            R.id.courses -> {
                navController.navigate(R.id.courses)
                bottomNavigationView.menu.findItem(R.id.courses).isChecked = true // Sync bottom nav state
            }
            R.id.profile -> {
                navController.navigate(R.id.profile)
                bottomNavigationView.menu.findItem(R.id.profile).isChecked = true // Sync bottom nav state
            }
            R.id.enrolledCourses -> {
                navController.navigate(R.id.enrolledCourses)
                bottomNavigationView.menu.findItem(R.id.enrolledCourses).isChecked = true // Sync bottom nav state
            }
        }

        // Close the drawer after selecting an item
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}