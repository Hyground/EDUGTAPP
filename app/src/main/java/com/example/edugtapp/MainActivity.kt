package com.example.edugtapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        println("este es una prueeba ------------------------------------------")
        println("este es una prueeba ------------------------------------------")
        println("este es una prueeba ------------------------------------------")

        println("este es una comic Miguel ------------------------------------------")


        var numero = 10
        var numero2 = 20
        var resul = numero + numero2

        println("este es una prueeba ------------------------------------------")
        println("este es una prueeba ------------------------------------------")
        println("este es una prueeba ------------------------------------------")
        println("este es una prueeba ------------------------------------------")


        println("Hola mundo")
        println("Hola mundopequeño commit mio frank")

        //Commit Alonzo
        var num1 = 15
        var num2 = 3

        var suma = num1 + num2
        var resta = num1 - num2
        var multiplicacion = num1 * num2
        var division = num1 / num2

        println("Suma: $suma")
        println("Resta: $resta")
        println("Multiplicación: $multiplicacion")
        println("División: $division")

        var resultado = """
            Números: $num1 y $num2
            Suma: $suma
            Resta: $resta
            Multiplicación: $multiplicacion
            División: $division
            """.trimIndent()
        println(" El resultado es : $resultado")

    }
}