// In build.gradle.kts at the root of your project
// Look for something like this around line 14:
// val VERSION: String by project

// Change it to:
val VERSION_NAME: String by project
// And update any references to VERSION to use VERSION_NAME
