package com.lefa.thearchive

import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

@Composable
fun MeantimeQuizScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var questionIndex by remember { mutableStateOf(0) }
    var backgroundColor by remember { mutableStateOf(RoseBackground) }

    // Alert State
    var showAggressiveAlert by remember { mutableStateOf(false) }
    var showLoveAlert by remember { mutableStateOf(false) } // "We know the answer"
    var showSentAlert by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Question Data
    val questions = listOf(
        "What does D-TECH stand for?",
        "What did D-TECH previously stand for?",
        "What are my names and surname?", // 3 fields
        "Solve for x: x² = -1",
        "How old am I (Years & Months)?", // Born June 2005
        "How many underwear do I have?",
        "When was the last time I saw your boobs?",
        "Do you love me?",
        "Would you like to exchange pics of our parts?",
        "Final Message"
    )

    // Inputs
    var input1 by remember { mutableStateOf("") }
    var input2 by remember { mutableStateOf("") }
    var input3 by remember { mutableStateOf("") }

    // Logic for Q8 (Love)
    var attemptsQ8 by remember { mutableStateOf(0) }

    // Background Color Animation
    val animatedBgColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 1000)
    )

    fun resetInputs() {
        input1 = ""
        input2 = ""
        input3 = ""
    }

    fun nextQuestion() {
        if (questionIndex < questions.size - 1) {
            questionIndex++
            resetInputs()
        } else {
            onFinished()
        }
    }

    // Check Logic
    fun checkAnswer() {
        val q = questionIndex
        val i1 = input1.trim().lowercase()
        val i2 = input2.trim().lowercase()
        val i3 = input3.trim().lowercase()

        when (q) {
            0 -> { // D-TECH
                if (i1.contains("dynamic") && (i1.contains("technology") || i1.contains("tech"))) {
                    nextQuestion()
                } else {
                    Toast.makeText(context, "Try again, baby.", Toast.LENGTH_SHORT).show()
                }
            }
            1 -> { // Old D-TECH
                if (i1.contains("destroyers") && (i1.contains("technology") || i1.contains("tech"))) {
                    nextQuestion()
                } else {
                    Toast.makeText(context, "Think harder...", Toast.LENGTH_SHORT).show()
                }
            }
            2 -> { // Names
                val combined = "$i1 $i2 $i3"
                if (combined.contains("jonas") && combined.contains("seakwa") && combined.contains("mochebane")) {
                    nextQuestion()
                } else {
                    Toast.makeText(context, "You don't know my name? 🥺", Toast.LENGTH_SHORT).show()
                }
            }
            3 -> { // Math x^2 = -1
                if (i1 == "i" || i1.contains("imaginary")) {
                    nextQuestion()
                } else {
                    Toast.makeText(context, "It's complex... but simple.", Toast.LENGTH_SHORT).show()
                }
            }
            4 -> { // Age (June 2005)
                val cal = Calendar.getInstance()
                val nowYear = cal.get(Calendar.YEAR)
                val nowMonth = cal.get(Calendar.MONTH) // 0-based

                var diffYear = nowYear - 2005
                var diffMonth = nowMonth - 5 // June is 5

                if (diffMonth < 0) {
                    diffYear--
                    diffMonth += 12
                }

                // Flexible check: User might write "20 years 6 months" or just "20 6"
                // Let's look for the numbers
                val hasYear = i1.contains(diffYear.toString())
                val hasMonth = i1.contains(diffMonth.toString())

                if (hasYear && hasMonth) {
                    nextQuestion()
                } else {
                     Toast.makeText(context, "I'm $diffYear years and $diffMonth months old!", Toast.LENGTH_SHORT).show()
                }
            }
            5 -> { // Underwear
                if (i1 == "6") {
                    nextQuestion()
                } else {
                    Toast.makeText(context, "Count again... ;)", Toast.LENGTH_SHORT).show()
                }
            }
            6 -> { // Boobs
                // Trigger Aggressive Alert
                showAggressiveAlert = true
                backgroundColor = Color.Red // Immediate Red

                // Handler to calm down
                Handler(Looper.getMainLooper()).postDelayed({
                   showAggressiveAlert = false
                   backgroundColor = Color(0xFF90EE90) // Light Green

                   Handler(Looper.getMainLooper()).postDelayed({
                       nextQuestion()
                       backgroundColor = RoseBackground // Reset for next
                   }, 2000)

                }, 5000)
            }
            7 -> { // Love (Logic handled in UI input mostly, but button can force next if manually typed 'no' somehow)
                 // If she somehow bypasses logic, we just move on
                 nextQuestion()
            }
            8 -> { // Pics
                 // Any key input triggers logic, but check button acts as "Yes"
                 showSentAlert = true
                 Handler(Looper.getMainLooper()).postDelayed({
                     showSentAlert = false
                     nextQuestion()
                 }, 4000)
            }
            9 -> { // Finale
                onFinished()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBgColor)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (questionIndex < 10) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Progress
                LinearProgressIndicator(
                    progress = (questionIndex + 1) / 10f,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = DeepLove,
                    trackColor = Color.White
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Question Text
                Text(
                    text = questions[questionIndex],
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (backgroundColor == Color.Red) Color.White else TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                // SPECIAL INPUTS
                when (questionIndex) {
                    2 -> { // Names - 3 Inputs
                        OutlinedTextField(value = input1, onValueChange = { input1 = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = input2, onValueChange = { input2 = it }, label = { Text("Middle Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = input3, onValueChange = { input3 = it }, label = { Text("Surname") }, modifier = Modifier.fillMaxWidth())
                    }
                    8 -> { // Pics - "Any key triggers Yes"
                        // Actually user said "on any key inout just type yes"
                        // We simulate this by ignoring what she types and appending "Yes" chars or just setting it.
                        OutlinedTextField(
                            value = input1,
                            onValueChange = {
                                input1 = "Yes"
                                showSentAlert = true
                                focusManager.clearFocus()
                                Handler(Looper.getMainLooper()).postDelayed({
                                     showSentAlert = false
                                     nextQuestion()
                                }, 4000)
                            },
                            label = { Text("Type anything...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    7 -> { // Love - Restricted Input "n" then "o"
                        OutlinedTextField(
                            value = input1,
                            onValueChange = { newValue ->
                                val oldValue = input1

                                // Detect Deletion or clear
                                if (newValue.length < oldValue.length) {
                                    attemptsQ8++
                                    input1 = "" // Clear it on delete
                                } else {
                                    // Typing
                                    if (newValue.isNotEmpty()) {
                                        if (input1.isEmpty()) input1 = "n"
                                        else if (input1 == "n") input1 = "no"
                                        // If already "no", do nothing (ignore)
                                    }
                                }

                                if (attemptsQ8 >= 2) {
                                    showLoveAlert = true
                                    focusManager.clearFocus()
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        showLoveAlert = false
                                        nextQuestion()
                                    }, 2500)
                                }
                            },
                            label = { Text("Answer...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    9 -> { // Final Message
                         Text(
                             "WELL AM OUT OF QUESTIONS..AT THIS POINT I CAN ONLY TELL YOU HOW MUCH I LOVE YOU .. 🌹.. BUT AM ALSO OUT OF IDEAS ON HOW TO DO THAT ..😪.. ASKIES ..BUT I DO LOVE YOU ..A LOT ..N I HOPE YOU GET TO BE HAPPY SOON(GO Back home I mean) .HAPPY NEW YEAR BABY",
                             textAlign = TextAlign.Center,
                             lineHeight = 24.sp,
                             fontSize = 16.sp
                         )
                    }
                    else -> {
                        OutlinedTextField(
                            value = input1,
                            onValueChange = { input1 = it },
                            label = { Text("Your Answer") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { checkAnswer() })
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Button (Hide for special auto-advance qs if needed, but keeping for safety)
                if (questionIndex != 9 && questionIndex != 8) {
                    Button(
                        onClick = { checkAnswer() },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepLove),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Check Answer")
                    }
                } else if (questionIndex == 9) {
                     Button(
                        onClick = { onFinished() },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepLove),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finish")
                    }
                }
            }
        }
    }

    // ALERTS

    if (showAggressiveAlert) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("NOO!", color = Color.Red, fontSize = 40.sp, fontWeight = FontWeight.Bold) },
            text = { Text("THE ANSWER IS \"A VERY VERY LONG TIME AGO\"", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            confirmButton = {},
            containerColor = Color.White
        )
    }

    if (showLoveAlert) {
        AlertDialog(
             onDismissRequest = {},
             text = { Text("It's okay, we know the answer... ❤️") },
             confirmButton = {},
             containerColor = PureWhite
        )
    }

    if (showSentAlert) {
        AlertDialog(
             onDismissRequest = {},
             text = { Text("Sent to Lefa") },
             confirmButton = {},
             containerColor = PureWhite
        )
    }
}
