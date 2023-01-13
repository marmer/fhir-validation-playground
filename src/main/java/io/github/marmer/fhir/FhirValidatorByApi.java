package io.github.marmer.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import io.github.marmer.fhir.FhirValidatorByApi.Issue.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.jetbrains.annotations.NotNull;

/**
 * https://hapifhir.io/hapi-fhir/docs/validation/instance_validator.html
 */
public class FhirValidatorByApi {

  /**
   * Documentation says this is expensive. In Prod this should be only called once. Here it is
   * static to play with performance impacts in this spike
   */
  private static final FhirContext ctx = FhirContext.forR4();

  /**
   * This is static in this spike to play around with performance impacts
   */
  private final FhirValidator validator = ctx.newValidator();


  @SneakyThrows
  public FhirValidatorByApi(final Path additionalRessourcesAsPath) {
    // TODO: marmer 11.01.2023 Siehe: Implementierung von NpmPackageValidationSupport und https://hapifhir.io/hapi-fhir/docs/validation/validation_support_modules.html
    //  https://simplifier.net/eRezept/~introduction bzw. davon die "packages" (ggf. noch mehr?) https://simplifier.net/eRezept/~packages

    // TODO: marmer 11.01.2023 validRessourceWithProfile.xml mit CLI validieren und schaen ob's geht
    // TODO: marmer 11.01.2023 Test erstellen, validRessourceWithProfile.xml nutzt
    // TODO: marmer 11.01.2023 Paket "herunterladen" (input stream und schön ist die Welt Inspiration von der CLI?)
    // TODO: marmer 11.01.2023 Entsprechend der Implementierung von NpmPackageValidationSupport die PrePopulatedValidationSupport füllen

    final var validationSupportChain = new ValidationSupportChain(
        ctx.getValidationSupport()
//        new DefaultProfileValidationSupport(ctx)
        //, // Hiermit wurde Patient schon erkannt
//        , new CommonCodeSystemsTerminologyService(ctx) //eigentlich nicht nötig
//        , new CachingValidationSupport( //Makes a huge difference in time consumption!
//        new RemoteTerminologyServiceValidationSupport(ctx, "http://hapi.fhir.org/baseR4")
        //sollte ggf. konfigurierbar sein, falls nicht nur alles Version R4 ist
//    )
        , new CachingValidationSupport(newPrePopulatedValidationSupport(additionalRessourcesAsPath))
        , new CachingValidationSupport(new SnapshotGeneratingValidationSupport(ctx))
    );
    final var instanceValidator = new FhirInstanceValidator(validationSupportChain);

    // TODO: marmer 12.01.2023 Configurable for prod!
    instanceValidator.setErrorForUnknownProfiles(false);
    // TODO: marmer 12.01.2023 Configurable for prod!
    instanceValidator.setNoExtensibleWarnings(false);
    // TODO: marmer 12.01.2023 Configurable for prod!
    instanceValidator.setAnyExtensionsAllowed(true);

    validator.registerValidatorModule(instanceValidator);
  }

  @SneakyThrows
  @NotNull
  private PrePopulatedValidationSupport newPrePopulatedValidationSupport(
      final Path additionalRessourcesAsPath) {
    final var customChosenProfileValidationSupport = new PrePopulatedValidationSupport(ctx);
    Files.walk(additionalRessourcesAsPath, FileVisitOption.FOLLOW_LINKS)
        .filter(Files::isRegularFile)
        .map(this::toNpmPackage)
        .forEach(pkg -> {
          if (pkg.getFolders().containsKey("package")) {
            // loadResourcesFromPackage
            loadResourcesFromPackage(customChosenProfileValidationSupport, pkg);

            //loadBinariesFromPackage
            loadBinariesFromPackage(customChosenProfileValidationSupport, pkg);
          }
        });
    return customChosenProfileValidationSupport;
  }

  @SneakyThrows
  private static void loadBinariesFromPackage(
      final PrePopulatedValidationSupport customChosenProfileValidationSupport,
      final NpmPackage pkg) {
    final List<String> binaries = pkg.list("other");
    for (final String binaryName : binaries) {
      customChosenProfileValidationSupport.addBinary(
          TextFile.streamToBytes(pkg.load("other", binaryName)), binaryName);
    }
  }

  private static void loadResourcesFromPackage(
      final PrePopulatedValidationSupport prePopulatedValidationSupport,
      final NpmPackage pkg) {
    final NpmPackage.NpmPackageFolder packageFolder = pkg.getFolders().get("package");

    for (final String nextFile : packageFolder.listFiles()) {
      if (nextFile.toLowerCase(Locale.US).endsWith(".json")) {
        final String input = new String(packageFolder.getContent().get(nextFile),
            StandardCharsets.UTF_8);
        final IBaseResource resource = ctx.newJsonParser().parseResource(input);
        prePopulatedValidationSupport.addResource(resource);
      }
    }
  }

  @SneakyThrows
  private NpmPackage toNpmPackage(final Path it) {
    return NpmPackage.fromPackage(Files.newInputStream(it, StandardOpenOption.READ),
        it.getFileName().toString());
  }

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

  @SneakyThrows // Not for prod!
  public ValidationResult validate(final Path resource) {
    // Fhir Version may be configurable for prod!

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
