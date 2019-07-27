/**
 * Radar Controller with Arduino
 * Federico Silvestri 559014
 */
#include <Arduino.h>
#include <Stepper.h>
#include <Servo.h>

#define STEP 50
#define SERVO_PIN 3
#define SERIAL_BUFFER_SIZE 50
#define SERIAL_COMMAND_SIZE 3
#define DATA_PIN 2
#define TRIGGER_PIN 4
#define ECHO_PIN 5

/*
  protocol numbers
*/
#define RESPONSE_INVALID_COMMAND 3
#define RESPONSE_COMMAND_OK 4
// requests
#define REQUEST_MOVE 0
#define REQUEST_READ 1


// stepper configuration
const int steps_per_revolution = 2048; // steps per revolution
const int role_per_minute = 15; // speed of stepper
const int min_h = 0; // min steps
const int max_h = 600; // max steps
int h_position = 0; // current position of stepper

// initialize the stepper library on pins 8 through 11:
Stepper h_stepper(steps_per_revolution, 11, 9, 10, 8);

// servo configuration
const int min_angle = 0;
const int max_angle = 180;
int current_angle = min_angle;

// initialize servo library
Servo servo;

void setup() {
  // initialize the serial port
  Serial.begin(57600);
  // assuming the zero position
  h_stepper.setSpeed(role_per_minute);
  // configuring Servo
  servo.attach(SERVO_PIN);
  // setting servo to 0
  servo.write(min_angle);
  // initialize the distance sensor
  pinMode(TRIGGER_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  pinMode(DATA_PIN, OUTPUT);
}

boolean move_h(int h) {
  if (h < min_h || h > max_h) {
    // you can't
    return false;
  }

  int steps = 0;
  if (h > h_position) {
    steps = h - h_position;
  } else if (h < h_position) {
    steps = -(h_position - h);
  }
  
  h_position += steps;
  h_stepper.step(steps);
  return true;
}

boolean move_angle(int angle) {
  if (angle < min_angle || angle > max_angle) {
    // you can't.
    return false;
  }

  current_angle = angle;
  servo.write(angle);
  return true;
}

long read_distance() {
  long duration;
  digitalWrite(TRIGGER_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIGGER_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIGGER_PIN, LOW);
  duration = pulseIn(ECHO_PIN, HIGH);
  return (duration/2) / 29.1;
}

bool parseCommand(int *command) {
  // command should be in format ACTION,HEIGHT,ANGLE
  char buf[SERIAL_BUFFER_SIZE];
  int readSize = Serial.readBytesUntil('\n', buf, SERIAL_BUFFER_SIZE);
  buf[readSize+1] = '\0';
  char *p = buf;
  char *str;
  int j = 0;
  while ((str = strtok_r(p, ";", &p)) != NULL) {
    if (j < SERIAL_COMMAND_SIZE) {
      command[j] = atoi(str);
      j += 1;
    }
    // flushing the strtok
  }

  if (j != SERIAL_COMMAND_SIZE) {
    // the command is not valid
    return false;
  }

  return true;
}

void send_response(int status, long value, int height, int angle) {
  char buff[50];
  // <response_code>;<radar_value>;<height>;<current_angle>
  sprintf(buff,"A%d;%ld;%d;%dB",RESPONSE_COMMAND_OK, value, h_position, current_angle);
  Serial.println(buff);
}

void execute_move(int height, int angle) {
  boolean s1 = move_h(height);
  boolean s2 = move_angle(angle);
  long value = read_distance();
  int response = s1 && s2 ? RESPONSE_COMMAND_OK : RESPONSE_INVALID_COMMAND;
  send_response(response, value, h_position, current_angle);
}

void execute_read() {
  long value = read_distance();
  send_response(RESPONSE_COMMAND_OK, value, h_position, current_angle);
}

void loop() {
  // do nothing
}

void serialEvent() {
  if (Serial.available()) {
    digitalWrite(DATA_PIN, HIGH);
    int command[SERIAL_COMMAND_SIZE];
    if (!parseCommand(command)) {
      // the command is not valid
      send_response(RESPONSE_INVALID_COMMAND, 0, 0, 0);
    } else {
      switch (command[0]) {
        case REQUEST_MOVE:
          execute_move(command[1], command[2]);
          break;
        case REQUEST_READ:
          execute_read();
          break;
        default:
            send_response(RESPONSE_INVALID_COMMAND, 0, 0, 0);
      }
    }
    digitalWrite(DATA_PIN, LOW);
  }
}