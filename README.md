Java-Enterprise-7-Glassfish-4-Netbeans-project-management-offices-callcenters-and-DB
====================================================================================

The system was developed using Java Enterprise 7.0 and glassfish server 4.0. It consists of:
-Two callcenters (B1,B2) made with WebServices and Enterprice Application Client, than communicate by jms/queue
-Every callcenter controls five offices and the callcenter communicate with them by the same number (five) of jms/queue .

Any request that may be send to the callcenter through Web Page and Servlet consists:
- Unique identifier (ID)
- List of signals, which indicates which offices we have need to contact.

The request is handled in a transactional context through the "two-phase commit" protocol.

Every request received to the office consists:
- Read from the database
- Resolve the signal
- Write to database.

Every office has three active replicas of the same database and communicate with the databases by jms/Topic.

This project was developed by Alessio Oglialoro, Gregory Callea and Riccardo Nocita.
