package com.google.mlkit.vision.demo.java.posedetector;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import static java.lang.Math.abs;
import static java.lang.Math.acos;

public class squatCompute {
    //initialize PoseLandmarks
    //for legs
    PoseLandmark leftHip;
    PoseLandmark rightHip;
    PoseLandmark leftKnee;
    PoseLandmark rightKnee;
    PoseLandmark leftAnkle;
    PoseLandmark rightAnkle;

    //for hands
    PoseLandmark leftElbow;
    PoseLandmark rightElbow;
    PoseLandmark leftShoulder;
    PoseLandmark rightShoulder;

    //declare the double variables for the left leg, right leg and left hand and right hand to store calculated angles.
    double leftAngle, rightAngle, rightHandAngle, leftHandAngle;
    //declare boolean Flag and check variables to check the pose and put a checkpoint for a pose.
    private static int flag = 0;
    private static int check=0;
    //declare boolean rightstandstraight and leftstandstraight variables for checking the user is standing upright or not.
    boolean rightStandStraight, leftStandStraight;

    public squatCompute(Pose pose)
    {
        //Initializes PoseLandmarks
        leftHip        = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        rightHip       = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        leftKnee       = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        rightKnee      = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        leftAnkle      = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        rightAnkle     = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        //for hands
        leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
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

        if (angle > 180) {
            angle = (360.0 - angle); // Always get the acute representation of the angle
        }

        return angle;
    }


    static void flagAngle(double langle, double rangle){
        if(langle > 160 || rangle > 160)
            flag = 1;
        //return true;

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

    public boolean isSquat()
    {
        //calling angle function for legs and storing in variable declared above
        leftAngle  = getAngle(leftAnkle, leftKnee, leftHip);
        rightAngle = getAngle(rightAnkle, rightKnee, rightHip);

        //calling angle function for hands and storing in variable declared above
        leftHandAngle  = getAngle(leftElbow, leftShoulder, leftHip);
        rightHandAngle = getAngle(rightElbow, rightShoulder, rightHip);

        //calling straight position function
        leftStandStraight = StraightPosition(leftShoulder, leftHip, leftAnkle);
        rightStandStraight = StraightPosition(rightShoulder, rightHip, rightAnkle);

        //calling flagangle function and setting the value of flag as 1
        flagAngle(leftAngle, rightAngle);

        //took the angle from either leg
        //this is the core function that determines if it Squat or not
        if((leftAngle < 120 || rightAngle < 120) && (leftHandAngle > 70 || rightHandAngle > 70) && (leftStandStraight || rightStandStraight) && flag==1)
        //currently when either angle is less than 110 and hand angle less than 70, it is squat
        //You can change this angle into any desired angle between 0 and 180.
        {
            //squat down
            check=1;
            return true;//increase the frame number
        }
        //checking if user coming from squat down to squat up
        if(check!=0) {
            flag = 0;
            check = 0;
        }
        return false;//increase the counter
    }
}
