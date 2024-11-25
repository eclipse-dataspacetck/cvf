# Dataspace TCK

```json
{
  "description": "A composable technology compatibility kit (TCK) for dataspace specifications built on the JUnit Platform.",
  "features": [
    "AUTOMATED",
    "EXTENSIBLE",
    "LANGUAGE_INDEPENDENT",
    "RUN_ANYWHERE"
  ],
  "options": [
    "Command line",
    "Docker",
    "JUnit"
  ],
  "modules": {
    "boot": "The bootstrap module used for interfacing between the TCK system and its host environment.",
    "core": "The core TCK framework and extensibility system.",
    "dsp": "Runtime and verification tests for the Dataspace Protocol Specification. Temporarily hosted here.",
    "tools": "Hosts the custom annotation processor to generate the test plan document",
    "buildSrc": "Contains a Gradle task definition to explicitly invoke the test plan generator"
  },
  "build": [
    {
      "cmd": "./gradlew clean build",
      "description": "builds and tests the project"
    },
    {
      "cmd": "./gradlew genTestPlan",
      "description": "Generates the test plan document and puts it in ./build. Check dsp/dsp-contract-negotiation/build.gradle.kts for an example usage. "
    }
  ],
  "run": {
    "commandline": "java -jar dsp/dsp-tck/build/libs/dsp-tck-runtime.jar -config config/tck/sample.tck.properties",
    "docker": "Coming soon",
    "junit": "See DspTckSuiteTest in the dsp-tck module"
  }
}
```
       
