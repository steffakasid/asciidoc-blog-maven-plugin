package de.steffenrumpf;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "generate_blog_posts", defaultPhase = LifecyclePhase.PRE_SITE)
public class GenerateBlogPosts extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "article_definition.yml", required = true)
    private String definitionFileName;

    @Parameter(defaultValue = "index", required = true)
    private String outputFile;

    @Parameter
    private int pageSize = 0;

    public void execute() throws MojoExecutionException {
        Path outDir = Paths.get(project.getBasedir() + File.separator + "generated_src" + File.separator + "site"
                + File.separator + "asciidoc");

        prepareOutputDir(outDir);

        try {
            File src = new File(project.getBasedir() + File.separator + "src" + File.separator + "site");

            compile(src, outDir);

        } catch (Exception e1) {
            getLog().error(e1);
        }
    }

    private void compile(File src, Path outDir) throws FileExistsException {
        if (src.exists()) {
            // TODO: Files must be sorted by site-date
            for (int i = 0; i < src.list().length; i++) {
                File srcFile = new File(src.getAbsolutePath() + File.separator + src.list()[i]);
                if (srcFile.isDirectory()) {
                    compile(srcFile, outDir);
                } else {
                    Asciidoctor asciidoctor = JRubyAsciidoctor.create();
                    Map<String, Object> options = new HashMap<String, Object>();

                    Document document = asciidoctor.loadFile(srcFile, options);
                    Map<String, Object> attrs = document.getAttributes();

                    try (final FileWriterWithEncoding writer = new FileWriterWithEncoding(
                            outDir.toAbsolutePath() + File.separator + outputFile + ((i > 0) ? i : "") + ".adoc",
                            "UTF-8")) {

                        final StringBuffer output = new StringBuffer();

                        if (pageSize > 0) {
                            // TODO: src.list().length might not be the sum of all articles if subdirs are
                            // used. Idea: Create a method to gather all files and then order them by date.
                            output.append(createPagingString(i, outputFile, src.list().length));
                        }
                        // TODO: must use relative path to project.getBaseDir()
                        output.append(createBlogPost(attrs, srcFile.getPath()));

                        writer.write(output.toString());
                    } catch (Exception e) {
                        getLog().error(e);
                    }
                }
            }
        } else {
            throw new FileExistsException(src);
        }
    }

    private static String createBlogPost(Map<String, Object> blogEntry, String postPath) {
        StringBuffer output = new StringBuffer();
        output.append("== ");
        output.append(blogEntry.get("site-title"));
        output.append("\n\n");
        output.append("Posted on: ");
        output.append(blogEntry.get("site-date"));
        output.append(" by ");
        output.append(blogEntry.get("site-author"));
        output.append("\n\n");
        output.append("include::");
        output.append(postPath);
        output.append("[]\n\n'''\n\n");
        
        return output.toString();
    }

    private static String createPagingString(int currentPage, String outputFile, int countPages) {
        StringBuffer returnSB = new StringBuffer();

        returnSB.append(":linkattrs: true\n\n");

        returnSB.append("[.pagination]\n");
        returnSB.append("--\n");
        for (int i = 0; i < countPages; i++) {
            String filename = outputFile + i + ".html";
            String role = "";
            if (i == currentPage) {
                role = ", role=active";
            }
            if (i == 0) {
                filename = outputFile + ".html";
            }
            returnSB.append("link:" + filename + "[\"" + i + "\"" + role + "]\n");
        }

        returnSB.append("--\n");

        return returnSB.toString();
    }

    static Map<Integer, List<Map<String, String>>> partition(List<Map<String, String>> list, int pageSize) {
        if (pageSize == 0) {
            Map<Integer, List<Map<String, String>>> returnV = new HashMap<Integer, List<Map<String, String>>>();
            returnV.put(pageSize, list);
            return returnV;
        } else {
            return IntStream.iterate(0, i -> i + pageSize).limit((list.size() + pageSize - 1) / pageSize).boxed()
                    .collect(toMap(i -> i / pageSize, i -> list.subList(i, min(i + pageSize, list.size()))));
        }
    }

    public void prepareOutputDir(Path folder) {
        try {
            FileUtils.deleteQuietly(folder.toFile());
            Files.createDirectories(folder);
        } catch (IOException e) {
            getLog().error(e);
        }
    }
}