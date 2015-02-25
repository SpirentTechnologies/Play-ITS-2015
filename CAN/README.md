# CAN ELM 327 adaptation

## Introduction
This is an implentation of the CAN ELM 327 adaptation. It uses messages in OpenXC-Json-Format, and builds a current  value-database. The data is received from the [OpenXC Simulator] (https://github.com/openxc/openxc-vehicle-simulator) or directly via hardware from the [ELM327 Module](http://elmelectronics.com/DSheets/ELM327DS.pdf). This database is used by the [TTWorkbench] (https://github.com/TestingTechnologies/Play-ITS-2015/tree/master/TTCN) for further treatment. If the messages are received directly from the hardware, it is necessary to convert the messages to the OpenXC message format.

## Data-Table
The data-table has follwing entries:

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
- [OpenXC Simulator] (https://github.com/openxc/openxc-vehicle-simulator)
- [OpenXC message Format] (https://github.com/openxc/openxc-message-format)
- [OpenXC] (https://github.com/openxc)

## Used Hardware
- [ELM327 Module](http://elmelectronics.com/DSheets/ELM327DS.pdf)
- COHDA-Boxes
- CAN compatible Car

## Instruction

<code>1.a.) Start the OpenXC Simulator
1.b.) Connect the Hardware
2.) run file.jar</code>
