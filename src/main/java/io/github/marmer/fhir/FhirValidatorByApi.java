package io.github.marmer.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import io.github.marmer.fhir.FhirValidatorByApi.Issue.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;

/**
 * https://hapifhir.io/hapi-fhir/docs/validation/instance_validator.html
 */
public class FhirValidatorByApi {

  public record Issue(Type type, String message, Integer locationLine, Integer locationColumn) {

    public enum Type {
      FATAL, ERROR, WARNING, INFO
    }

  }

  public record ValidationResult(List<Issue> issues) {

    public ValidationResult(final Issue... issues) {
      this(Stream.of(issues).toList());
    }
  }

  @SneakyThrows // TODO: marmer 10.01.2023 Not for prod!
  public ValidationResult validate(final Path resource) {
    final var ctx = FhirContext.forR4(); // Fhir Version may be configurable for prod!
    final var validator = ctx.newValidator();
    final var validationSupportChain = new ValidationSupportChain(
        new NpmPackageValidationSupport(ctx)
        ,
        new DefaultProfileValidationSupport(ctx) // Hiermit wurde Patient erkannt
//        ,
//        new CommonCodeSystemsTerminologyService(ctx)
        ,
        new InMemoryTerminologyServerValidationSupport(ctx) // Hiermit
//        ,
//        new SnapshotGeneratingValidationSupport(ctx)
//        ,
//        new UnknownCodeSystemWarningValidationSupport(ctx)
//        ,
//        new PrePopulatedValidationSupport(ctx)
//        ,
//        new RemoteTerminologyServiceValidationSupport(ctx)
    );
    final var instanceValidator = new FhirInstanceValidator(validationSupportChain);
    validator.registerValidatorModule(instanceValidator);

    final String ressourceText = Files.readString(resource);

    final var results = validator.validateWithResult(ressourceText);

    return new ValidationResult(
        results.getMessages().stream().map(it ->
                new Issue(
                    switch (it.getSeverity()) {
                      case FATAL -> Type.FATAL;
                      case ERROR -> Type.ERROR;
                      case WARNING -> Type.WARNING;
                      case INFORMATION -> Type.INFO;
                    },
                    it.getMessage(),
                    it.getLocationLine(),
                    it.getLocationCol()))
            .toList());
  }
}
