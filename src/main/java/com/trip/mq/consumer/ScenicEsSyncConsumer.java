package com.trip.mq.consumer;

import java.io.IOException;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trip.constants.MQTopics;
import com.trip.entity.Scenic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RocketMQMessageListener(topic = MQTopics.SCENIC_SYNC_TOPIC, consumerGroup = MQTopics.SCENIC_ES_SYNC_GROUP)
public class ScenicEsSyncConsumer implements RocketMQListener<String> {

    @Autowired
    private ElasticsearchClient client;

    @Override
    public void onMessage(String message) {
        log.info("Received binlog scenic es sync message: {}", message);

        JSONObject json = JSON.parseObject(message);
        String type= json.getString("type");
        JSONArray dataArray = json.getJSONArray("data");

        if (dataArray == null || dataArray.isEmpty()) {
            log.warn("No data found in the message.");
            return;
        }

        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject data = dataArray.getJSONObject(i);
            String id = data.getString("id");

            try {
                if ("DELETE".equals(type)) {
                    deleteFromES(id);
                } else {
                    Scenic scenic = Scenic.fromMap(data);
                    upsertToES(id, scenic);
                }
            } catch (Exception e) {
                log.error("处理 ES 同步异常，ID: {}", id, e);
                }
            }
        }

    private void deleteFromES(String id) {
        try {
        client.delete(d -> d
            .index("scenic")
            .id(id)
        );
    } catch (IOException e) {
        log.error("ES同步失败", e);
    }}
    private void upsertToES(String id, Scenic scenic) {
        try {
        client.index(i -> i
            .index("scenic")
            .id(id)
            .document(scenic)
        );
    } catch (IOException e) {
        log.error("ES同步失败", e);
    }
    }
        
    
}
