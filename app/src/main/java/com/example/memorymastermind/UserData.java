package com.example.memorymastermind;

import java.io.Serializable;

public class UserData implements Serializable {
    private String correctGuesses;
    private String timestamp;
    private String username;

    public UserData(){

    }
    public UserData(String correctGuesses,String username) {
        this.correctGuesses = correctGuesses;
        this.username = username;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public UserData(String correctGuesses, String username, String timestamp) {
        this.correctGuesses = correctGuesses;
        this.username = username;
        this.timestamp = timestamp;
    }

    public String getCorrectGuesses() {
        return correctGuesses;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setCorrectGuesses(String correctGuesses) {
        this.correctGuesses = correctGuesses;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
