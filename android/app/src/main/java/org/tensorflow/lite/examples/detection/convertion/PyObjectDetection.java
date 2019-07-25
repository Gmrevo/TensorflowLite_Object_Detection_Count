package org.tensorflow.lite.examples.detection.convertion;

import android.util.Log;

import java.util.ArrayList;

public class PyObjectDetection {

    int count_Num, plus_bit, minus_bit, file_no, capPos, space_cnt1, space_cnt2, state, y0, y1, w0;
    float py;
    ArrayList<Integer> myBoxes;
    ArrayList<Float> myPoints;

  public  PyObjectDetection() {
        count_Num = 0;
        plus_bit = 0;
        minus_bit = 0;
        file_no = 154;
        capPos = 0;
        space_cnt1 = 0;
        space_cnt2 = 0;
        y0 = 480 / 5;  // whate ever may be height/5 of frame
        y1 = 480;
        w0 = 640;      // what ever may be width of frame
        myBoxes = new ArrayList<>();
        myPoints = new ArrayList<>();
    }


    public int detecteCount(ArrayList<Box> inputslist) {

        space_cnt1 = 0;
        space_cnt2 = 0;
        count_Num = 0;
        state = 0;
        py = 0;
        myBoxes.clear();
        myPoints.clear();

        if (inputslist.size() !=0) {

            Box box = inputslist.get(0);

            space_cnt1 = 0;

            py = box.getY() + (box.getH() / 2);

            myPoints.add(py);
            Log.d("mmk","py value::"+py+"  and mypoints array:"+myPoints);

            if (py < y0) {
                state = 1;
                if (myBoxes.isEmpty()) {
                    myBoxes.add(state);
                } else {
                    int pre_state = myBoxes.get(myBoxes.size() - 1);
                    if (state != pre_state) {
                        myBoxes.add(state);
                    }
                }

            } else if (y0 < py && py < y1) {
                state = 2;
                if (myBoxes.isEmpty()) {
                    myBoxes.add(state);
                } else {
                    int pre_state = myBoxes.get(myBoxes.size() - 1);
                    if (state != pre_state)
                        myBoxes.add(state);
                }
            }
        } else {
            space_cnt1 = space_cnt1 + 1;
        }

        if (space_cnt1 > 40) {
            space_cnt1 = 0;
            int x = 5;
            float avg = 0;

            for (float y : myPoints) {
                avg = avg + y;
                //  cv2.circle(self.frame, (x, int(y)), 3, (0, 0, 255), -1)
                x = x + 5;
                if (x > w0) {
                    x = 5;
                }
            }
            myBoxes.clear();

            if (myPoints.size() > 10) {
                avg = (avg / myPoints.size()) - 15;
                float dy1 = myPoints.get(0);
                float dy2 = myPoints.get(myPoints.size() - 1);
                if (dy1 < avg)
                    myBoxes.add(1);
                else if (dy1 > avg)
                    myBoxes.add(2);
                if (dy2 < avg)
                    myBoxes.add(1);
                else if (dy2 > avg)
                    myBoxes.add(2);
             /*  if myDebug == 1:
               cv2.line(self.frame, (0, avg), (1024, avg), (255, 255, 0), 2)
               cv2.imshow('Debug', self.frame)
               */
            }

            if (myBoxes.size() >= 2) {
                int dy1 = myBoxes.get(0);
                int dy2 = myBoxes.get(myBoxes.size() - 1);
                int dir = dy2 - dy1;
                if (dir > 0)
                    this.count_Num = count_Num + 1;
                else if (dir < 0)
                    this.count_Num = count_Num - 1;
            }
            Log.d("mmk", "myBoxes array::" + myBoxes);
            myBoxes.clear();
            myPoints.clear();
        }


        Log.d("mmk", "count Number::" + count_Num);
        return count_Num;
    }


}
