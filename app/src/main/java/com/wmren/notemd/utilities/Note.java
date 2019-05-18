package com.wmren.notemd.utilities;

public class Note {

    private String title;
    private String content;
    private String summary;
    private String date;
    private String id;

    public Note(String title, String content, String date, String id) {
        this.title = title;
        this.content = content;
        generateSummary();
        this.date = date;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSummary() {
        return summary;
    }

    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    private void generateSummary() {
        if (content.length() < 100) {
            summary = content;
        } else {
            summary = content.substring(0, 100);
        }
    }
}
