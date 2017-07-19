package handstandsam.com.firebaseiot

import android.app.Activity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference

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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

    @Synchronized
    private fun updateResult() {
//        dbRefVotes?.database


    }

    private fun createNewVotingSession() {
        dbRefVotes?.removeValue()
        dbRefSessions?.setValue(System.currentTimeMillis())
    }
}
