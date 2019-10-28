package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.common.utilities.Gamepad;
import org.firstinspires.ftc.teamcode.modules.elevator.Elevator;
import org.firstinspires.ftc.teamcode.modules.swerve.SwerveDrive;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp")
public class FullTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        SwerveDrive swerveDrive = new SwerveDrive(this, null);
        Elevator elevator = new Elevator(this);
        Gamepad gamepad = new Gamepad(this);
        //TensorFlowLite tensorFlowLite = new TensorFlowLite(this,.25);
        waitForStart();
        while (opModeIsActive()){
            swerveDrive.swerveKinematics.update();
            swerveDrive.fod(gamepad);
            elevator.updateByGamepad(gamepad);
            gamepad.update();

        }
    }
}
