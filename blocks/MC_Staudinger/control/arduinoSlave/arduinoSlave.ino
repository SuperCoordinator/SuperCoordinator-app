/**
    Modbus slave example 1:
    The purpose of this example is to link a data array
    from the Arduino to an external device.

    Recommended Modbus Master: QModbus
    http://qmodbus.sourceforge.net/
*/

#include <ModbusRtu.h>


#define IN0 2
#define IN1 3
#define IN2 4
#define IN3 5
#define IN4 6
#define IN5 7
#define IN6 8
#define IN7 9

#define OUT0 A0
#define OUT1 A1
#define OUT2 A2
#define OUT3 A3
#define OUT4 A4
#define OUT5 A5
#define OUT6 12
#define OUT7 13

uint16_t regIN;
uint16_t regOUT;



// data array for modbus network sharing
uint16_t au16data[2] = {
  regIN, regOUT
};

/**
    Modbus object declaration
    u8id : node id = 0 for master, = 1..247 for slave
    port : serial port
    u8txenpin : 0 for RS-232 and USB-FTDI
                 or any pin number > 1 for RS-485
*/
Modbus slave(1, Serial, 0); // this is slave @1 and RS-232 or USB-FTDI




void setup() {
  pinMode(IN0, INPUT_PULLUP);
  pinMode(IN1, INPUT_PULLUP);
  pinMode(IN2, INPUT_PULLUP);
  pinMode(IN3, INPUT_PULLUP);

  pinMode(IN4, INPUT_PULLUP);
  pinMode(IN5, INPUT_PULLUP);
  pinMode(IN6, INPUT_PULLUP);
  pinMode(IN7, INPUT_PULLUP);

  pinMode(OUT0, OUTPUT);
  pinMode(OUT1, OUTPUT);
  pinMode(OUT2, OUTPUT);
  pinMode(OUT3, OUTPUT);

  pinMode(OUT4, OUTPUT);
  pinMode(OUT5, OUTPUT);
  pinMode(OUT6, OUTPUT);
  pinMode(OUT7, OUTPUT);



  Serial.begin( 9600 ); // baud-rate at 19200
  slave.start();
}

void loop() {

  regIN = 0;

  regIN |= !digitalRead(IN0) << 0;
  regIN |= !digitalRead(IN1) << 1;
  regIN |= !digitalRead(IN2) << 2;
  regIN |= !digitalRead(IN3) << 3;
  regIN |= !digitalRead(IN4) << 4;
  regIN |= !digitalRead(IN5) << 5;
  regIN |= !digitalRead(IN6) << 6;
  regIN |= !digitalRead(IN7) << 7;

  au16data[0] = regIN;
  //  Serial.print(" Register: ");
  //  Serial.println(regIN);
  //  Serial.print( "IN3: ");
  //  Serial.println(!digitalRead(IN3));

  //  digitalWrite(OUT0,LOW);

  //    digitalWrite(OUT0,(!(regOUT >> 0) & 1));
  //    digitalWrite(OUT1,(!(regOUT >> 1) & 1));
  //    digitalWrite(OUT2,(!(regOUT >> 2) & 1));
  //    digitalWrite(OUT3,(!(regOUT >> 3) & 1));
  //    digitalWrite(OUT4,(!(regOUT >> 4) & 1));
  //    digitalWrite(OUT5,(!(regOUT >> 5) & 1));
  //    digitalWrite(OUT6,(!(regOUT >> 6) & 1));
  //    digitalWrite(OUT7,(!(regOUT >> 7) & 1));

  digitalWrite(OUT0, !bitRead(regOUT, 0));
  digitalWrite(OUT1, !bitRead(regOUT, 1));
  digitalWrite(OUT2, !bitRead(regOUT, 2));
  digitalWrite(OUT3, !bitRead(regOUT, 3));
  digitalWrite(OUT4, !bitRead(regOUT, 4));
  digitalWrite(OUT5, !bitRead(regOUT, 5));
  digitalWrite(OUT6, !bitRead(regOUT, 6));
  digitalWrite(OUT7, !bitRead(regOUT, 7));
  //  Serial.println(regOUT);
  //  regOUT = 0;
  regOUT = au16data[1];
  slave.poll( au16data, 2 );

}
