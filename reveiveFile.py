READ srv_addr, srv_port, file_input_name, file_output_name
VARIABLE
	ds DatagramSocket
	dp DatagramPacket
	ia InetAddress
	buff, buffReceive, block, buffAck byte[]
	fos FileOutputStream
	code, errorCode byte


BEGIN
	// Création de la readRequest 
	buff[0] = 0
	buff[1] = 1
	int X, Y
	FOR X = 0 to file_input_name.length()
		buff[X + 2] = file_input_name.getChar(X)
	END FOR
	buff[X + 2] = 0
	FOR Y = 0 to "octet".length()
		buff[Y + 3] = "octet".getChar(Y)
	END FOR
	buff[Y] = 0

	// Envoi de la readRequest
	sendReadRequest(ia, ds, buff, srv_port)

	// Création du fichier de sortie
	File file = createNewFile(file_output_name)
	fos = new FileOutputStream(fos)
	bool again = true


	// Reception du fichier
	WHILE again
		buffReceive = new byte[516]
		dp = new DatagramPacket(buffReceive, buffReceive.length)
		ds.receive(dp)
		IF dp.length() < 516
			again = false
		END IF

		code = dp.getData()[1];
		block[0] = dp.getData()[2]
		block[1] = dp.getData()[3]
		
		IF code == 3
			fos.write(dp.getData(), 4, dp.length() - 4)

			// Création de la ACK Request
			buffAck[0] = 0
			buffAck[1] = 3
			buffAck[2] = block[0]
			buffAck[3] = block[1]

			// Envoi de la ACK Request
			sendRequest(ia, ds, buffAck, dp.getPort())
		ELSE IF code == 5
			errorCode = dp.getData()[3]
			fos.close()
			file.delete()

		END IF

	END WHILE

	fos.close()
	ds.close()

END