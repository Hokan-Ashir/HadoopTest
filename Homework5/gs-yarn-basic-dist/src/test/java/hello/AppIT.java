package hello;

import hello.client.ClientApplication;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.yarn.boot.test.junit.AbstractBootYarnClusterTests;
import org.springframework.yarn.test.context.MiniYarnClusterTest;
import org.springframework.yarn.test.junit.ApplicationInfo;
import org.springframework.yarn.test.support.ContainerLogUtils;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@MiniYarnClusterTest
public class AppIT extends AbstractBootYarnClusterTests {

    @Test
    public void testApp() throws Exception {
        String[] args = new String[]{
                "--spring.yarn.client.files[0]=file:target/gs-yarn-basic-dist/gs-yarn-basic-container-0.1.0.jar",
                "--spring.yarn.client.files[1]=file:target/gs-yarn-basic-dist/gs-yarn-basic-appmaster-0.1.0.jar"};
        ApplicationInfo info = submitApplicationAndWait(ClientApplication.class, args);
        assertThat(info.getYarnApplicationState(), is(YarnApplicationState.FINISHED));

        List<Resource> resources = ContainerLogUtils.queryContainerLogs(
                getYarnCluster(), info.getApplicationId());
        assertThat(resources, notNullValue());
        assertThat(resources.size(), is(4));

        for (Resource res : resources) {
            File file = res.getFile();
            String content = ContainerLogUtils.getFileContent(file);
            if (file.getName().endsWith("stdout")) {
                assertThat(file.length(), greaterThan(0l));
                if (file.getName().equals("Container.stdout")) {
                    assertThat(content, containsString("Hello from HelloPojo"));
                }
            } else if (file.getName().endsWith("stderr")) {
                assertThat("stderr with content: " + content, file.length(), is(0l));
            }
        }
    }

}
