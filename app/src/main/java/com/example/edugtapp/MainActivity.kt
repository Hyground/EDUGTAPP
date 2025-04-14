package com.example.edugtapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log

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

        println("Commit de prueba de Iván")



            // Solo prueba Iván xd

        val n1 = 10.0
        val n2 = 5.0

        Log.d("Calculadora", "Suma: ${sumar(n1, n2)}")
        Log.d("Calculadora", "Resta: ${restar(n1, n2)}")
        Log.d("Calculadora", "Multiplicación: ${multiplicar(n1, n2)}")
        Log.d("Calculadora", "División: ${dividir(n1, n2)}")
    }

    fun sumar(a: Double, b: Double): Double {
        return a + b
    }

    fun restar(a: Double, b: Double): Double {
        return a - b
    }

    fun multiplicar(a: Double, b: Double): Double {
        return a * b
    }

    fun dividir(a: Double, b: Double): Double {
        return if (b != 0.0) {
            a / b
        } else {
            Double.NaN // No se puede dividir entre cerooooo
        }
    }
}