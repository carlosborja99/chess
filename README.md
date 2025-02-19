# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
https://sequencediagram.org/index.html?presentationMode=readOnly&shrinkToFit=true#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUpjBKlyBWy5lKABYnE5uj0pqN1MB7PcpgBRKDeMowPQcGCQtEYyYHXGGU6yeRKFTqcp0sAAVQ6sJud2xQsUyjUqhOhWMpQAYkhODBpZRFWywnLgKNMIqRSr8flCRgSU4AMwUqkq2n0vpMlnlYAISHADhheQAa3QvPN2kt6hOoOK4JgaB8CAQh1jKjVMgjyrFID9mX1cJuioVmdFqtOGoUHA5+cVKZEacFJZVpRzKDkKAUPjAqVhwC7qSL4eFWbL6p0pUrHM73dr-OjhUBF1ZCOhyLUiawi+B86K1EuMGuHTuk3Kqyefe79QgIbQJ6m+zrx2t8GQRJgACYyRSBkfTWMYKejxTBeqRXjed6rAcpjoBwpheL4-gBNA7B0jAAAyEDREkARpBkWSvnkhRHPu1R1E0rQGOoCRoBSJqjLMLxvBwfLFJQ6ZbvuPR0SgDH6K8SwAucwIxvWKDlAgmE6rCGFYai6KxNic6NsOpbiigUoytx2JDkqpbphq2q6jW2hGjA3E6ZGqjWraYD2k6vQujSdLjIyzLQOUkrBP20BIAAXigywwAcFojju-LlAAcigyQwCBYHoI+og7hxrIyTqkSqBumApWFrGsoeQx-hBQF9HF16hgB959Cxe4gqcNnlJ+5K9D+hWjMVXxleBlWQdBnBwd4fiBF4KDoOhmG+MwOHpJkmA2emxGshU0gMmhDL1AyzQtJRqjUd0XUJYt7FCZxB1oIJQJsSJVBxtOqQ+f5sLSCg3CZHJGKKamKDWQRtmkvZlJ9NSqhui5HpuaynkgQ9AVhkphQhapMgvepKAUFRiSwiAPjMuY6O7YkxYqSq+njjJk1PSjmSYjA2O46QO3UaYpiI1a9W-aUTUUgcMEDQhgSQhyaHQjAADif6qtNeFzb9C15eUFSi+tW32H++39vFaCPldC4nflZ0XUucuieUd0w5Tr0oO9CmJXiBIc6SACMzpA66zmnp67l6l53Yw4FNU3Q2CNNmKMAStbYCwqrowRX+RO6WzNoO1+Dmu057qe6y7Kcig3KxGGFkjgUGoSuLMd-gAkmgABm0CbAQmNlygsejNixjzqzodN1XtdQPXTOF3pz7hWL6ktygwuxLC3c13X8SE7bAqnCl5RN5Pm567ltVXFM0dqC5FT9HvFfSC5jvvg6xJPLhea-u1ExfDoCCgEG+bHg-Tx7+PD97DAjQB1ddmORbIfhToDPeqgD5H0rqfco59L7XxmigN+RUP5TCfi-FB98+hfC-n+H+f8eb9U8INRC2AcbYG4PAXMhgm4pCQTLYBxsSgK1qA0FWasfagXKjRXoeDRgAJBLrS6+VYoax4Z-P848VhhhytdcEZsoB+RQNPP8EdPqiR+sA+0ztU7A1Bh7CG5QfBcL9nDL66ZO6qFKBKKKyQm5RykXHQeicGqkmaoDfR7tXJejZP6HOecwAFysaTLUOoORN3HoaHQYR+FiCUsvPWkVord2kNlTew95YHl3jAs+F9iRBW1nVQoDVQEeJyaME+eTL6FL6rBEh-MAiWBehJGKAApCAOoxZ-kCBgkAQZGHmGYSRKokpyItD3urS8PCKSUOAM0qAcAIASSgLMY+0hBHHREaUQGcyFlLJWSsAA6iwCuG0WgACE0IKDgAAaVwbkuB+TClyKIl9U23klGPTuprDRgdvpALfE7F2XiM5GLMqYr5sNamL2UgnMUAArTpaBYRIvSrneSWIXFRmfKU9x3RslpxBt48Gvjs5ckxQXeGGZibZlSCgfpBzoBY2WdAP5SU4WWRbPS-pJ8WUrPjpZHceLHQEs8W7MFvifR+gDDAYMFVgoh1HEYccHSulN2xWWeRKhIqJmTAk4RS5ShqrQA4yEmUEBYpSrijmXNehQWZrzBpQ0AheHmS+NssBgDYEoYQee2Eb5iEGYRXcLDKgrTWhtLaxgimhOepbWKSYjDaD0AYZN8hNA6GFRzGAABWMBRKDE+K9rCDgagczEAbkkCA1cYCUBZJiaqmbs3aLKdzMwnAgA
