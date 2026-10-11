package com.qa.nopcommerce.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to link an automated TestNG test method to a traceable Test Case ID
 * defined in docs/test-cases.csv and requirements in docs/requirements.md.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TestCase {
    /**
     * Test Case Identifier (e.g., "TC-AUTH-001", "TC-CART-002")
     */
    String value();

    /**
     * Optional requirement identifier link (e.g., "REQ-AUTH-001")
     */
    String requirementId() default "";
}
