const int PIEZO1 = A0;  // Analog input pin that the potentiometer is attached to
const int PIEZO2 = A1;
const int PIEZO3 = A2;
const int PIEZO4 = A3;

int sensorValue1 = 0;  // value read from the pot
int sensorValue2 = 0;
int sensorValue3 = 0;
int sensorValue4 = 0;


int max_val1 = 0;
int max_val2 = 0;
int max_val3 = 0;
int max_val4 = 0;

const int sample_frame = 200;
const int sensitivty = 300;

void setup() {
  // initialize serial communications at 9600 bps:
  Serial.begin(9600);
  pinMode(12,OUTPUT);
  pinMode(11,OUTPUT);
  pinMode(10,OUTPUT);
  pinMode(9,OUTPUT);
}

void loop() {

  for(int i = 0; i < sample_frame; i++)
  {

    sensorValue1 = analogRead(PIEZO1);
    if(sensorValue1 > max_val1)
    {
      max_val1 = sensorValue1;
    }

    sensorValue2 = analogRead(PIEZO2);
    if(sensorValue2 > max_val2)
    {
      max_val2 = sensorValue2;
    }

    sensorValue3 = analogRead(PIEZO3);
    if(sensorValue3 > max_val3)
    {
      max_val3 = sensorValue3;
    }
    sensorValue4 = analogRead(PIEZO4);
    if(sensorValue4 > max_val4)
    {
      max_val4 = sensorValue4;
    }

  }

  int a = max_val1;
  int b = max_val2;
  int c = max_val3;
  int d = max_val4;
  Serial.print("a:"); Serial.print(sensorValue1); Serial.print(", ");
  Serial.print("b:"); Serial.print(sensorValue2); Serial.print(", ");
  Serial.print("c:"); Serial.print(sensorValue3); Serial.print(", ");
  Serial.print("d:"); Serial.print(sensorValue4); Serial.print(", ");
  Serial.println();

  if(a > sensitivty || b > sensitivty || c > sensitivty || d > sensitivity)
  {
    if(max_val1 > max_val2 && max_val1 > max_val3 && max_val1 > maxval4)
    {
      //Serial.println("LEFT");
      digitalWrite(11,HIGH);
      delay(1000);
      digitalWrite(11,LOW);
    }
    else if(max_val2 > max_val1 && max_val2 > max_val3 && max_val2 > max_val4)
    {
      //Serial.println("RIGHT");
      digitalWrite(12,HIGH);
      delay(1000);
      digitalWrite(12,LOW);
    }

    else if(max_val3 > max_val1 && max_val3 > max_val2 && max_val3 > max_val4)
    {
      //Serial.println("RIGHT");
      digitalWrite(10,HIGH);
      delay(1000);
      digitalWrite(10,LOW);
    }
     else if(max_val4 > max_val1 && max_val4 > max_val2 && max_val4 > max_val3)
    {
      //Serial.println("RIGHT");
      digitalWrite(9,HIGH);
      delay(1000);
      digitalWrite(9,LOW);
    }

  }

  max_val1 = 0;
  max_val2 = 0;
  max_val3 = 0;
  max_val4 = 0;
}
