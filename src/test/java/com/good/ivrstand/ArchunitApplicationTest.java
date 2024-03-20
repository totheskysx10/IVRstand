package com.good.ivrstand;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


public class ArchunitApplicationTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("com.spring.vsurin.bookexchange");

    @Test
    @DisplayName("Требования слоеной архитектуры соблюдены")
    void testArchitecture() {
        ArchRule rule = Architectures.layeredArchitecture()
                .layer("domain").definedBy("..domain..")
                .layer("app").definedBy("..app..")
                .layer("extern").definedBy("..api..", "..infrastructure..")
                .whereLayer("app").mayOnlyBeAccessedByLayers("app", "extern")
                .whereLayer("extern").mayOnlyBeAccessedByLayers("extern");
        rule.check(classes);
    }

    @Test
    @DisplayName("Контроллеры не должны обращаться напрямую к репозиториям")
    void testApiLayerDoesNotContainRepository() {
        ArchRule rule = noClasses().that().resideInAPackage("..api..")
                .should().accessClassesThat().resideInAPackage("..repository..");

        rule.check(classes);
    }
}