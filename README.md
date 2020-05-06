# asciidoc-template-maven-plugin

## Introduction

I searched for a way to use the maven-site plugin somehow in a blog like way. That means I wanted to have multiple article/ blog post files which should then be generated into on single html static content page. I didn't find a way to do this with the maven-site plugin directly so I decided to create a simple maven plugin which could read a YAML file which basically defines the blog posts and generates a main page from this. Also a simple paging is included into the plugin.

## Build

[source,sh]
----
mvn clean install
----

## Usage

Add the following to your pom of a maven-site project:

```xml
<plugin>
    <groupId>de.steffenrumpf</groupId>
    <artifactId>asciidoc-template-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <definitionFileName>article_definition.yml</definitionFileName>
        <outputFile>index</outputFile>
        <pageSize>10</pageSize>
    </configuration>
    <executions>
        <execution>
        <phase>generate-sources</phase>
        <goals>
            <goal>generate_blog_posts</goal>
        </goals>
        </execution>
    </executions>
</plugin>
```

## Structure of article definition YAML file

Blog post must be just placed below `src/site/` in a typicall maven project. The files can also be structured in folders. Every asciidoc file must define the following metadata:

```yaml
:site-date: 25-11-20
:site-title: MacBook
:site-author: sid
:site-tags: PC-Krams
```
The `site-date` will be used to bring all posts into order.

## Example Site

My website http://www.steffen-rumpf.de is created using this plugin. The full code can also be found on github.

## Further reading

- https://maven.apache.org/guides/plugin/guide-java-plugin-development.html
- https://maven.apache.org/plugins/maven-site-plugin/usage.html