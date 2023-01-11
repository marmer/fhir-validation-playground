package io.github.marmer.fhir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.github.marmer.fhir.FhirValidatorByApi.Issue.Type;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FhirValidatorByApiTest {

  private final FhirValidatorByApi underTest = new FhirValidatorByApi();

  @Test
  @DisplayName("Invalid xml resource violating the base profile should be recognized as invalid")
  @SneakyThrows
  void validate_InvalidXmlResourceViolatingTheBaseProfileShouldBeRecognizedAsInvalid() {
    // Preparation
    final var resourceToValidate = ressource("/invalidRessourcePatientBadSystem.xml");

    // Execution
    final var result = underTest.validate(resourceToValidate);

    // Assertion
    assertThat(
        result.issues()).extracting("type", "message"/*, // TODO: marmer 10.01.2023 "message"*/)
        .asList()
        .containsExactlyInAnyOrder(
            tuple(Type.WARNING,
                // TODO: marmer 10.01.2023 Erwartbare Message angeben oder zumindest line und column!
                "hier eine sinnvolle erwartete Message angeben")
        );
  }

  @Test
  @DisplayName("Should serve no result on valid resources")
  @SneakyThrows
  void validate_ShouldServeNoResultOnValidResources() {
    // Preparation
    final var resourceToValidate = ressource("/validRessourcePatient.xml");

    // Execution
    final var result = underTest.validate(resourceToValidate);

    // Assertion
    assertThat(result.issues()).isEmpty();
  }

  @Test
  @DisplayName("Invalid xml resource violating the patient profile should be recognized as invalid")
  @SneakyThrows
  void validate_InvalidXmlResourceViolatingThePatientProfileShouldBeRecognizedAsInvalid() {
    // Preparation
    final var resourceToValidate = ressource("/invalidRessourceBaseProfileError.xml");

    // Execution
    final var result = underTest.validate(resourceToValidate);

    // Assertion
    assertThat(
        result.issues()).extracting("type", "message")
        .asList()
        .containsExactlyInAnyOrder(
            tuple(Type.ERROR,
                "cvc-enumeration-valid: Wert 'shouldNotBeHere' ist nicht Facet-gültig in Bezug auf Enumeration '[usual, official, temp, secondary, old]'. Er muss ein Wert aus der Enumeration sein."),
            tuple(Type.ERROR,
                "cvc-attribute.3: Wert 'shouldNotBeHere' des Attributs 'value' bei Element 'use' hat keinen gültigen Typ 'IdentifierUse-list'."));
  }

  @SneakyThrows
  @NotNull
  private static Path ressource(final String ressourcePath) {
    return Path.of(
        FhirValidationByCLI.class.getResource(ressourcePath).toURI());
  }

  // TODO: marmer 10.01.2023 Errors?
  // TODO: marmer 10.01.2023 Warnings?
  // TODO: marmer 10.01.2023 Fatal?

  // TODO: marmer 10.01.2023 External Reference Issues?
  // TODO: marmer 10.01.2023 External Extension issues?
  //  Is here a difference to Profile?

  // TODO: marmer 10.01.2023 External Profile issues? (maybe they don't matter here)

  // TODO: marmer 10.01.2023 External Code-System issues?

  // TODO: marmer 10.01.2023 External transitive Reference Issues?
  // TODO: marmer 10.01.2023 External transitive Extension issues?
  // TODO: marmer 10.01.2023 External transitive Profile issues? (maybe they don't matter here)
  // TODO: marmer 10.01.2023 External transitive Code-System issues?

  // TODO: marmer 10.01.2023 External Resources? Are they relevant? Because the client would load them anyway, won't it?
}
