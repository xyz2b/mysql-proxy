package org.xyz.proxy.util;

import java.util.Random;

public class RandomUtil {
    private static Random random = new Random();

    public static int randomInt(int start, int end) {
        return random.nextInt(end) + start;
    }
}
