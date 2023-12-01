package org.wjx.service.Impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author xiu
 * @create 2023-11-30 12:21
 */
public class test {
    public static void main(String[] args) {
        int[][] res = {
                {0, 0, 0, 1, 1, 1, 1, 0},
                {0, 1, 0, 0, 0, 0, 1, 1},
                {0, 1, 0, 0, 0, 0, 1, 1},
                {0, 1, 0, 0, 0, 0, 1, 1},
        };
        System.out.println(Arrays.stream(res).flatMapToInt(Arrays::stream).filter(i -> i == 1).count());
        int num=3;
        int[] seats = findSeats(res, num);
        System.out.println(Arrays.toString(seats));

    }

    private static int[] findSeats(int[][] ints, int num) {
        int count = num + 1;
        int[] prefixsum = new int[ints.length * ints[0].length + 1];
        prefixsum[0] = ints[0][0];
        int pcount = 1;
        for (int i = 0; i < ints.length; i++) {
            for (int j = 0; j < ints[i].length; j++) {
                prefixsum[pcount] = prefixsum[pcount - 1] + ints[i][j];
                pcount++;
            }
        }
        int currentNumber = prefixsum[0];
        int currentCount = 1;
        for (int i = 0; i < prefixsum.length; i++) {
            if (currentCount == count)
                return new int[]{(i - count + 1) / ints[0].length, (i - count + 1) % ints[0].length};
            if (prefixsum[i] == currentNumber) currentCount++;
            else {
                currentCount = 1;
                currentNumber = prefixsum[i];
            }
        }
        return new int[]{-1, -1};
    }
}
