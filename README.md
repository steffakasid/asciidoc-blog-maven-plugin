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

The article definition YAML file must be placed under `src/site/article_definition.yml`. The name of the file can also be changed but remember to change your pom referencing the file as well. The structure is very simple and looks like the following for to blog posts:

```yaml
- Date: "25-05-2018"
  Title: "Updated website"
  Author: "sid"
  Tags: "PC-Krams, Site"
  Content: "articles/20180525.adoc"
- Date: "23-05-2018"
  Title: "From wordpress to maven-site"
  Author: "sid"
  Tags: "PC-Krams, Site, Java, Maven"
  Content: "articles/20180523.adoc"
```

The file will be processed sequential so first entry will be the first post visible on the page.

## Example Site

My website http://www.steffen-rumpf.de is created using this plugin. The full code can also be found on github.

## Further reading

- https://maven.apache.org/guides/plugin/guide-java-plugin-development.html
- https://maven.apache.org/plugins/maven-site-plugin/usage.html