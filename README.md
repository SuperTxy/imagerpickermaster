
Gradle
------
Add it in your root build.gradle at the end of repositories: 

```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
Add the dependency dependencies 

```
dependencies {
	compile 'com.github.SuperTxy:imagerpickermaster:v1.0.0'
}
```