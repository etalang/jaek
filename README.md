# jaek
Just Another Eta Kompiler

## Usage

### Environment
The `charlessherk/cs4120-vm` Docker image contains the necessary dependencies (javac, kotlinc, jflex, etc...). You can either build the project in the image or install the dependencies locally onto your machine. Note that building on the Docker image is **incredibly slow** on M1 chips, so it's preferable to build locally during development. You should start the Docker image with `--network none`. You should share the **production** directory.

Ensure you are using Gradle 6.7.1. You should automatically have the wrapper from the gradle-wrapper.properties. This version supports our version of Kotlin, and it is the version of gradle we force onto the Docker image.

### Development
You do not need to build the Jar to develop locally. You can simply let IntelliJ create a run configuration for the main method, and IntelliJ should respect the downloaded dependencies and work in "production mode".

IntelliJ is able to index, find, and use all the local dependencies in dependencies/ just fine. However, it takes longer to compile Kotlin when you have all plugins disabled. Thus, I modified the build.gradle at the top level to allow IntelliJ to compile Kotlin normally.

### Production
We have a special build.gradle file in production/, along with the downloaded offline dependencies. These are configured to work fully offline!

On the VM: ensure the shared folder has **src/, dependencies/, and build.gradle**. On M1s, the AMD64 image often freezes, but it does finish a nonzero quantity of times.

On the VM or locally: run `gradle6 --no-daemon shadowJar`. Then, navigate to `production/build/libs` and run the jar with `java -jar jaek-1.0-all.jar`.