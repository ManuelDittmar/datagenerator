package org.camunda.consulting.datagenerator;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import org.camunda.bpm.model.xml.ModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class JobWorkerCreator {

  private static final Logger LOG = LoggerFactory.getLogger(JobWorkerCreator.class);

  @Autowired
  ZeebeClient zeebeClient;

  @PostConstruct
  public void createJobWorkers() {
    File directory = new File("src/main/resources/processes");

    FilenameFilter bpmnFilter = (dir, name) -> name.toLowerCase().endsWith(".bpmn");

    File[] bpmnFiles = directory.listFiles(bpmnFilter);

    if (bpmnFiles != null) {
      for (File bpmnFile : bpmnFiles) {
        LOG.info("Reading BPMN model from file: {}", bpmnFile.getName());
        // Read each BPMN model from the file
        BpmnModelInstance model = Bpmn.readModelFromFile(bpmnFile);
        // Execute the createJobWorkers method for each model
        createJobWorkers(model);
      }
    } else {
      LOG.error("No .bpmn files found in the specified directory.");
    }
  }

  public void createJobWorkers(ModelInstance model){
    List<ZeebeTaskDefinition> serviceTasks = (List<ZeebeTaskDefinition>) model.getModelElementsByType(
        ZeebeTaskDefinition.class);
    for(ZeebeTaskDefinition serviceTask : serviceTasks){
      System.out.println(serviceTask.getType());
      zeebeClient.newWorker().jobType(serviceTask.getType()).handler((client, job) -> {
        LOG.info("Job with type {} and key {} is handled", job.getType(), job.getKey());
        client.newCompleteCommand(job.getKey()).send().join();
      }).open();
      }
    }

}
