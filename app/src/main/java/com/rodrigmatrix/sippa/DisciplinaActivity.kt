package com.rodrigmatrix.sippa

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rodrigmatrix.sippa.fragments.NotasFragment
import com.rodrigmatrix.sippa.ui.main.SectionsPagerAdapter
import kotlinx.android.synthetic.main.activity_disciplina.*

class DisciplinaActivity : AppCompatActivity() {
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disciplina)
        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val id = intent.getStringExtra("id")
        sectionsPagerAdapter.removeFragments()
        sectionsPagerAdapter.addFragment(NoticiasFragment.newInstance(id))
        sectionsPagerAdapter.addFragment(NotasFragment.newInstance(id))
        sectionsPagerAdapter.addFragment(PlanoAulaFragment.newInstance(id))
        sectionsPagerAdapter.addFragment(ArquivosFragment.newInstance(id))
        view_pager.offscreenPageLimit = 4
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
        val name = intent.getStringExtra("name")
        title = if(name.contains(" -")){
            name!!.split(" -")[0]
        } else{
            name
        }

        toolbar.setNavigationOnClickListener {
            sectionsPagerAdapter.removeFragments()
            this.finish()
        }
        tabs.setupWithViewPager(view_pager)
        view_pager.adapter = sectionsPagerAdapter
        tabs.getTabAt(0)?.setIcon(R.drawable.ic_noticias_24dp)
        tabs.getTabAt(1)?.setIcon(R.drawable.ic_notas_24dp)
        tabs.getTabAt(2)?.setIcon(R.drawable.ic_plano_24dp)
        tabs.getTabAt(3)?.setIcon(R.drawable.ic_arquivos_24dp)
        if(intent.getStringExtra("option") == "grades"){
            val op = tabs.getTabAt(1)
            op?.select()
        }
        else{
            val op = tabs.getTabAt(0)
            op?.select()
        }
    }
}