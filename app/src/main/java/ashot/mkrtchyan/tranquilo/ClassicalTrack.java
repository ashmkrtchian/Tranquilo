package ashot.mkrtchyan.tranquilo;

class ClassicalTrack {
    String title;
    String composer;
    String duration;
    String youtubeId;
    String category;

    ClassicalTrack(String title, String composer, String duration,
                   String youtubeId, String category) {
        this.title     = title;
        this.composer  = composer;
        this.duration  = duration;
        this.youtubeId = youtubeId;
        this.category  = category;
    }
}