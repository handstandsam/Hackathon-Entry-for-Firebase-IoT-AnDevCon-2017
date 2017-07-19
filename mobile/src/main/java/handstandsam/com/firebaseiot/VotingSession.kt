package handstandsam.com.firebaseiot

data class VotingSession(val timestamp: Long) {
    val votes: MutableMap<String, Boolean> = mutableMapOf()
}
