package org.firstinspires.ftc.teamcode.RobotModules;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.ArrayList;
import java.util.List;

public class TensorFlowLite {

    private static final String TFOD_MODEL_ASSET = "Skystone.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Stone";
    private static final String LABEL_SECOND_ELEMENT = "Skystone";
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;
    private Telemetry telemetry;
    private LinearOpMode linearOpMode;
    private HardwareMap hardwareMap;
    String pattern;


    public void status(String s) {
        telemetry.addLine(s);
        telemetry.update();
    }


    /**
     * Constructor for the TensorFlowLite object detection class
     *
     * @param l Input this to run using LinearOpMode functions
     */
    public TensorFlowLite(LinearOpMode l) {
        linearOpMode = l;
        telemetry = linearOpMode.telemetry;
        hardwareMap = linearOpMode.hardwareMap;
        pattern = "Unknown";


        initVuforia();

        status("Vuforia has been initialized.");

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
            status("Tensor Flow has been initialized.");
        } else {
            status("This device does not support Tensor Flow.");
        }
    }


    /**
     * This function activates the TensorFlow Object Detection engine
     */
    public void activateTfod() {
        if (tfod != null) {
            tfod.activate();
        }
    }


    /**
     * This function deactivates the TensorFlow Object Detection engine
     */
    public void shutDownTfod() {
        if (tfod != null) {
            tfod.shutdown();
        }
    }


    /**
     * This function is used to determine the SkyStone Arrangement Pattern.
     * It will set the pattern variable to A, B or C depending on SkyStone arrangement.
     * The function has a background elimination method based off of the one implemented in Rover Ruckus
     */
    public void determinePattern() {
        if (tfod != null) {
            List<Recognition> updateRecognitions = tfod.getUpdatedRecognitions();
            if (updateRecognitions != null) {
                telemetry.addData("Objects Detected", updateRecognitions.size());
                if (updateRecognitions.size() >= 3) {
                    int SkyStoneX = -1;
                    int SkyStoneY = -1;
                    int Stone1X = -1;
                    int Stone1Y = -1;
                    int Stone2X = -1;
                    int Stone2Y = -1;

                    ArrayList<Integer> SkystoneXvals = new ArrayList<Integer>();
                    ArrayList<Integer> SkystoneYvals = new ArrayList<Integer>();
                    ArrayList<Integer> StoneXvals = new ArrayList<Integer>();
                    ArrayList<Integer> StoneYvals = new ArrayList<Integer>();
                    for (Recognition recognition : updateRecognitions) {
                        if (recognition.getLabel().equals(LABEL_SECOND_ELEMENT)) {
                            SkystoneXvals.add((int) recognition.getLeft());
                            SkystoneYvals.add((int) recognition.getBottom());
                        } else {
                            StoneXvals.add((int) recognition.getLeft());
                            StoneYvals.add((int) recognition.getBottom());
                        }
                    }
                    int indexToRemove = 0;
                    for (int i = 0; i < SkystoneYvals.size(); i++) {
                        if (SkyStoneY <= SkystoneYvals.get(i) || SkyStoneX < SkystoneXvals.get(i)) {
                            SkyStoneY = SkystoneXvals.get(i);
                            SkyStoneX = SkystoneXvals.get(i);
                        }
                    }

                    for (int i = 0; i < StoneYvals.size(); i++) {
                        if (Stone1Y < StoneYvals.get(i)) {
                            Stone1Y = StoneYvals.get(i);
                            Stone1X = StoneXvals.get(i);
                            indexToRemove = i;
                        }
                    }
                    if (StoneYvals.size() > 0) {
                        StoneXvals.remove(indexToRemove);
                        StoneYvals.remove(indexToRemove);
                    }
                    for (int i = 0; i < StoneYvals.size(); i++) {
                        if (Stone2Y < StoneYvals.get(i)) {
                            Stone2Y = StoneYvals.get(i);
                            Stone2X = StoneXvals.get(i);
                        }
                    }
                    if (SkyStoneX != -1 && Stone1X != -1 && Stone2X != -1) {
                        if (SkyStoneX < Stone1X && SkyStoneX < Stone2X) {
                            pattern = "A";
                            telemetry.addData("Pattern", "A");
                        } else if (SkyStoneX > Stone1X && SkyStoneX > Stone2X) {
                            pattern = "C";
                            telemetry.addData("Pattern", "C");
                        } else {
                            pattern = "B";
                            telemetry.addData("Position", "B");
                        }
                    }
                }
                telemetry.addData("Pattern Variable: ", pattern);
                telemetry.update();

            }
        }
    }

    public void updateTensorFlowSingle() {
        if (tfod != null) {
            List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
            if (updatedRecognitions != null) {
                telemetry.addData("# Object Detected", updatedRecognitions.size());
                // the following code was taken form FTC sample code and partially modified for our robot's purposes
                // It is alright if not understood.
                //x and y position values for minerals
                int SkystoneX = -1;
                int SkystoneY = -1;
                double Azimuth = 0;
                /* Creates an array list of x and y values for all minerals*/
                ArrayList<Integer> SkyStoneXvals = new ArrayList<Integer>();
                ArrayList<Integer> SkyStoneYvals = new ArrayList<Integer>();
                ArrayList<Double> SkyStoneAzimuths = new ArrayList<Double>();
                for (Recognition recognition : updatedRecognitions) {
                    if (recognition.getLabel().equals(LABEL_SECOND_ELEMENT)) {
                        SkyStoneXvals.add((((int) recognition.getLeft()) + ((int) recognition.getRight())) / 2);
                        SkyStoneYvals.add((int) recognition.getBottom());
                        SkyStoneAzimuths.add(recognition.estimateAngleToObject(AngleUnit.DEGREES));

                    }
                }
                int indexToRemove = 0;
                for (int i = 0; i < SkyStoneYvals.size(); i++) {
                    if (SkystoneY < SkyStoneYvals.get(i)) {
                        SkystoneY = SkyStoneYvals.get(i);
                        SkystoneX = SkyStoneXvals.get(i);
                        Azimuth = SkyStoneAzimuths.get(i);
                    }
                }
                telemetry.addData("Gold X value: ", SkystoneX);
                telemetry.addData("Azimuth: ", Azimuth);
                if (SkystoneX != -1) {
                    if (SkystoneX <= 426 && Azimuth <= -15) {
                        pattern = "A";
                        telemetry.addData("Position: ", "Left");
                    } else if (SkystoneX >= 852 && Azimuth >= 15) {
                        pattern = "C";
                        telemetry.addData("Position: ", "Right");
                    } else {
                        pattern = "B";
                        telemetry.addData("Position: ", "Center");
                    }
                }

                telemetry.addData("Pattern Variable: ", pattern);
                telemetry.update();
            }
        }
    }


    /**
     * Acccesses the private variable pattern which is set by determinePattern() function
     *
     * @return pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * This function initializes Vuforia to use camera on phones for object detection.
     */
    private void initVuforia() {
        //This method will create all neccessary vuforia parameters and set them
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = UniversalConstants.vuforiaLicenceKey;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        vuforia = ClassFactory.getInstance().createVuforia(parameters);
    }

    /**
     * This function initializes Vuforia to use an external USB camera for object detection.
     */
    private void initVuforiaWebCam() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = UniversalConstants.vuforiaLicenceKey;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }


    /**
     * This function loads all necessary information into the TensorFlow Object Detection Engine to use for image recognition.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        //Change these parameters when loading a new model asset for object detection. Set appropriate labels
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
    }
}