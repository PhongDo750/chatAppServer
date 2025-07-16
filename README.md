# 💬 ChatServer

ChatServer is a full-featured real-time messaging backend built with **Spring Boot**, supporting secure authentication and rich social interactions. It enables users to chat, post content, and interact with others in real-time.

## ✨ Features

### 🔐 Authentication
- Login via **username/password**
- Support for **OAuth2 login** (Google)

### 💬 Messaging
- Real-time **1-on-1** and **group messaging** via **WebSocket**
- **Push notifications** for instant message updates
- **Message history** and timestamps

### 🌐 Social Features
- Create, edit, and delete **posts**
- Like, comment, and share posts
- Follow your **friend feed** (recent activity)
- Search users and groups

---

## 🛠️ Tech Stack

| Technology      | Description               |
|-----------------|---------------------------|
| **Java 21**     | Programming language      |
| **Spring Boot** | Backend framework         |
| **WebSocket**   | Real-time messaging       |
| **JWT**         | Stateless authentication  |
| **OAuth2**      | Third-party login support |
| **Cloudinary**  | Upload image              |
| **Lombok**      | Reduces boilerplate code  |
| **Maven**       | Build tool                |

---

## 📁 Project Structure
```text
com.example.chatAppServer/
├── cloudinary/          # Cloudinary upload & image services
├── common/              # Common constants
├── controller/          # REST API controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── exceptionhandler/    # Xử lý ngoại lệ toàn cục (GlobalExceptionHandler)
├── helper/              # Utility/helper methods
├── mapper/              # Entity <-> DTO mapping
├── redis/               # Redis presence config
├── repository/          # Spring Data JPA Repositories
├── security/            # Basic security + CORS config 
├── service/             # Business logic
├── token/               # JWT provider, token store
└── websocket/           # WebSocket configuration & handlers
```
## 👤 Author

**Đỗ Gia Phong**  
📧 Email: [dogiaphong213@gmail.com](mailto:dogiaphong213@gmaill.com)  
💻 GitHub: [@PhongDo750](https://github.com/PhongDo750)   
