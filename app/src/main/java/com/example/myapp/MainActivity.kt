package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import kotlin.concurrent.thread
import java.io.File

class MainActivity : Activity() {

    lateinit var out: TextView
    lateinit var home: File

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)

        home = File(filesDir, "home")
        home.mkdirs()

        val setup = Button(this).apply { text = "SETUP" }
        val stream = Button(this).apply { text = "STREAM" }
        out = TextView(this)

        setup.setOnClickListener { runSetup() }
        stream.setOnClickListener { runStream() }

        val l = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(setup)
            addView(stream)
            addView(ScrollView(this@MainActivity).apply { addView(out) })
        }
        setContentView(l)
    }

    fun log(s: String) = runOnUiThread { out.append(s + "\n") }

    fun sh(cmd: String) {
        thread {
            try {
                val p = Runtime.getRuntime().exec(arrayOf("sh","-c",cmd))
                log(p.inputStream.bufferedReader().readText())
                log(p.errorStream.bufferedReader().readText())
            } catch (e: Exception) {
                log("ERR: ${e.message}")
            }
        }
    }

    fun runSetup() {
        log("== SETUP ==")
        sh("""
            cd ${home.absolutePath} || exit 1
            mkdir -p bin
            cd bin

            wget -O busybox https://busybox.net/downloads/binaries/1.31.1-defconfig-multiarch/busybox
            chmod +x busybox

            wget -O dropbearmulti https://github.com/mkj/dropbear/releases/download/DROPBEAR_2022.83/dropbearmulti
            chmod +x dropbearmulti

            ln -s dropbearmulti ssh

            wget -O python https://github.com/indygreg/python-build-standalone/releases/download/20240107/cpython-3.11.7+20240107-x86_64-unknown-linux-gnu-install_only.tar.gz

            echo SETUP DONE
        """.trimIndent())
    }

    fun runStream() {
        log("== STREAM ==")
        sh("""
            cd ${home.absolutePath} || exit 1
            ./bin/busybox sh -c "
              python -m http.server 8080 &
              ./bin/ssh -R 80:localhost:8080 nokey@localhost.run
            "
        """.trimIndent())
    }
}
