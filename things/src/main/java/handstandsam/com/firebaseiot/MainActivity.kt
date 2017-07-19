package handstandsam.com.firebaseiot

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.things.contrib.driver.apa102.Apa102
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.firebase.database.*
import java.io.IOException
import java.util.HashMap

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity() {

    var dbRefSessions: DatabaseReference? = null
    var dbRefVotes: DatabaseReference? = null

    private val TAG = "MainActivityJava"
    private val LEDSTRIP_BRIGHTNESS = 1
    private val NUMBER_OF_LEDS = 7
    private val NUMBER_OF_LOTTIE_SLIDES = 5

    private val SLIDE_DURATION = 20000
    internal var btnVoteUp: Button? = null
    internal var btnVoteDown:Button? = null
    internal var txtVoteUpCount: TextView? = null
    internal var txtVoteDownCount:TextView? = null

    private var voteUpCount: Int = 0
    private var voteDownCount: Int = 0
    private var mDisplay: AlphanumericDisplay? = null

    private var mLedstrip: Apa102? = null

    private var currentLottieIndex: Int = 0

    private var lottieSwitchHandler: Handler? = null

    private val startNextLottieRunnable = Runnable {
        //TODO: tell firebase to switch to new session

        currentLottieIndex++
        runOnUiThread { loadNextLottieSlide() }
    }

    private var lottieAnimationView: LottieAnimationView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        voteUpCount = 0
        voteDownCount = 0

        lottieSwitchHandler = Handler()

        currentLottieIndex = 0
        lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottieView)


        initializeHAT()

        startLottieSlides()

//        dbRefSessions = FirebaseDatabase.getInstance().getReference("VotingSessions")
//        dbRefVotes = FirebaseDatabase.getInstance().getReference("Votes")
//
//        createNewVotingSession()

//        dbRefVotes?.addValueEventListener(object : ValueEventListener {
//            @SuppressLint("WrongViewCast")
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
////                val value = dataSnapshot.getValue(List::class.java) as List<UserVote>
//
////                findViewById<TextView>(R.id.voting_session_title).text = value
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//            }
//        })

        renderVotes()
    }

    @Synchronized
    private fun updateResult() {
//        dbRefVotes?.database


    }

    private fun createNewVotingSession() {
        dbRefVotes?.removeValue()
        dbRefSessions?.setValue(System.currentTimeMillis())
    }

    private fun startLottieSlides() {
        lottieSwitchHandler?.post(object : Runnable {
            override fun run() {
                runOnUiThread {
                    currentLottieIndex++
                    loadNextLottieSlide()
                }
                lottieSwitchHandler?.postDelayed(this, SLIDE_DURATION.toLong())
            }
        })

    }

    fun renderVotes(){
        dbRefSessions = FirebaseDatabase.getInstance().getReference("VotingSessions")
        dbRefVotes = FirebaseDatabase.getInstance().getReference("Votes")
//        myVoteRef = dbRefVotes?.push()

//        findViewById<Button>(R.id.yes_button).setOnClickListener {
//            setVoteValue(true)
//        }
//        findViewById<Button>(R.id.no_button).setOnClickListener {
//            setVoteValue(false)
//        }
//
//        dbRefSessions?.addValueEventListener(object : ValueEventListener {
//            @SuppressLint("WrongViewCast")
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val value: Long? = dataSnapshot.getValue(Long::class.java)
//                findViewById<TextView>(R.id.voting_session_title).text = value.toString()
//                setVoteValue(null)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//            }
//        })


//        findViewById<Button>(R.id.next_round).setOnClickListener {
//            //Faking server code
//            dbRefVotes?.removeValue()
//            dbRefSessions?.setValue(System.currentTimeMillis())
//        }


        dbRefVotes?.addValueEventListener(object : ValueEventListener {


            @SuppressLint("WrongViewCast")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var yesCount: Int = 0
                var noCount: Int = 0

                dataSnapshot.children.forEach {
                    Log.w("TEST", "Item $it + ${it.javaClass.name}")
                    Log.w("TEST", "Item ${it.value} ${it.value}")
                    val hashMap: HashMap<*, *> = (it.value as HashMap<*, *>)
                    Log.w("TEST", "HashMap ${hashMap}")
                    Log.w("TEST", "HashMap Value ${hashMap["vote"]}")

                    val votedYes: Boolean? = hashMap["vote"] as Boolean?

                    when (votedYes) {
                        true -> yesCount++
                        false -> noCount++
                    }
                }
                //Loop over
                Log.w("TEST",  "Yes $yesCount & No $noCount")

                if(yesCount != 0 && noCount!= 0) {
                    updateVoteRainbow(yesCount, noCount)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })

    }

    private fun initializeHAT() {
        // SPI ledstrip
        try {
            mLedstrip = Apa102(BoardDefaults.getSpiBus(), Apa102.Mode.BGR)
            mLedstrip?.setBrightness(LEDSTRIP_BRIGHTNESS)

            //this was added to put the green on the left, red on the rightr
            mLedstrip?.setDirection(Apa102.Direction.REVERSED)
            clearRainbow()
        } catch (e: IOException) {
            Log.d(TAG, "onCreate: ", e)
            mLedstrip = null // Led strip is optional.
        }

        // Display
        try {
            mDisplay = AlphanumericDisplay(BoardDefaults.getI2cBus())
            mDisplay?.setEnabled(true)
            resetDisplay()
            Log.d(TAG, "Initialized I2C Display")
        } catch (e: IOException) {
            Log.e(TAG, "Error initializing display", e)
            Log.d(TAG, "Display disabled")
            mDisplay = null
        }

    }

    private fun loadNextLottieSlide() {
        resetDisplay()
        clearRainbow()
        if (currentLottieIndex >= NUMBER_OF_LOTTIE_SLIDES) {
            currentLottieIndex = 0
        }
        val lottieJsonName = "lottie_$currentLottieIndex.json"
        val animationView = findViewById<LottieAnimationView>(R.id.lottieView)
        animationView.setAnimation(lottieJsonName)
        animationView.loop(true)
        animationView.playAnimation()
    }

    private fun updateVoteRainbow(voteUps: Int, voteDowns: Int) {
        val voteUpPercent = voteUps.toFloat() / (voteUps + voteDowns)
        val voteDownPercent = voteDowns.toFloat() / (voteUps + voteDowns)

        val firstTwoDigits = String.format("%02d", voteUps)
        val lastTwoDigits = String.format("%02d", voteDowns)
        val output = firstTwoDigits + lastTwoDigits

        if (mDisplay != null) {
            try {
                mDisplay?.display(output)
            } catch (ioe: IOException) {
                Log.d(TAG, "updateVoteRainbow: ", ioe)
            }

        }

        //        //debugging purposes only
        //        updateVoteUpTxt(voteUpCount, voteUpPercent);
        //        updateVoteDownTxt(voteDownCount, voteDownPercent);

        val colors = IntArray(NUMBER_OF_LEDS)
        for (i in colors.indices) {
            if (voteUpPercent > (i + 1).toFloat() / 7.toFloat()) {
                colors[i] = Color.GREEN
            } else if (voteDownPercent > (7 - (i + 1)).toFloat() / 7) {
                colors[i] = Color.RED
            } else {
                colors[i] = Color.YELLOW
            }
        }

        try {
            mLedstrip?.write(colors)
        } catch (e: IOException) {
            Log.e(TAG, "Error setting ledstrip", e)
        }

        //for some reason, it might not flush(?) after the first write
        try {
            mLedstrip?.write(colors)
        } catch (e: IOException) {
            Log.e(TAG, "Error setting ledstrip", e)
        }

    }

    private fun clearRainbow() {
        val emptyRainbow = intArrayOf(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK)
        try {
            mLedstrip?.write(emptyRainbow)
        } catch (e: IOException) {
            Log.e(TAG, "Error setting ledstrip", e)
        }

        try {
            mLedstrip?.write(emptyRainbow)
        } catch (e: IOException) {
            Log.e(TAG, "Error setting ledstrip", e)
        }

    }

    private fun resetDisplay() {
        if (mDisplay != null) {
            try {
                mDisplay?.display("0000")
            } catch (ioe: IOException) {
                Log.d(TAG, "updateVoteRainbow: ", ioe)
            }

        }
    }

    //For debugging purposes
    private fun updateVoteUpTxt(count: Int, percent: Float) {
        txtVoteUpCount?.setText("Count:$count %:$percent")
    }

    //For debugging purposes
    private fun updateVoteDownTxt(count: Int, percent: Float) {
        txtVoteDownCount?.setText("Count:$count %:$percent")
    }

}
