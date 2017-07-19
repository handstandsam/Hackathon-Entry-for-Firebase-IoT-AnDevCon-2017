package handstandsam.com.firebaseiot

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var dbRefSessions: DatabaseReference? = null
    var dbRefVotes: DatabaseReference? = null
    var myVoteRef: DatabaseReference? = null

    val uniqueID = UUID.randomUUID()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbRefSessions = FirebaseDatabase.getInstance().getReference("VotingSessions")
        dbRefVotes = FirebaseDatabase.getInstance().getReference("Votes")
        myVoteRef = dbRefVotes?.push()

        findViewById<Button>(R.id.yes_button).setOnClickListener {
            myVoteRef?.setValue(UserVote(uniqueID.toString(), true))
        }
        findViewById<Button>(R.id.no_button).setOnClickListener {
            myVoteRef?.setValue(UserVote(uniqueID.toString(), false))
        }

        dbRefSessions?.addValueEventListener(object : ValueEventListener {
            @SuppressLint("WrongViewCast")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value: Long? = dataSnapshot.getValue(Long::class.java)
                findViewById<TextView>(R.id.voting_session_title).text = value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })



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
                val scoreText = "Yes $yesCount & No $noCount"
                findViewById<TextView>(R.id.score).setText(scoreText)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })

    }
}