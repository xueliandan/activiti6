package com.example.boot_activiti6.event.entity;

/**
 * @author zxb 2023/9/22 15:05
 */
public class CakeEvent {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Cake{" +
                "name='" + name + '\'' +
                '}';
    }
}
