
#include <avr/wdt.h>
#include <Wire.h> //Includes the Wire.h library for serial communication
#include "Timer.h" // Includes the Timer library
#include <SFE_BMP180.h> //Includes the SFE_BMP180 library - Barometric pressure
#include "HTU21D.h" // Includes the HTU21D library - Humidity
#include <SparkFun_MMA8452Q.h> // Includes the SFE_MMA8452Q library - Acceletometer
//#include "Adafruit_MCP9808.h" // Includes the Adafruit_MCP9808 library - method for measuring from sensor is included in project, but not used - Temperature

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#define MESSAGE_SIZE 20

//Timer definition
Timer t;
//Barometric sensor definition
SFE_BMP180 pressure;
//Humidity sensor definition
HTU21D myHumidity;
//Accelerometer definition
MMA8452Q accel;
//Temperature sensor definition - this sensor isn't used
//Adafruit_MCP9808 tempsensor = Adafruit_MCP9808();


// temperature variables definiton
float TemperatureNumber;
float TemperatureNumberMin;
float TemperatureNumberMax;
int TemperatureCounter;

// humidity variables definiton
float HumidityNumber;
float HumidityNumberMin;
float HumidityNumberMax;
int HumidityCounter;

// barometric pressure variables definiton
float PressureNumber;
float PressureNumberMin;
float PressureNumberMax;
int PressureCounter;

// definition of string array
// strings are used to add bytes into the sending message 
String spaces[15] =  { "", " ", "  ","   ","    ","     ","      ","       ","        ","         ","          ","           ", "            ", "             ", "              " };

///////////////////////////////////////////////////////////////////////////////////////////////////////////

void setup()
{
  Serial.begin(9600);
  Serial.println("REBOOT");
  wdt_enable(WDTO_8S);
  
      accel.init(); //accelerometer inicialization

      if (pressure.begin())
        Serial.println("BMP180 init success");
      else
      {
        // Oops, something went wrong, this is usually a connection problem,
        // see the comments at the top of this sketch for the proper connections.   
        Serial.println("BMP180 init fail\n\n");
        while(1); // Pause forever.
      }

      if(myHumidity.begin()){
        Serial.println("HTU21D init success");
      }else{
      // Oops, something went wrong, this is usually a connection problem,
      // see the comments at the top of this sketch for the proper connections.
        Serial.println("HTU21D init fail\n\n");
        while(1); // Pause forever.
      }

      // Setting Timer to messure data in 1000ms periods
      t.every(1000, read_temperatureAnalog); 
      t.every(1000, read_humidity);
      t.every(1000, read_pressure);
       // Setting Timer to messure data in 100ms periods
      t.every(100, read_acceleration);

      // variables initialization, it helps count average every 20s
      TemperatureNumber = 0;
      TemperatureCounter = 20;
      
      HumidityNumber = 0;
      HumidityCounter = 20;
      
      PressureNumber = 0;
      PressureCounter = 20;      
}

void loop()
{
   t.update(); // Timer update
         
}


void read_acceleration()
{ 

      if (accel.available())
      {
        // First, use accel.read() to read the new variables:
        accel.read();
        
        read_accelerationX();
        delay(50);
        read_accelerationY();
        delay(50);
        read_accelerationZ();
      
      }
}

void read_accelerationX(){ 
 
  
    float numx = accel.cx * 9.81;

    sendMessage("[4]", numx);
    
    
     
    
}

void read_accelerationY(){

    float numy = accel.cy * 9.81;  

    sendMessage("[5]",numy);
    
    
  
  }


void read_accelerationZ(){

    float numz = accel.cz * 9.81; 

    sendMessage("[6]", numz);
   
    
  
  }



void read_temperatureAnalog() { // messuring temperature from termistor

//***************************************************************
//this section is available on url http://playground.arduino.cc/Main/Kty81-110
        unsigned int port = 0;
         float temp              = 82;
         ADCSRA = 0x00;
         ADCSRA = (1<<ADEN)|(1<<ADPS2)|(1<<ADPS1)|(1<<ADPS0);
         ADMUX = 0x00;
         ADMUX = (1<<REFS0);
         ADMUX |= port;   

         for (int i=0;i<=64;i++)
         {
                 ADCSRA|=(1<<ADSC);
                 while (ADCSRA & (1<<ADSC));
                 temp += (ADCL + ADCH*256);
         }

         temp /= 101;
         temp -= 156;
   
//***************************************************************
if( -55 < temp && temp < 150 ){
            if(TemperatureCounter == 20){// if starts new cycle
              
              TemperatureNumberMin = temp;
              TemperatureNumberMax = temp;
              
              }else{
                
              if(TemperatureNumberMin > temp){
              TemperatureNumberMin = temp;
              }
              if(TemperatureNumberMax < temp){
              TemperatureNumberMax = temp;
              }
                }


            
            if(TemperatureCounter > 1){//if this measurement is 1-19 just add data into TemperatureNumber variable and check max and min
         
                
                if((TemperatureCounter % 2) == 0){ // reset watchdog, needs to be called at least once per 8 seconds
                 
                  wdt_reset();
                  }
            
              
              TemperatureNumber = TemperatureNumber + temp;
              TemperatureCounter--;
            }else{//if this measurement is the last one in the cycle add data into TemperatureNumber variable and check mac and min
                  //then substract mac and min from TemperatureNumber variable and count average
                  // data are sent in specified format

              
                TemperatureNumber = TemperatureNumber + temp;
                TemperatureNumber = TemperatureNumber - TemperatureNumberMin;
                TemperatureNumber = TemperatureNumber - TemperatureNumberMax;
                
                float finalTemperature = TemperatureNumber/18;
              
                 sendMessage("[1]", finalTemperature);
                 
                TemperatureCounter=20;
                TemperatureNumber = 0;
              
              }
}

       
 }





void read_humidity(){

  float humd = myHumidity.readHumidity();
   
if(0 < humd && humd < 100 ){
  
            if(HumidityCounter == 20){// if starts new cycle
          
            HumidityNumberMin = humd;
            HumidityNumberMax = humd;
          
            }else{
                       if(HumidityNumberMin > humd){
                  HumidityNumberMin = humd;
                }
                if(HumidityNumberMax < humd){
                  HumidityNumberMax = humd;
                }
}
            
            if(HumidityCounter > 1){//if this measurement is 1-19 just add data into TemperatureNumber variable and check max and min
      
              
              HumidityNumber = HumidityNumber + humd;
              HumidityCounter--;
            }else{//if this measurement is the last one in the cycle add data into TemperatureNumber variable and check mac and min
                  //then substract mac and min from TemperatureNumber variable and count average
                  // data are sent in specified format


              
                HumidityNumber = HumidityNumber + humd;
                HumidityNumber = HumidityNumber - HumidityNumberMin;
                HumidityNumber = HumidityNumber - HumidityNumberMax;
                
              
              
                 
                float finalHumidity = HumidityNumber/18;
              
                sendMessage("[2]", finalHumidity);
                
                HumidityCounter=20;
                HumidityNumber = 0;
              }
}
        



   
  
}



void read_pressure()
{
  char status;
  double T,P;


  
  // Start a temperature measurement:
  // If request is successful, the number of ms to wait is returned.
  // If request is unsuccessful, 0 is returned.
//***************************************************************
//this part is implemented in BMP180 library example

  status = pressure.startTemperature();
  if (status != 0)
  {
    // Wait for the measurement to complete:
    delay(status);

    // Retrieve the completed temperature measurement:
    // Note that the measurement is stored in the variable T.
    // Function returns 1 if successful, 0 if failure.

    status = pressure.getTemperature(T);
    if (status != 0)
    {

      // Start a pressure measurement:
      // The parameter is the oversampling setting, from 0 to 3 (highest res, longest wait).
      // If request is successful, the number of ms to wait is returned.
      // If request is unsuccessful, 0 is returned.

      status = pressure.startPressure(3);
      if (status != 0)
      {
        // Wait for the measurement to complete:
        delay(status);

        // Retrieve the completed pressure measurement:
        // Note that the measurement is stored in the variable P.
        // Note also that the function requires the previous temperature measurement (T).
        // (If temperature is stable, you can do one temperature measurement for a number of pressure measurements.)
        // Function returns 1 if successful, 0 if failure.

        status = pressure.getPressure(P,T);
        if (status != 0)
        {
//***************************************************************
        if(300 < P && P < 1100 ){

        if(PressureCounter == 20){// if starts new cycle
          
          PressureNumberMin = P;
          PressureNumberMax = P;
          
          }else{
                         if(PressureNumberMin > P){
                  PressureNumberMin = P;
                }
                if(PressureNumberMax < P){
                  PressureNumberMax = P;
                }
}
            
            if(PressureCounter > 1){//if this measurement is 1-19 just add data into TemperatureNumber variable and check max and min
  
              
              PressureNumber = PressureNumber + P;
              PressureCounter--;
            }else{//if this measurement is the last one in the cycle add data into TemperatureNumber variable and check mac and min
                  //then substract mac and min from TemperatureNumber variable and count average
                  // data are sent in specified format


              
                PressureNumber = PressureNumber + P;
                PressureNumber = PressureNumber - PressureNumberMin;
                PressureNumber = PressureNumber - PressureNumberMax;
                
                float finalPressure = PressureNumber/18;
              
                sendMessage("[3]", finalPressure);
              PressureCounter=20;
              PressureNumber = 0;
              }
          
        
        }

            
        }
      
      }
     
    }

  }
}

void sendMessage(String ID, float data){
  
    String Data_N = String(data,2);  
    int size = Data_N.length();
    String sizeS = String(size);
     //first number in message represents data type and second is length of data, midle section si filled with string from Spaces, last section of message are data
    Serial.print(ID+sizeS+spaces[MESSAGE_SIZE-size-4]+Data_N);
  }

/*
void read_temperature(){

  tempsensor.shutdown_wake(0);   // Don't remove this line! required before reading temp
  float temp = tempsensor.readTempC();
  delay(250);
  tempsensor.shutdown_wake(1); // shutdown MSP9808 - power consumption ~0.1 mikro Ampere
       if(TemperatureCounter == 20){// if starts new cycle
              
              TemperatureNumberMin = temp;
              TemperatureNumberMax = temp;
              
              }
            
            if(TemperatureCounter > 1){//if this measurement is 1-19 just add data into TemperatureNumber variable and check max and min
               if(TemperatureNumberMin > temp){
                  TemperatureNumberMin = temp;
                }
                if(TemperatureNumberMax < temp){
                  TemperatureNumberMax = temp;
                }
 
            
              
              TemperatureNumber = TemperatureNumber + temp;
              TemperatureCounter--;
            }else{//if this measurement is the last one in the cycle add data into TemperatureNumber variable and check mac and min
                  //then substract mac and min from TemperatureNumber variable and count average
                  // data are sent in specified format


                if(TemperatureNumberMin > temp){
                  TemperatureNumberMin = temp;
                }
                if(TemperatureNumberMax < temp){
                  TemperatureNumberMax = temp;
                }

              
                TemperatureNumber = TemperatureNumber + temp;
                TemperatureNumber = TemperatureNumber - TemperatureNumberMin;
                TemperatureNumber = TemperatureNumber - TemperatureNumberMax;
                
                float finalTemperature = TemperatureNumber/18;
              
                String temperature_N = String(finalTemperature,2);  
                int sizeT = temperature_N.length();
                String sizeTS = String(sizeT);
                //first number in message represents data type and second is length of data, midle section si filled with string from Spaces, last section of message are data
                Serial.print("[1]"+sizeTS+spaces[20-sizeT-4]+temperature_N);
                TemperatureCounter=20;
                TemperatureNumber = 0;
              
              }
  }
 */       
   
