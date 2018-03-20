/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5699.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();

	private Victor rightDrive1;
	private Victor rightDrive2;
	private Victor leftDrive1;
	private Victor leftDrive2;
	
	private Spark conveyor;
	
	private Ultrasonic frontSensor, leftSensor, rightSensor;
	private ADXRS450_Gyro gyro;
	
	private boolean turningDone = false;
	private boolean turningSet = false;
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		SmartDashboard.putData("Auto choices", m_chooser);
		
		this.frontSensor = new Ultrasonic(9, 8);//check port numbers
		this.rightSensor = new Ultrasonic(7, 6);//check port numbers
		this.leftSensor = new Ultrasonic(5, 4);//check port numbers
		
		this.frontSensor.setAutomaticMode(true);
		this.rightSensor.setAutomaticMode(true);
		this.leftSensor.setAutomaticMode(true);
		
		this.rightDrive1 = new Victor(0);//check port number
		this.rightDrive2 = new Victor(1);//check port number
		this.leftDrive1 = new Victor(2);//check port number
		this.leftDrive2 = new Victor(3);//check port number
		
		this.conveyor = new Spark(4);//check port number
		
		this.gyro = new ADXRS450_Gyro();
		
		this.gyro.calibrate();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		switch (m_autoSelected) {
			case kCustomAuto:
				// Put custom auto code here
				break;
			case kDefaultAuto:
				break;
			default:
				autonomous();
				break;
		}
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
	
	
	/**************    AUTONOMOUS    **************/
	
	private boolean step[] = {true,true,true,true,true,true,true,true,true};
	private String robotPlace;
	
	private void autonomous() {
		//go straight until figures out its placement
		if(this.step[0]) {
			this.setMotors(0.3, 0.3);//set motors at a speed that ultrasonic sensors can work
			this.robotPlace = placement();//try to find its placement
			if(this.robotPlace != "") {   //if placement is found, go to the second step
				this.step[0] = false;//finish the step
				this.setMotors(0, 0);//stop
				System.out.println("step 1 of autonomous is done:");//indicates that the first step is done
				System.out.println("\tplacement of the robot is " + robotPlace);
			}
		}
		else{
			if(this.robotPlace == "right") {
				this.placementRight();//tells robot to do when it is placed in
			}else if(this.robotPlace == "left") {
				this.placementLeft();
			}else if(this.robotPlace == "middle") {
				this.placementMiddle();
			}
		}
	}
	/*** AUTONOMOUS FOR PLACEMENTS OF THE ROBOT ***/
	private void placementRight() {
		if(this.colorSide() == 'R') {
			if(this.step[1]) {
				this.setMotors(0.5, 0.5);
				Timer.delay(1);
				this.setMotors(0, 0);
			}else if(this.step[2]) {
				this.turnRight();
				if(this.turningDone) {
					this.turnReset();
					this.step[2] = false;
				}
			}else if(this.step[3]) {
				boolean travelDone = this.goStraightUntil(this.frontSensor, 300, "greater");
				if(travelDone) {
					this.step[3] = false;
				}
			}else if(this.step[4]) {
				this.conveyor.set(1);
				Timer.delay(2);
				this.conveyor.set(0);
			}else {
				System.out.println("placement right autonomous done. Switch color side: right");
			}
		}else if(colorSide() == 'L') {
			if(this.step[1]) {
				boolean travelDone = this.goStraightUntil(this.leftSensor, 600, "greater");
				if(travelDone) {
					this.step[1] = false;
				}
			}else if(this.step[2]) {
				this.turnLeft();
				if(this.turningDone) {
					this.turnReset();
					this.step[2] = false;
				}
			}else if(this.step[3]) {
				this.setMotors(1, 1);
				Timer.delay(2);
				this.step[3] = false;
			}else if(this.step[4]) {
				boolean travelDone = this.goStraightUntil(this.leftSensor, 600, "greater");
				if(travelDone) {
					this.setMotors(0.5, 0.5);
					Timer.delay(1);
					this.setMotors(0, 0);
					this.step[4] = false;
				}
			}else if(this.step[5]) {
				this.turnLeft();
				if(this.turningDone) {
					this.turnReset();
					this.step[5] = false;
				}
			}else if(this.step[6]) {
				this.setMotors(0.3, 0.3);
				Timer.delay(1.5);
				this.step[6] = false;
			}else if(this.step[7]) {
				this.turnLeft();
				if(this.turningDone) {
					this.turnReset();
					this.conveyor.set(1);
					Timer.delay(2);
					this.conveyor.set(0);
				}
			}else {
				System.out.println("placement right autonomous done. Switch color side: left");
			}
		}
	}
	private void placementLeft() {
		if(colorSide() == 'R') {
			if(this.step[1]) {
				boolean travelDone = this.goStraightUntil(this.rightSensor, 600, "greater");
				if(travelDone) {
					this.step[1] = false;
				}
			}else if(this.step[2]) {
				this.turnRight();
				if(this.turningDone) {
					this.turnReset();
					this.step[2] = false;
				}
			}else if(this.step[3]) {
				this.setMotors(1, 1);
				Timer.delay(2);
				this.step[3] = false;
			}else if(this.step[4]) {
				boolean travelDone = this.goStraightUntil(this.rightSensor, 600, "greater");
				if(travelDone) {
					this.setMotors(0.5, 0.5);
					Timer.delay(1);
					this.setMotors(0, 0);
					this.step[4] = false;
				}
			}else if(this.step[5]) {
				this.turnRight();
				if(this.turningDone) {
					this.turnReset();
					this.step[5] = false;
				}
			}else if(this.step[6]) {
				this.setMotors(0.3, 0.3);
				Timer.delay(1.5);
				this.step[6] = false;
			}else if(this.step[7]) {
				this.turnLeft();
				if(this.turningDone) {
					this.turnReset();
					this.conveyor.set(1);
					Timer.delay(2);
					this.conveyor.set(0);
				}
			}else {
				System.out.println("placement left autonomous done. Switch color side: right");
			}

		}else if(colorSide() == 'L') {
			if(this.step[1]) {
				this.setMotors(0.5, 0.5);
				Timer.delay(1);
				this.setMotors(0, 0);
			}else if(this.step[2]) {
				this.turnLeft();
				if(this.turningDone) {
					this.turnReset();
					this.step[2] = false;
				}
			}else if(this.step[3]) {
				boolean travelDone = this.goStraightUntil(this.frontSensor, 300, "greater");
				if(travelDone) {
					this.step[3] = false;
				}
			}else if(this.step[4]) {
				this.conveyor.set(1);
				Timer.delay(2);
				this.conveyor.set(0);
			}else {
				System.out.println("placement left autonomous done. Switch color side: left");
			}
		}
	}
	private void placementMiddle() {
		if(colorSide() == 'R') {
			if(this.step[1]) {
				this.turnRight();
				if(this.turningDone) {
					this.turnReset();
					this.step[1] = false;
				}
			}else if(this.step[2]) {
				boolean travelDone = this.goStraightUntil(this.leftSensor, 500, "greater");
				if(travelDone) {
					this.step[2] = false;
				}
			}else if(this.step[3]) {
				this.turnRight();
				if(this.turningDone) {
					this.turnReset();
					this.step[3] = false;
				}
			}else if(this.step[4]) {
				boolean travelDone = this.goStraightUntil(this.leftSensor, 100, "less");
				if(travelDone) {
					this.conveyor.set(1);
					Timer.delay(2);
					this.conveyor.set(0);
					this.step[4] = false;
				}
			}else {
				System.out.println("placement middle autonomous done. Switch color side: right");
			}
		}else if(colorSide() == 'L') {
			if(this.step[1]) {
				this.turnLeft();
				if(this.turningDone) {
					this.turnReset();
					this.step[1] = false;
				}
			}else if(this.step[2]) {
				boolean travelDone = this.goStraightUntil(this.rightSensor, 500, "greater");
				if(travelDone) {
					this.step[2] = false;
				}
			}else if(this.step[3]) {
				this.turnLeft();
				if(this.turningDone) {
					this.turnReset();
					this.step[3] = false;
				}
			}else if(this.step[4]) {
				boolean travelDone = this.goStraightUntil(this.rightSensor, 100, "less");
				if(travelDone) {
					this.conveyor.set(1);
					Timer.delay(2);
					this.conveyor.set(0);
					this.step[4] = false;
				}
			}else {
				System.out.println("placement middle autonomous done. Switch color side: left");
			}
		}
	}
	
	/*** AUTONOMOUS METHODS ***/
	private void setMotors(double right, double left) {
		this.rightDrive1.set(right);
		this.rightDrive2.set(right);
		this.leftDrive1.set(left * -1);
		this.leftDrive2.set(left * -1);
	}
	private String placement() {
		if(this.frontSensor.getRangeMM() <= 450 && this.rightSensor.getRangeMM() > 1500 && this.leftSensor.getRangeMM() > 1500) {
			return "middle";
		}else if(this.rightSensor.getRangeMM() <= 450 && this.frontSensor.getRangeMM() > 1500 && this.leftSensor.getRangeMM() <= 450) {
			return "left";
		}else if(this.leftSensor.getRangeMM() <= 450 && this.rightSensor.getRangeMM() <= 450 && this.frontSensor.getRangeMM() > 1500) {
			return "right";
		}
		return "";
	}
	private void turnRight() {
		if(!this.turningDone && !this.turningSet) {
			this.turningSet = true;
			this.gyro.reset();
		}else if(this.turningSet && !this.turningDone) {
			this.setMotors(0.3, -0.3);
			if(this.gyro.getAngle() >= 90) {
				this.setMotors(0, 0);
				this.turningDone = true;
				System.out.println("turning right done");
			}
		}
	}
	private void turnLeft() {
		if(!this.turningDone && !this.turningSet) {
			this.turningSet = true;
			this.gyro.reset();
		}else if(this.turningSet && !this.turningDone) {
			this.setMotors(-0.3, 0.3);
			if(this.gyro.getAngle() <= -90) {
				this.setMotors(0, 0);
				this.turningDone = true;
				System.out.println("turning left done");
			}
		}
	}
	private void turnReset() {
		this.turningDone = false;
		this.turningSet = false;
		this.gyro.reset();
	}
	private char colorSide() {
		 return DriverStation.getInstance().getGameSpecificMessage().charAt(0);
	}
	private boolean goStraightUntil(Ultrasonic sensor, double distance, String gorl) {
		this.setMotors(0.3, 0.3);
		if(gorl == "less") {
			if(sensor.getRangeMM() <= 450) {
				System.out.println("going straight done");
				this.setMotors(0, 0);
				return true;
			}
		}else if(gorl == "greater") {
			if(sensor.getRangeMM() >= 450) {
				System.out.println("going straight done");
				this.setMotors(0, 0);
				return true;
			}
		}
		return false;
	}
	
}
