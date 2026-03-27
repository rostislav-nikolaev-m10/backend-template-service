package com.m10.integration.test.infrastructure.kafka;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.m10.integration.test.infrastructure.Docker;

@Slf4j
@RequiredArgsConstructor
public class KafkaContainerManager {

    public static final String BEAN_NAME = KafkaContainerManager.class.getName();

    private final KafkaContainerDefinition containerDefinition;

    private KafkaContainer container;
    private boolean topicsCreated = false;

    synchronized public KafkaContainer getContainer() {
        if (container != null) {
            return container;
        }
        container = createContainer();
        container.start();
        return container;
    }

    synchronized public void createInitialTopics() {
        if (topicsCreated) {
            return;
        }
        try (AdminClient admin = AdminClient.create(Map.of(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getContainer().getBootstrapServers()
        ))) {
            var currentTopics = admin.listTopics().names().get().stream()
                .filter(t -> !t.startsWith("_"))
                .collect(Collectors.toSet());
            List<NewTopic> topics = Arrays.stream(containerDefinition.initialTopics())
                .filter(t -> !currentTopics.contains(t))
                .map(name -> new NewTopic(name, 1, (short) 1))
                .toList();
            admin.createTopics(topics).all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create Kafka topics", e);
        } finally {
            topicsCreated = true;
        }
    }

    synchronized public void resetKafkaTopicsOffsets() {
        try (AdminClient admin = AdminClient.create(Map.of(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getContainer().getBootstrapServers()
        ))) {
            // Получаем список всех не-системных топиков
            Set<String> topics = getNonInternalTopics(admin);

            if (!topics.isEmpty()) {
                // Получаем информацию о партициях
                Map<TopicPartition, Long> endOffsets = new HashMap<>();

                for (String topic : topics) {
                    // Получаем список партиций для топика
                    List<TopicPartitionInfo> partitions = admin.describeTopics(List.of(topic))
                        .allTopicNames()
                        .get()
                        .get(topic)
                        .partitions();

                    // Создаем группу потребителей для сброса офсетов
                    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                        Map.of(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getContainer().getBootstrapServers(),
                            ConsumerConfig.GROUP_ID_CONFIG, "reset-group-" + UUID.randomUUID(),
                            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()
                        ))) {

                        // Получаем конечные офсеты для каждой партиции
                        List<TopicPartition> topicPartitions = partitions.stream()
                            .map(p -> new TopicPartition(topic, p.partition()))
                            .collect(Collectors.toList());

                        consumer.assign(topicPartitions);
                        Map<TopicPartition, Long> latestOffsets = consumer.endOffsets(topicPartitions);
                        endOffsets.putAll(latestOffsets);
                    }
                }

                // Сдвигаем офсеты в конец для всех групп потребителей
                var consumerGroups = admin.listConsumerGroups().all().get();
                var consumerGroupsDescriptions =
                    admin.describeConsumerGroups(consumerGroups.stream()
                            .map(ConsumerGroupListing::groupId).toList()
                        )
                        .all().get();
                consumerGroups.forEach(consumerGroup -> {
                    var description = consumerGroupsDescriptions.get(consumerGroup.groupId());
                    var consumerGroupTopics = description.members().stream()
                        .flatMap(member -> member.assignment().topicPartitions().stream())
                        .map(TopicPartition::topic)
                        .collect(Collectors.toSet());
                    var consumerGroupEndOffsets = endOffsets.entrySet()
                        .stream()
                        .filter(entry -> consumerGroupTopics.contains(entry.getKey().topic()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    try {
                        admin.alterConsumerGroupOffsets(
                            consumerGroup.groupId(),
                            consumerGroupEndOffsets.entrySet().stream()
                                .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> new OffsetAndMetadata(e.getValue())
                                ))
                        ).all().get();
                    } catch (Exception e) {
                        log.warn(String.format("Failed to alter consumer group [%s] offsets", consumerGroup.groupId()), e);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сбросить оффсеты консумеров Kafka", e);
        }
    }

    private KafkaContainer createContainer() {
        var container = new KafkaContainer(DockerImageName.parse(containerDefinition.containerName()))
            .withNetwork(Docker.network)
            .withNetworkAliases(Docker.NETWORK_NAME)
            .withReuse(true);
        container.setNetworkAliases(List.of(Docker.NETWORK_NAME));
        return container;
    }

    private Set<String> getNonInternalTopics(AdminClient admin) throws ExecutionException, InterruptedException {
        return admin.listTopics().names().get().stream()
            .filter(t -> !t.startsWith("_"))
            .collect(Collectors.toSet());
    }

}
