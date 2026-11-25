# JobSphere

JobSphere is a Spring Boot backend for a job portal. Its codebase is organized around job seekers, employers, job listings, applications, profile management, and administration.

This repository contains the backend application in the `jobSite/` directory.

## Main capabilities represented in the codebase

- **Authentication and security:** Spring Security, JWT-based authentication components, OTP-related services, and Google OAuth2 client integration.
- **Job seeker profiles:** profile details, skills, sectors, tags, social links, CVs, and projects.
- **Employer features:** company profiles and company verification.
- **Job management:** job listings, saved jobs, likes, applications, and job alerts.
- **Supporting services:** notifications, email, Cloudinary file/image integration, and PDF generation.
- **Administration:** admin authentication, user management, and company verification workflows.

The exact behavior and completeness of each workflow should be confirmed by running and testing the application.

## Technology stack

- Java 21
- Spring Boot 3.5.8
- Maven
- Spring Web
- Spring Security and OAuth2 Client
- Spring Data JPA
- PostgreSQL
- Flyway database migrations
- JWT (JJWT)
- Springdoc OpenAPI / Swagger UI
- Cloudinary
- Spring Boot Mail
- OpenPDF
- JUnit / Spring Boot Test

## Project structure

```text
Jobsphere/
└── jobSite/
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    └── src/
        └── main/
            ├── java/com/jobsphere/jobsite/
            │   ├── config/
            │   ├── controller/
            │   ├── dto/
            │   ├── repository/
            │   └── service/
            └── resources/
                ├── application.properties
                └── db/migration/
```

## Prerequisites

- JDK 21
- PostgreSQL
- Git

External integrations such as Google OAuth, email delivery, and Cloudinary also require their own credentials if you want to use those features.

## Run locally

1. Clone the repository:

   ```bash
   git clone https://github.com/kura458/Jobsphere.git
   cd Jobsphere/jobSite
   ```

2. Configure the database and required integration settings in your local application configuration. Supply credentials through environment variables or a private local configuration; do not commit passwords, API keys, JWT secrets, or OAuth client secrets.

3. Start the application with the Maven wrapper.

   On Windows:

   ```cmd
   mvnw.cmd spring-boot:run
   ```

   On macOS or Linux:

   ```bash
   ./mvnw spring-boot:run
   ```

Flyway migrations are included under `src/main/resources/db/migration`. Confirm your PostgreSQL database is reachable and that the application's database settings are correct before starting the service.

## API documentation

The project includes Springdoc OpenAPI. When the application starts successfully, the Swagger UI is commonly available at:

```text
http://localhost:8080/swagger-ui/index.html
```

If the server uses a different port or context path, adjust the URL accordingly.

## Security note

Never publish real credentials or production secrets in this repository. Use local environment variables or a secure secrets manager, and rotate any credential that has been committed accidentally.

## Author

[Kura Lemma](https://github.com/kura458)

---

Backend project for learning and developing a job-portal application.
