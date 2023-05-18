# jaek 

**Just Another Eta Kompiler**. Compiler for [Eta](https://www.cs.cornell.edu/courses/cs4120/2023sp/project/language.pdf)—an imperative, statically-typed, C-like language—and its feature-rich extension [Rho](https://www.cs.cornell.edu/courses/cs4120/2023sp/project/rlang.pdf).

- Noah Schiff '25
- Kate Meuse '24
- Bryan Lu '24
- Tia Vu MS '24

## Build and Run

You can either run the compiler inside the [`charlessherk/cs4120-vm`](https://hub.docker.com/r/charlessherk/cs4120-vm) Docker image or on your local machine.
The Docker image is the preferred method, as it is the environment used by the CS 4120 autograder.

1. If on an Intel machine, pull the Docker image with `docker pull charlessherk/cs4120-vm`. Otherwise, read the **Apple Silicon** information below.
1. Create a folder on your local machine to share with the Docker image.
1. Use `make zip` to generate a **submission.zip**.
1. Move the **submission.zip** to the shared folder and unzip it.
1. `docker run -it -v <shared folder>:/home/student/shared charlessherk/cs4120-vm`
1. Inside the docker image, `cd shared/submission`
1. Build the compiler with `./etac-build` if you want to run it directly instead of with the test harness

Whether locally or inside Docker container, you can call the compiler through the CLI with `./etac [OPTIONS] [<source files>]`.
You can call the script with no arguments or with `--help` for a description of the options.

### Apple Silicon

Note that building on the Docker image is **incredibly slow** on M1 chips, so it's preferable to build locally during development.
For compile-time correctness tests only, you can use the [`charlessherk/cs4120-vm-arm`](https://hub.docker.com/r/charlessherk/cs4120-vm-arm) Docker image on M1 chips as the generated assembly is x86-64.

### Development
You do not need to build the Jar to develop locally. You can simply let IntelliJ create a run configuration for the main method, and IntelliJ should respect the downloaded dependencies and work in "production mode".

IntelliJ is able to index, find, and use all the local dependencies just fine. However, it takes longer to compile Kotlin when you have all plugins disabled. Thus, the build.gradle at the top level is modified to allow IntelliJ to compile Kotlin normally.

## Testing
### Test Harness
We have made the repository compatible with the `eth` testing harness. The `make zip` command and Docker setup includes the relevant files.
On the VM, we have two existing test suites—example tests provided by the course staff and our comprehensive test cases.

If the zip is unzipped in the `production` folder, the example tests pre-provided can be run from that directory with 
```bash
eth ~/eth/tests/pa6/ethScript -compilerpath <shared folder>
```
Include the `-p` flag to preserve the output of the compiler.
More `eth` flags can be found by accessing the native help page.
#### Personal Test Cases
We have constructed our own test cases that are compatible with `eth`.
These can be called using this format, with the shared folder inserted as above.

```bash
eth <shared folder>/<script file> -compilerpath <shared folder>
```
The script files for each segment can be found here:
- [lexer](src/tests/lexer/errorScript) test cases (PA1)
- [parser](src/tests/parser/parseScript) test cases (PA2)
- [typing](src/tests/typing/ethScript) test cases (PA3)
- [ir](src/tests/ir/ethScript) test cases (PA4)
- [assembly](src/tests/assembly/ethScript) test cases (PA5)
- [all](src/tests/ethScript) test cases (PA6)

### GitHub Actions
We have set up a GitHub Actions workflow to run the test harness on every push to any branch. The workflow will fail if any of the test cases fail. The workflow can be found [here](.github/workflows/eth.yml). Currently, the workflow runs all the tests.