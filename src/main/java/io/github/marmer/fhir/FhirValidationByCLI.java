package io.github.marmer.fhir;

import lombok.SneakyThrows;

public class FhirValidationByCLI {

  // TODO: Option 1:
  //  FhirValidationByCLI Direkt aufrufen
  //  => - System.exit schmeißt und raus, was keine weiteren Aktionen zulässt
  // TODO: Option 2:
  //  CLI im neuen Prozess starten und mit Java steuern.
  //  Exit-Code für Erfolgsentscheid nutzen
  //  Operation Outcome mit Output-Flag und ggf. suffix nutzen um mehrere Ressourcen auf einmal validieren zu können und Details den Nutzern präsentieren
  //  => + Alle Möglichkeiten, welche die FHIR Community derzeit nutzt und kennt mit einem Schlag verfügbar
  //  => + Es muss sich nicht manuell um Extensions und verknüpfte Ressourcen gekümmert werden
  //  => - Prozess-Bla. Je nach System KÖNNTE es zu Problemen mit Pfaden kommen
  // TODO: Option 3:
  //  Fork vom Validator erstellen, die System.exit Bestandteile anpassen und rest wie Option1
  //  => + Vorteile von Option 2 ihne dessen Nachteile
  //  => - Wir haben das Problem, dass wir den Fork zusätzlich warten müssen, wenn sich die CLI-Api ändert
  // TODO: Option 4
  //  Selbst validieren mit InstanceValidator entsprechend https://hapifhir.io/hapi-fhir/docs/validation/instance_validator.html
  //  => + Offiziell empfohlener Weg
  //  => - Müssen Ressourcen selbst parsen und transitive Ressourcen nachladen
  //  => => - Parseprobleme
  //  => => - Nachladeprobleme
  //  => - Alles was die FHIR-Experten vom Validator kennen und sich ggf. noch wünschen, müsste selbst gemacht werden

  @SneakyThrows
  public static void main(final String[] args) {
//    validationByCallingMain();
//    validateByCreatingNewProcess();
    // TODO: marmer 05.01.2023
    //  Manual process control
    //  Try to find a way to lat maven download the jar!
  }
//
//  private static void validateByCreatingNewProcess() {
////    final var validatorProcess = Runtime.getRuntime()
////        .exec()
//  }
//
//  private static void validationByCallingMain() throws Exception {
//    final Path operationOutcomeFile = Files.createTempFile("operationOutcome", ".xml");
//    operationOutcomeFile.toFile().deleteOnExit();
//
//    try {
//      validate("/invalidRessourcePatientBadSystem.xml",
//          "C:\\Users\\mariano.mertinat\\IdeaProjects\\fhir-validation-spike\\oeprationoutcome.xml");
//
//      final var unmarshaller = JAXBContext.newInstance(OperationOutcome.class)
//          .createUnmarshaller();
//
//      final var operationOutcome = (OperationOutcome) unmarshaller.unmarshal(
//          operationOutcomeFile.toFile());
//
//      System.out.println("##### This line is never reached");
//      operationOutcome.getIssue()
//          .forEach(issue -> System.out.println(issue.getDetails().getText()));
//
//      // TODO: marmer 05.01.2023 use prefix (or postfix?) Param und read all "operation outcomes"
//      // TODO: validate multiple resources
//    } finally {
//      Files.deleteIfExists(operationOutcomeFile);
//    }
//  }
//
//
//  private static void validate(final String resourcePath, final String outputFile)
//      throws Exception {
//    ValidatorCli.main(
//        new String[]{
//            Path.of(FhirValidationByCLI.class.getResource(resourcePath).toURI()).toString(),
//            "-security-checks",
//            "-output",
//            outputFile});
//  }
//
//  private static void validValidationXml() throws Exception {
//    validate("/validRessourcePatient.xml",
//        "C:\\Users\\mariano.mertinat\\IdeaProjects\\fhir-validation-spike\\blubba.xml");
//  }
//
//  private static void invalidValidationXml() throws Exception {
//    validate("/invalidRessourcePatientBadSystem.xml",
//        "C:\\Users\\mariano.mertinat\\IdeaProjects\\fhir-validation-spike\\blubba.xml");
//  }
//
//  private static void printHelp() throws Exception {
//    ValidatorCli.main(
//        new String[]{
//            "/?"});
//  }
}
