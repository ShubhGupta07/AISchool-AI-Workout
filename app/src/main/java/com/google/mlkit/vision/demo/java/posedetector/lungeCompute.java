package com.google.mlkit.vision.demo.java.posedetector;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import static java.lang.Math.abs;
import static java.lang.Math.acos;

public class lungeCompute {

    //initialize PoseLandmarks
    PoseLandmark leftHip;
    PoseLandmark rightHip;
    PoseLandmark leftKnee;
    PoseLandmark rightKnee;
    PoseLandmark leftAnkle;
    PoseLandmark rightAnkle;
    PoseLandmark leftFootIndex;
    PoseLandmark rightFootIndex;
    PoseLandmark leftShoulder;
    PoseLandmark rightShoulder;


    //declare the variables left leg angle and right leg angle to store calculated angle
    double leftAngle, rightAngle;
    //declare the variables left distance and right distance to check the distance between the index finger of legs
    boolean leftdistance, rightdistance;
    //declare boolean flag, flagcheck and check variables to check the pose and put a checkpoint for a pose.
    private static int flag = 0;
    private static boolean check = true;
    private static int flagcheck=0;
    //declare boolean rightstandstraight and leftstandstraight variables for checking the user is standing upright or not.
    boolean rightStandStraight, leftStandStraight;

    public lungeCompute(Pose pose)
    {
        //Initializes PoseLandmarks
        leftHip        = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        rightHip       = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        leftKnee       = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        rightKnee      = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        leftAnkle      = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        rightAnkle     = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
        leftFootIndex  = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
        rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);
        leftShoulder   = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        rightShoulder  = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);

    }

    static double getAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        // Computes the angle between two vectors, one connects firstPoint and midPoint and the other connects
        // midPoint and lastPoint
        double A, B, C;
        double Fx, Fy, Mx, My, Lx, Ly;

        Fx = firstPoint.getPosition().x;
        Fy = firstPoint.getPosition().y;
        Mx = midPoint.getPosition().x;
        My = midPoint.getPosition().y;
        Lx = lastPoint.getPosition().x;
        Ly = lastPoint.getPosition().y;

        //calculate distance between two points
        A = Math.sqrt(Math.pow(Lx-Fx,2) + Math.pow(Ly-Fy, 2));
        B = Math.sqrt(Math.pow(Lx-Mx,2) + Math.pow(Ly-My, 2));
        C = Math.sqrt(Math.pow(Mx-Fx,2) + Math.pow(My-Fy, 2));

        //cosine formula
        double angle  = Math.toDegrees(acos((B*B+C*C-A*A)/(2*B*C))); // Trigonometry result

        angle = abs(angle); // Angle should never be negative

        //if(angle > 160)
        //    flag=1;
        if (angle > 180) {
            angle = (360.0 - angle); // Always get the acute representation of the angle
        }
        // System.out.printf(String.valueOf(angle));
        return angle;
    }

    static boolean StraightPosition(PoseLandmark shoulder, PoseLandmark hips, PoseLandmark ankle){
        double y_shoulder, y_hips, y_ankle;
        y_shoulder = shoulder.getPosition().y;
        y_hips = hips.getPosition().y;
        y_ankle = ankle.getPosition().y;

        if((y_shoulder < y_hips) && (y_hips < y_ankle))
            return true;

        return false;

    }

    static void flagAngle(double langle, double rangle){
        if(langle > 160 && rangle > 160)
            flag = 1;
    }

    static boolean getDistance(PoseLandmark rLegFinger,PoseLandmark lLegFinger, PoseLandmark knee, PoseLandmark hips){
        double legDistance, thighLength;
        double rLeg_x, rLeg_y, lLeg_x, lLeg_y, knee_x, knee_y, hips_x, hips_y;

        //get x and y coordinates
        rLeg_x = rLegFinger.getPosition().x;
        rLeg_y = rLegFinger.getPosition().y;
        lLeg_x = lLegFinger.getPosition().x;
        lLeg_y = lLegFinger.getPosition().y;
        knee_x = knee.getPosition().x;
        knee_y = knee.getPosition().y;
        hips_x = hips.getPosition().x;
        hips_y = hips.getPosition().y;

        //distance formula to calculate distance
        legDistance = Math.sqrt(Math.pow(rLeg_x-lLeg_x,2) + Math.pow(rLeg_y-lLeg_y, 2));
        thighLength = Math.sqrt(Math.pow(knee_x-hips_x,2) + Math.pow(knee_y-hips_y, 2));

        //condition for checking stretched legs;
        if(legDistance > thighLength/2)
            return true;
        else
            return false;
    }

    public boolean isLunges()
    {
        //calling angle function for legs and storing in variable declared above
        leftAngle  = getAngle(leftAnkle, leftKnee, leftHip);
        rightAngle = getAngle(rightAnkle, rightKnee, rightHip);

        //we need to calculate distance between index finger of legs and knee and hips
        rightdistance = getDistance(rightFootIndex, leftFootIndex, rightKnee, rightHip);
        leftdistance = getDistance(rightFootIndex, leftFootIndex, leftKnee, leftHip);

        //calling straight position function
        leftStandStraight = StraightPosition(leftShoulder, leftHip, leftAnkle);
        rightStandStraight = StraightPosition(rightShoulder, rightHip, rightAnkle);

        //calling flagangle function and setting the value of flag as 1
        flagAngle(leftAngle, rightAngle);

        if((leftAngle < 110 || rightAngle < 110) && (rightdistance || leftdistance) && (leftStandStraight || rightStandStraight) && flag==1)
         //currently when either angle is less than 110, it is Lunges
        //You can change this angle into any desired angle between 0 and 180.
        {
            flagcheck++;
            //lunge done wrong
            if(leftAngle < 60 && rightAngle < 60){
                check=false;
                flag=0;
                return true;//increase frame number
            }
            //lunge is correct
            else if(leftAngle >= 60 || rightAngle >= 60){
                check=true;
                return true;//increase frame number
            }

        }
        //reset flag and flagcheck
        if(flagcheck!=0){
            flag = 0;
            flagcheck = 0;
        }
        //check for the lunge correction
        if(check)
            return false;//increase counter
        else
            return true;//increase frame number
    }

}
