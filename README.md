# Mikhail2048's Properties Maven Plugin

This is a copy of the mojohause [properties-maven-plugin](http://www.mojohaus.org/properties-maven-plugin/).
That plugin of maven is not under active development, so I took charge. This project also is distributed over [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.txt).
In general, **usages of this plugin is very, very similar to one in the original version**, but with some extensions requested from users.

# 1. About plugin
Our plugin helps developers to do a couple of useful things with <a href="https://maven.apache.org/pom.html#properties">Maven Properties</a>:

1. Load properties into maven build from external sources - goal `read-project-properties`.
2. Write the properties used in maven build to files - goals `write-project-properties` and `write-active-profile-properties`.

Let's explore each of this goals one by one.

# 2. Goal `read-project-properties`

<h3>2.1 Common usage</h3>

As I said, this goal is used to load properties from different sources into Maven build. The simple usage will look like this:

```
<plugin>
    <groupId>tech.polivakha.maven</groupId>
    <artifactId>properties-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <configuration>
                <includes>
                    <include>/home/user/props/*.properties</include>
                </includes>
                <files>
                    <file>/home/user/common/common.properties</file>
                </files>
                <urls>
                    <url>http://example.com:8080/web/common</url>
                </urls>
            </configuration>
            <goals>
                <goal>read-project-properties</goal>
            </goals>
            <phase>compile</phase>
        </execution>
    </executions>
</plugin>
```

Let's briefly explore this plugin configuration:

1. Here, we set the phase to `compile`. This is because `read-project-properties` **does not have any default phase**, 
so if you did not specify phase explicitly, the plugin will not run.

2. In configuration, we stated, then properties that we want to load, are located in files that follow the 
pattern (see `<includes>` tag). We also have one specific file to load properties from (see `<files>` tag). And finally,
we have a files that is located somewhere over the network (see `<urls>` tag), and we want to load this properties file
from this network into the maven build process. 

These properties files must be just regular [java properties files](https://docs.oracle.com/cd/E23095_01/Platform.93/ATGProgGuide/html/s0204propertiesfileformat01.html). 
Any attempt to read files that does not represent the java properties file will result in error, meaning that the plugin will fail your build.

<h3>2.2 Possible sources for loading files</h3>

As of today, the latest version supports loading files from 3 sources:

1. Concrete files. Example is
   ```
   <files>
       <file>/path/to/common/common.properties</file>
       <file>/path/to/very/common/another.properties</file>
       <file>/path/to/not/that/common/some.properties</file>
   </files>
   ```
   These are concrete properties files that you would want to load properties from. **The properties will be loaded from each of this files
   in the order they declared**. That means, properties from file `some.properties` in case of conflict will override properties from file
   `another.properties`. So the order in which you specify files here matters. <br/>
   
   By _"conflict"_, we mean the situation, when we have multiple key value pairs where the key is the same, but value is different.<br/>

2. Loading files from URL
   ```
   <urls>
       <url>http://example.com:8080/web/common</url>
   </urls>
   ```
   Using `<urls>` tag, you can specify network resources. In our example we basically said, that there is a file, called `common`, that is
   located under `/web` directories path in the server `example.com`, served by process under port `8080` and is accessible over plain http.
   This is exactly the same way the one will request static content form Nginx for example. In regard to precedence, the same rules apply here,
   as we have for `files`. I.e. if you specified multiple URLs, then properties files would be loaded from each of these URLs, but the latter 
   URLs properties can override the properties from the former URLs.
3. Files by pattern
   ```
   <includes>
       <include>/home/user/props/*.properties</include>
       <include>/etc/service/configs/**/*.properties</include>
   </includes> 
   ```
   Here, we specified, that any file, that has `.properties`  extension and is located at `` is a properties file that should be loaded.
   Along with that, files that have `.proeprties` extension and located somewhere under `/etc/service/configs/` or in its child directories, 
   are properties files that should be loaded. 
   
   The order properties files, loaded by `/home/user/props/*.properties` _**is not guaranteed**_. You have only guarantee, that files, loaded
   by `/home/user/props/*.properties` can potentially override any properties from previous `<include>` tag, in case of conflict.

