package org.camunda.consulting.datagenerator;

import io.camunda.zeebe.client.ZeebeClient;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProcessInstanceStarter {

  @Autowired
  private ZeebeClient zeebeClient;

  @Autowired
  private JsonTemplateProcessor JsonTemplateProcessor;

  @Value("${scheduler.processIds}")
  List<String> processIds;

  @Scheduled(fixedRate = 5000)
  public void startProcessInstance() throws IOException {
    for (String processId : processIds) {
      zeebeClient.newCreateInstanceCommand()
          .bpmnProcessId(processId)
          .latestVersion()
          .variables(JsonTemplateProcessor.processTemplate(new File("src/main/resources/payload.json")))
          .send()
          .join();
    }
  }
}
