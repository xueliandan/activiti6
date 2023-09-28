package com.example.boot_activiti6.event.entity;

/**
 * @author zxb 2023/9/26 8:53
 */
public class AppleEvent {
    private String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "AppleEvent{" +
                "color='" + color + '\'' +
                '}';
    }
}
