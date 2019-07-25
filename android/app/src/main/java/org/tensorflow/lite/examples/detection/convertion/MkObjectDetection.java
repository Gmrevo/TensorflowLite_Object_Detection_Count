package org.tensorflow.lite.examples.detection.convertion;


import android.graphics.RectF;
import android.util.Log;

import org.tensorflow.lite.examples.detection.tflite.Classifier.Recognition;

import java.util.ArrayList;
import java.util.List;

public class MkObjectDetection {

    int totalframes =0,countNumber=0,emptyFrames=0;
    int state =0;
    int width=480,height=640;
    int py=310;
    int pyIgnore=500;
    int preState=0;
    boolean flag = false;
    Float min_score_thresh = 0.0f;
    ArrayList<Integer> boxposition = new ArrayList<>();



  //  Classifier.Recognition recognition = new Classifier.Recognition();

    public int detectProductCount(List<Recognition> inputs)

    {
       totalframes = totalframes+1;

      if (!inputs.isEmpty())
      {
          Recognition box = inputs.get(0);

          RectF blocation = box.getLocation();
           min_score_thresh = box.getConfidence();

          Log.d("mmk","left position::"+blocation.left);

          if(blocation.left>0 && blocation.left<=py)
          {
              state =1;
              flag = true;
              if (boxposition.isEmpty())
              {
                  boxposition.add(state);
              }else {
                  preState = boxposition.get(boxposition.size()-1);
                  if (preState!= 1)
                      boxposition.add(1);
              }
          }
          if (blocation.left> py)
          {
              state = 2;
              if (boxposition.isEmpty())
              {
                  boxposition.add(state);
              }else {
                 preState= boxposition.get(boxposition.size()-1);
                  if (preState != 2)
                      boxposition.add(state);
              }
          }
          Log.d("mmk","state::"+state+"::: boxposition::"+boxposition);

      }else {
          emptyFrames = emptyFrames+1;
      }

        if (emptyFrames>5)
        {
            if (!boxposition.isEmpty() && boxposition.size()>1 )
            {
                if (boxposition.get(boxposition.size()-1)==2 && boxposition.get(boxposition.size()-2)==1)
                {
                    countNumber = countNumber+1;

                }else if (boxposition.get(boxposition.size()-1)==1 && boxposition.get(boxposition.size()-2)==2){
                    if (countNumber !=0)
                        countNumber = countNumber-1;
                }


            }
            boxposition.clear();
            emptyFrames =0;

        }
        Log.d("mmk","empty frames::"+emptyFrames);
        return  countNumber;
    }
}

//boxposition.get(boxposition.size()-1)==2 && boxposition.get(boxposition.size()-2)==1