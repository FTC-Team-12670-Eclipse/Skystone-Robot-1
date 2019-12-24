package org.firstinspires.ftc.teamcode.testers;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.common.Robot;
import org.firstinspires.ftc.teamcode.common.utilities.Debugger;
import org.firstinspires.ftc.teamcode.common.utilities.Pose;
import org.firstinspires.ftc.teamcode.common.utilities.Util;
import org.firstinspires.ftc.teamcode.common.utilities.WayPoint;
import org.firstinspires.ftc.teamcode.modules.HardStops;
import org.firstinspires.ftc.teamcode.modules.elevator.Clamp;
import org.firstinspires.ftc.teamcode.modules.jewelswatter.JewelSwatter;

import java.util.ArrayList;

@Autonomous(name = "TestOp: PID Tester")
public class WaypointPIDTester extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        Debugger robotDebugger = new Debugger(Util.getContext(), this, (ArrayList<String>) Debugger.Marker.getDebuggingMarkers());
        robotDebugger.initialize("PID Test");
        Robot robot = new Robot(this, robotDebugger);

        waitForStart();
        WayPoint[] wayPoints = new WayPoint[] {
                new WayPoint(new Pose(0,0,90),0.8, Clamp.ClampState.PARTIAL, JewelSwatter.JewelSwatterState.STOW_ALL,0, true,0,0, HardStops.HardStopState.DEPLOY, 0),
                new WayPoint(new Pose(0,-24,90),0.25, Clamp.ClampState.PARTIAL, JewelSwatter.JewelSwatterState.STOW_ALL,0, true,0,0, HardStops.HardStopState.DEPLOY, 0.01),
                new WayPoint(new Pose(0,-24.5,90),0.25, Clamp.ClampState.PARTIAL, JewelSwatter.JewelSwatterState.STOW_ALL,0, false,0,0, HardStops.HardStopState.DEPLOY, 0.01),
        };

        robot.requestState(Robot.RobotState.PATH_FOLLOWING);
        robot.setWayPoints(wayPoints);

        while (opModeIsActive()) {
            robot.updateAll();
        }
    }
}
