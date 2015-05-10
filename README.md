# fopator: Formating Object PDF Creator

fopator is bulk PDF creator. It reads XML data from one folder,  XSL (thus creating objects Formating objects file)

Features:
- generates PDFSs in many threads (for performance reasons)
- different xslt locations and PDF output locations can be configured using regular expressions on original XML file
- configurable (you can define you own application.properties file and overide default values)
  For configuration parametres see default [application.properties](./src/main/resources/application.properties) file

To run example:

  ./example/run.sh

Main components:
- Spring Boot
- Spring Integration
- Apache FOP framework

## License

fopator is licensed under the [MIT](./LICENSE).