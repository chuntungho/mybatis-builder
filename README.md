# mybatis-builder

A free GUI tool integrated with Mybatis Generator, compatible with both IDEA Ultimate and CE. Alternatively, it supports to "Run Mybatis Generator" on configuration file popup menu.

Supported databases: mysql/postgresql/oracle/sqlserver/sqlite/h2.

Provides with additional features as below.
- Auto merger for java file.
- Lombok @Data on java model.
- Unified custom mapper name.
- Custom mapper annotation, default is Spring @Repository.
- Select with lock, which will generate selectByPrimaryKeyWithLock/selectByExampleWithLock.