# J2TS - Java to TypeScript [![](https://jitpack.io/v/eu.sourceway/j2ts.svg)](https://jitpack.io/#eu.sourceway/j2ts)

## What is J2TS?
The goal J2TS is to provide an annotation processor that generates TypeScript interfaces out of Java classes. 

## Usage

### Setup
#### Maven
Add JitPack Maven Repository
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add dependencies to your project
```xml
<dependencies>
    <dependency>
        <groupId>eu.sourceway.j2ts</groupId>
        <artifactId>j2ts-annotations</artifactId>
        <version>${J2TS_VERSION}</version>
    </dependency>
    <dependency>
        <groupId>eu.sourceway.j2ts</groupId>
        <artifactId>j2ts-apt</artifactId>
        <version>${J2TS_VERSION}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle
Add JitPack Maven Repository
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add dependencies to your project
```groovy
dependencies {
    implementation 'eu.sourceway.j2ts:j2ts-annotations:$J2TS_VERSION'
    annotationProcessor 'eu.sourceway.j2ts:j2ts-apt:$J2TS_VERSION'
}
```

### Code & Configuration
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
