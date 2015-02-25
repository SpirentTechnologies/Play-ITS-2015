# CAN ELM 327 adaptation

## Introduction
This is an Implentation of the CAN ELM 327 Adaptation. It uses Messages in OpenXC-Json-Format, and builds a current  Value-Database. The Data is received from the [OpenXC Simulator] (https://github.com/openxc/openxc-vehicle-simulator) or directly via Hardware from the [ELM327 Module](http://elmelectronics.com/DSheets/ELM327DS.pdf) .This Database is used by the [TTWorkbench] (https://github.com/TestingTechnologies/Play-ITS-2015/tree/master/TTCN) for further treatment. If the Messages are received directly from the Hardware, it is necessary to convert the Messages to 

## Data-Table
The Data-Table has follwing Entries:

- steering_wheel_angle
- torque_at_transmission
- engine_speed
- vehicle_speed
- accelerator_pedal_position
- parking_brake_status
- brake_pedal_status
- transmission_gear_position
- gear_lever_position
- odometer
- ignition_status
- fuel_level
- fuel_consumed_since_restart
- door_status
- headlamp_status
- high_beam_status
- windshield_wiper_status
- latitude
- longitude

## Used Software

## Used Hardware

## Anleitung

<code>

Blub

</code>
