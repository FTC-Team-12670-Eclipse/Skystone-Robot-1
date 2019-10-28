package org.firstinspires.ftc.teamcode.modules.jewelSwatter;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp (name = "armTester")

public class armTester extends LinearOpMode {
    @Override

    public void runOpMode() throws InterruptedException {
        arm arm1 = new arm(this);
        waitForStart();
        while (opModeIsActive()) {
            //sets the positions if you press x
            if (gamepad2.x) {
                arm1.setPositions(180, 0);
                arm1.updateTelemetry();
            }
            //sets the positions if you press y
            if (gamepad2.y) {
                arm1.setPositions(0, 0);
                arm1.updateTelemetry();

            }
        }
    }
}
