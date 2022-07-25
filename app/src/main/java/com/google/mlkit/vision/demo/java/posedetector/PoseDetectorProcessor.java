/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo.java.posedetector;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.demo.java.VisionProcessorBase;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;

/** A processor to run pose detector. */
public class  PoseDetectorProcessor extends VisionProcessorBase<Pose> {

  private static final String TAG = "PoseDetectorProcessor";

  private final PoseDetector detector;

  private final boolean showInFrameLikelihood;

  public int count = 0;
  public int SquatFrameCount = 0;
  private Context context;

  private TextView counter;
  private TextView FrameNumber;

  public PoseDetectorProcessor(
      Context context, PoseDetectorOptionsBase options, boolean showInFrameLikelihood) {
    super(context);
    this.context = context;
    this.showInFrameLikelihood = showInFrameLikelihood;
    detector = PoseDetection.getClient(options);


    counter = (TextView) ((Activity) context).findViewById(R.id.counter);
    FrameNumber = (TextView) ((Activity) context).findViewById(R.id.frameNumber);
  }

  public void updateCounter(Pose pose)
  {
    //use SquatCompute module to determine if the pose is squat
    if (new squatCompute(pose).isSquat()) {

      SquatFrameCount++; //if it is squat down, add 1 to SquatFrameCount
      FrameNumber.setText(Integer.toString(SquatFrameCount)); //and show it on TextView
    } else {
      //if it is squat up,
      if (SquatFrameCount > 0)  //if the SquatFrameCount is non-zero, user was in squat down position previously
      {
        count += 1; //add 1 to Squat count
        SquatFrameCount = 0; //reset the SquatFrameCount to 0
        counter.setText(Integer.toString(count)); //show count on TextView
        FrameNumber.setText(Integer.toString(SquatFrameCount)); //show SquatFrameCount on TextView
      }
    }

  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<Pose> detectInImage(InputImage image) {
    return detector.process(image);
  }

  @Override
  protected void onSuccess(@NonNull Pose pose, @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.add(new PoseGraphic(graphicOverlay, pose, showInFrameLikelihood));
    if (!pose.getAllPoseLandmarks().isEmpty()) {
      updateCounter(pose);

    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Pose detection failed!", e);
  }
}
