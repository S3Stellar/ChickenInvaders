package com.naorfarag.chickeninvaders;

public class PlayerNickScore implements Comparable<PlayerNickScore> {
    public int score;
    public String nickname;

    public PlayerNickScore(int intValue, String stringValue) {
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
    public int compareTo(PlayerNickScore o) {
        return Integer.compare(o.score, this.score);
    }
}
