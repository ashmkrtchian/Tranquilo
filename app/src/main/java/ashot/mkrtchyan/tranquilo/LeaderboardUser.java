package ashot.mkrtchyan.tranquilo;

public class LeaderboardUser {

    private final String uid;
    private final String name;
    private final long   calmCoins;
    private final String avatarUrl;
    private final int    rank;

    public LeaderboardUser(String uid, String name, long calmCoins, String avatarUrl, int rank) {
        this.uid       = uid;
        this.name      = name;
        this.calmCoins = calmCoins;
        this.avatarUrl = avatarUrl;
        this.rank      = rank;
    }

    public String getUid()        { return uid; }
    public String getName()       { return name; }
    public long   getCalmCoins()  { return calmCoins; }
    public String getAvatarUrl()  { return avatarUrl; }
    public int    getRank()       { return rank; }
}