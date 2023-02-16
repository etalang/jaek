# jaek
Just Another Eta Kompiler

## Usage

### Environment
The `charlessherk/cs4120-vm` Docker image contains the necessary dependencies (javac, kotlinc, jflex, etc...). You can either build the project in the image or install the dependencies locally onto your machine. Note that building on the Docker image is **incredibly slow** on M1 chips, so it's preferable to build locally during development. You should start the Docker image with `--network none`. You should share the **production** directory.

Ensure you are using Gradle 7.6. You should automatically have the wrapper from the gradle-wrapper.properties. This version supports our version of Kotlin, and it is the version of Gradle used by the Docker image.

### Development
You do not need to build the Jar to develop locally. You can simply let IntelliJ create a run configuration for the main method, and IntelliJ should respect the downloaded dependencies and work in "production mode".

IntelliJ is able to index, find, and use all the local dependencies in dependencies/ just fine. However, it takes longer to compile Kotlin when you have all plugins disabled. Thus, I modified the build.gradle at the top level to allow IntelliJ to compile Kotlin normally.

### Production
We have a special build.gradle file in production/, along with the downloaded offline dependencies. These are configured to work fully offline!

On the VM: ensure the shared folder has **src/, dependencies/, and build.gradle**.
You can use the `make zip` command locally to generate a zip with the necessary files, and then move it to the shared folder.
On M1s, the AMD64 image often freezes, but it does finish a nonzero quantity of times.

On the VM or locally: run `gradle6 --no-daemon shadowJar`.
Then, navigate to `production/build/libs` and run the jar with `java -jar jaek-1.0-all.jar`.
The production environment has an `etac-build` script that will run this gradle command for you.
Both environments have an `etac` script that lets you use the jar CLI with format `./etac [OPTIONS] [<source files>]`.

### Testing
We have made the repository compatible with the `eth` testing harness. The `make zip` command above includes the relevant files. On the VM, we have two existing test suites.
If the zip is unzipped in the `production` folder, the example tests pre-provided can be run from that directory with 
```bash
eth ~/eth/tests/pa2/ethScript -compilerpath ~/shared/production/
```
Our personal test cases can be run with
```bash
eth src/tests/errorScript -compilerpath ~/shared/production/
```
Include the `-p` flag to preserve the `.lexed` output of the lexer. 
More `eth` flags can be found by accessing the native help page.