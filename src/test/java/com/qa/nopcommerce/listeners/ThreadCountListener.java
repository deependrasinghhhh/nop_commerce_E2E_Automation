package com.qa.nopcommerce.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.List;

/**
 * Honors {@code -DthreadCount=<n>} for TestNG {@code <test>} blocks configured
 * with parallel execution, overriding static XML {@code thread-count} values.
 */
public class ThreadCountListener implements IAlterSuiteListener {
    private static final Logger log = LoggerFactory.getLogger(ThreadCountListener.class);

    @Override
    public void alter(List<XmlSuite> suites) {
        String rawThreadCount = System.getProperty("threadCount");
        if (rawThreadCount == null || rawThreadCount.isBlank()) {
            return;
        }

        int threadCount;
        try {
            threadCount = Integer.parseInt(rawThreadCount.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid -DthreadCount value '" + rawThreadCount + "'. Expected a positive integer.", e);
        }
        if (threadCount < 1) {
            throw new IllegalArgumentException(
                    "Invalid -DthreadCount value '" + threadCount + "'. Must be >= 1.");
        }

        for (XmlSuite suite : suites) {
            for (XmlTest test : suite.getTests()) {
                if (test.getParallel() != null && test.getParallel().isParallel()) {
                    log.info("Overriding TestNG thread-count for '{}' from {} to {} via -DthreadCount",
                            test.getName(), test.getThreadCount(), threadCount);
                    test.setThreadCount(threadCount);
                }
            }
        }
    }
}
