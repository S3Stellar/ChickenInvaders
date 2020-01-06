package com.naorfarag.chickeninvaders;

public class PlayerAttributes implements Comparable<PlayerAttributes> {
    private int score;
    private String nickname;
    private double latitude;
    private double longitude;

    public PlayerAttributes(int intValue, String stringValue, double longitude, double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.score = intValue;
        this.nickname = stringValue;
    }

    public String toString() {
        return "(" + this.score + ", " + this.nickname + ")";
    }

    public int getScore() {
        return score;
    }

    public String getNickname() {
        return nickname;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public int compareTo(PlayerAttributes o) {
        return Integer.compare(o.score, this.score);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
