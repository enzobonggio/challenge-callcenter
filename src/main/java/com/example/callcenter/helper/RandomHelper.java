package com.example.callcenter.helper;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class RandomHelper {

    private final Random random = new Random();

    public int nextInt(int int1, int int2) {
        return random.nextInt(int2 - int1) + int1;
    }
}
