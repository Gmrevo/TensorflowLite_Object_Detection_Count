package org.tensorflow.lite.examples.detection.convertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tensorflow.lite.examples.detection.env.Logger;

import android.os.Build;


import static java.lang.StrictMath.abs;
import static org.tensorflow.lite.examples.detection.convertion.State.In;
import static org.tensorflow.lite.examples.detection.convertion.State.Init;
import static org.tensorflow.lite.examples.detection.convertion.State.Mid;
import static org.tensorflow.lite.examples.detection.convertion.State.Out;


public class UnitTestCpp{
    int cartTop = 200;
    CProductCount productCount;

    List<Point> planeArray;

    public  UnitTestCpp(int imgheight, int imgwidth){

        productCount = new CProductCount();

        planeArray = new ArrayList<>();
        //Polygon([(0, 640), (0, cartTop), (360, cartTop), (360, 640)])
        planeArray.add(0,new Point(0,imgwidth));//640
        planeArray.add(1,new Point(0,cartTop));
        planeArray.add(2,new Point(imgheight,cartTop));
        planeArray.add(3,new Point(imgheight,imgwidth));//360
//india cart
         //planeArray.add(0,new Point(0,720));
        //planeArray.add(1,new Point(0,258));
        //planeArray.add(2,new Point(297,258));
        //planeArray.add(3,new Point(935,258));
        //planeArray.add(4,new Point(1280,258));
        //planeArray.add(5,new Point(1280,720));

    }




    public int processObject(ArrayList<detection> detections, int imgheight, int imgwidth, detection box){


        double min_score_thresh = 0.85;
        int height = imgheight;
        int width  = imgwidth;
        int maxind = -1;
        int PlaneDist = 130;
        double maxscore = -1.0;
        int d = 0;

        // # Cart Plane

      /*  Point []Plane = new Point[6];//
        for(int i = 0; i < Plane.length; i++) {
            Plane[i] = new Point();
        }
        Plane[0].x = 0;Plane[0].y = 720;
        Plane[1].x = 0;Plane[1].y = 258;
        Plane[2].x = 297;Plane[2].y = 258;
        Plane[3].x = 935;Plane[3].y = 258;
        Plane[4].x = 1280;Plane[4].y = 258;
        Plane[5].x = 1280;Plane[5].y = 720;


        */
        System.out.println("mmk Plane Array"+planeArray.toString());


        for (detection idx : detections) {

            final float confidence = idx.getConfidence();
            final int   centroidY =  (idx.getYmin() + (idx.getYmax()-idx.getYmin())/2);//*height;

            if(confidence > maxscore && confidence > min_score_thresh && (centroidY-cartTop) < PlaneDist)
            {
                maxind = d++;
                maxscore = confidence;
            }
        }

        if(maxind>-1)
        {
            productCount.resetInactiveCount();

            box = detections.get(maxind);
            int boxwidthfactor  = (int) (Math.abs(box.xmax - box.xmin)  * 0.3);//* width
            int boxheightfactor = (int) (Math.abs(box.ymax - box.ymin)  * 0.3);//* height

            int ymin = (box.ymin ) + boxheightfactor;//* height
            int xmin = (box.xmin )  + boxwidthfactor;// * width
            int ymax = (box.ymax ) - boxheightfactor;
            int xmax = (box.xmax )  - boxwidthfactor;

            int minScale = (int)(height / 4);
            int boxwidth = Math.abs(xmax - xmin);
            if(boxwidth<minScale)
                xmax = xmin + minScale;
            int boxheight = Math.abs(ymax - ymin);
            if(boxheight<minScale)
                ymax = ymin + minScale;
            if(ymin<1)
                ymin = 1;
            if(ymax>=height)
                ymax = height -1;
            if(xmin<1)
                xmin = 1;
            if(xmax >=width)
                xmax = width - 1;

            BoundingBox bbox = new BoundingBox(new Point(xmin, ymin), new Point(xmin,ymax), new Point(xmax, ymax), new Point(xmax, ymin));
            // BoundingBox bbox = new BoundingBox(new Point(0, 720), new Point(0,258), new Point(297, 258), new Point(935, 258));

            Point cent = new Point(xmin + Math.abs(xmax-xmin)/2, ymin + Math.abs(ymax-ymin)/2);
            productCount.setCurrCentroid(cent);
            productCount.setCurrBBox(bbox);
            productCount.changeCount(planeArray);
        }

        productCount.incInactiveCount();
        productCount.checkActivity();

        return productCount.getCount();
    }
}


class Point {
    int x;
    int y;

    public Point(){}

    public Point(int x, int y){
        this.x=x;
        this.y=y;
    }
    public String toString(){
        return "("+ this.x+","+this.y+")";
    }

}

class BoundingBox
{
    Point mleftTop;
    Point mrightTop;
    Point mleftBottom;
    Point mrightBottom;

    public BoundingBox (Point leftT, Point rightT, Point leftB, Point rightB)
    {
        mleftTop = leftT;
        mrightTop = rightT;
        mleftBottom = leftB;
        mrightBottom = rightB;
    }

}
enum State{

    In,
    Mid,
    Out,
    Init
}
class CProductCount
{

    State mlastState;// = State.Init
    State mcurrState;// = State.Init
    State minterstate;// = State.Init
    int mCount;
    int mlastappearCount;
    int mcurrappearCount;
    Point mlastCentroid;// = Point(0,0)#in case of multiple objects this should be for each object
    Point mcurrCentroid;// = Point(0,0)
    BoundingBox mCurrBBox;// = BoundingBox(Point(0,0),Point(0,0),Point(0,0),Point(0,0))
    int minactiveCount;
    int mframe;

    CProductCount()
    {
        mlastState = Init;
        mcurrState = Init;
        minterstate = Init;
        mCount     =    0;
        mlastappearCount = 0;
        mcurrappearCount = 0;
        mlastCentroid = new Point(0,0);//#in case of multiple objects this should be for each object
        mcurrCentroid = new Point(0,0);
        mCurrBBox = new BoundingBox(new Point(0,0),new Point(0,0),new Point(0,0),new Point(0,0));
        minactiveCount = 0;
        mframe = 0;
        System.out.println("mmk object in const "+this.mlastCentroid);
    }

    public boolean onSegment(Point p, Point q,Point r)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (q.x <= Integer.max(p.x, r.x) && q.x >= Integer.min(p.x, r.x) && q.y <= Integer.max(p.y, r.y) && q.y >= Integer.min(p.y, r.y))
                return true;
        }

        return false;
    }

    // To find orientation of ordered triplet (p, q, r).
// The function returns following values 
// 0 --> p, q && r are colinear 
// 1 --> Clockwise 
// 2 --> Counterclockwise 
    public int orientation(Point p,Point q,Point r)
    {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0)
            return 0; // colinear 
        else if (val > 0)
            return 1;
        else
            return 2; // clock or counterclock wise 
    }
    // The function that returns true if line segment 'p1q1'
// && 'p2q2' intersect. 
    public boolean doIntersect(Point p1, Point q1, Point p2, Point q2)
    {
        // Find the four orientations needed for general && 
        // special cases 
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
            return true;

        // Special Cases
        // p1, q1 && p2 are colinear && p2 lies on segment p1q1
        if ((o1 == 0) && onSegment(p1, p2, q1)) return true;

        // p1, q1 && p2 are colinear && q2 lies on segment p1q1
        if ((o2 == 0) && onSegment(p1, q2, q1)) return true;

        // p2, q2 && p1 are colinear && p1 lies on segment p2q2
        if ((o3 == 0) && onSegment(p2, p1, q2)) return true;

        // p2, q2 && q1 are colinear && q1 lies on segment p2q2
        if ((o4 == 0) && onSegment(p2, q1, q2)) return true;

        return false; // Doesn't fall in any of the above cases  
    }
    // Returns true if the point p lies inside the polygon[] with n vertices
    public boolean isInside( List<Point> polygon, int n,Point p)
    {
        // There must be at least 3 vertices in polygon[]
        if (n < 3) return false;

        // Create a point for line segment from p to infinite
        Point extreme = new Point(Integer.MAX_VALUE, p.y);
        // Count intersections of the above line with sides of polygon
        int count = 0;
        int i = 0;

        while (true)
        {
            int next = (i + 1) % n;

            // Check if the line segment from 'p' to 'extreme' intersects
            // with the line segment from 'polygon[i]' to 'polygon[next]' 
            if (doIntersect(polygon.get(i), polygon.get(next), p, extreme))
                if (orientation(polygon.get(i), p, polygon.get(next)) == 0)
                    return onSegment(polygon.get(i), p, polygon.get(next));

            count+=1;
            i = next;
            if (i==0)
                break;
        }

        System.out.println("mmk object count "+count);
        System.out.println("mmk object countnp "+n +"  "+p);


        // Return true if count is odd, false otherwise
        // Ininside() 127- 3 params : polygon array, n, point ie position lefttop

        if(count%2 ==1)

            return true;
        return false; // Same as (count%2 == 1)

    }
    public void setCurrCentroid(Point cent)
    {
        mcurrCentroid = cent;
    }

    public void findState(List<Point> polygon)
    {
        int n = polygon.size();

    //    List<Point> polygonList = Arrays.asList(polygon);

         boolean lefttopinside      = isInside(polygon, n, mCurrBBox.mleftTop);
         boolean righttopinside     = isInside(polygon, n, mCurrBBox.mrightTop);
         boolean leftbottominside   = isInside(polygon, n, mCurrBBox.mleftBottom);
         boolean rightbottominside  = isInside(polygon, n, mCurrBBox.mrightBottom);



     //  boolean lefttopinside = polygonList.contains(mCurrBBox.mleftTop);
     //   boolean righttopinside = polygonList.contains(mCurrBBox.mrightTop);
     //   boolean leftbottominside = polygonList.contains(mCurrBBox.mleftBottom);
     //   boolean rightbottominside = polygonList.contains(mCurrBBox.mrightBottom);

     // System.out.println("mmk mLeftTopPolygon "+polygonList.contains(new Point(297,258)));

        System.out.println("mmk Polygon Size "+ lefttopinside+"  "+righttopinside+"  "+leftbottominside +"  "+rightbottominside);
        System.out.println("mmk mLeftTop"+mCurrBBox.mleftTop);
     //   System.out.println("mmk ArrayList"+polygonList.get(2));

        int inside = 0;
        if(lefttopinside)inside+=1;
        if(righttopinside);inside+=1;
        if(leftbottominside)inside+=1;
        if(rightbottominside)inside+=1;
        if (inside ==4)
            mcurrState = In;
        else if (inside ==0)
            mcurrState = Out;
        else if (inside ==2)
            mcurrState = Mid;
    }

    public void checkActivity()
    {
        //mframe
        mframe+=1;
        if (minactiveCount > 50)
            //print("doing init")
            minactiveCount =  0;
        mcurrState = Init;
        mlastState = Init;
        minterstate = Init;
        mlastappearCount = 0;
        mcurrappearCount = 0;
    }
    public void changeCount(List<Point> cartplane)
    {
        System.out.println("mmk object"+this.mlastCentroid);
         Logger logger = new Logger();


        int CURRCOUNTTHRESH = 5;
        int LASTCOUNTTHRESH = 6;
        int DISTTHRESH = 1500;
        findState(cartplane);

        if (mcurrState == minterstate)
            mcurrappearCount+=1;

        minterstate = mcurrState;
        //  if(mcurrState!=mlastState)
        //      System.out.println("frame no: " + str(frame))
        //      System.out.println("last state: " + str(    .mlastState) + " curr state: " + str(    .mcurrState))

        //    System.out.println( str(    .mlastappearCount) + " " + str(    .mcurrappearCount))
        // print(     .mcurrappearCount)
        int distThresh = DISTTHRESH;

        logger.i("mmk centroid" +DISTTHRESH);

        int dist = abs( mlastCentroid.x -  mcurrCentroid.x) + abs(mlastCentroid.y -  mcurrCentroid.y);
        logger.i("mmk dist" +dist);
        if(dist < distThresh)
        {
            logger.i("mmk checkingCondition"+mcurrState +  "    " + mlastState+"    " + mlastappearCount+"    " +LASTCOUNTTHRESH+" "+mcurrappearCount+" "+CURRCOUNTTHRESH);

            if (    mcurrState== In && mlastState == Mid &&     mlastappearCount >LASTCOUNTTHRESH &&     mcurrappearCount > CURRCOUNTTHRESH)
            {
                logger.i("mmk Increment" );

                mcurrState = Init;
                mlastState = Init;
                mlastappearCount = 0;
                mcurrappearCount = 0;
                //print ("incremented")
                mCount+=1;

            }
            else if (((( mcurrState== Out ||    mcurrState== Mid) &&  mlastState == In)) && mlastappearCount >LASTCOUNTTHRESH &&  mcurrappearCount > CURRCOUNTTHRESH)
            {
                logger.i("mmk Decrement" );

                mCount-=1;
                mcurrState = Init;
                mlastState = Init;
                mlastappearCount = 0;
                mcurrappearCount = 0;
                //print("decremented");
            }
        }
        if   (mlastState !=    mcurrState &&    mcurrappearCount > LASTCOUNTTHRESH)//:#
        {
            logger.i("mmk Else" );
//
            mlastState =     mcurrState;
            mlastCentroid =  mcurrCentroid;
            mlastappearCount =   mcurrappearCount;
            mcurrappearCount = 0;
        }

    }
    public void setCurrBBox(BoundingBox box)
    {
        mCurrBBox = box;
    }
    public int getCount()
    {
        if(mCount<0)
            mCount = 0;
        return mCount;
    }
    public void incInactiveCount()
    {
        minactiveCount+=1;
    }

    public void resetInactiveCount()
    {
        minactiveCount = 0;
    }


}




/**

 main function
 {
 class CProductCount productCount; = (0, 4,4)


 # Cart Plane
 polygon = Polygon([(0, 720), (0, 258), (297, 258), (935, 258), (1280, 258), (1280, 720)])

 min_score_thresh = 0.89
 boxes_  = np.squeeze(boxes)
 scores_ = np.squeeze(scores)
 height = image_np.shape[0]
 width  = image_np.shape[1]
 maxind = -1
 maxscore = -1.0
 for b in range(boxes_.shape[0]):
 if scores_[b] > min_score_thresh && scores_[b] > maxscore:
 maxscore = scores_[b]
 maxind   = b
 if(maxind>-1):
 productCount.resetInactiveCount()
 box = boxes_[maxind]#tuple(boxes[i].tolist())
 #decrease 20 percent on each side depending on width && height of box
 boxwidthfactor  = int (abs(box[1] - box[3]) * width * 0.4)
 boxheightfactor = int (abs(box[0] - box[2] ) * height * 0.4)

 ymin = int(box[0] * height) + boxheightfactor
 xmin = int(box[1] * width)  + boxwidthfactor
 ymax = int(box[2] * height) - boxheightfactor
 xmax = int(box[3] * width)  - boxwidthfactor
 # for display original BBox
 cv2.rectangle(image_np, (xmin - boxwidthfactor, ymin - boxheightfactor), (xmax + boxwidthfactor, ymax + boxheightfactor), (255, 0, 0), 10)
 minScale = int(height / 4)
 boxwidth = abs(xmax - xmin)
 if(boxwidth<minScale):
 xmax = xmin + minScale
 boxheight = abs(ymax - ymin)
 if(boxheight<minScale):
 ymax = ymin + minScale
 if(ymin<1):
 ymin = 1
 if(ymax>=height):
 ymax = height -1
 if(xmin<1):
 xmin = 1
 if(xmax >=width):
 xmax = width - 1

 bbox = BoundingBox(Point(xmin, ymin), Point(xmin,ymax), Point(xmax, ymax), Point(xmax, ymin))
 cent = Point(xmin + (xmax-xmin)/2, ymin + (ymax-ymin)/2,)
 productCount.setCurrCentroid(cent)
 productCount.setCurrBBox(bbox)
 productCount.changeCount(polygon)
 productCount.incInactiveCount()
 productCount.checkActivity()

 text = "Count: "
 text += str(productCount.getCount())
 }
 */
