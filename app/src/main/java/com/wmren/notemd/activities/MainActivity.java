package com.wmren.notemd.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.dx.command.Main;
import com.wmren.notemd.fragments.InfoFragment;
import com.wmren.notemd.utilities.NotesDB;
import com.wmren.notemd.R;
import com.wmren.notemd.fragments.HomePageScrollView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    public static NotesDB notesDB;

    private HomePageScrollView homePageScrollView = new HomePageScrollView();
    private InfoFragment infoFragment = new InfoFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notesDB = new NotesDB(this, "MyNotes.db", null, 1);

        //将原生actionbar替换为toolbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //findView部分
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        FloatingActionButton fab = findViewById(R.id.fab); //添加新便签


        //新建新便签
        fab.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, NoteviewActivity.class);
            intent.putExtra("noteStatus", 10);
            startActivity(intent);
        });

        //toolbar左侧点击按钮展开菜单
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //设置左滑菜单的按钮监听
        navigationView.setCheckedItem(R.id.nav_home);
        navigationView.setNavigationItemSelectedListener(this);

        addFragment(homePageScrollView, R.id.homepage_fragment);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //用于在主活动中创建导航栏
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //导航栏按钮选区时的监听事件
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // 左滑菜单栏按钮监听事件
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                replaceFragment(homePageScrollView, R.id.homepage_fragment);
                break;
            case R.id.nav_search: {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_info:
                replaceFragment(infoFragment, R.id.homepage_fragment);
                break;
            default:
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //切换碎片所用的函数
    private void replaceFragment(Fragment fragment, int fragmentId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(fragmentId, fragment);
        transaction.commit();
    }

    //添加新碎片所用的函数
    private void addFragment(Fragment fragment, int fragmentId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(fragmentId, fragment);
        transaction.commit();
    }
}
