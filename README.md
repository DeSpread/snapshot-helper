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

### Running the server

1. Configure the snapshot settings in `compose.yaml`.

2. Run the Docker container:
   ```bash
   docker compose up
   ```

### Example of POST API Usage

To request a snapshot using the POST API, send a request to the following endpoint:

`/api/v1/snapshot/multipart`

Example payload:

```json
{
    "sourceDirectoryPath":  "/home/ubuntu/source_directory_path",
    "s3Key": "{your_s3_object_name}.tar.lz4"
}
```

In this example:
- `sourceDirectoryPath` represents the directory you want to compress into a tarball. The files and subdirectories within this path will be compressed into a single `.tar.lz4` file.
- For instance, if `sourceDirectoryPath` is set to `/home/ubuntu/source_directory_path`, and the structure of `source_directory_path` is:

  ```
  source_directory_path
  ├── data_directory_1
  └── data_directory_2
  ```

  After compressing, the tarball will contain both `data_directory_1` and `data_directory_2`. When you extract the tarball, it will restore `data_directory_1` and `data_directory_2` exactly as they were in the original directory.

### Contributing
Feel free to submit pull requests or open issues to suggest improvements or report bugs.

## License
This project is licensed under the MIT License.