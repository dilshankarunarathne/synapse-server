# Synapse Distribution Server

This project proposes a novel distributed programming paradigm specifically designed for resource-constrained devices operating at the edge of the network. The paradigm leverages the combined processing power of nearby devices to tackle computationally intensive tasks, overcoming limitations of individual devices. A Domain-Specific Language (DSL) facilitates user-friendly task specification for collaborative execution, while secure communication protocols ensure reliable and efficient data exchange among participating devices. This research aims to develop a scalable and fault-tolerant solution for distributed task execution, enabling efficient processing at the edge with reduced latency.

## Core Modules

1. **Job Management**
   - Implement functionalities to create, schedule, monitor, and manage jobs submitted by users through the Synapse DSL.

2. **Client Communication**
   - Develop mechanisms for communication between the central system and distributed devices (clients) for task execution and data exchange.

3. **Synapse DSL Interpreter**
   - Build an interpreter or compiler to translate Synapse DSL code into executable instructions for resource-constrained devices.

## Leader Election Mechanism

- Implement a leader election mechanism to ensure fault tolerance and data consistency.

## Synapse Language Specification

- Define the DSL specification to ensure clarity and completeness for users.

## Security

1. **Secure Communication**
   - Implement secure communication channels to protect data transmission between components (core system, clients, servers).

2. **Authentication and Authorization**
   - Integrate the AuthZ server for user authentication and authorization when accessing Synapse functionalities or managing jobs.

3. **Code Sandboxing**
   - Explore mechanisms to isolate task execution on client devices to prevent unauthorized access or malicious code execution.

## Communication

1. **Protocol Selection**
   - Choose an appropriate communication protocol or messaging system for reliable and efficient communication between the core system and clients.

2. **Data Serialization/Deserialization**
   - Define a format to represent and exchange data between components during task execution and communication.

## License

This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License. See the LICENSE file for details.  <img src="https://licensebuttons.net/l/by-nc-sa/4.0/88x31.png" alt="CC BYNCSA 40"></img>

Shield: [![CC BY-NC-SA 4.0][cc-by-nc-sa-shield]][cc-by-nc-sa]

This work is licensed under a
[Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License][cc-by-nc-sa].

[![CC BY-NC-SA 4.0][cc-by-nc-sa-image]][cc-by-nc-sa]

[cc-by-nc-sa]: http://creativecommons.org/licenses/by-nc-sa/4.0/
[cc-by-nc-sa-image]: https://licensebuttons.net/l/by-nc-sa/4.0/88x31.png
[cc-by-nc-sa-shield]: https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg

