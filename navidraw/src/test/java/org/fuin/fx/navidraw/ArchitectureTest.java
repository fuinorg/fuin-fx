package org.fuin.fx.navidraw;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.DependencyRules.NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Tests architectural aspects.
 */
@AnalyzeClasses(packagesOf = ArchitectureTest.class, importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    private static final String THIS_PACKAGE = ArchitectureTest.class.getPackageName();

    @ArchTest
    static final ArchRule no_accesses_to_upper_package = NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;

    /**
     * A UI library that quietly grows a dependency is a library nobody can drop into an application. Ikonli
     * is in the list because it is optional and stays confined to {@link IkonliGraphics}, which is checked
     * separately below.
     */
    @ArchTest
    static final ArchRule access_only_to_defined_packages = classes()
            .that()
            .resideInAPackage(THIS_PACKAGE)
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(THIS_PACKAGE, "java..",
                    "javafx..",
                    "org.jspecify.annotations..",
                    "org.kordamp.ikonli..");

    @ArchTest
    static final ArchRule only_the_ikonli_helper_touches_ikonli = classes()
            .that()
            .resideOutsideOfPackage("org.kordamp.ikonli..")
            .and()
            .haveNameNotMatching(".*IkonliGraphics")
            .should()
            .onlyDependOnClassesThat()
            .resideOutsideOfPackage("org.kordamp.ikonli..");

}
