package ashot.mkrtchyan.tranquilo;

class ClassicalTrack {
    String id;
    String title;
    String composer;
    String duration;
    String audioUrl;
    boolean isFavorite;

    ClassicalTrack(String id, String title, String composer, String audioUrl) {
        this.id         = id;
        this.title      = title;
        this.composer   = composer;
        this.audioUrl   = audioUrl;
        this.duration   = "--:--";
        this.isFavorite = false;
    }
}