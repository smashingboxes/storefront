#include <SPI.h>
#include "led_strip.h"

color_t pink  = { 0xEB, 0x16, 0x67 };
color_t black = { 0, 0, 0 };
led_strip_t strip;
uint8_t current = 0;

void led_transfer(uint8_t x) {
  SPI.transfer(x);
  Serial.begin(9600);
}

void setup() {
  SPI.begin();
  led_init(&strip, led_transfer);
}

int offset_count(int current, int led)
{
  if(led > current) return (120 - led + current);
  return current - led;
}

float offset_to_lerp(int offs)
{
  if(offs > 10) return 0.0;
  return 1.0 / (float)offs;
}

void move_function(uint16_t index, color_t *color)
{
  uint8_t distance_from_head = offset_count(current, index);
  float lerp_amount = offset_to_lerp(distance_from_head);
  lerp_color(&pink, &black, lerp_amount, color);
}

void clear_leds(uint16_t index, color_t *color)
{
  memset(color, 0, sizeof(color_t));
}

void loop() {
  static bool moving = false;
  if(!moving) {
    led_map(&strip, clear_leds);
    while(Serial.available()) {
      Serial.read();
      moving = true;
    }
  } else {
    current = (current + 1) % 120;
    if (current == 0) {
      moving = false;
    } else {
    led_map(&strip, move_function);
    }
  }
  
  led_refresh(&strip);
  delay(25);
}