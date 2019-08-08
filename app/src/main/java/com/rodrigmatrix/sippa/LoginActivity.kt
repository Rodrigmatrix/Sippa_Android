package com.rodrigmatrix.sippa

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.view.isVisible
import androidx.room.Room
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.snackbar.Snackbar
import com.rodrigmatrix.sippa.persistance.Student
import com.rodrigmatrix.sippa.persistance.StudentsDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import okhttp3.*
import kotlin.coroutines.CoroutineContext


class LoginActivity : AppCompatActivity(), CoroutineScope {
    lateinit var cd: ConnectionDetector
    lateinit var database: StudentsDatabase
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        database = Room.databaseBuilder(
            applicationContext,
            StudentsDatabase::class.java, "database.db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
        var student = database.studentDao().getStudent()
        if(student != null){
            when (student.theme) {
                "light" -> {
                    setDefaultNightMode(MODE_NIGHT_NO)
                }
                "dark" -> {
                    setDefaultNightMode(MODE_NIGHT_YES)
                }
                "automatic" -> {
                    setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
        setContentView(R.layout.activity_login)
        cd = ConnectionDetector()
        setFields()
        reload_button.isEnabled = false
        login_offline.setOnClickListener {
            progressLogin.isVisible = true
            if(student != null && student.hasSavedData){
                launch(handler){
                    login("offline")
                }
            }
            else{
                showError("Nenhum dado de login salvo. Para usar este recurso, efetue login em sua conta.")
            }
        }
        login_btn.setOnClickListener{
            main_activity.hideKeyboard()
            if(isValidLoginAndPass()){
                progressLogin.isVisible = true
                login_btn.isEnabled = false
                login_offline.isEnabled = false
                reload_button.isEnabled = false
                val student = database.studentDao().getStudent()
                if(student != null){
                    launch(handler){
                        login(student.jsession)
                    }
                }
                else{
                    launch(handler){
                        login("")
                    }
                }
            }
        }
        reload_button.setOnClickListener {
            progressLogin.isVisible = true
            reload_button.isEnabled = false
            launch(handler){
                getCaptcha()
            }

        }
    }

    private val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("Exception", ":$throwable")
    }

    override fun onResume() {
        if(login_btn.isEnabled){
            launch(handler) {
                getCaptcha()
            }
        }
        super.onResume()
    }

    private suspend fun getCaptcha(){
        if(!cd.isConnectingToInternet(this@LoginActivity)){
            showError("Verifique sua conexão com a internet")
            return
        }
        val request = Request.Builder()
            .url("https://sistemas.quixada.ufc.br/apps/sippa/captcha.jpg")
            .build()
        val client = OkHttpClient()
        withContext(Dispatchers.IO){
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                var data = response.header("Set-Cookie").toString()
                data = data.replace("[","")
                val parts = data.split(";")
                val jsession = parts[0]
                val student = database.studentDao().getStudent()
                if(student != null){
                    student.jsession = jsession
                    database.studentDao().deleteStudent()
                    database.studentDao().insertStudent(student)
                }
                else{
                    database.studentDao().deleteStudent()
                    database.studentDao().insertStudent(
                        Student(0, jsession, "",
                            "", "", "","", "","automatic", false, "")
                    )
                }
                if(response.body() != null) {
                    var bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
                    if(bmp != null) {
                        runOnUiThread {
                            captcha_input.text!!.clear()
                            progressLogin.isVisible = false
                            login_btn.isEnabled = true
                            reload_button.isEnabled = true
                            captcha_image.setImageBitmap(bmp)
                        }
                    }
                }
            }
            else{
                showError("Verifique se o Sippa está online no momento ou sua conexão com a internet.")
            }
        }
    }
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    private fun setFields(){
        val student = database.studentDao().getStudent()
        if((student != null) && (student.login != "")){
            runOnUiThread {
                login_input.setText(student.login)
                password_input.setText(student.password)
            }
        }
    }

    private fun isValidLoginAndPass(): Boolean{
        var isValid = true
        if(login_input.text.toString().isEmpty()){
            login_field.error = "Digite seu login"
            isValid = false
        }
        else{
            login_field.error = null
        }
        if(password_input.text.toString().isEmpty()){
            password_field.error = "Digite sua senha"
            isValid = false
        }
        else{
            password_field.error = null
        }
        if(captcha_input.text.toString().isEmpty()){
            captcha.error = "Vazio"
            isValid = false
        }
        else{
            captcha.error = null
        }
        return isValid
    }

    private fun login(cookie: String) {
        println(cookie)
        if(cookie == "offline"){
            var student = database.studentDao().getStudent()
            student.jsession = "offline"
            database.studentDao().deleteStudent()
            database.studentDao().insertStudent(student)
            runOnUiThread {
                progressLogin.isVisible = false
                captcha_input.text!!.clear()
                login_btn.isEnabled = true
                login_offline.isEnabled = true
                reload_button.isEnabled = true
                progressLogin.isVisible = false
            }
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("login", login_input.text.toString())
            intent.putExtra("password", password_input.text.toString())
            startActivity(intent)
        }
        else{
            var login = login_input.text.toString()
            login.replace("&", "%26")
            login.replace("=", "%3D")
            var password = password_input.text.toString()
            password = password.replace("&", "%26")
            password = password.replace("=", "%3D")
            var encoded = "login=" + login + "&senha=" + password + "&conta=aluno&captcha=" + captcha_input.text.toString() + "&comando=CmdLogin&enviar=Entrar"
            if(!cd.isConnectingToInternet(this@LoginActivity)){
                showError("Verifique sua conexão com a internet")
                return
            }
            Fuel.post("https://sistemas.quixada.ufc.br/apps/ServletCentral")
                .header("Content-Type" to "application/x-www-form-urlencoded")
                .header("Cookie", cookie)
                .body(encoded)
                .timeout(60000)
                .timeoutRead(60000)
                .response { _, response, _ ->
                    println(response.statusCode)
                    if(response.statusCode != 200){
                        showError("Verifique se o Sippa está online no momento ou sua conexão com a internet.")
                    }
                    when{
                        response.toString().contains("Olá ALUNO(A)") -> {
                            if(response.toString().contains("Por favor, altere a sua senha.")){
                                val client = OkHttpClient()
                                val request = Request.Builder()
                                    .url("https://sistemas.quixada.ufc.br/apps/ServletCentral?comando=CmdListarDisciplinaAluno")
                                    .header("Content-Type", "application/x-www-form-urlencoded")
                                    .header("Cookie", cookie)
                                    .build()
                                val response = client.newCall(request).execute()
                                if (response.isSuccessful) {
                                    val res = response.body()!!.string()
                                    showWeakPassword(res)
                                }
                                else{
                                    showError("Erro ao efetuar login. Verifique sua conexão com a internet")
                                }
                            }
                            else{
                                setData(response.toString())
                            }
                        }
                        response.toString().contains("Erro 500: Contacte o Administrador do Sistema") -> {
                            launch(handler){
                                getCaptcha()
                            }
                            showError("Tempo de conexão expirado. Digite o novo captcha")
                        }

                        response.toString().contains("Erro ao digitar os caracteres. Por favor, tente novamente.") -> {
                            launch(handler){
                                getCaptcha()
                            }
                            showError("Captcha incorreto. Digite o novo captcha")
                        }

                        response.toString().contains("Aluno não encontrado ou senha inválida.") -> {
                            launch(handler){
                                getCaptcha()
                            }
                            showError("Aluno não encontrado ou senha inválida.")
                        }
                    }
                }
        }
    }
    private fun showWeakPassword(res: String){
        runOnUiThread {
            var dialog = AlertDialog.Builder(this@LoginActivity)
            dialog.setTitle("Senha muito fraca!")
            dialog.setMessage("Sua senha é vulnerável. Atualize sua senha do sistema.")
            dialog.setPositiveButton("Continuar") { _, _ ->
                setData(res)
            }
            dialog.setNegativeButton("Cancelar") { _, _ ->
                showError("Login cancelado. Por favor atualize sua senha do sippa")
                launch(handler){
                    getCaptcha()
                }
            }
            dialog.setOnCancelListener {
                showError("Login cancelado. Por favor atualize sua senha do sippa")
                launch(handler){
                    getCaptcha()
                }
            }
            var alert = dialog.create()
            alert.show()
        }
    }
    private fun showError(error: String){
        runOnUiThread {
            progressLogin.isVisible = false
            captcha_input.text!!.clear()
            login_btn.isEnabled = true
            login_offline.isEnabled = true
            reload_button.isEnabled = true
            Snackbar.make(main_activity, error, Snackbar.LENGTH_LONG).show()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") {
        if(it.length >= 3){
            return@joinToString it.capitalize()
        }
        else{
            return@joinToString it
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setData(response: String){
        val student = database.studentDao().getStudent()
        var resArray = response.split("Olá ALUNO(A) ")
        resArray = resArray[1].split("</h1>")
        val name = resArray[0]
        student.id = 0
        student.responseHtml = response
        student.classSetHtml = ""
        student.matricula = login_input.text.toString().removeRange(0, 1)
        student.name = name.toLowerCase().capitalizeWords()
        database.studentDao().insertStudent(student)
        runOnUiThread {
            progressLogin.isVisible = false
            captcha_input.text?.clear()
            login_btn.isEnabled = true
            reload_button.isEnabled = true
            login_offline.isEnabled = true
        }
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("login", login_input.text.toString())
        intent.putExtra("password", password_input.text.toString())
        this.startActivity(intent)
    }
}