package com.datascience;




import java.util.ArrayList;
import java.util.List;

public class DataMart {
    private final List<NewsEvent> newsEvents = new ArrayList<>();
    private final List<VideoEvent> videoEvents = new ArrayList<>();

    public DataMart() {

    }

    public void addEvent(Object event) {
        if (event instanceof NewsEvent) {
            newsEvents.add((NewsEvent) event);
        } else if (event instanceof VideoEvent) {
            videoEvents.add((VideoEvent) event);
        }
    }


    public List<NewsEvent> getNewsEvents() {
        return newsEvents;
    }

    public List<VideoEvent> getVideoEvents() {
        return videoEvents;
    }
}
