package handstandsam.com.firebaseiot

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MainActivity : AppCompatActivity() {

    var newRef: DatabaseReference? = null
    val uniqueID = UUID.randomUUID()
    var vote: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Write a message to the database
        val dbRefSessions: DatabaseReference = FirebaseDatabase.getInstance().getReference("VotingSessions")
        dbRefSessions.setValue(System.currentTimeMillis())

        val dbRefVotes: DatabaseReference = FirebaseDatabase.getInstance().getReference("Votes")
        newRef = dbRefVotes.push()
        newRef?.setValue(UserVote(uniqueID.toString(), vote))

        findViewById<Button>(R.id.toggle).setOnClickListener {
            if (vote == null) {
                vote = true
            } else {
                vote = !vote!!
            }
            newRef?.setValue(UserVote(uniqueID.toString(), vote))
        }
    }
}
