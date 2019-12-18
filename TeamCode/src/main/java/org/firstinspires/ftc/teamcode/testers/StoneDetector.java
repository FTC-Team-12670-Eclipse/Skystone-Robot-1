package org.firstinspires.ftc.teamcode.testers;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.vuforia.Frame;
import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.teamcode.common.UniversalConstants;
import org.firstinspires.ftc.teamcode.common.utilities.Stopwatch;

@TeleOp(name = "TestOp: Grayscale Tester")
public class StoneDetector extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        parameters.vuforiaLicenseKey = UniversalConstants.vuforiaLicenceKey;
        VuforiaLocalizer vuforia = ClassFactory.getInstance().createVuforia(parameters);
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565,true);
        vuforia.setFrameQueueCapacity(2);

        telemetry.addLine("Ready to go!");
        telemetry.update();
        waitForStart();

//        Stopwatch stopwatch = new Stopwatch();
//        stopwatch.start();
        // get image from vuforia
        Frame frame = vuforia.getFrameQueue().take();
        telemetry.addData("Vu Height", frame.getImage(1).getHeight());
        telemetry.addData("Vu Width", frame.getImage(1).getWidth());
        telemetry.addData("Vu Format", frame.getImage(1).getFormat() == PIXEL_FORMAT.RGB565);

        // build an android bitmap
        Image vu_img = frame.getImage(1);
        Bitmap bitmap = Bitmap.createBitmap(vu_img.getWidth(), vu_img.getHeight(), Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(vu_img.getPixels());

        // crop the image for accuracy
        int x = bitmap.getWidth()/4;
        int y = 0;
        int width = bitmap.getWidth()/2;
        int height = bitmap.getHeight();
        bitmap = Bitmap.createBitmap(bitmap,x,y,width,height);

        // scale the image down to a 2x1 image
        bitmap = Bitmap.createScaledBitmap(bitmap, 2, 1, false);

//        stopwatch.stop();
        telemetry.addLine();
        telemetry.addData("Bit Height", bitmap.getHeight());
        telemetry.addData("Bit Width", bitmap.getWidth());
        telemetry.addData("Left", rgbToGray(bitmap.getPixel(0,0)));
        telemetry.addData("Right", rgbToGray(bitmap.getPixel(1,0)));
//        telemetry.addData("Processing time", stopwatch.millis());
//        stopwatch.start();
//        {
//            // average left side
//            int leftAvg = 0;
//            int counter = 0;
//            for (int i = 0; i < height; i++) {
//                for (int j = 0; j < width / 2; j++) {
//                    leftAvg += rgbToGray(bitmap.getPixel(j, i));
//                    counter++;
//                }
//            }
//            leftAvg /= counter;
//            telemetry.addData("Left Color", leftAvg);
//        }
//
//        {
//            // average right side
//            int rightAvg = 0;
//            int counter = 0;
//            for (int i = 0; i < height; i++) {
//                for (int j = width/2; j < width; j++) {
//                    rightAvg += rgbToGray(bitmap.getPixel(j, i));
//                    counter++;
//                }
//            }
//            rightAvg /= counter;
//            telemetry.addData("Right Color", rightAvg);
//        }
//
//        stopwatch.stop();
//        telemetry.addData("Math time", stopwatch.millis());
        telemetry.update();
        while (opModeIsActive()) {
            idle();
        }
    }


    public int rgbToGray(int color) {
        int R = (color >> 16) & 0xff;
        telemetry.addData("R", R);
        int G = (color >>  8) & 0xff;
        telemetry.addData("G", G);
        int B = (color      ) & 0xff;
        telemetry.addData("B", B);
        int gray = (int) Math.round(0.3*R + 0.59*G + 0.11*B);
        return gray;
    }
}
