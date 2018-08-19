# Swift Solutions Task Scheduler

## Team Members
| Name             | GitHub | UoA UPI
| ---------------- | ------------ | --------- |
| Winston Zhao | @winstonzhao | wzha539 |
| Simon Su | @sloushsu | zsu801 |
| Harith Wannigama | @WanniCode | hwan678 |
| Bowen Zheng | @bowenzheng98 | tzhe946 |
| Kerwin Sun | @KerwinSun | ksun182 |

## Overview
A graph in DOT format that represents that the multiple task scheduling with communication costs input is required. The nodes and edges should have weights that represent, processing time and communication costs respectively. The scheduler will find the optimal schedule that can minimize the time used to do all the tasks.

Inputs: 	&#60;INPUT.dot&#62; 	&#60;P&#62; 	&#60;[OPTION]&#62;

Optional Flags:

&nbsp;&nbsp;&nbsp;&nbsp;-p N&nbsp;&nbsp;&nbsp;&nbsp;use N cores for execution in parallel (default is sequential)

&nbsp;&nbsp;&nbsp;&nbsp;-v&nbsp;&nbsp;&nbsp;&nbsp;visualize the search

&nbsp;&nbsp;&nbsp;&nbsp;-o OUTPUT&nbsp;&nbsp;&nbsp;&nbsp;output file is named OUTPUT (default is INPUT-output.dot)

&nbsp;&nbsp;&nbsp;&nbsp;-verbose &nbsp;&nbsp;&nbsp;&nbsp;show the debug messages

This program will output a dot file that represents the optimal schedule. The nodes will have properties "Start" and "Processor", which represent the start time and processor that the task has been scheduled on.

## Building Project from Source Code
In an IDE with Maven installed, run the Maven clean and install goals. This will generate a .jar file in the target folder.
This jar is ready to run.

## Where to Find Information
 Wiki (See side bar):
 - Meeting Minutes
 - Decisions
 - Background Research
 - Dependencies (External Libraries) used (or lack thereof)
 - Development Workflow

