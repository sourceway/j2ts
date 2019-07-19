# J2TS - Java to TypeScript

## What is J2TS?
The goal J2TS is to provide an annotation processor that generates TypeScript interfaces out of Java classes. 

## Usage
TBD


## Known alternatives
- [jtsgen](https://github.com/dzuvic/jtsgen): 
        A powerful annotation processor generating TypeScript interfaces from Java classes.
        Unfortunately not compatible with other annotations processors and limited incremental compilation support.
- [typescript-generator](https://github.com/vojtechhabarta/typescript-generator):
        Plugin for Maven and Gradle to generate TypeScript from Java classes.


## Releasing
```bash
./mvnw --batch-mode release:prepare release:perform release:clean
```
