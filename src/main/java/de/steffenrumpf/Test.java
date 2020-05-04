package de.steffenrumpf;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;

public class Test {
    

    public static void main(String[] args) {
        File source = new File("src/test/resources/project-to-test/src/site/articles/article1.adoc");
        Asciidoctor asciidoctor = JRubyAsciidoctor.create();
        Map<String,Object> options = new HashMap<String,Object>();
        Document document = asciidoctor.loadFile(source, options);
        Map<String,Object> attrs = document.getAttributes();
        System.out.println(attrs.get("site-date"));
        System.out.println(attrs.get("site-title"));
        System.out.println(attrs.get("site-author"));
        System.out.println(attrs.get("site-tags"));
    }
}