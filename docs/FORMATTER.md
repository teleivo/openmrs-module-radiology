# Formatter
# java.xml
The `tools/formatter/java.xml` file contains coding conventions used by the OpenMRS Radiology Module for its Java files.

The conventions are close to the OpenMRS conventions described in
http://wiki.openmrs.org/display/docs/Coding+Conventions but do differ in some
aspects.

Import java.xml into Eclipse so that control-shift-f will format your code
accordingly.

# Automation
A maven plugin is used to automatically format .java and .js files according to
the rules defined in java.xml when compiling the radiology module with maven.

You can run the formatter from the command line with
```bash
mvn formatter:format
```

If you are having trouble with the formatter or want to see more details about
whats going on just do
```bash
mvn formatter:format --debug
```
