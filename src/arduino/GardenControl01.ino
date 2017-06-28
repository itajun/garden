#include "Arduino.h"

const byte LED_PIN = 13;

const byte HUMIDITY_INPUT_PIN = 0;
const byte LIGHT_INPUT_PIN = 5;

const byte PUMP_A_PIN1 = 3;
const byte PUMP_A_PIN2 = 2;

const byte PUMP_B_PIN1 = 4;
const byte PUMP_B_PIN2 = 5;

const unsigned short CYCLE_SLEEP = 5000; // 5 secs
const unsigned short CYCLES_UPDATE_SENSOR = 1; // 2,5 minutes
const int CYCLE_PUMP_TIMEOUT = 20; // 5 minutes

int pumpACycle = 0;
int pumpBCycle = 0;

unsigned short sensorCycle = 0;

String lastCommand = "";

void setup()
{
    Serial.begin(9600);

    pinMode(LED_PIN, OUTPUT);
    pinMode(PUMP_A_PIN1, OUTPUT);
    pinMode(PUMP_A_PIN2, OUTPUT);
    pinMode(PUMP_B_PIN1, OUTPUT);
    pinMode(PUMP_B_PIN2, OUTPUT);
    pinMode(HUMIDITY_INPUT_PIN, INPUT);
}

void updateSensorValues() {
	if (sensorCycle >= CYCLES_UPDATE_SENSOR) {
		unsigned short humidity = analogRead(HUMIDITY_INPUT_PIN);
		sendMessage("log_humidity " + String(humidity));

		unsigned short light = analogRead(LIGHT_INPUT_PIN);
		sendMessage("log_light " + String(light));

		sensorCycle = 0;
	} else {
		sensorCycle++;
	}
}

void failsafePumps() {
	// Let's make sure it doesn't run forever if communication is lost
	if (pumpACycle > 0 && pumpACycle++ > CYCLE_PUMP_TIMEOUT) {
		digitalWrite(PUMP_A_PIN1, LOW);
		digitalWrite(PUMP_A_PIN2, LOW);
		pumpACycle = 0;
	}
	if (pumpBCycle > 0 && pumpBCycle++ > CYCLE_PUMP_TIMEOUT) {
		digitalWrite(PUMP_B_PIN1, LOW);
		digitalWrite(PUMP_A_PIN2, LOW);
		pumpBCycle = 0;
	}
}

void readCommands() {
	if (Serial.available() > 0) {
		lastCommand = Serial.readString();
	} else {
		lastCommand = "";
	}
}

void processCommands() {
	if (lastCommand.length() > 0) {
		unsigned int indexFirstSpace = lastCommand.indexOf(" ");
		if (indexFirstSpace <= 0) {
			indexFirstSpace = lastCommand.length() -2; // \n\r
		}
		String command = lastCommand.substring(0, indexFirstSpace);
		String payload = lastCommand.substring(indexFirstSpace + 1, lastCommand.length() - 2); // \n\r
		if (command.equals("pump_a")) {
			if (payload.equals("fw")) {
				digitalWrite(PUMP_A_PIN1, HIGH);
				digitalWrite(PUMP_A_PIN2, LOW);
				pumpACycle = 1;
			} else if (payload.equals("bw")) {
					digitalWrite(PUMP_A_PIN1, LOW);
					digitalWrite(PUMP_A_PIN2, HIGH);
					pumpACycle = 1;
			} else { // off
				digitalWrite(PUMP_A_PIN1, LOW);
				digitalWrite(PUMP_A_PIN2, LOW);
				pumpACycle = 0;
			}
		} else if (command.equals("pump_b")) {
			if (payload.equals("on")) {
				digitalWrite(PUMP_B_PIN1, HIGH);
				digitalWrite(PUMP_B_PIN2, LOW);
				pumpBCycle = 1;
			} else if (payload.equals("bw")) {
					digitalWrite(PUMP_B_PIN1, LOW);
					digitalWrite(PUMP_B_PIN2, HIGH);
					pumpACycle = 1;
			} else {
				digitalWrite(PUMP_B_PIN1, LOW);
				digitalWrite(PUMP_B_PIN2, LOW);
				pumpACycle = 0;
			}
		} else if (command.equals("ping")) {
			sendMessage("callback " + payload);
		}
	}
}

void sendMessage(String message) {
	Serial.println(message);
	Serial.flush();
}

void loop()
{
	readCommands();
	processCommands();
	updateSensorValues();
	failsafePumps();
	delay(CYCLE_SLEEP);
}
