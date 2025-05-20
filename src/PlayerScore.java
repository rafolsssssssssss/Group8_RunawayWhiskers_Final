public class PlayerScore {
    private final String username;
    private final int score;

    public PlayerScore(String username, int score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() { return username; }
    public int getScore() { return score; }
}
