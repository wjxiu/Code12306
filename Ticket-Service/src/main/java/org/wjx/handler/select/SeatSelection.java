package org.wjx.handler.select;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author xiu
 * @create 2023-11-30 14:59
 */
@Slf4j
public class SeatSelection {
    public static void main(String[] args) {
        int[][] res = {
                {0, 0, 0, 1, 1, 1, 1, 0},
                {0, 1, 0, 0, 0, 0, 1, 1},
                {0, 1, 0, 0, 0, 0, 1, 1},
                {0, 1, 0, 0, 0, 0, 1, 1},
        };
        int num = 3;
        int[][] adjacent = adjacent(res, num);
        System.out.println(Arrays.deepToString(adjacent));
    }

    /**
     * 根据人数在给定座位二维分布图(0,未选;1已选)的选中的相邻座位的坐标 类似 [[1,2],[2,1]]
     *
     * @param seatLayout 座位二维分布图(0,未选;1已选)
     * @param num        人数
     * @return 如果有足够的相邻座位 返回坐标,否则返回null
     */
    public static int[][] adjacent(int[][] seatLayout, int num) {
        int[] seats = findSeats(seatLayout, num);

        if (seats == null) return null;
        return convert(seats, num, seatLayout[0].length);
    }

    /**
     * 根据人数在给定座位二维分布图(0,未选;1已选)的选中的不相邻座位的坐标 类似 [[1,2],[2,1]]
     *
     * @param seatLayout 座位二维分布图(0,未选;1已选)
     * @param numSeats   人数
     * @return 如果有足够的座位 返回坐标,否则返回空数组
     */
    public static int[][] nonAdjacent(int[][] seatLayout, int numSeats) {
        int numRows = seatLayout.length;
        int numCols = seatLayout[0].length;
        List<int[]> selectedSeats = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (seatLayout[i][j] == 0) {
                    selectedSeats.add(new int[]{i, j});
                    if (selectedSeats.size() == numSeats) break;
                }
            }
            if (selectedSeats.size() == numSeats) break;
        }
        return convertToActualSeat(selectedSeats);
    }

    private static int[][] convertToActualSeat(List<int[]> selectedSeats) {
        int[][] actualSeat = new int[selectedSeats.size()][2];
        for (int i = 0; i < selectedSeats.size(); i++) {
            int[] seat = selectedSeats.get(i);
            int row = seat[0] + 1;
            int col = seat[1] + 1;
            actualSeat[i][0] = row;
            actualSeat[i][1] = col;
        }
        return actualSeat;
    }


    private static int[][] convert(int[] res, int num, int col) {
        int[][] res1 = new int[num][2];
        int x = res[0];
        int y = res[1];
        int temp = x * col + y;
        for (int i = 0; i < num; i++) {
            res1[i] = new int[]{temp / col, temp % col};
            temp++;
        }
        return res1;
    }

    //    private static int[] findSeats(int[][] ints, int num) {
//        int count = num + 1;
//        int[] prefixsum = new int[ints.length * ints[0].length + 1];
//        prefixsum[0] = ints[0][0];
//        int pcount = 1;
//        for (int i = 0; i < ints.length; i++) {
//            for (int j = 0; j < ints[i].length; j++) {
//                if (i==0&&j==0)continue;
//                prefixsum[pcount] = prefixsum[pcount - 1] + ints[i][j];
//                pcount++;
//            }
//        }
//        int currentNumber = prefixsum[0];
//        int currentCount = 1;
//        for (int i = 0; i < prefixsum.length; i++) {
//            if (currentCount == count)
//                return new int[]{(i - count + 1) / ints[0].length, (i - count + 1) % ints[0].length};
//            if (prefixsum[i] == currentNumber) currentCount++;
//            else {
//                currentCount = 1;
//                currentNumber = prefixsum[i];
//            }
//        }
//        return null;
//    }
    private static int[] findSeats(int[][] array, int targetCount) {
        for (int i = 0; i < array.length; i++) {
            int[] col = array[i];
            int count=0;
            for (int j = 0; j < col.length; j++) {
                if (col[j]==0)count++;
                if (col[j]==1)count=0;
                if (count==targetCount) return new int[]{i,j-targetCount+1};
            }
        }
        return null;
    }
}

class Main {
    public static void main(String[] args) {
        int[][] array = {
                {1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0},

        };
        int targetCount = 1;

        int[] resultIndex = findSeats(array, targetCount);
        System.out.println(Arrays.toString(resultIndex));

    }

    private static int[] findSeats(int[][] array, int targetCount) {
        for (int i = 0; i < array.length; i++) {
            int[] col = array[i];
            int count=0;
            for (int j = 0; j < col.length; j++) {
                if (col[j]==0)count++;
                if (col[j]==1)count=0;
                if (count==targetCount) return new int[]{i,j-targetCount+1};
            }
        }
        return null;
    }
}
