package xyz.keriteal.bili

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import xyz.keriteal.bili.databinding.ActivityMainBinding
import xyz.keriteal.bili.ui.favorites.FavoriteActivity
import xyz.keriteal.bili.ui.history.HistoryActivity
import xyz.keriteal.bili.ui.login.LoginActivity
import xyz.keriteal.bili.ui.settings.SettingsActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBarMain.toolBar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navViewBottom: BottomNavigationView = binding.navViewBottom
        val navViewSide = binding.navViewSide
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navViewBottom.setupWithNavController(navController)

        navViewSide.setNavigationItemSelectedListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings ->
                startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_login ->
                startActivity(Intent(this, LoginActivity::class.java))
            R.id.nav_history ->
                startActivity(Intent(this, HistoryActivity::class.java))
            R.id.nav_favorites ->
                startActivity(Intent(this, FavoriteActivity::class.java))
        }
        return false
    }
}