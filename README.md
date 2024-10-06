
# Snapshot Helper

Snapshot Helper is a utility designed to assist with managing and creating snapshots in a streamlined way.
The tool is written in Kotlin and uses Docker for containerization, making it easy to deploy and use in various environments.

## Features
- Automates snapshot creation
- Docker container support
- Easily configurable through YAML

## Getting Started

### Prerequisites
- Docker
- Java 21+ (Currently LTS)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/DeSpread/snapshot-helper.git
   cd snapshot-helper
   ```
2. Build the project:
   ```bash
   ./gradlew build
   ```

### Usage
1. Run the Docker container:
   ```bash
   docker compose up
   ```

2. Configure the snapshot settings in `compose.yaml`.

## Contributing
Feel free to submit pull requests or open issues to suggest improvements or report bugs.

## License
This project is licensed under the MIT License.
