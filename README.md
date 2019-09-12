# mybatis-builder

![ci-status](https://travis-ci.org/chuntungho/mybatis-builder.svg?branch=master)

A free GUI tool integrated with Mybatis Generator, compatible with IntelliJ IDEA, Android Studio. Alternatively, it supports to "Run Mybatis Generator" on configuration file popup menu.

Supported databases: mysql/postgresql/mariadb/oracle/sqlserver/sqlite/h2.

Provides with additional features as below.
- Auto merger for java file.
- Lombok @Data on java model.
- Unified custom mapper type, example type and SQL file name.
- Custom mapper annotation, default is Spring @Repository.
- Select with lock, which will generate selectByPrimaryKeyWithLock/selectByExampleWithLock.
- Resolve placeholders of printed SQL in MyBatis log and "Copy as Executable SQL" to clipboard.

[Getting Started](https://chuntung.com/mybatis-builder/)
