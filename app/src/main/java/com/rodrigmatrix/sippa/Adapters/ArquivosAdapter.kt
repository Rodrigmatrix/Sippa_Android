package com.rodrigmatrix.sippa

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.*
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.rodrigmatrix.sippa.persistance.File
import com.rodrigmatrix.sippa.persistence.StudentsDatabase
import kotlinx.android.synthetic.main.arquivo_row.view.*

class ArquivosAdapter(private val arquivos: MutableList<File>): RecyclerView.Adapter<ArquivosViewHolder>() {

    override fun getItemCount(): Int {
        return arquivos.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArquivosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val arquivosRow = layoutInflater.inflate(R.layout.arquivo_row, parent, false)
        return ArquivosViewHolder(arquivosRow)
    }

    override fun onBindViewHolder(holder: ArquivosViewHolder, position: Int) {
        val arquivoData = arquivos[position]
        holder.view.arquivo_name?.text = arquivoData.name
    }
}

class ArquivosViewHolder(val view: View): RecyclerView.ViewHolder(view){
    init {
        view.download_button.setOnClickListener {
            val url = """https://academico.quixada.ufc.br/ServletCentral?comando=CmdLoadArquivo&id=${view.arquivo_name.text}"""
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(view.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    makeRequest()
                }
                else{
                    startDownload(url, view.arquivo_name.text.toString())
                }
            }
            else{
                startDownload(url, view.arquivo_name.text.toString())
            }
        }
    }
    private fun startDownload(url: String, name: String){
        val database = Room.databaseBuilder(
            view.context,
            StudentsDatabase::class.java, "database.db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
        if(database.studentDao().getStudent().jsession == "offline"){
            Snackbar.make(view,"Download em modo offline disponível em breve(próximo semestre)", Snackbar.LENGTH_LONG).show()
        }
        else{
            if(name == "Nenhum arquivo disponível nessa disciplina"){
                Snackbar.make(view,"Nenhum arquivo disponível nessa disciplina", Snackbar.LENGTH_LONG).show()
            }
            else{
                Snackbar.make(view,"Iniciando Download. Verifique suas notificações", Snackbar.LENGTH_LONG).show()
                val request = DownloadManager.Request(Uri.parse(url))
                request.addRequestHeader("Cookie", database.studentDao().getStudent().jsession)
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                request.setTitle("Sippa - " + view.arquivo_name.text)
                request.setDescription("Baixando arquivo...")
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                val manager = view.context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                manager.enqueue(request)
            }
        }

    }
    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            view.context as Activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1000)
    }
}