# 📧 Messaging Service Platform

This project is a comprehensive, multi-tenant messaging service built with Spring Boot. It provides a robust platform for tenants to manage users, create targeted segments, design message templates, and deliver communications through various channels like Email, SMS, and WhatsApp.

This service is designed to be run with its companion **Spring Cloud API Gateway**, which provides critical features like rate limiting and a unified entry point.

> 🏢 **Note**: This project was developed during a backend internship at **4Sale International Co.**

## ✨ Key Features

- **🏢 Multi-Tenancy**: A secure system where each tenant manages their own isolated set of users, templates, and segments
- **🔐 Role-Based Access Control**: Differentiates between ADMIN (system-wide management) and TENANT (organization-specific management) roles
- **🚦 API Gateway Integration**: Comes with a pre-configured Spring Cloud Gateway that provides rate limiting (1 request/sec per IP) specifically for message sending endpoints
- **👥 User Management**: Tenants can create, view, update, and delete users within their organization
- **🎯 Dynamic User Segmentation**: Create powerful user segments based on criteria like city, user type, and age for targeted campaigns
- **📝 Template Engine**: Design and manage reusable message templates with dynamic placeholders (e.g., `${username}`)
- **📱 Multi-Channel Delivery**:
  - **📧 Email**: Integrated with SMTP (e.g., Gmail)
  - **📱 SMS**: Integrated with Twilio
  - **💬 WhatsApp**: Integrated with Twilio
- **⚡ Flexible Message Brokering**:
  - Supports Apache Kafka for high-throughput, partitioned messaging
  - Supports NATS JetStream for a lightweight, high-performance alternative
  - Easily switch between brokers using Spring Profiles
- **🚀 Advanced Messaging Capabilities**:
  - **Direct Messaging**: Send a message to a single user
  - **Campaign Messaging**: Send messages to entire user segments
  - **⏰ Priority Queues**: High-priority messages are processed with more resources in the Kafka setup
  - **📅 Message Scheduling**: Schedule messages for one-time future delivery
  - **🔄 Recurring Messages**: Set up CRON-based schedules for recurring message campaigns
- **🛡️ Resilience and Logging**:
  - Automatic retry mechanism for failed messages
  - Dead-letter queue handling for unrecoverable messages
  - Comprehensive logging of every message's lifecycle for auditing and debugging
- **🌐 Web Interface**: A user-friendly web UI built with Thymeleaf and Tailwind CSS for both Admins and Tenants to manage the platform

## 🚦 API Gateway & Rate Limiting

To enhance security and ensure the stability of the Messaging Service, this project is designed to be run behind a dedicated Spring Cloud API Gateway. This gateway acts as the single entry point for message sending API requests.

### 🔑 Key responsibilities of the Gateway:

- **🚧 Rate Limiting**: Protects the `/api/message/**` endpoints by enforcing a limit of 1 request per second for each client IP address. This prevents abuse and ensures fair usage
- **🌉 Centralized Routing**: Manages routing to the backend service for message requests

The complete source code and setup instructions for the gateway can be found in its own repository:
**[megox/spring-cloud-gateway-ratelimiter](https://github.com/megox/spring-cloud-gateway-ratelimiter)**

## 🏗️ System Architecture

The application is built around a modular, service-oriented architecture where the API Gateway is the front door.

```
Client -> API Gateway (Port 8081) -> Messaging Service (Port 8080) -> Message Broker -> Handlers
```

1. **🚪 Entry Point (API Gateway)**: Message sending requests first hit the Spring Cloud Gateway on port 8081. It applies rate limiting filters
2. **🛣️ Routing**: The Gateway forwards valid message requests to the MessageService on port 8080
3. **💾 Message Persistence**: The MessageService saves the message to a MySQL database with a status (PENDING, SCHEDULED, SENT, FAILED)
4. **📨 Message Broker (Kafka/NATS)**:
   - Immediate messages (PENDING) are sent directly to a message broker topic/subject
   - Scheduled messages are picked up by a MessageSchedulerService which sends them to the broker when they are due
5. **👂 Message Consumers**: Consumers listen on the broker's topics/subjects. When a message is received, it's passed to the appropriate handler based on its channel (Email, SMS, etc.)
6. **🔧 Channel Handlers & Providers**: Each channel has a MessageHandler that contains the business logic (retries, logging) and calls a Provider (e.g., EmailProvider, SmsProvider) which handles the final delivery

## 🛠️ Technologies Used

- **🍃 Backend**: Spring Boot 3
- **☕ Language**: Java 21
- **🌉 Gateway**: Spring Cloud Gateway
- **💾 Data**: Spring Data JPA, Hibernate, MySQL, Redis (for rate limiting)
- **🔐 Security**: Spring Security (Form-based Login & JWT for APIs)
- **📨 Messaging**:
  - Spring for Apache Kafka
  - NATS JetStream Client (jnats)
- **🌐 Web**: Spring MVC, Thymeleaf, Tailwind CSS
- **🔗 API**: Spring Web
- **🌍 External Integrations**:
  - Twilio SDK (for SMS & WhatsApp)
  - JavaMailSender (for SMTP/Email)
- **🔧 Tooling**: Maven, Lombok, MapStruct
- **🐳 Infrastructure**: Docker, Docker Compose

## 🚀 Setup and Installation

### 📋 Prerequisites

- ☕ Java 21 (or newer)
- 📦 Apache Maven
- 🐳 Docker and Docker Compose
- 🗄️ A MySQL database instance
- 📥 A local clone of the API Gateway repository

### 1. 🗄️ Database Setup

Create a MySQL database named `message_service_db`.

Update the database URL, username, and password in this project's `src/main/resources/application.properties`.

### 2. ⚙️ Configure External Services

In this project's `application.properties`, update your credentials for:

- **📧 SMTP (Email)**: `spring.mail.*` properties
- **📱 Twilio (SMS & WhatsApp)**: `twilio.*` properties
- **🔐 Admin Key**: Set a secret key for creating admin accounts: `security.admin.registration-key`

### 3. 🚀 Run Backend Infrastructure

This project and its gateway require several backend services.

**Start Redis**: The API Gateway requires Redis for rate limiting. Navigate to the Spring-Gateway project directory and run:

```sh
# This uses the compose.yaml in the gateway project
docker-compose up -d
```

**Start Kafka/NATS**: Navigate to this Messaging_Service project directory and run:

```sh
# This uses the docker-compose.yml in this project
docker-compose up -d
```

### 4. 📝 Choose a Messaging Profile

In this project's `application.properties`, set the active Spring profile to either `kafka` or `nats`.

```properties
# To use Kafka
spring.profiles.active=kafka
```

### 5. ▶️ Run the Applications

Start the applications in the correct order:

**Run the Messaging Service (Port 8080):**
In the Messaging_Service directory, run:

```sh
./mvnw spring-boot:run
```

**Run the API Gateway (Port 8081):**
In a new terminal, navigate to the Spring-Gateway directory and run:

```sh
./mvnw spring-boot:run
```

The web interface is accessible at `http://localhost:8080`, but all API calls should go through the gateway at `http://localhost:8081`.

## 📖 How to Use

> **⚠️ Important**: With the gateway running, API requests for sending messages should be sent to the gateway's port (8081), not directly to the service port (8080). The gateway will forward the request after applying rate limiting. Other endpoints like the web interface can be accessed directly on port 8080.

### 🔐 Register

Navigate to the web UI at `http://localhost:8080/register`.

Register as a standard Tenant.

### 🔑 Login

Log in with your new credentials at `http://localhost:8080/login`.

### 📊 Use the Tenant Dashboard

- Create users, templates, and segments via the web interface
- When you use the "Send a Message" or "Send Direct Message" features, the application will automatically make an API call to itself, which should be routed through the gateway

### 🔗 API Example (Directly)

To send a message via the API, you would make a POST request to the gateway's port:

```http
POST http://localhost:8081/api/message/request
Content-Type: application/json

{
  "tenantId": 1,
  "targetType": "USER",
  "targetId": 1,
  "channel": "EMAIL",
  "templateId": 1,
  "priority": "STANDARD"
}
```

If you send more than one of these requests per second from the same IP, the gateway will respond with `429 Too Many Requests`.

## 🤝 Contributing

Feel free to submit issues, fork the repository, and create pull requests for any improvements.
