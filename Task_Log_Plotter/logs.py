import time

import redis
import threading


class TrafficInterceptorData:
    def __init__(self, taskId, timestamp):
        self.taskId = taskId
        self.timestamp = timestamp

    @staticmethod
    def parse(bits):
        return TrafficInterceptorData(int(bits[0]), int(bits[2]))


class TaskDispatcherReplicaData:
    def __init__(self, taskId, timestamp, faasName, replicas):
        self.taskId = taskId
        self.timestamp = timestamp
        self.faasName = faasName
        self.replicas = replicas

    @staticmethod
    def parse(bits):
        return TaskDispatcherReplicaData(int(bits[0]), int(bits[2]), bits[3], int(bits[5]))


class TaskDispatcherMessageQueueData:
    def __init__(self, taskId, timestamp, faasName, queueLength):
        self.taskId = taskId
        self.timestamp = timestamp
        self.faasName = faasName
        self.queueLength = queueLength

    @staticmethod
    def parse(bits):
        return TaskDispatcherMessageQueueData(int(bits[0]), int(bits[2]), bits[3], int(bits[5]))


class FaasResultData:
    def __init__(self, taskId, timestamp, faasName, result):
        self.taskId = taskId
        self.timestamp = timestamp
        self.faasName = faasName
        self.result = result

    @staticmethod
    def parse(bits):
        return FaasResultData(int(bits[0]), int(bits[2]), bits[3], float(bits[5]))


class FaasProcessingTimeData:
    def __init__(self, taskId, timestamp, faasName, processingTime):
        self.taskId = taskId
        self.timestamp = timestamp
        self.faasName = faasName
        self.processingTime = processingTime

    @staticmethod
    def parse(bits):
        return FaasProcessingTimeData(int(bits[0]), int(bits[2]), bits[3], int(bits[5]))


class AggregatorNewRequestData:
    def __init__(self, taskId, timestamp):
        self.taskId = taskId
        self.timestamp = timestamp

    @staticmethod
    def parse(bits):
        return AggregatorNewRequestData(int(bits[0]), int(bits[2]))


class AggregatorPartialResponseData:
    def __init__(self, taskId, timestamp, verdict, decisionTime):
        self.taskId = taskId
        self.timestamp = timestamp
        self.verdict = verdict
        self.decisionTime = decisionTime

    @staticmethod
    def parse(bits):
        return AggregatorPartialResponseData(int(bits[0]), int(bits[2]), bits[4], int(bits[5]))


class AggregatorFinalResponseData:
    def __init__(self, taskId, timestamp, verdict, decisionTime):
        self.taskId = taskId
        self.timestamp = timestamp
        self.verdict = verdict
        self.decisionTime = decisionTime

    @staticmethod
    def parse(bits):
        return AggregatorFinalResponseData(int(bits[0]), int(bits[2]), bits[4], int(bits[5]))


class AggregatorWeightsData:
    def __init__(self, taskId, timestamp, weights):
        self.taskId = taskId
        self.timestamp = timestamp
        self.weights = weights

    @staticmethod
    def parse(bits):
        weightsBits = bits[4:]
        weights = {}
        for weightBit in weightsBits:
            aux = weightBit.split(":")
            weights[aux[0]] = float(aux[1])

        return AggregatorWeightsData(int(bits[0]), int(bits[2]), weights)


class SlidingVector:
    def __init__(self, size, initialData):
        self.data = []
        self.size = size

        for i in range(size - 1):
            self.data.append([])

        if initialData is not None:
            self.data.append(initialData)
        else:
            self.data.append([])

    def append(self, data):
        self.data[self.size - 1].append(data)

    def appendArray(self, array):
        if len(array) == 0:
            return

        for i in range(self.size - 1):
            self.data[i] = self.data[i + 1]

        self.data[self.size - 1] = array

    def getData(self):
        result = []
        for arr in self.data:
            if len(arr) > 0:
                result.extend(arr)

        return result


class LogCollector:
    timestamp = int(time.time() * 1000)
    logsSize = 10
    logs = {
        "traffic_interceptor": SlidingVector(logsSize, []),
        "task_dispatcher_replicas": SlidingVector(logsSize, []),
        "task_dispatcher_messages": SlidingVector(logsSize, []),
        "faas_result": SlidingVector(logsSize, []),
        "faas_processing_time": SlidingVector(logsSize, []),
        "aggregator_new_requests": SlidingVector(logsSize, []),
        "aggregator_partial_responses": SlidingVector(logsSize, []),
        "aggregator_final_responses": SlidingVector(logsSize, []),
        "aggregator_weights": SlidingVector(logsSize, []),
    }
    collecting = False

    @staticmethod
    def getData(logs):
        components = {
            "traffic_interceptor": [],
            "task_dispatcher_replicas": [],
            "task_dispatcher_messages": [],
            "faas_result": [],
            "faas_processing_time": [],
            "aggregator_new_requests": [],
            "aggregator_partial_responses": [],
            "aggregator_final_responses": [],
            "aggregator_weights": [],
        }

        for log in logs:
            logBits = log.split(' ')
            if logBits[1] == "traffic_interceptor":
                components["traffic_interceptor"].append(TrafficInterceptorData.parse(logBits))

            if logBits[1] == "aggregator" and logBits[3] == "new_time":
                components["aggregator_new_requests"].append(AggregatorNewRequestData.parse(logBits))

            if logBits[1] == "aggregator" and logBits[3] == "partial_response":
                components["aggregator_partial_responses"].append(AggregatorPartialResponseData.parse(logBits))

            if logBits[1] == "aggregator" and logBits[3] == "final_response":
                components["aggregator_final_responses"].append(AggregatorFinalResponseData.parse(logBits))

            if logBits[1] == "aggregator" and logBits[3] == "final_weights":
                components["aggregator_weights"].append(AggregatorWeightsData.parse(logBits))

            if logBits[1] == "task_dispatcher" and logBits[4] == "replicas":
                components["task_dispatcher_replicas"].append(TaskDispatcherReplicaData.parse(logBits))

            if logBits[1] == "task_dispatcher" and logBits[4] == "messages":
                components["task_dispatcher_messages"].append(TaskDispatcherMessageQueueData.parse(logBits))

            if logBits[1] == "faas" and logBits[4] == "result":
                components["faas_result"].append(FaasResultData.parse(logBits))

            if logBits[1] == "faas" and logBits[4] == "processing_time":
                components["faas_processing_time"].append(FaasProcessingTimeData.parse(logBits))

        return components

    @staticmethod
    def getLogs(r: redis.Redis):
        maxReads = 1000
        while LogCollector.collecting:
            messages = []
            message = r.lpop("logs")
            reads = 0
            while message is not None:
                messages.append(message)
                message = r.lpop("logs")
                reads += 1
                if reads == maxReads:
                    break

            currentData = LogCollector.getData(messages)
            for key in currentData.keys():
                LogCollector.logs[key].appendArray(currentData[key])

            if reads < maxReads:
                time.sleep(1)

    @staticmethod
    def startCollecting(r: redis.Redis):
        LogCollector.collecting = True
        threading.Thread(target=LogCollector.getLogs, args=(r,)).start()

    @staticmethod
    def stopCollecting():
        LogCollector.collecting = False
