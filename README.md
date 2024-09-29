# Dataspace TCK

```json
{
  "description": "A composable test compliance kit (TCK) for dataspace specifications built on the JUnit Platform.",
  "features": [
    "AUTOMATED",
    "EXTENSIBLE",
    "LANGUAGE_INDEPENDENT",
    "RUN_ANYWHERE"
  ],
  "options": [
    "Command Line",
    "Docker",
    "JUnit"
  ],
  "modules": {
    "boot": "The bootstrap module used for interfacing between the TCK system and its host environment.",
    "core": "The core TCK framework and extensibility system.",
    "dsp": "Runtime and verification tests for the Dataspace Protocol Specification. Temporarily hosted here."
  },
  "build": [
    "./gradlew clean build"
  ],
  "run": {
    "commandline": "java -jar dsp/dsp-tck/build/libs/dsp-tck-runtime.jar -config config/tck/sample.tck.properties",
    "docker": "Coming soon",
    "junit": "See DspTckSuiteTest in the dsp-tck module"
  }
}
```
       
