package com.example.calculadora

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var currentExpression = ""
    private var resultDisplayed = false

    private lateinit var labelResultado: TextView
    private lateinit var resultadoFinal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TextViews
        labelResultado = findViewById(R.id.labelResultado)
        resultadoFinal = findViewById(R.id.resultadoFinal)

        // Number buttons
        findViewById<Button>(R.id.button0).setOnClickListener { appendToExpression("0") }
        findViewById<Button>(R.id.button1).setOnClickListener { appendToExpression("1") }
        findViewById<Button>(R.id.button2).setOnClickListener { appendToExpression("2") }
        findViewById<Button>(R.id.button3).setOnClickListener { appendToExpression("3") }
        findViewById<Button>(R.id.button4).setOnClickListener { appendToExpression("4") }
        findViewById<Button>(R.id.button5).setOnClickListener { appendToExpression("5") }
        findViewById<Button>(R.id.button6).setOnClickListener { appendToExpression("6") }
        findViewById<Button>(R.id.button7).setOnClickListener { appendToExpression("7") }
        findViewById<Button>(R.id.button8).setOnClickListener { appendToExpression("8") }
        findViewById<Button>(R.id.button9).setOnClickListener { appendToExpression("9") }

        // Operation buttons
        findViewById<Button>(R.id.buttonMas).setOnClickListener { appendToExpression("+") }
        findViewById<Button>(R.id.buttonMenos).setOnClickListener { appendToExpression("-") }
        findViewById<Button>(R.id.buttonMulti).setOnClickListener { appendToExpression("*") }
        findViewById<Button>(R.id.buttonDividir).setOnClickListener { appendToExpression("/") }
        findViewById<Button>(R.id.buttonComa).setOnClickListener { appendToExpression(".") }
        findViewById<Button>(R.id.buttonSEN).setOnClickListener { appendToExpression("sin(") }
        findViewById<Button>(R.id.buttonCOS).setOnClickListener { appendToExpression("cos(") }
        findViewById<Button>(R.id.buttonTAN).setOnClickListener { appendToExpression("tan(") }

        // Clear button
        findViewById<Button>(R.id.buttonC).setOnClickListener {
            currentExpression = ""
            labelResultado.text = ""
            resultadoFinal.text = ""
        }

        // Equals button
        findViewById<Button>(R.id.buttonIgual).setOnClickListener {
            try {
                val result = evaluateExpression(currentExpression)
                resultadoFinal.text = result.toString()
            } catch (e: Exception) {
                resultadoFinal.text = "ERROR"
            }
        }
    }

    private fun appendToExpression(string: String) {
        if (resultDisplayed) {
            currentExpression = ""
            resultDisplayed = false
        }
        currentExpression += string
        labelResultado.text = currentExpression
    }

    private fun evaluateExpression(expression: String): Double {
        val sanitizedExpression = expression.replace("sin", "Math.sin")
            .replace("cos", "Math.cos")
            .replace("tan", "Math.tan")
        return eval(sanitizedExpression)
    }

    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) x /= parseFactor()
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                    while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else if (ch in 'a'.code..'z'.code) {
                    while (ch in 'a'.code..'z'.code) nextChar()
                    val func = expression.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> Math.sqrt(x)
                        "sin" -> Math.sin(Math.toRadians(x))
                        "cos" -> Math.cos(Math.toRadians(x))
                        "tan" -> Math.tan(Math.toRadians(x))
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = Math.pow(x, parseFactor())

                return x
            }
        }.parse()
    }
}
