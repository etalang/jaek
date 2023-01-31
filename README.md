# jaek
Just Another Eta Kompiler

## Usage

### Environment
The `charlessherk/cs4120-vm` Docker image contains the necessary dependencies (javac, kotlinc, jflex, etc...). You can either build the project in the image or install the dependencies locally onto your machine. Note that building on the Docker image is **incredibly slow** on M1 chips, so it's preferable to build locally during development.

### Build
Either locally or inside the Docker image, use `make` at top level to compile project. A `etac.jar` file will be created.

### Run
Run the jar with `java -jar etac.jar`.

### Development
Ignore Gradle.
Add Kotlin dependencies to kotlinc -cp. Add Java dependencies to javac -cp.