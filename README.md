# Spotbugs 修改版

主要修改了以下文件，用于使FindSecBugs使用ehcache

```
 CHANGELOG.md                                       |  1 +
 spotbugs/build.gradle                              |  2 ++
 .../java/edu/umd/cs/findbugs/BugAnnotation.java    |  7 ++++
 .../umd/cs/findbugs/SAXBugCollectionHandler.java   | 41 ++++++++++++++++++----
 .../edu/umd/cs/findbugs/SourceLineAnnotation.java  |  3 +-
 .../findbugs/VersionInsensitiveBugComparator.java  |  2 +-
 .../classfile/FieldOrMethodDescriptor.java         |  3 +-
 .../cs/findbugs/classfile/MethodDescriptor.java    |  4 ++-
 .../classfile/analysis/AnnotationValue.java        |  3 +-
 .../cs/findbugs/classfile/analysis/MethodInfo.java |  3 +-
```

# 构建

```bash
gradlew publishToMavenLocal
```

