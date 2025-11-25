package com.example.fusion0_lottery;

public class LotteryWinners {
    private String name;
    public String joinDate;

    private String status;

    public LotteryWinners() {}

    public LotteryWinners(String name) {
        this.name = name;
        this.status = "Pending";
    }

    public LotteryWinners(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
