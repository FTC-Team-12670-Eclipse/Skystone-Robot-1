package org.firstinspires.ftc.teamcode.modules.swerve;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.common.UniversalConstants;
import org.firstinspires.ftc.teamcode.common.utilities.Pose;
import org.firstinspires.ftc.teamcode.common.utilities.Stopwatch;

import static org.firstinspires.ftc.teamcode.common.UniversalConstants.ModuleConfig;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.MotorDirection;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.AUTO_MAX_SPEED;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.ROBOT_STATUS;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.clipAngle;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.driveGearRatio;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.pwmRange;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.servoDefaultAngle;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.servoRange;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.ticksPerRevolution;
import static org.firstinspires.ftc.teamcode.common.UniversalConstants.wheelCircumference;

public class SwerveModule {

    public static double MID_POS = 0.5; // Default value. Updated in constructor.
    public static double MAX_DEGREES = servoDefaultAngle + servoRange / 2.0;
    public static double MIN_DEGREES = servoDefaultAngle - servoRange / 2.0;
    /* Public OpMode members. */
    public Servo turnServo;
    public DcMotor driveMotor;

    /* local OpMode members. */
    LinearOpMode linearOpMode;
    private ModuleConfig swerveModule;
    private double internalServoPosition; // considers the servo delta
    private double servoPosition; // ignores the servo delta
    private double mileage; // in inches
    private boolean disabled = false;
    private MotorDirection motorDirection;
    private Pose pose;
    private double target;
    private SwerveDrive swerveDrive;
    private boolean oneEighty; // servo is making a 180 degree turn


    public SwerveModule(LinearOpMode l, ModuleConfig module, SwerveDrive swerveDrive) {
        swerveModule = module;
        this.swerveDrive = swerveDrive;

        // Save reference to LinearOpMode
        linearOpMode = l;

        // Define and initialize ALL installed servos.
        turnServo = linearOpMode.hardwareMap.get(Servo.class, swerveModule.servoID);
        turnServo.setDirection(swerveModule.servoDirection);

        // Set the rotation servo for extended PWM range
        if (turnServo.getController() instanceof ServoControllerEx) {
            // Confirm its an extended range servo controller before we try to set to avoid crash
            ServoControllerEx theControl = (ServoControllerEx) turnServo.getController();
            theControl.setServoPwmRange(turnServo.getPortNumber(), pwmRange);
        }

        servoPosition = servoDefaultAngle;
        internalServoPosition = servoDefaultAngle + swerveModule.servoDelta;
        MID_POS = Math.abs((internalServoPosition - MAX_DEGREES)) / servoRange;
        turnServo.setPosition(MID_POS);

        // Define and Initialize Motors
        driveMotor = linearOpMode.hardwareMap.get(DcMotor.class, swerveModule.motorID);
        motorDirection = swerveModule.motorDirection;
        driveMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        driveMotor.setPower(0);
        driveMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        driveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        driveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        mileage = 0;

        if (swerveModule.isModuleDisabled)
            disableModule();

        pose = new Pose(swerveModule.x, swerveModule.y, 90);
    }

    public double swivel(double toAngle) {
        if (!disabled) {

            if (Double.isNaN(toAngle)) {
                return swivel(servoDefaultAngle);
            }

            toAngle = clipAngle(toAngle);
            servoPosition = toAngle;
            toAngle += swerveModule.servoDelta;

            resetMotorDirection();

            if (toAngle > -135 && toAngle < -45) {
                toAngle += 180;
                reverseMotorDirection();
            } else {
                if (toAngle >= -180 && toAngle <= -135)
                    toAngle += 360;

                if (Math.abs(toAngle + 180 - internalServoPosition) < Math.abs(toAngle - internalServoPosition) && toAngle + 180 >= MIN_DEGREES && toAngle + 180 <= MAX_DEGREES) {
                    toAngle += 180;
                    reverseMotorDirection();
                } else if (Math.abs(toAngle - 180 - internalServoPosition) < Math.abs(toAngle - internalServoPosition) && toAngle - 180 >= MIN_DEGREES && toAngle - 180 <= MAX_DEGREES) {
                    toAngle -= 180;
                    reverseMotorDirection();
                }
            }

            Range.throwIfRangeIsInvalid(toAngle, MIN_DEGREES, MAX_DEGREES);

            double distance = Math.abs(toAngle - internalServoPosition);
            internalServoPosition = toAngle;

            turnServo.setPosition(Math.abs((toAngle - MAX_DEGREES)) / servoRange);
            return distance;
        }

        linearOpMode.telemetry.addData(swerveModule.servoID.toUpperCase(), "DISABLED");
        return 0;
    }

    @Deprecated
    public void setPosition(double position) {
        turnServo.setPosition(position);
    }

    public void setPower(double power) {
        power = Range.clip(power, 0, AUTO_MAX_SPEED);
        power = motorDirection.assignDirection(power);

        if (ROBOT_STATUS != UniversalConstants.Status.DEBUGGING)
            driveMotor.setPower(power);
    }

    // position is in inches
    public void hubPID(double position) {
        int encoder =  (int)(Math.round(position * ticksPerRevolution / wheelCircumference / driveGearRatio));
        driveMotor.setTargetPosition(encoder);
    }

    public Pose getPose() {
        return pose;
    }

    /**
     * @return Returns the last commanded servo position
     * @// TODO: 8/11/2019 Update this method once we have encoders
     */
    public double getServoPosition() {
        return servoPosition;
    }


    public void disableModule() {
        disabled = true;
        ServoImplEx servoImplEx = new ServoImplEx((ServoControllerEx) turnServo.getController(), turnServo.getPortNumber(), ServoConfigurationType.getStandardServoType());
        servoImplEx.setPwmDisable();
        driveMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    public void enableModule() {
        disabled = false;
        ServoImplEx servoImplEx = new ServoImplEx((ServoControllerEx) turnServo.getController(), turnServo.getPortNumber(), ServoConfigurationType.getStandardServoType());
        servoImplEx.setPwmEnable();
        driveMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        driveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void reverseMotorDirection() {
        motorDirection = swerveModule.motorDirection.getOpposite();
    }

    public void resetMotorDirection() {
        motorDirection = swerveModule.motorDirection;
    }

    public MotorDirection getMotorDirection() {
        return this.motorDirection;
    }

    public void enablePID() {
        driveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    public void disablePID() {
        driveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public static int convertInchesToTicks(double inches) {
        return (int) Math.round(inches * ticksPerRevolution / wheelCircumference / driveGearRatio);
    }

    public void movePID(int position, double power) {
        double wheelStamp = swerveDrive.swerveKinematics.wheelStamps[swerveModule.ordinal()];
        int ticks = convertInchesToTicks(wheelStamp);
        int target = ticks + (int) motorDirection.assignDirection(position);
        driveMotor.setTargetPosition(target);
        driveMotor.setPower(power);
    }

    public String toString() {
        return swerveModule.moduleName;
    }
}