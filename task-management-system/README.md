# Task Management System

## Overview
The Task Management System is designed to help users manage their daily tasks efficiently. Users can create, view, update, and delete tasks, subtasks, and stories within a project. Each task, subtask, and story can have a description, deadline, and assigned user.

## Features
- **User Registration and Login**: Users can register and log in to the system.
- **Task Management**:
  - Create new tasks with a description and deadline.
  - View a list of tasks assigned to the logged-in user.
  - Update tasks, including changing the description, deadline, and status.
  - Delete tasks.
  - Move tasks, subtasks, and stories across the project hierarchy.
  - Get the current workload of a user.
  - View the number of tasks in different states like assigned, pending, etc.

## Project Structure
```
task-management-system
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── controller
│   │   │   ├── model
│   │   │   ├── repository
│   │   │   ├── service
│   │   │   └── exception
│   │   └── resources
├── pom.xml
└── README.md
```

## Technologies Used
- Java
- Spring Boot
- Maven

## Getting Started
1. Clone the repository.
2. Navigate to the project directory.
3. Run the application using your preferred IDE or via command line.

## License
This project is licensed under the MIT License.