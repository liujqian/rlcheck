package edu.berkeley.cs.jqf.fuzz.rl;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;

import java.io.File;
import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;

/**
 * Created by clemieux on 6/17/19.
 */
public class RLDriver {

    public static void main(String[] args) {
        if (System.console() == null) {
            System.out.println("Cannot find a console in the current JVM session!");
        }
        boolean isInDev = null != System.getenv("DEVELOPMENT");
        Duration runTime = null;
        if (isInDev) {
            // Hardcode the arguments for now.
            // args = new String[]{
            // "edu.berkeley.cs.jqf.examples.maven.ModelReaderTest",
            // "testWithInputStream",
            // "edu.berkeley.cs.jqf.examples.xml.XmlRLGenerator",
            // "jqf/configFiles/mavenConfig.json",
            // "jqf/output"
            // };

            args = new String[] {
                    "edu.berkeley.cs.jqf.examples.str.SimpleStringTest",
                    "testSimpleStr",
                    "edu.berkeley.cs.jqf.examples.str.SimpleStringGenerator",
                    "jqf/configFiles/strConfig.json",
                    "jqf/output"
            };
            final long MAX_RUN_TIME_SEC = 4;
            runTime = Duration.ofSeconds(MAX_RUN_TIME_SEC);
        }

        if (args.length < 4) {
            System.err.println("Usage: java " + RLDriver.class
                    + " TEST_CLASS TEST_METHOD GENERATOR_CLASS CONFIG_FILE [OUTPUT_DIR]");
            System.exit(1);
        }

        String testClassName = args[0];
        String testMethodName = args[1];
        String genClassName = args[2];
        String configurationFileName = args[3];
        String outputDirectoryName = args.length > 4 ? args[4] : "fuzz-results";
        System.out.printf(
                "testClassNames: %s, testMethodName: %s, generatorClassName: %s, configurationFileName: %s, outputDirectoryName: %s",
                testClassName, testMethodName, genClassName, configurationFileName, outputDirectoryName);
        File outputDirectory = new File(outputDirectoryName);
        try {

            RLParamParser paramParser = new RLParamParser();
            RLParams params = paramParser.getParamsFile(configurationFileName);

            // Load the generator
            Class<?> clazz = Class.forName(genClassName);
            System.out.println(clazz.toString());
            Constructor<?> ctor = clazz.getConstructor();
            System.out.println(ctor);
            RLGenerator gen = (RLGenerator) ctor.newInstance();

            // Set parameters
            gen.init(params);

            // Load the guidance
            String title = testClassName + "#" + testMethodName + " (" + genClassName + ")";
            Guidance guidance = new RLGuidance(gen, title, runTime, outputDirectory);

            // Run the Junit test
            GuidedFuzzing.run(testClassName, testMethodName, guidance, System.out);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}
