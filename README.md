# mybatis-builder

![ci-status](https://travis-ci.org/chuntungho/mybatis-builder.svg?branch=master)
[![Join the chat at https://gitter.im/mybatis-builder/community](https://badges.gitter.im/mybatis-builder/community.svg)](https://gitter.im/mybatis-builder/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[Getting Started](https://mybatis.chuntung.com) | [Donate](https://chuntung.com/donate)

A free GUI tool integrated with MyBatis Generator, which is specially compatible with IDEA CE.

Supported databases: mysql/postgresql/mariadb/oracle/sqlserver/sqlite/h2.

Provides with additional features as below.
- Merge existing MyBatis files automatically.
- Support select with lock statement.
- Support Lombok @Data annotation on java model.
- Customizable mapper annotation, default is Spring @Repository.
- Customizable patterns for mapper type, example type and SQL file name.
- "Copy as Executable SQL" from MyBatis log.
- "Run MyBatis Generator" with official configuration file.


References:

- Project [dependencies](dependencies.md)
- MyBatis Generator [doc](http://mybatis.org/generator)
- MyBatis Dynamic SQL [doc](https://github.com/mybatis/mybatis-dynamic-sql)
- [Free MyBatis Plugin](https://github.com/chuntungho/free-mybatis-plugin/releases/) for Idea IC
