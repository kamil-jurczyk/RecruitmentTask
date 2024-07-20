## About project
Recruitment task with such requirements:
"given username and header “Accept: application/json”, list all his github repositories, which are not forks"

## Technology used in project
* Java 21
* Spring boot 3.3.2
* Maven
* Lombok
* WireMock

## How to run

### 1. Clone the repository
Please clone the repository by https or ssh (below I use the https method).
```
https://github.com/kamil-jurczyk/RecruitmentTask.git
```
### 2. Provide Bearer Token
Please insert your Bearer token in application.properties file in github.token

### 3. Run the project
Next, you can run the project using Maven:
```
mvn spring-boot:run
```

## REST API Endpoint

Application provides one endpoint:
```
/users/{username}/repos (GET)
```
For existing username in GitHub returns a response in such format:
```
[
    {
        "repositoryName": "kindle-clippings",
        "ownerLogin": "kamil-jurczyk",
        "branchList": [
            {
                "name": "master",
                "commit": "cc6e5474ac20e186a1a56f0b48ec522b8c14818e"
            }
        ]
    }
]
```
If user was not found response is:
```
{
    "status": 404,
    "message": "User not found"
}
```
