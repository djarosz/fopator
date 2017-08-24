# fopator - Formating Object PDF Creator

fopator is bulk PDF creator. It reads XML data from one folder pairs it with XSLT and creates Apache FOP
framework to create resulting PDF file

Features:
- generates PDFSs in many threads (for performance reasons)
- different xslt locations and PDF output locations can be configured using regular expressions on original XML file
- configurable (you can define you own application.properties file and override default values)
  For configuration parametres see default [application.properties](./src/main/resources/application.properties) file

To run example:

```
  ./example/run.sh
```

Main components:
- Spring Boot
- Spring Integration
- Apache FOP framework

## License

fopator is licensed under the [MIT](./LICENSE).
