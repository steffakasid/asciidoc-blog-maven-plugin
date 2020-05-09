package de.steffenrumpf;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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

            getLog().info("Gathering all relevant files...");
            List<ExtendedDocument> allDocuments = getAllFiles(src);

            getLog().info("Sorting found files...");
            Collections.sort(allDocuments);

            getLog().info("Partition all documents based on pageSize...");
            Map<Integer, List<ExtendedDocument>> list = partition(allDocuments, pageSize);

            getLog().info("Creating blog pages...");
            compile(list, outDir);

        } catch (Exception e1) {
            getLog().error(e1);
        }
    }

    private List<ExtendedDocument> getAllFiles(File folder) {
        List<ExtendedDocument> fileList = new ArrayList<ExtendedDocument>();
        if (folder.exists()) {
            for (File f : folder.listFiles()) {
                if (f.isDirectory()) {
                    fileList.addAll(getAllFiles(f));
                } else {
                    if (f.getName().endsWith(".adoc")) {
                        getLog().info("Found: " + f.getAbsolutePath());
                        Asciidoctor asciidoctor = JRubyAsciidoctor.create();
                        Map<String, Object> options = new HashMap<String, Object>();

                        ExtendedDocument document = new ExtendedDocument(asciidoctor.loadFile(f, options), f);
                        fileList.add(document);
                    }
                }
            }
        }
        return fileList;
    }

    private void compile(Map<Integer, List<ExtendedDocument>> list, Path outDir) throws FileExistsException {
        list.forEach((key, listOfDocuments) -> {
            try (final FileWriterWithEncoding writer = new FileWriterWithEncoding(
                    outDir.toAbsolutePath() + File.separator + outputFile + ((key > 0) ? key : "") + ".adoc",
                    "UTF-8")) {
                final StringBuffer output = new StringBuffer();
                if (pageSize > 0) {
                    output.append(createPagingString(key, outputFile, list.size()));
                }
                for (ExtendedDocument doc : listOfDocuments) {

                    output.append(createBlogPost(doc));
                }
                writer.write(output.toString());
            } catch (Exception e) {
                getLog().error(e);
            }
        });
    }

    private String createBlogPost(ExtendedDocument post) {
        Map<String, Object> attrs = post.getAttributes();
        StringBuffer output = new StringBuffer();
        output.append("== ");
        output.append(attrs.get("site-title"));
        output.append("\n\n");
        output.append("Posted on: ");
        output.append(attrs.get("site-date"));
        output.append(" by ");
        output.append(attrs.get("site-author"));
        output.append("\n\n");
        output.append("include::");
        output.append(getRelativePath(post.getFile()));
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

    static Map<Integer, List<ExtendedDocument>> partition(List<ExtendedDocument> list, int pageSize) {
        if (pageSize == 0) {
            Map<Integer, List<ExtendedDocument>> returnV = new HashMap<Integer, List<ExtendedDocument>>();
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

    public String getRelativePath(File file) {
        String path = file.getAbsolutePath();
        return path.replace(outDir.toString(), ".");
    }
}