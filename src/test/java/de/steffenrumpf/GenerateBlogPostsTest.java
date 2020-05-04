package de.steffenrumpf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class GenerateBlogPostsTest {
    private static GenerateBlogPosts myMojo = null;

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
    @BeforeClass
    public static void setUp() throws Exception {
        File pom = new File("target/test-classes/project-to-test/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        proj = rule.readMavenProject(pom);
        assertNotNull(proj);

        myMojo = (GenerateBlogPosts) rule.lookupConfiguredMojo(pom, "generate_blog_posts");
        assertNotNull(myMojo);
        myMojo.execute();
    }

    /**
     * @throws Exception if any
     */
    @Test
    public void testDefinitionsFileName() throws Exception {

        String definitionFileName = (String) rule.getVariableValueFromObject(myMojo, "definitionFileName");

        assertNotNull(definitionFileName);
        // overwritten by project-to-test/pom.xml
        assertTrue(definitionFileName.equals("article_definition_test.yml"));
    }

    /**
     * Tests if the outputFile was successfull overwritten in
     * project-to-test/pom.xml
     * 
     * @throws Exception if any
     */
    @Test
    public void testOutputFile() throws Exception {
        String outputFileName = (String) rule.getVariableValueFromObject(myMojo, "outputFile");
        assertNotNull(outputFileName);
        assertTrue(outputFileName.equals("indexTest"));
    }

    /**
     * Tests if the default pageSize was used
     * 
     * @throws Exception if any
     */
    @Test
    public void testPageSize() throws Exception {
        int pageSize = (Integer) rule.getVariableValueFromObject(myMojo, "pageSize");
        assertTrue(pageSize == 0);
    }

    /**
     * Test if the folder ${basedir}/generated_src/site/asciidoc was created
     */
    @Test
    public void TestGeneratedFolder() {
        Path generatedSrcPath = Paths.get(proj.getBasedir() + File.separator + "generated_src" + File.separator + "site"
                + File.separator + "asciidoc");
        assertTrue(generatedSrcPath.toString(), Files.exists(generatedSrcPath));
    }

    /**
     * Test if the output file ${basedir}/generated_src/site/asciidoc/indexTest.adoc
     * Also check that the default file was created nor was the indexTest2.adoc
     * created
     */
    @Test
    public void TestGeneratedSingleFile() {
        Path generatedSrcPath = Paths.get(proj.getBasedir() + File.separator + "generated_src" + File.separator + "site"
                + File.separator + "asciidoc" + File.separator + "indexTest.adoc");
        assertTrue(generatedSrcPath.toString(), Files.exists(generatedSrcPath));

        Path noIndexAdoc = Paths.get(proj.getBasedir() + File.separator + "generated_src" + File.separator + "site"
                + File.separator + "asciidoc" + File.separator + "index.adoc");
        assertFalse(noIndexAdoc.toString(), Files.exists(noIndexAdoc));

        Path noIndex2Adoc = Paths.get(proj.getBasedir() + File.separator + "generated_src" + File.separator + "site"
                + File.separator + "asciidoc" + File.separator + "index2.adoc");
        assertFalse(noIndex2Adoc.toString(), Files.exists(noIndex2Adoc));

        Path noIndexTest2Adoc = Paths.get(proj.getBasedir() + File.separator + "generated_src" + File.separator + "site"
                + File.separator + "asciidoc" + File.separator + "indexText2.adoc");
        assertFalse(noIndexTest2Adoc.toString(), Files.exists(noIndexTest2Adoc));
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
    public void TestMultipleFiles() throws Exception {

        File pom = new File("target/test-classes/project-to-test.1/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        proj = rule.readMavenProject(pom);
        assertNotNull(proj);

        GenerateBlogPosts myMojo2 = (GenerateBlogPosts) rule.lookupConfiguredMojo(pom, "generate_blog_posts");
        assertNotNull(myMojo2);
        myMojo2.execute();

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

        myMojo.prepareOutputDir(folderToCheck);

        assertTrue(Files.exists(folderToCheck));

        Files.delete(folderToCheck);
    }

    @Test
    public void checkMap() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map1 = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();
        Map<String, String> map3 = new HashMap<>();
        map1.put("1", "1");
        map2.put("2", "2");
        map3.put("3", "3");

        list.add(map1);
        list.add(map2);
        list.add(map3);
    }

}
