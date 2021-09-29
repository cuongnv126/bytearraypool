# ByteArrayPool
An utility to usage efficience ByteArray in Kotlin and Java.

## Usage
### Create byte[] from pool
```kotlin
// Create byte[] with size = 16KB.
val byte16k = ByteArrayPool.getInstance().get(16 * 1024)
byte16k.use {
    // Do something with 16KB.
}
```
### RecyclingBufferedInputStream
```kotlin
// Use as input stream
val inputStream = FileInputStream("./something.txt").recyclingStream()
inputStream.recycling { // auto recycle
    it.stream { data, read, allRead ->
        // Do something on sink of stream.
        return@stream true
    }
}
```
### RecyclingBufferedOutputStream
```kotlin
// Similar with input stream
val outputStream = RecyclingBufferedOutputStream(
    ByteArrayOutputStream()
)

outputStream.use { // auto recycle and close
    // Do something with stream
}

outputStream.recycling { // auto recycle
    // Do something with stream
}
```

## Dependency

### Maven
```xml
<dependency>
  <groupId>org.cuongnv.bytearraypool</groupId>
  <artifactId>bytearraypool</artifactId>
  <version>0.0.1</version>
</dependency>
```

### Gradle Kotlin DSL
```kotlin
implementation("org.cuongnv.bytearraypool:bytearraypool:0.0.1")

```
### Gradle Groovy
```groovy
implementation 'org.cuongnv.bytearraypool:bytearraypool:0.0.1'
```

## License
```
Copyright 2021 Cuong V. Nguyen (github.com/cuongnv126).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
