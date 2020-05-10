package de.steffenrumpf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class GenerateBlogPostsTest {
    private static GenerateBlogPosts cut = null;

    private static MavenProject proj = null;

    @ClassRule
    public static MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
        }

        @Override
        protected void after() {
        }
    };

    /**
     * Sets up the test fixture.
     */
    @Before
    public void init() throws Exception {
        File pom = new File("target/test-classes/project-to-test/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        proj = rule.readMavenProject(pom);
        assertNotNull(proj);

        cut = (GenerateBlogPosts) rule.lookupConfiguredMojo(pom, "generate_blog_posts");
        assertNotNull(cut);
        cut.setOutDir(Paths.get(proj.getBasedir().getAbsolutePath()));
    }

    /**
     * @throws Exception if any
     */
    @Test
    public void testGetAllFiles() throws Exception {
        
        File folder = new File(proj.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
        + "asciidoc");
        List<ExtendedDocument> allFiles = cut.getAllFiles(folder);
        assertEquals(3, allFiles.size());
    }

    /**
     * Tests if the outputFile was successfull overwritten in
     * project-to-test/pom.xml
     * 
     * @throws Exception if any
     */
    @Test
    public void testCreateBlogPost() throws Exception {
        File testFile = new File("testFile.txt");

        String blogPost = cut.createBlogPost(testFile);

        assertTrue(blogPost.contains("include::"+testFile.getAbsolutePath()+"[]"));

    }

    /**
     * Tests if the default pageSize was used
     * 
     * @throws Exception if any
     */
    @Test
    public void testPagingString() throws Exception {
        String pagingString = cut.createPagingString(0, 10);

        assertTrue(pagingString.contains("link:indexTest.html[\"0\", role=active]"));
        assertTrue(pagingString.contains("link:indexTest9.html[\"9\"]"));
    }

    /**
     * Test if the folder ${basedir}/generated_src/site/asciidoc was created
     */
    @Test
    public void testGetRelativePath() {
        
        String relativePath = cut.getRelativePath(new File(Paths.get("target/test-classes/project-to-test/").toFile().getAbsolutePath()+"/somemore"));
        assertEquals("./somemore", relativePath);
    }

    /**
     * Test if the output file ${basedir}/generated_src/site/asciidoc/indexTest.adoc
     * Also check that the default file was created nor was the indexTest2.adoc
     * created
     */
    @Test
    public void testGeneratedSingleFile() throws Exception {
        cut.execute();

        String outputFileName = (String) rule.getVariableValueFromObject(cut, "outputFile");
        assertEquals("indexTest", outputFileName);

        Path generatedSrcPath = Paths.get(proj.getBasedir() + File.separator + "generated_src" + File.separator + "site"
                + File.separator + "asciidoc" + File.separator + "indexTest.adoc");
        assertTrue(Files.exists(generatedSrcPath));

        Path indexTest1Adoc = Paths.get(proj.getBasedir() + File.separator + "generated_src" + File.separator + "site"
                + File.separator + "asciidoc" + File.separator + "indexTest1.adoc");
        assertFalse(Files.exists(indexTest1Adoc));
    }

    /**
     * Executes the second test project project-to-test.1
     * 
     * uses: 
     * default DefitionsFileName 
     * default outputFile 
     * pageSize = 1 overwritten
     * 
     * Creates multiple OutputFiles
     * 
     * @throws Exception
     */
    @Test
    public void testMultipleFiles() throws Exception {

        File pom = new File("target/test-classes/project-to-test.1/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        proj = rule.readMavenProject(pom);
        assertNotNull(proj);

        cut = (GenerateBlogPosts) rule.lookupConfiguredMojo(pom, "generate_blog_posts");
        assertNotNull(cut);
        cut.execute();

        int pageSize = (int) rule.getVariableValueFromObject(cut, "pageSize");
        assertEquals(1, pageSize);

        String generateSrc = proj.getBasedir() + File.separator + "generated_src" + File.separator + "site"
                + File.separator + "asciidoc";

        Path generatedSrcPath = Paths.get(generateSrc + File.separator + "index.adoc");
        assertTrue(generatedSrcPath.toString(), Files.exists(generatedSrcPath));

        Path generatedSrcPath1 = Paths.get(generateSrc + File.separator + "index1.adoc");
        assertTrue(generatedSrcPath1.toString(), Files.exists(generatedSrcPath1));

        Path generatedSrcPath2 = Paths.get(generateSrc + File.separator + "index2.adoc");
        assertTrue(generatedSrcPath2.toString(), Files.exists(generatedSrcPath2));

        Path generatedSrcPath3 = Paths.get(generateSrc + File.separator + "index3.adoc");
        assertFalse(generatedSrcPath3.toString(), Files.exists(generatedSrcPath3));
    }

    @Test
    public void checkPrepareOutputFolder() throws Exception {

        Path folderToCheck = Paths.get(proj.getBasedir().getAbsolutePath(), "TEST", "TOBEDELETED");
        
        assertFalse(Files.exists(folderToCheck));

        Files.createDirectories(folderToCheck);

        cut.prepareOutputDir(folderToCheck);

        assertTrue(Files.exists(folderToCheck));

        Files.delete(folderToCheck);
    }
}
