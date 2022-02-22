# RSA-Client-Server-Signature-JAVA

####Created for learning

Following are the steps to initiate the project

- javac RSAKeyGen.java
- java RSAKeyGen <client_name>
- Place created .pub file in *Server* folder and both .prv and .pub file in *Client* folder.
- javac Server.java (Inside Server Directory)
- java Server <port>
- javac Client.java <Inside Client Directory)
- java Client <host_name> <port> <client_name>

This will connect client and server on provided port and send a encrypted post from client to server using RSA signature. 

