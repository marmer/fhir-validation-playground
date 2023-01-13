package io.github.marmer.fhir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.github.marmer.fhir.FhirValidatorByApi.Issue.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FhirValidatorByApiTest {

  private static FhirValidatorByApi underTest;

  @SneakyThrows
  @BeforeAll
  static void setUpAll() {
    final var additionalRessourcesAsPath = Paths.get(
        FhirValidatorByApiTest.class.getResource("/fhirpackages").toURI());
    underTest = new FhirValidatorByApi(additionalRessourcesAsPath);
  }

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
        result.issues()).extracting("type", "message")
        .asList()
        .containsExactlyInAnyOrder(
            tuple(Type.WARNING,
                "Keiner der angegebenen Codes ist im Valueset 'IdentifierType' (http://hl7.org/fhir/ValueSet/identifier-type), und ein Code sollte aus diesem Valueset stammen, es sei denn, er enthält keinen geeigneten Code) (Codes = http://terminology.hl7.org/CodeSystem/Blubba#MR)")
        );
  }

  @Test
  @DisplayName("Should serve no result on valid xml resources")
  @SneakyThrows
  void validate_ShouldServeNoResultOnValidXmlResources() {
    // Preparation
    final var resourceToValidate = ressource("/validRessourcePatient.xml");

    // Execution
    final var result = underTest.validate(resourceToValidate);

    // Assertion
    assertThat(result.issues()).isEmpty();
  }

  @Test
  @DisplayName("Should serve no result on valid profile xml resources")
  @SneakyThrows
  void validate_ShouldServeNoResultOnValidProfileXmlResources() {
    // Preparation
    final var resourceToValidate = ressource("/validRessourceWithProfile.xml");

    // Execution
    final var result = underTest.validate(resourceToValidate);

    // Assertion
    assertThat(result.issues()).isEmpty();
  }

  @Test
  @DisplayName("Should serve info on valid xml resources with extension")
  @SneakyThrows
  void validate_ShouldServeInfoOnValidXmlResourcesWithExtension() {
    // Preparation
    final var resourceToValidate = ressource("/validRessourcePatientWithRandomExtension.xml");

    // Execution
    final var result = underTest.validate(resourceToValidate);

    // Assertion
    assertThat(
        result.issues()).extracting("type")
        .asList()
        .containsOnly(Type.INFO);
  }

  @Test
  @DisplayName("Should serve no result on valid json resources")
  @SneakyThrows
  void validate_ShouldServeNoResultOnValidJsonResources() {
    // Preparation
    final var resourceToValidate = ressource("/validRessourcePatient.json");

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
        result.issues())
        .extracting("type")
        .asList()
        .containsOnly(Type.ERROR, Type.WARNING);
  }

  @Test
  @DisplayName("Malformed xml resource of patient profile should be recognized as invalid")
  @SneakyThrows
  void validate_MalformedXmlResourceOfPatientProfileShouldBeRecognizedAsInvalid() {
    // Preparation
    final var resourceToValidate = ressource("/invalidRessourcePatientMalformed.xml");

    // Execution
    final var result = underTest.validate(resourceToValidate);

    // Assertion
    assertThat(
        result.issues()).extracting("type")
        .asList()
        .containsOnly(Type.FATAL);
  }

  @SneakyThrows
  @NotNull
  private static Path ressource(final String ressourcePath) {
    return Path.of(
        FhirValidationByCLI.class.getResource(ressourcePath).toURI());
  }

  // TODO: marmer 11.01.2023 Profil oder Bundleangabe für vorherigen Download oder zum lokal hinpacken oder oder oder. Nachdenken ;)
  // TODO: marmer 12.01.2023 den ganzen Spaß hinter einem Proxy ausführen!!!
}
