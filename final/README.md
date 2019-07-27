# JRadar project

A very simple project developed with java + arduino.


## Requirements

This application requires the following native libraries:

	1. JOGL
	2. RXTX

With Linux you can install RXTX library by executing `apt install librxtx-java`

## Error fixing

If no serial ports are available on Linux launch the command `sudo chmod 777 /dev/tty*`
or launch application with root.